package com.rupeek.maidbooking.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingDetails;
import com.rupeek.maidbooking.model.booking.BookingOccurrence;
import com.rupeek.maidbooking.model.booking.BookingType;
import com.rupeek.maidbooking.model.booking.CancellationResult;
import com.rupeek.maidbooking.model.booking.CreateBookingCommand;
import com.rupeek.maidbooking.model.booking.OccurrenceStatus;
import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.payment.PaymentReceipt;
import com.rupeek.maidbooking.model.payment.PaymentResult;
import com.rupeek.maidbooking.repository.BookingRepository;
import com.rupeek.maidbooking.repository.MaidRepository;
import com.rupeek.maidbooking.service.AvailabilityService;
import com.rupeek.maidbooking.service.BookingService;
import com.rupeek.maidbooking.strategy.booking.BookingPlanner;
import com.rupeek.maidbooking.strategy.payment.PaymentGateway;
import com.rupeek.maidbooking.strategy.pricing.PricingPolicy;
import com.rupeek.maidbooking.strategy.refund.RefundPolicy;


@Service
public final class BookingServiceImpl implements BookingService {
    private final MaidRepository maidRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;
    private final Map<BookingType, BookingPlanner> planners;
    private final Map<PaymentMethod, PaymentGateway> gateways;
    private final PricingPolicy pricingPolicy;
    private final RefundPolicy refundPolicy;
    private final Clock clock;

    @Autowired
    public BookingServiceImpl(MaidRepository maidRepository,
                              BookingRepository bookingRepository,
                              AvailabilityService availabilityService,
                              List<BookingPlanner> bookingPlanners,
                              List<PaymentGateway> paymentGateways,
                              PricingPolicy pricingPolicy,
                              RefundPolicy refundPolicy,
                              Clock clock) {
        this.maidRepository = maidRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityService = availabilityService;
        this.planners = bookingPlanners.stream().collect(
                Collectors.toUnmodifiableMap(BookingPlanner::supports, Function.identity()));
        this.gateways = paymentGateways.stream().collect(
                Collectors.toUnmodifiableMap(PaymentGateway::supportedMethod, Function.identity()));
        this.pricingPolicy = pricingPolicy;
        this.refundPolicy = refundPolicy;
        this.clock = clock;
    }

    @Override
    public BookingDetails create(CreateBookingCommand command) {
        validateCommand(command);
        BookingPlanner planner = planners.get(command.getType());
        if (planner == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Unsupported booking type");
        }
        List<TimeSlot> requestedSlots = planner.plan(command, clock);

        // The availability check and reservation share one per-maid lock.
        // This closes the check-then-book race for this single-process implementation.
        return maidRepository.withMaidLock(command.getMaidId(), () -> {
            Maid maid = requireMaid(command.getMaidId());
            if (!maid.offers(command.getServiceType())) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                        "Maid does not provide requested service");
            }
            for (TimeSlot slot : requestedSlots) {
                if (!availabilityService.isAvailable(maid, slot)) {
                    throw new BusinessException(ErrorCode.CONFLICT,
                            "Maid not available for " + slot.getStart());
                }
            }

            UUID bookingId = UUID.randomUUID();
            List<BookingOccurrence> occurrences = requestedSlots.stream()
                    .map(slot -> BookingOccurrence.builder()
                            .id(UUID.randomUUID())
                            .bookingId(bookingId)
                            .maidId(maid.getId())
                            .slot(slot)
                            .price(pricingPolicy.price(maid, slot))
                            .build())
                    .toList();
            Booking booking = Booking.builder()
                    .id(bookingId)
                    .maidId(maid.getId())
                    .customerId(command.getCustomerId().trim())
                    .serviceType(command.getServiceType().trim().toLowerCase(Locale.ROOT))
                    .type(command.getType())
                    .createdAt(LocalDateTime.now(clock))
                    .occurrenceIds(occurrences.stream().map(BookingOccurrence::getId).toList())
                    .build();

            bookingRepository.save(booking, occurrences);
            return BookingDetails.builder().booking(booking).occurrences(occurrences).build();
        });
    }

    @Override
    public BookingDetails get(UUID bookingId) {
        Booking booking = requireBooking(bookingId);
        return BookingDetails.builder()
                .booking(booking)
                .occurrences(booking.getOccurrenceIds().stream()
                        .map(this::requireOccurrence).toList())
                .build();
    }

    /**
     * Each recurring occurrence is paid independently. A matching replay returns the
     * already stored receipt; another payment attempt is rejected.
     */
    @Override
    public PaymentReceipt pay(UUID bookingId, UUID occurrenceId,
                              PaymentMethod method, String idempotencyKey) {
        if (method == null || idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Payment method and idempotency key are required");
        }
        BookingOccurrence occurrence = requireOwnedOccurrence(bookingId, occurrenceId);

        synchronized (occurrence) {
            PaymentReceipt existing = occurrence.getPayment();
            if (existing != null) {
                if (existing.getIdempotencyKey().equals(idempotencyKey) && existing.getMethod() == method) {
                    return existing;
                }
                throw new BusinessException(ErrorCode.CONFLICT, "Occurrence has already been paid");
            }
            if (occurrence.getStatus() != OccurrenceStatus.RESERVED
                    || !occurrence.getSlot().getEnd().isAfter(LocalDateTime.now(clock))) {
                throw new BusinessException(ErrorCode.CONFLICT,
                        "Cannot pay a cancelled or finished occurrence");
            }

            PaymentGateway gateway = gateways.get(method);
            if (gateway == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "Unsupported payment method");
            }
            PaymentResult result = gateway.charge(occurrence.getId(), occurrence.getPrice());
            PaymentReceipt receipt = PaymentReceipt.builder()
                    .providerPaymentId(result.getProviderReference())
                    .method(method)
                    .amount(occurrence.getPrice())
                    .refundedAmount(BigDecimal.ZERO.setScale(2))
                    .idempotencyKey(idempotencyKey)
                    .build();
            occurrence.setPayment(receipt);
            return receipt;
        }
    }

    @Override
    public CancellationResult cancelOccurrence(UUID bookingId, UUID occurrenceId) {
        BookingOccurrence occurrence = requireOwnedOccurrence(bookingId, occurrenceId);
        return maidRepository.withMaidLock(occurrence.getMaidId(), () -> {
            synchronized (occurrence) {
                BigDecimal refund = cancelOne(occurrence);
                return CancellationResult.builder()
                        .bookingId(bookingId)
                        .cancelledOccurrenceIds(List.of(occurrenceId))
                        .totalRefund(refund)
                        .build();
            }
        });
    }

    /** Cancel future reserved slots only, leaving past or cancelled occurrences unchanged. */
    @Override
    public CancellationResult cancelSeries(UUID bookingId) {
        Booking booking = requireBooking(bookingId);
        return maidRepository.withMaidLock(booking.getMaidId(), () -> {
            List<UUID> cancelledIds = new ArrayList<>();
            BigDecimal totalRefund = BigDecimal.ZERO.setScale(2);

            for (UUID occurrenceId : booking.getOccurrenceIds()) {
                BookingOccurrence occurrence = requireOccurrence(occurrenceId);
                synchronized (occurrence) {
                    if (occurrence.getStatus() == OccurrenceStatus.RESERVED
                            && occurrence.getSlot().getStart().isAfter(LocalDateTime.now(clock))) {
                        totalRefund = totalRefund.add(cancelOne(occurrence));
                        cancelledIds.add(occurrenceId);
                    }
                }
            }
            if (cancelledIds.isEmpty()) {
                throw new BusinessException(ErrorCode.CONFLICT,
                        "No future reserved occurrences to cancel");
            }
            return CancellationResult.builder()
                    .bookingId(bookingId)
                    .cancelledOccurrenceIds(List.copyOf(cancelledIds))
                    .totalRefund(totalRefund)
                    .build();
        });
    }

    /** Called while holding the maid lock and the occurrence monitor. */
    private BigDecimal cancelOne(BookingOccurrence occurrence) {
        if (occurrence.getStatus() != OccurrenceStatus.RESERVED
                || !occurrence.getSlot().getStart().isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "Only future reserved occurrences can be cancelled");
        }

        BigDecimal refund = refundPolicy.refundableAmount(occurrence, clock);
        if (refund.signum() > 0) {
            PaymentReceipt receipt = occurrence.getPayment();
            PaymentGateway gateway = gateways.get(receipt.getMethod());
            gateway.refund(receipt.getProviderPaymentId(), refund);
            occurrence.setPayment(receipt.withRefund(refund));
        }
        occurrence.markCancelled();
        return refund;
    }

    private void validateCommand(CreateBookingCommand command) {
        if (command == null || command.getMaidId() == null || command.getType() == null
                || command.getCustomerId() == null || command.getCustomerId().isBlank()
                || command.getServiceType() == null || command.getServiceType().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Maid, customer, service and booking type are required");
        }
    }

    private Maid requireMaid(UUID maidId) {
        return maidRepository.findById(maidId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Maid not found"));
    }

    private Booking requireBooking(UUID bookingId) {
        return bookingRepository.findBooking(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Booking not found"));
    }

    private BookingOccurrence requireOccurrence(UUID id) {
        return bookingRepository.findOccurrence(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "Booking occurrence not found"));
    }

    private BookingOccurrence requireOwnedOccurrence(UUID bookingId, UUID occurrenceId) {
        requireBooking(bookingId);
        BookingOccurrence occurrence = requireOccurrence(occurrenceId);
        if (!occurrence.getBookingId().equals(bookingId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Occurrence not found in booking");
        }
        return occurrence;
    }
}

package com.rupeek.maidbooking;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.booking.BookingDetails;
import com.rupeek.maidbooking.model.booking.BookingOccurrence;
import com.rupeek.maidbooking.model.booking.BookingType;
import com.rupeek.maidbooking.model.booking.CancellationResult;
import com.rupeek.maidbooking.model.booking.CreateBookingCommand;
import com.rupeek.maidbooking.model.booking.OccurrenceStatus;
import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.model.maid.AvailabilityWindow;
import com.rupeek.maidbooking.model.maid.Gender;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;
import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.payment.PaymentReceipt;
import com.rupeek.maidbooking.repository.BookingRepository;
import com.rupeek.maidbooking.repository.MaidRepository;
import com.rupeek.maidbooking.repository.impl.InMemoryBookingRepository;
import com.rupeek.maidbooking.repository.impl.InMemoryMaidRepository;
import com.rupeek.maidbooking.service.AvailabilityService;
import com.rupeek.maidbooking.service.BookingService;
import com.rupeek.maidbooking.service.MaidSearchService;
import com.rupeek.maidbooking.service.MaidService;
import com.rupeek.maidbooking.service.impl.AvailabilityServiceImpl;
import com.rupeek.maidbooking.service.impl.BookingServiceImpl;
import com.rupeek.maidbooking.service.impl.MaidSearchServiceImpl;
import com.rupeek.maidbooking.service.impl.MaidServiceImpl;
import com.rupeek.maidbooking.strategy.booking.InstantBookingPlanner;
import com.rupeek.maidbooking.strategy.booking.RecurringBookingPlanner;
import com.rupeek.maidbooking.strategy.booking.ScheduledBookingPlanner;
import com.rupeek.maidbooking.strategy.filter.GenderMaidFilter;
import com.rupeek.maidbooking.strategy.filter.LocalityMaidFilter;
import com.rupeek.maidbooking.strategy.filter.PriceMaidFilter;
import com.rupeek.maidbooking.strategy.filter.RatingMaidFilter;
import com.rupeek.maidbooking.strategy.filter.ServiceTypeMaidFilter;
import com.rupeek.maidbooking.strategy.filter.SkillMaidFilter;
import com.rupeek.maidbooking.strategy.payment.CardPaymentGateway;
import com.rupeek.maidbooking.strategy.payment.PaymentGateway;
import com.rupeek.maidbooking.strategy.payment.UpiPaymentGateway;
import com.rupeek.maidbooking.strategy.payment.WalletPaymentGateway;
import com.rupeek.maidbooking.strategy.pricing.HourlyPricingPolicy;
import com.rupeek.maidbooking.strategy.refund.TimeBasedRefundPolicy;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private Clock clock;
    private MaidRepository maidRepository;
    private BookingRepository bookingRepository;
    private MaidService maidService;
    private AvailabilityService availability;
    private MaidSearchService search;
    private BookingService bookingService;
    private Maid maid;

    @BeforeEach void setUp() {
        clock = Clock.fixed(Instant.parse("2026-11-08T03:30:00Z"), ZONE); // Sunday, 09:00 IST
        maidRepository = new InMemoryMaidRepository();
        bookingRepository = new InMemoryBookingRepository();
        maidService = new MaidServiceImpl(maidRepository);
        availability = new AvailabilityServiceImpl(bookingRepository);
        search = new MaidSearchServiceImpl(maidRepository, availability, List.of(new LocalityMaidFilter(), new ServiceTypeMaidFilter(),
                new SkillMaidFilter(), new GenderMaidFilter(), new RatingMaidFilter(), new PriceMaidFilter()), clock);
        List<PaymentGateway> gateways = Arrays.stream(PaymentMethod.values())
                .map(m -> (PaymentGateway) switch (m) {
                    case CARD -> new CardPaymentGateway();
                    case UPI -> new UpiPaymentGateway();
                    case WALLET -> new WalletPaymentGateway();
                }).toList();
        bookingService = new BookingServiceImpl(maidRepository, bookingRepository, availability,
                List.of(new InstantBookingPlanner(), new ScheduledBookingPlanner(), new RecurringBookingPlanner()),
                gateways, new HourlyPricingPolicy(), new TimeBasedRefundPolicy(), clock);
        List<AvailabilityWindow> windows = Arrays.stream(DayOfWeek.values())
                .map(day -> new AvailabilityWindow(day, LocalTime.of(8, 0), LocalTime.of(22, 0))).toList();
        maid = maidService.onboard("Anita", "HSR Layout", Gender.FEMALE, new BigDecimal("4.8"),
                new BigDecimal("300"), Set.of("cleaning", "cooking"), windows);
    }

    private CreateBookingCommand scheduled(LocalDateTime start) {
        return new CreateBookingCommand(maid.getId(), "customer-1", "cleaning", BookingType.SCHEDULED,
                start, 60, null, null);
    }
    private LocalDateTime at(int day, int hour) { return LocalDateTime.of(2026, 11, day, hour, 0); }

    @Test void instantBookingStartsAtCurrentClock() {
        BookingDetails details = bookingService.create(new CreateBookingCommand(maid.getId(), "c", "cooking",
                BookingType.INSTANT, null, 30, null, null));
        assertEquals(at(8, 9), details.getOccurrences().get(0).getSlot().getStart());
        assertEquals(BookingType.INSTANT, details.getBooking().getType());
    }

    @Test void scheduledBookingPriceAndAdjacentBoundary() {
        BookingDetails first = bookingService.create(scheduled(at(10, 10)));
        assertEquals(new BigDecimal("300.00"), first.getOccurrences().get(0).getPrice());
        assertDoesNotThrow(() -> bookingService.create(scheduled(at(10, 11))));
        assertEquals(1, bookingService.get(first.getBooking().getId()).getOccurrences().size());
    }

    @Test void overlappingBookingIsRejected() {
        bookingService.create(scheduled(at(10, 10)));
        BusinessException error = assertThrows(BusinessException.class, () ->
                bookingService.create(scheduled(LocalDateTime.of(2026, 11, 10, 10, 30))));
        assertEquals(ErrorCode.CONFLICT, error.getCode());
    }

    @Test void onlyAvailableMaidsAreDiscoveredAndFiltersCompose() {
        TimeSlot slot = new TimeSlot(at(10, 10), at(10, 11));
        MaidSearchCriteria matching = new MaidSearchCriteria("hsr layout", "cleaning", "cooking",
                Gender.FEMALE, new BigDecimal("4.5"), new BigDecimal("400"), slot);
        assertEquals(List.of(maid), search.search(matching));
        bookingService.create(scheduled(at(10, 10)));
        assertTrue(search.search(matching).isEmpty());
        assertTrue(search.search(new MaidSearchCriteria(null, "dishwashing", null,
                null, null, null, new TimeSlot(at(10, 12), at(10, 13)))).isEmpty());
    }

    @Test void outsideAvailabilityAndCrossMidnightRejected() {
        assertEquals(ErrorCode.CONFLICT, assertThrows(BusinessException.class,
                () -> bookingService.create(scheduled(at(10, 7)))).getCode());
        assertEquals(ErrorCode.INVALID_REQUEST, assertThrows(BusinessException.class,
                () -> bookingService.create(scheduled(at(10, 23)))).getCode());
    }

    @Test void recurringCreatesMatchingWeekdaysAndPricesEachOccurrence() {
        CreateBookingCommand command = new CreateBookingCommand(maid.getId(), "c", "cleaning",
                BookingType.RECURRING, at(15, 10), 60, Set.of(DayOfWeek.SUNDAY), LocalDate.of(2026, 11, 29));
        BookingDetails details = bookingService.create(command);
        assertEquals(3, details.getOccurrences().size());
        assertEquals(List.of(15, 22, 29), details.getOccurrences().stream().map(o -> o.getSlot().getStart().getDayOfMonth()).toList());
        assertTrue(details.getOccurrences().stream().allMatch(o -> o.getPayment() == null));
    }

    @Test void recurringIsAllOrNothingWhenAnyOccurrenceConflicts() {
        bookingService.create(scheduled(at(22, 10)));
        CreateBookingCommand command = new CreateBookingCommand(maid.getId(), "c", "cleaning",
                BookingType.RECURRING, at(15, 10), 60, Set.of(DayOfWeek.SUNDAY), LocalDate.of(2026, 11, 29));
        assertEquals(ErrorCode.CONFLICT,
                assertThrows(BusinessException.class, () -> bookingService.create(command)).getCode());
        assertDoesNotThrow(() -> bookingService.create(scheduled(at(15, 10))));
    }

    @Test void paymentIsPerOccurrenceAndIdempotent() {
        BookingDetails details = bookingService.create(scheduled(at(10, 10)));
        UUID bookingId = details.getBooking().getId(), occurrenceId = details.getOccurrences().get(0).getId();
        PaymentReceipt first = bookingService.pay(bookingId, occurrenceId, PaymentMethod.UPI, "pay-key-1");
        PaymentReceipt replay = bookingService.pay(bookingId, occurrenceId, PaymentMethod.UPI, "pay-key-1");
        assertEquals(first.getProviderPaymentId(), replay.getProviderPaymentId());
        assertEquals(ErrorCode.CONFLICT, assertThrows(BusinessException.class,
                () -> bookingService.pay(bookingId, occurrenceId, PaymentMethod.CARD, "pay-key-2")).getCode());
        assertEquals(ErrorCode.NOT_FOUND, assertThrows(BusinessException.class,
                () -> bookingService.pay(UUID.randomUUID(), occurrenceId, PaymentMethod.UPI, "other")).getCode());
    }

    @Test void cancellationRefundsAndReleasesInventory() {
        BookingDetails details = bookingService.create(scheduled(at(10, 10)));
        UUID id = details.getBooking().getId(), occurrenceId = details.getOccurrences().get(0).getId();
        bookingService.pay(id, occurrenceId, PaymentMethod.CARD, "pay1");
        CancellationResult cancelled = bookingService.cancelOccurrence(id, occurrenceId);
        assertEquals(new BigDecimal("300.00"), cancelled.getTotalRefund());
        assertEquals(OccurrenceStatus.CANCELLED, bookingService.get(id).getOccurrences().get(0).getStatus());
        assertEquals(new BigDecimal("300.00"), bookingService.get(id).getOccurrences().get(0).getPayment().getRefundedAmount());
        assertDoesNotThrow(() -> bookingService.create(scheduled(at(10, 10))));
        assertEquals(ErrorCode.CONFLICT, assertThrows(BusinessException.class,
                () -> bookingService.cancelOccurrence(id, occurrenceId)).getCode());
    }

    @Test void refundPolicySupportsPartialAndNoRefund() {
        BookingDetails partial = bookingService.create(scheduled(at(8, 12)));
        BookingDetails noRefund = bookingService.create(scheduled(LocalDateTime.of(2026, 11, 8, 9, 30)));
        BookingOccurrence p = partial.getOccurrences().get(0), n = noRefund.getOccurrences().get(0);
        bookingService.pay(partial.getBooking().getId(), p.getId(), PaymentMethod.WALLET, "p");
        bookingService.pay(noRefund.getBooking().getId(), n.getId(), PaymentMethod.WALLET, "n");
        assertEquals(new BigDecimal("150.00"), bookingService.cancelOccurrence(partial.getBooking().getId(), p.getId()).getTotalRefund());
        assertEquals(new BigDecimal("0.00"), bookingService.cancelOccurrence(noRefund.getBooking().getId(), n.getId()).getTotalRefund());
    }

    @Test void cancelWholeSeriesButLeaveOneAlreadyCancelled() {
        BookingDetails details = bookingService.create(new CreateBookingCommand(maid.getId(), "c", "cleaning",
                BookingType.RECURRING, at(15, 10), 60, Set.of(DayOfWeek.SUNDAY), LocalDate.of(2026, 11, 29)));
        bookingService.cancelOccurrence(details.getBooking().getId(), details.getOccurrences().get(1).getId());
        CancellationResult series = bookingService.cancelSeries(details.getBooking().getId());
        assertEquals(2, series.getCancelledOccurrenceIds().size());
        assertTrue(bookingService.get(details.getBooking().getId()).getOccurrences().stream()
                .allMatch(o -> o.getStatus() == OccurrenceStatus.CANCELLED));
    }

    @Test void overlappingAvailabilityWindowsAreRejected() {
        List<AvailabilityWindow> invalid = List.of(
                new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(13, 0)));
        assertEquals(ErrorCode.INVALID_REQUEST, assertThrows(BusinessException.class,
                () -> maidService.onboard("Nina", "Koramangala", Gender.FEMALE,
                        new BigDecimal("4.1"), new BigDecimal("200"), Set.of("cleaning"), invalid)).getCode());
    }

    @Test void cancellingUnpaidOccurrenceFreesSlotWithoutRefund() {
        BookingDetails details = bookingService.create(scheduled(at(10, 10)));
        UUID bookingId = details.getBooking().getId(), occurrenceId = details.getOccurrences().get(0).getId();
        assertEquals(new BigDecimal("0.00"),
                bookingService.cancelOccurrence(bookingId, occurrenceId).getTotalRefund());
        assertEquals(ErrorCode.CONFLICT, assertThrows(BusinessException.class,
                () -> bookingService.pay(bookingId, occurrenceId, PaymentMethod.CARD, "late-pay")).getCode());
        assertDoesNotThrow(() -> bookingService.create(scheduled(at(10, 10))));
    }

    @Test void repeatedPaymentWithSameKeyOnDifferentOccurrenceIsIndependent() {
        BookingDetails recurring = bookingService.create(new CreateBookingCommand(maid.getId(), "c", "cleaning",
                BookingType.RECURRING, at(15, 10), 60, Set.of(DayOfWeek.SUNDAY), LocalDate.of(2026, 11, 22)));
        BookingOccurrence first = recurring.getOccurrences().get(0), second = recurring.getOccurrences().get(1);
        PaymentReceipt one = bookingService.pay(recurring.getBooking().getId(), first.getId(), PaymentMethod.CARD, "shared-key");
        PaymentReceipt two = bookingService.pay(recurring.getBooking().getId(), second.getId(), PaymentMethod.CARD, "shared-key");
        assertNotEquals(one.getProviderPaymentId(), two.getProviderPaymentId());
    }

    @Test void concurrentBookingAttemptsCanOnlyReserveOnce() throws Exception {
        int clients = 16;
        ExecutorService executor = Executors.newFixedThreadPool(clients);
        CountDownLatch ready = new CountDownLatch(clients), go = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < clients; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    try {
                        if (!go.await(3, TimeUnit.SECONDS)) throw new AssertionError("Latch timeout");
                        bookingService.create(scheduled(at(10, 10)));
                        succeeded.incrementAndGet();
                    } catch (BusinessException e) {
                        if (e.getCode() != ErrorCode.CONFLICT) throw e;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(e);
                    }
                }));
            }
            assertTrue(ready.await(3, TimeUnit.SECONDS));
            go.countDown();
            for (Future<?> future : futures) future.get(5, TimeUnit.SECONDS);
            assertEquals(1, succeeded.get());
        } finally {
            go.countDown();
            executor.shutdownNow();
        }
    }

    @Test void unknownMaidAndWrongServiceAreRejected() {
        CreateBookingCommand unknown = new CreateBookingCommand(UUID.randomUUID(),
                "customer", "cleaning", BookingType.SCHEDULED, at(10, 10), 60, null, null);
        assertEquals(ErrorCode.NOT_FOUND, assertThrows(BusinessException.class,
                () -> bookingService.create(unknown)).getCode());
        CreateBookingCommand missingSkill = new CreateBookingCommand(maid.getId(),
                "customer", "gardening", BookingType.SCHEDULED, at(10, 10), 60, null, null);
        assertEquals(ErrorCode.INVALID_REQUEST, assertThrows(BusinessException.class,
                () -> bookingService.create(missingSkill)).getCode());
    }

    @Test void recurringCannotExtendBeyondSupportedHorizon() {
        CreateBookingCommand command = new CreateBookingCommand(maid.getId(), "customer", "cleaning",
                BookingType.RECURRING, at(15, 10), 60, Set.of(DayOfWeek.SUNDAY),
                LocalDate.of(2027, 3, 31));
        assertEquals(ErrorCode.INVALID_REQUEST, assertThrows(BusinessException.class,
                () -> bookingService.create(command)).getCode());
    }

    @Test void occurrenceCannotBePaidOrCancelledThroughAnotherBooking() {
        BookingDetails one = bookingService.create(scheduled(at(10, 10)));
        BookingDetails two = bookingService.create(scheduled(at(10, 12)));
        UUID wrongBookingId = two.getBooking().getId();
        UUID occurrenceId = one.getOccurrences().get(0).getId();
        assertEquals(ErrorCode.NOT_FOUND, assertThrows(BusinessException.class,
                () -> bookingService.pay(wrongBookingId, occurrenceId,
                        PaymentMethod.CARD, "wrong-owner")).getCode());
        assertEquals(ErrorCode.NOT_FOUND, assertThrows(BusinessException.class,
                () -> bookingService.cancelOccurrence(wrongBookingId, occurrenceId)).getCode());
        assertEquals(OccurrenceStatus.RESERVED, one.getOccurrences().get(0).getStatus());
    }

    @Test void searchRejectsPastSlot() {
        TimeSlot past = new TimeSlot(LocalDateTime.of(2026, 11, 8, 8, 0),
                LocalDateTime.of(2026, 11, 8, 8, 30));
        assertEquals(ErrorCode.INVALID_REQUEST, assertThrows(BusinessException.class,
                () -> search.search(new MaidSearchCriteria(null, null, null,
                        null, null, null, past))).getCode());
    }
}

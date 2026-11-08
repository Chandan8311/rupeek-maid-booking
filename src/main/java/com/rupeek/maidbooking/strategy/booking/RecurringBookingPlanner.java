package com.rupeek.maidbooking.strategy.booking;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.constants.BookingConstants;
import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.booking.BookingType;
import com.rupeek.maidbooking.model.booking.CreateBookingCommand;
import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.utils.TimeSlotUtils;

@Component
public final class RecurringBookingPlanner implements BookingPlanner {
    @Override
    public BookingType supports() {
        return BookingType.RECURRING;
    }

    @Override
    public List<TimeSlot> plan(CreateBookingCommand command, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (command.getStartAt() == null || !command.getStartAt().isAfter(now)
                || command.getRepeatUntil() == null || command.getRepeatOn() == null
                || command.getRepeatOn().isEmpty()
                || command.getRepeatUntil().isBefore(command.getStartAt().toLocalDate())
                || command.getRepeatUntil().isAfter(command.getStartAt().toLocalDate()
                        .plusMonths(BookingConstants.MAX_RECURRENCE_MONTHS))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Recurring booking requires future start, weekdays and an end date within 3 months");
        }

        List<TimeSlot> slots = new ArrayList<>();
        for (LocalDate day = command.getStartAt().toLocalDate();
             !day.isAfter(command.getRepeatUntil()); day = day.plusDays(1)) {
            if (!command.getRepeatOn().contains(day.getDayOfWeek())) {
                continue;
            }
            LocalDateTime occurrenceStart = LocalDateTime.of(day, command.getStartAt().toLocalTime());
            if (!occurrenceStart.isAfter(now)) {
                continue;
            }
            slots.add(TimeSlotUtils.of(occurrenceStart, command.getDurationMinutes()));
            if (slots.size() > BookingConstants.MAX_RECURRING_OCCURRENCES) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                        "Too many recurring occurrences");
            }
        }
        if (slots.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Recurrence creates no future occurrences");
        }
        return List.copyOf(slots);
    }
}

package ru.practicum.shareit.validation;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateValidator {

    public boolean isCorrectDate(LocalDateTime start, LocalDateTime end) {
        return start.isAfter(LocalDateTime.now()) && end.isAfter(LocalDateTime.now()) && start.isBefore(end);
    }
}

package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.annotation.FutureOrPresentSelectDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FutureOrPresentSelectDateValidator implements ConstraintValidator<FutureOrPresentSelectDate, LocalDate> {
    private LocalDate selectDate;

    @Override
    public void initialize(FutureOrPresentSelectDate constraintAnnotation) {
        selectDate = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true;
        }
        return !date.isBefore(selectDate);
    }
}
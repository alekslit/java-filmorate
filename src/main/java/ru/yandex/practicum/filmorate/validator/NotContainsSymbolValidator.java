package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.annotation.NotContainsSymbol;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotContainsSymbolValidator implements ConstraintValidator<NotContainsSymbol, String> {
    private String selectSymbol;
    
    @Override
    public void initialize(NotContainsSymbol constraintAnnotation) {
        selectSymbol = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String string, ConstraintValidatorContext context) {
        if (string == null) {
            return true;
        }
        return !string.contains(selectSymbol);
    }
}
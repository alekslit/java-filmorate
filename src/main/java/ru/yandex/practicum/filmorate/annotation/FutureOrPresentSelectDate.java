package ru.yandex.practicum.filmorate.annotation;

import ru.yandex.practicum.filmorate.validator.FutureOrPresentSelectDateValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

// Аннотация для проверки даты (должна быть >= selectedDate):
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {FutureOrPresentSelectDateValidator.class})
public @interface FutureOrPresentSelectDate {
    String message() default "{Дата должна быть позже или равняться выбранной дате}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String value() default "1895-12-28";
}
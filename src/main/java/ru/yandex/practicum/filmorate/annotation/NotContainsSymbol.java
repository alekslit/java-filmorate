package ru.yandex.practicum.filmorate.annotation;

import ru.yandex.practicum.filmorate.validator.NotContainsSymbolValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

// Аннотация для проверки на отсутствие выбранного символа:
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {NotContainsSymbolValidator.class})
public @interface NotContainsSymbol {
    String message() default "{Строка не должна содержать выбранный символ}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String value() default " ";
}

package ru.yandex.kardomoblieapp.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
@Constraint(validatedBy = RussianCensorValidator.class)
public @interface Censored {

    String message() default "Строка содержит нецензурные слова.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

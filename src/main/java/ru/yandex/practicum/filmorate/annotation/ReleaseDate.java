package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Constraint(validatedBy = ReleaseDateValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseDate {
    String message() default "Дата должна быть после {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
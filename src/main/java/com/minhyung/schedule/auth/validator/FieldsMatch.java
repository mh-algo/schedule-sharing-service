package com.minhyung.schedule.auth.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 두 필드의 값이 같은지 검증하는 어노테이션
 * <p> 이 어노테이션은 클래스 레벨에 적용되며, 두 필드 값이 일치하는지 여부를 검증합니다.
 * 검증할 필드의 이름은 {@code first}와 {@code second}에 지정합니다.
 * <p> 검증 로직은 getter 메서드를 통해 동작하므로, 비교할 필드에 대한 <b>getter 메서드가 반드시 선언</b>되어 있어야 합니다.
 * <p> {@code message} 속성은 검증 실패 시 반환될 메시지를 지정합니다.
 * 해당 메시지는 두 필드의 값이 일치하지 않을 때 표시됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldsMatchValidator.class)
public @interface FieldsMatch {
    String message() default "두 필드 값이 일치하지 않습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String first();
    String second();
}

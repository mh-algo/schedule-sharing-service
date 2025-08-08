package com.minhyung.schedule.auth.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {
    private String firstFieldName;
    private String secondFieldName;

    /**
     * 비교할 필드의 이름을 초기화
     * <p> {@link FieldsMatch} 어노테이션에서 입력받은 {@code first}와 {@code second} 값을
     * 각각 {@code firstFieldName}과 {@code secondFieldName}으로 설정합니다.
     *
     * @param constraintAnnotation {@link FieldsMatch} 어노테이션 인스턴스로, 비교할 필드의 이름을 포함합니다.
     */
    @Override
    public void initialize(FieldsMatch constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
    }

    /**
     * 필드의 값을 비교 후 검증
     * <p> 두 필드의 값이 같을 경우 true, 다를 경우 false를 반환합니다.
     *
     * @param object 검증할 객체로, 비교할 필드를 포함합니다.
     * @param constraintValidatorContext 검증 작업에 필요한 컨텍스트 정보가 포함된 객체
     * @return 두 필드의 값이 같을 경우 true, 다를 경우 false, {@link ReflectiveOperationException}이 발생하면 false
     */
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        try {
            Object firstValue = getFieldValue(object, firstFieldName);
            Object secondValue = getFieldValue(object, secondFieldName);

            return Objects.equals(firstValue, secondValue);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * 객체에서 지정된 필드의 값을 반환
     * <p> 주어진 객체에서 필드 이름을 기반으로 getter 메서드를 찾아 호출하여 값을 반환합니다.
     * <p> 객체에 필드의 getter 메서드가 {@code public}이 아닌 경우,
     * {@code setAccessible(true)}로 접근 권한을 설정한 후 메서드를 호출하여 값을 반환합니다.
     * <p> 주어진 객체에 선언된 메서드가 존재하지 않는 경우, 상속받은 클래스의 {@code public} 메서드 중 해당되는 메서드를 찾습니다.
     * 메서드가 존재할 경우 호출하여 값을 반환합니다.
     *
     * @param object 값을 가져올 대상 객체
     * @param fieldName 값을 가져올 필드의 이름
     * @return 지정된 필드 이름에 해당하는 필드의 값
     * @throws IllegalAccessException 메서드에 접근할 수 없는 경우 발생
     * @throws NoSuchMethodException 해당 이름의 메서드가 존재하지 않을 때 발생
     * @throws InvocationTargetException 메서드 호출 중 예외가 발생할 때 발생
     */
    private Object getFieldValue(Object object, String fieldName) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String methodName = object.getClass().isRecord()
                ? fieldName
                : getMethodName(fieldName);

        try {
            Method declaredMethod = object.getClass().getDeclaredMethod(methodName);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(object);
        } catch (NoSuchMethodException e) {
            Method publicMethod = object.getClass().getMethod(methodName);
            return publicMethod.invoke(object);
        }
    }

    /**
     * 주어진 필드 이름에 해당하는 getter 메서드의 이름 반환
     * <p> 필드 이름의 첫 글자를 대문자로 변환한 후 "get" 접두사를 추가하여 getter 메서드 이름을 생성합니다.
     *
     * @param fieldName getter 메서드 이름을 생성할 필드 이름
     * @return 주어진 필드 이름에 대응하는 getter 메서드 이름
     */
    private static String getMethodName(String fieldName) {
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}

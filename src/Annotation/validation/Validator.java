package Annotation.validation;

import java.lang.reflect.Field;

public class Validator {

    public static void validateField(Field field, String value) throws IllegalArgumentException {
        // Check for @Required annotation
        if (field.isAnnotationPresent(Required.class) && (value == null || value.isEmpty())) {
            Required annotation = field.getAnnotation(Required.class);
            throw new IllegalArgumentException(annotation.message());
        }

        // Check for @Size annotation (for String length)
        if (field.isAnnotationPresent(Size.class)) {
            Size annotation = field.getAnnotation(Size.class);
            if (value.length() < annotation.min() || value.length() > annotation.max()) {
                throw new IllegalArgumentException(annotation.message());
            }
        }

        // Check for @Min and @Max annotations (for numeric values)
        try {
            if (field.isAnnotationPresent(Min.class)) {
                Min annotation = field.getAnnotation(Min.class);
                double numericValue = Double.parseDouble(value);  // Convert to double
                if (numericValue < annotation.value()) {
                    throw new IllegalArgumentException(annotation.message());
                }
            }

            if (field.isAnnotationPresent(Max.class)) {
                Max annotation = field.getAnnotation(Max.class);
                double numericValue = Double.parseDouble(value);  // Convert to double
                if (numericValue > annotation.value()) {
                    throw new IllegalArgumentException(annotation.message());
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric format for field: " + field.getName());
        }
    }


    public static void validate(Object obj) throws IllegalArgumentException, IllegalAccessException {
        Class<?> objClass = obj.getClass();
        for (Field field : objClass.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);

            // Check @Required annotation for non-primitive, non-null fields
            if (!field.getType().isPrimitive() && field.isAnnotationPresent(Required.class) && (value == null) ) {
                Required annotation = field.getAnnotation(Required.class);
                throw new IllegalArgumentException(annotation.message());
            }

            // Check @Size annotation (for CharSequence types)
            if (field.isAnnotationPresent(Size.class) && value instanceof CharSequence) {
                Size annotation = field.getAnnotation(Size.class);
                int length = ((CharSequence) value).length();
                if (length < annotation.min() || length > annotation.max()) {
                    throw new IllegalArgumentException(annotation.message());
                }
            }

            // Check @Min annotation (for numeric fields, including Double and Integer)
            if (field.isAnnotationPresent(Min.class) && value instanceof Number) {
                Min annotation = field.getAnnotation(Min.class);
                if (((Number) value).doubleValue() < annotation.value()) {
                    throw new IllegalArgumentException(annotation.message());
                }
            }

            // Check @Max annotation (for numeric fields, including Double and Integer)
            if (field.isAnnotationPresent(Max.class) && value instanceof Number) {
                Max annotation = field.getAnnotation(Max.class);
                if (((Number) value).doubleValue() > annotation.value()) {
                    throw new IllegalArgumentException(annotation.message());
                }
            }
        }
    }
}

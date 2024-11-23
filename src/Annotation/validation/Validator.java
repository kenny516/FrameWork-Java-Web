package Annotation.validation;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Validator {

    public static boolean validate(Object obj, HttpServletRequest request) throws IllegalAccessException {
        Class<?> objClass = obj.getClass();
        Map<String, String> errors = new HashMap<>();

        for (Field field : objClass.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);

            try {
                validateField(field, value);
            } catch (IllegalArgumentException e) {
                // Ajouter l'erreur à la requête avec le nom conventionnel "error_<nom_du_champ>"
                errors.put("error_" + field.getName(), e.getMessage());
            }
        }

        // Ajouter les erreurs à la requête
        if (!errors.isEmpty()) {
            for (Map.Entry<String, String> entry : errors.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
            return false; // Indique qu'il y a des erreurs
        }

        return true; // Indique que la validation a réussi
    }

    private static void validateField(Field field, Object value) throws IllegalArgumentException {
        if (field.isAnnotationPresent(Required.class)) {
            validateRequired(field, value);
        }

        if (field.isAnnotationPresent(Size.class) && value instanceof CharSequence) {
            validateSize(field, (CharSequence) value);
        }

        if (field.isAnnotationPresent(Min.class) && value instanceof Number) {
            validateMin(field, (Number) value);
        }

        if (field.isAnnotationPresent(Max.class) && value instanceof Number) {
            validateMax(field, (Number) value);
        }

        if (field.isAnnotationPresent(Email.class) && value instanceof String) {
            validateEmail(field, (String) value);
        }
    }

    private static void validateRequired(Field field, Object value) {
        if (value == null || (value instanceof CharSequence && ((CharSequence) value).toString().isEmpty())) {
            Required annotation = field.getAnnotation(Required.class);
            throw new IllegalArgumentException(annotation.message());
        }
    }

    private static void validateSize(Field field, CharSequence value) {
        Size annotation = field.getAnnotation(Size.class);
        int length = value.length();
        if (length < annotation.min() || length > annotation.max()) {
            throw new IllegalArgumentException(annotation.message());
        }
    }

    private static void validateMin(Field field, Number value) {
        Min annotation = field.getAnnotation(Min.class);
        if (value.doubleValue() < annotation.value()) {
            throw new IllegalArgumentException(annotation.message());
        }
    }

    private static void validateMax(Field field, Number value) {
        Max annotation = field.getAnnotation(Max.class);
        if (value.doubleValue() > annotation.value()) {
            throw new IllegalArgumentException(annotation.message());
        }
    }

    private static void validateEmail(Field field, String value) {
        Email annotation = field.getAnnotation(Email.class);
        String regex = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.matches(regex, value)) {
            throw new IllegalArgumentException(annotation.message());
        }
    }
}
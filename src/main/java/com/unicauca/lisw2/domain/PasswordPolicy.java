package com.unicauca.lisw2.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Clase que implementa la política de contraseñas de la aplicación. 
// Se encarga de validar que una contraseña cumpla con los requisitos mínimos de seguridad.




public final class PasswordPolicy {

    private static final int MIN_LENGTH = 6;
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile(".*[^a-zA-Z0-9].*");

    private PasswordPolicy() {   
    }

   
    public static List<String> validate(String rawPassword) {
        List<String> errors = new ArrayList<>();

        if (rawPassword == null || rawPassword.isEmpty()) {
            errors.add("La contraseña no puede estar vacía");
            return errors; // no tiene sentido seguir validando un valor nulo o vacio
        }
        if (rawPassword.length() < MIN_LENGTH) {
            errors.add("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        if (!HAS_DIGIT.matcher(rawPassword).matches()) {
            errors.add("La contraseña debe tener al menos un dígito");
        }
        if (!HAS_UPPERCASE.matcher(rawPassword).matches()) {
            errors.add("La contraseña debe tener al menos una mayúscula");
        }
        if (!HAS_SPECIAL_CHAR.matcher(rawPassword).matches()) {
            errors.add("La contraseña debe tener al menos un carácter especial");
        }
        return errors;
    }

    public static boolean isValid(String rawPassword) {
        return validate(rawPassword).isEmpty();
    }
}
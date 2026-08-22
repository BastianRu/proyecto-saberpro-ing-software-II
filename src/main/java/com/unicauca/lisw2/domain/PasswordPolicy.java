package com.unicauca.lisw2.domain;
 
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Reglas de política de contraseñas
// Minimo 6 caracteres, al menos un digito, al menos un caracter especial y al menos una mayuscula




public final class PasswordPolicy {

  private static final int MIN_LENGTH = 6;
  
  private static final Pattern HAS_DIGIT  = Pattern.compile(".*\\d.*");
  private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
  private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");


  private PasswordPolicy() {
   }


   public static List<String> validate(String rawPassword){


    List<String> errors = new ArrayList<>();

    if(rawPassword == null || rawPassword.isBlank()){
      errors.add("Password cannot be null or empty");
      return errors;
    }

    if(rawPassword.length() < MIN_LENGTH){
      errors.add("Password must be at least " + MIN_LENGTH + " characters long");
    }

    if(!HAS_DIGIT.matcher(rawPassword).matches()){
      errors.add("Password must contain at least one digit");
    }

    if(!HAS_UPPERCASE.matcher(rawPassword).matches()){
      errors.add("Password must contain at least one uppercase letter");
    }

    if(!HAS_SPECIAL_CHAR.matcher(rawPassword).matches()){
      errors.add("Password must contain at least one special character");
    }

    return errors;
   }



   public static boolean isValid(String rawPassword){
    return validate(rawPassword).isEmpty();
   }



}
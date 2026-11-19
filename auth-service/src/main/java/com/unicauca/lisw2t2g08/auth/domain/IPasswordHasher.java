

package com.unicauca.lisw2t2g08.auth.domain;

public interface  IPasswordHasher {
    String hash(String password);
    boolean matches(String password, String hashedPassword);
}
package com.gemalto.cota.error;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Book id not found : " + id);
    }

}

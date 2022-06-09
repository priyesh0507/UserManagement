package com.gemalto.cota.error;

import java.util.Set;

public class UserUnSupportedFieldPatchException extends RuntimeException {

    public UserUnSupportedFieldPatchException(Set<String> keys) {
        super("Field " + keys.toString() + " update is not allow.");
    }

}

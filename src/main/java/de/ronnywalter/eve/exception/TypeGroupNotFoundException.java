package de.ronnywalter.eve.exception;

public class TypeGroupNotFoundException extends RuntimeException {

    public TypeGroupNotFoundException(Integer id) {
        super("could not find typegroup " + id);
    }
}

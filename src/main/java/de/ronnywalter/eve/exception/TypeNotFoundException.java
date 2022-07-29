package de.ronnywalter.eve.exception;

public class TypeNotFoundException extends RuntimeException {

    public TypeNotFoundException(Integer id) {
        super("could not find type " + id);
    }
}

package de.ronnywalter.eve.exception;

public class TypeCategoryNotFoundException extends RuntimeException {

    public TypeCategoryNotFoundException(Integer id) {
        super("could not find typecategory " + id);
    }
}

package de.ronnywalter.eve.exception;

public class RegionNotFoundException extends RuntimeException {

    public RegionNotFoundException(Integer id) {
        super("could not find region " + id);
    }
}

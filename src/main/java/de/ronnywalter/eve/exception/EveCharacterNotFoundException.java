package de.ronnywalter.eve.exception;

public class EveCharacterNotFoundException extends RuntimeException {
    public EveCharacterNotFoundException() {
        super();
    }

    public EveCharacterNotFoundException(String message) {
        super(message);
    }
}

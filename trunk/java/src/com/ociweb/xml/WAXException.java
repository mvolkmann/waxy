package com.ociweb.xml;

public abstract class WAXException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WAXException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}

package com.ociweb.xml;

import java.io.IOException;

public abstract class WAXException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WAXException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public IOException getIOException() {
        return (IOException) getCause();
    }

}

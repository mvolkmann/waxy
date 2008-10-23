package com.ociweb.xml;

import java.io.IOException;

public class WAXIOException extends WAXException {

    private static final long serialVersionUID = 1L;

    public WAXIOException(final IOException ioException) {
        super("Unexpected IOException: " + ioException.getMessage(),
                ioException);
    }

    public IOException getIOException() {
        return (IOException) getCause();
    }

}

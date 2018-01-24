package org.stellar.sdk.http;

import java.io.IOException;

public class ClientProtocolException extends IOException {

    public ClientProtocolException() {
        super();
    }

    public ClientProtocolException(final String s) {
        super(s);
    }

    public ClientProtocolException(final Throwable cause) {
        initCause(cause);
    }

    public ClientProtocolException(final String message, final Throwable cause) {
        super(message);
        initCause(cause);
    }

}

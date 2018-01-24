package org.stellar.sdk.http;

import org.stellar.sdk.http.ClientProtocolException;

public class HttpResponseException extends ClientProtocolException {

    private final int statusCode;

    public HttpResponseException(final int statusCode, final String s) {
        super(s);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

}

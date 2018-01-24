package org.stellar.sdk;

import java.io.IOException;

public interface ResponseHandler<T> {
    T handleResponse(final okhttp3.Response response) throws IOException;
}

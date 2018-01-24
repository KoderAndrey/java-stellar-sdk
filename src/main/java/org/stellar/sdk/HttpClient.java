package org.stellar.sdk;

import okhttp3.*;
import org.stellar.sdk.requests.ResponseHandlerClass;

import java.io.IOException;

public enum HttpClient {
    INSTANCE;

    private final OkHttpClient okHttpClient;

    private HttpClient(){
        okHttpClient = new OkHttpClient();
    }

    public static OkHttpClient instance() {
        return INSTANCE.okHttpClient;
    }

    public static Call newCall(Request request) {
        return INSTANCE.okHttpClient.newCall(request);
    }

    public static <T>
    T executeGetAndHandleResponse(HttpUrl url,
                                  ResponseHandler<T> responseHandler)
            throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response =
                     INSTANCE.okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            return responseHandler.handleResponse(response);
        }

    }
}

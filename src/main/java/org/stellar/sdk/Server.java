package org.stellar.sdk;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.stellar.sdk.http.HttpClient;
import org.stellar.sdk.requests.*;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.IOException;

/**
 * Main class used to connect to Horizon server.
 */
public class Server {
    private HttpUrl serverUrl;
    private OkHttpClient httpClient = HttpClient.instance();

    public Server(String url) {
        serverUrl = HttpUrl.parse(url);
        if (serverUrl == null) {
            throw new RuntimeException("Server constructor parameter is not"
                                       + " a well-formed HTTP or HTTPS URL");
        }
    }

    /**
     * Returns {@link AccountsRequestBuilder} instance.
     */
    public AccountsRequestBuilder accounts() {
        return new AccountsRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link EffectsRequestBuilder} instance.
     */
    public EffectsRequestBuilder effects() {
        return new EffectsRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link LedgersRequestBuilder} instance.
     */
    public LedgersRequestBuilder ledgers() {
        return new LedgersRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link OffersRequestBuilder} instance.
     */
    public OffersRequestBuilder offers() {
        return new OffersRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link OperationsRequestBuilder} instance.
     */
    public OperationsRequestBuilder operations() {
        return new OperationsRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link OrderBookRequestBuilder} instance.
     */
    public OrderBookRequestBuilder orderBook() {
        return new OrderBookRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link TradesRequestBuilder} instance.
     */
    public TradesRequestBuilder trades() {
        return new TradesRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link PathsRequestBuilder} instance.
     */
    public PathsRequestBuilder paths() {
        return new PathsRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link PaymentsRequestBuilder} instance.
     */
    public PaymentsRequestBuilder payments() {
        return new PaymentsRequestBuilder(serverUrl);
    }

    /**
     * Returns {@link TransactionsRequestBuilder} instance.
     */
    public TransactionsRequestBuilder transactions() {
        return new TransactionsRequestBuilder(serverUrl);
    }

    /**
     * Submits transaction to the network.
     * @param transaction transaction to submit to the network.
     * @return {@link SubmitTransactionResponse}
     * @throws IOException
     */
    public SubmitTransactionResponse submitTransaction(Transaction transaction) throws IOException {
        HttpUrl transactionsUrl;
        transactionsUrl = serverUrl.newBuilder().addPathSegment("transactions").build();
        RequestBody formBody = new FormBody.Builder()
                .add("tx", transaction.toEnvelopeXdrBase64())
                .build();
        Request request = new Request.Builder()
                .url(transactionsUrl)
                .post(formBody)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful())
                return null;
            String responseString = response.body().string();
            SubmitTransactionResponse submitTransactionResponse =
                    GsonSingleton.getInstance().
                            fromJson(responseString,
                                     SubmitTransactionResponse.class);
            return submitTransactionResponse;
        }
    }

    /**
     * To support mocking a client
     * @param httpClient
     */
    void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}

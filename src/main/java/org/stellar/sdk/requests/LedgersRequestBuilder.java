package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;

import okhttp3.HttpUrl;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.stellar.sdk.http.HttpClient;
import org.stellar.sdk.ResponseHandler;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.LedgerResponse;
import org.stellar.sdk.responses.Page;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Builds requests connected to ledgers.
 */
public class LedgersRequestBuilder extends RequestBuilder {
  public LedgersRequestBuilder(HttpUrl serverUrl) {
    super(serverUrl, "ledgers");
  }

  /**
   * Requests specific <code>uri</code> and returns {@link LedgerResponse}.
   * This method is helpful for getting the links.
   * @throws IOException
   */
  public LedgerResponse ledger(HttpUrl url) throws IOException {
    TypeToken type = new TypeToken<LedgerResponse>() {};
    ResponseHandler<LedgerResponse> responseHandler = new ResponseHandlerClass<LedgerResponse>(type);
    return HttpClient.executeGetAndHandleResponse(url, responseHandler);
  }

  /**
   * Requests <code>GET /ledgers/{ledgerSeq}</code>
   * @see <a href="https://www.stellar.org/developers/horizon/reference/ledgers-single.html">Ledger Details</a>
   * @param ledgerSeq Ledger to fetch
   * @throws IOException
   */
  public LedgerResponse ledger(long ledgerSeq) throws IOException {
    this.setSegments("ledgers", String.valueOf(ledgerSeq));
    return this.ledger(this.buildUrl());
  }

  /**
   * Requests specific <code>uri</code> and returns {@link Page} of {@link LedgerResponse}.
   * This method is helpful for getting the next set of results.
   * @return {@link Page} of {@link LedgerResponse}
   * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
   * @throws IOException
   */
  public static Page<LedgerResponse> execute(HttpUrl url) throws IOException, TooManyRequestsException {
    TypeToken type = new TypeToken<Page<LedgerResponse>>() {};
    ResponseHandler<Page<LedgerResponse>> responseHandler = new ResponseHandlerClass<Page<LedgerResponse>>(type);
    return HttpClient.executeGetAndHandleResponse(url, responseHandler);
  }

  /**
   * Allows to stream SSE events from horizon.
   * Certain endpoints in Horizon can be called in streaming mode using Server-Sent Events.
   * This mode will keep the connection to horizon open and horizon will continue to return
   * responses as ledgers close.
   * @see <a href="http://www.w3.org/TR/eventsource/" target="_blank">Server-Sent Events</a>
   * @see <a href="https://www.stellar.org/developers/horizon/learn/responses.html" target="_blank">Response Format documentation</a>
   * @param listener {@link EventListener} implementation with {@link LedgerResponse} type
   * @return EventSource object, so you can <code>close()</code> connection when not needed anymore
   */
  public EventSource stream(final EventListener<LedgerResponse> listener) {
    Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
    WebTarget target = client.target(this.buildUrl().uri());
    EventSource eventSource = new EventSource(target) {
      @Override
      public void onEvent(InboundEvent inboundEvent) {
        String data = inboundEvent.readData(String.class);
        if (data.equals("\"hello\"")) {
          return;
        }
        LedgerResponse ledger = GsonSingleton.getInstance().fromJson(data, LedgerResponse.class);
        listener.onEvent(ledger);
      }
    };
    return eventSource;
  }

  /**
   * Build and execute request.
   * @return {@link Page} of {@link LedgerResponse}
   * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
   * @throws IOException
   */
  public Page<LedgerResponse> execute() throws IOException, TooManyRequestsException {
    return this.execute(this.buildUrl());
  }

  @Override
  public LedgersRequestBuilder cursor(String token) {
    super.cursor(token);
    return this;
  }

  @Override
  public LedgersRequestBuilder limit(int number) {
    super.limit(number);
    return this;
  }

  @Override
  public LedgersRequestBuilder order(Order direction) {
    super.order(direction);
    return this;
  }
}

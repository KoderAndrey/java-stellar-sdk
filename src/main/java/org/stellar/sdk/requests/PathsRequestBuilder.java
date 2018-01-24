package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;

import okhttp3.HttpUrl;
import org.stellar.sdk.*;
import org.stellar.sdk.http.HttpClient;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.PathResponse;

import java.io.IOException;

/**
 * Builds requests connected to paths.
 */
public class PathsRequestBuilder extends RequestBuilder {
  public PathsRequestBuilder(HttpUrl serverUrl) {
    super(serverUrl, "paths");
  }

  public PathsRequestBuilder destinationAccount(KeyPair account) {
    urlBuilder.addQueryParameter("destination_account", account.getAccountId());
    return this;
  }

  public PathsRequestBuilder sourceAccount(KeyPair account) {
    urlBuilder.addQueryParameter("source_account", account.getAccountId());
    return this;
  }

  public PathsRequestBuilder destinationAmount(String amount) {
    urlBuilder.addQueryParameter("destination_amount", amount);
    return this;
  }

  public PathsRequestBuilder destinationAsset(Asset asset) {
    urlBuilder.addQueryParameter("destination_asset_type", asset.getType());
    if (asset instanceof AssetTypeCreditAlphaNum) {
      AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
      urlBuilder.addQueryParameter("destination_asset_code", creditAlphaNumAsset.getCode());
      urlBuilder.addQueryParameter("destination_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
    }
    return this;
  }

  /**
   * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
   * @throws IOException
   */
  public static Page<PathResponse> execute(HttpUrl url) throws IOException, TooManyRequestsException {
    TypeToken type = new TypeToken<Page<PathResponse>>() {};
    ResponseHandler<Page<PathResponse>> responseHandler = new ResponseHandlerClass<Page<PathResponse>>(type);
    return HttpClient.executeGetAndHandleResponse(url, responseHandler);
  }

  /**
   * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
   * @throws IOException
   */
  public Page<PathResponse> execute() throws IOException, TooManyRequestsException {
    return this.execute(this.buildUrl());
  }
}

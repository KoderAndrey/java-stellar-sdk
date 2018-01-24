package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.HttpClient;
import org.stellar.sdk.ResponseHandler;
import org.stellar.sdk.responses.TradeResponse;

import java.io.IOException;

/**
 * Builds requests connected to trades.
 */
public class TradesRequestBuilder extends RequestBuilder {
    public TradesRequestBuilder(HttpUrl serverUrl) {
        super(serverUrl, "order_book/trades");
    }

    public TradesRequestBuilder buyingAsset(Asset asset) {
        urlBuilder.addQueryParameter("buying_asset_type", asset.getType());
        if (asset instanceof AssetTypeCreditAlphaNum) {
            AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
            urlBuilder.addQueryParameter("buying_asset_code", creditAlphaNumAsset.getCode());
            urlBuilder.addQueryParameter("buying_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
        }
        return this;
    }

    public TradesRequestBuilder sellingAsset(Asset asset) {
        urlBuilder.addQueryParameter("selling_asset_type", asset.getType());
        if (asset instanceof AssetTypeCreditAlphaNum) {
            AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
            urlBuilder.addQueryParameter("selling_asset_code", creditAlphaNumAsset.getCode());
            urlBuilder.addQueryParameter("selling_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
        }
        return this;
    }

    public static TradeResponse execute(HttpUrl url) throws IOException, TooManyRequestsException {
        TypeToken type = new TypeToken<TradeResponse>() {};
        ResponseHandler<TradeResponse> responseHandler = new ResponseHandlerClass<TradeResponse>(type);
        return HttpClient.executeGetAndHandleResponse(url, responseHandler);
    }

    public TradeResponse execute() throws IOException, TooManyRequestsException {
        return this.execute(this.buildUrl());
    }
}

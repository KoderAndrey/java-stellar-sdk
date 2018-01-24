package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;

import okhttp3.ResponseBody;
import org.stellar.sdk.ResponseHandler;
import org.stellar.sdk.http.client.ClientProtocolException;
import org.stellar.sdk.http.client.HttpResponseException;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.Response;

import java.io.IOException;

public class ResponseHandlerClass<T> implements ResponseHandler<T> {

  private TypeToken<T> type;

  /**
   * "Generics on a type are typically erased at runtime, except when the type is compiled with the
   * generic parameter bound. In that case, the compiler inserts the generic type information into
   * the compiled class. In other cases, that is not possible."
   * More info: http://stackoverflow.com/a/14506181
   * @param type
   */
  public ResponseHandlerClass(TypeToken<T> type) {
    this.type = type;
  }

  public T handleResponse(final okhttp3.Response response) throws IOException, TooManyRequestsException {
    int statusCode = response.code();
    ResponseBody body = response.body();

    // Too Many Requests
    if (statusCode == 429) {
      int retryAfter = Integer.parseInt(response.header("Retry-After"));
      throw new TooManyRequestsException(retryAfter);
    }
    // Other errors
    if (statusCode >= 300) {
      throw new HttpResponseException(statusCode, response.message());
    }
    // No content
    if (body == null) {
      throw new ClientProtocolException("Response contains no content");
    }
    String content = body.string();

    T object = GsonSingleton.getInstance().fromJson(content, type.getType());
    if (object instanceof Response) {
      ((Response) object).setHeaders(
              response.header("X-Ratelimit-Limit"),
              response.header("X-Ratelimit-Remaining"),
              response.header("X-Ratelimit-Reset")
      );
    }
    return object;
  }
}

package org.stellar.sdk.responses;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import okhttp3.HttpUrl;
import org.stellar.sdk.HttpClient;
import org.stellar.sdk.ResponseHandler;
import org.stellar.sdk.requests.ResponseHandlerClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Represents page of objects.
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/page.html" target="_blank">Page documentation</a>
 */
public class Page<T> extends Response {
  @SerializedName("records")
  private ArrayList<T> records;
  @SerializedName("links")
  private Links links;

  Page() {}

  public ArrayList<T> getRecords() {
    return records;
  }

  public Links getLinks() {
    return links;
  }

  /**
   * @return The next page of results or null when there is no more results
   * @throws URISyntaxException
   * @throws IOException
   */
  public Page<T> getNextPage() throws URISyntaxException, IOException {
    if (this.getLinks().getNext() == null) {
      return null;
    }
    TypeToken type = new TypeToken<Page<T>>() {};
    ResponseHandler<Page<T>> responseHandler = new ResponseHandlerClass<Page<T>>(type);
    HttpUrl url = HttpUrl.parse(this.getLinks().getNext().getHref());
    return HttpClient.executeGetAndHandleResponse(url, responseHandler);
  }

  /**
   * Links connected to page response.
   */
  public static class Links {
    @SerializedName("next")
    private final Link next;
    @SerializedName("prev")
    private final Link prev;
    @SerializedName("self")
    private final Link self;

    Links(Link next, Link prev, Link self) {
      this.next = next;
      this.prev = prev;
      this.self = self;
    }

    public Link getNext() {
      return next;
    }

    public Link getPrev() {
      return prev;
    }

    public Link getSelf() {
      return self;
    }
  }
}

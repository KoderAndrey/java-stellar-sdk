package org.stellar.sdk.federation;

import com.google.common.net.InternetDomainName;
import com.google.gson.reflect.TypeToken;
import com.moandjiezana.toml.Toml;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.stellar.sdk.http.HttpClient;
import org.stellar.sdk.ResponseHandler;
import org.stellar.sdk.http.HttpResponseException;
import org.stellar.sdk.requests.ResponseHandlerClass;

import java.io.IOException;
import java.net.URI;

/**
 * FederationServer handles a network connection to a
 * <a href="https://www.stellar.org/developers/learn/concepts/federation.html" target="_blank">federation server</a>
 * instance and exposes an interface for requests to that instance.
 *
 * For resolving a stellar address without knowing which federation server
 * to query use {@link Federation#resolve(String)}.
 *
 * @see <a href="https://www.stellar.org/developers/learn/concepts/federation.html" target="_blank">Federation docs</a>
 */
public class FederationServer {
  private final HttpUrl serverUrl;
  private final InternetDomainName domain;
  private static OkHttpClient httpClient = HttpClient.instance();

  /**
   * Creates a new <code>FederationServer</code> instance.
   * @param serverUri Federation Server URI
   * @param domain Domain name this federation server is responsible for
   * @throws FederationServerInvalidException Federation server is invalid (malformed URL, not HTTPS, etc.)
   */
  public FederationServer(URI serverUri, InternetDomainName domain) {
    if (serverUri.getScheme() != "https") {
      throw new FederationServerInvalidException();
    }
    serverUrl = HttpUrl.get(serverUri);
    this.domain = domain;
  }

  /**
   * Creates a new <code>FederationServer</code> instance.
   * @param serverUri Federation Server URI
   * @param domain Domain name this federation server is responsible for
   * @throws FederationServerInvalidException Federation server is invalid (malformed URL, not HTTPS, etc.)
   */
  public FederationServer(String serverUri, InternetDomainName domain) {
    serverUrl = HttpUrl.parse(serverUri);
    if (serverUrl == null || !serverUrl.scheme().equals("https") ) {
      throw new FederationServerInvalidException();
    }
    this.domain = domain;
  }

  /**
   * Creates a <code>FederationServer</code> instance for a given domain.
   * It tries to find a federation server URL in stellar.toml file.
   * @see <a href="https://www.stellar.org/developers/learn/concepts/stellar-toml.html" target="_blank">Stellar.toml docs</a>
   * @param domain Domain to find a federation server for
   * @throws ConnectionErrorException Connection problems
   * @throws NoFederationServerException Stellar.toml does not contain federation server info
   * @throws FederationServerInvalidException Federation server is invalid (malformed URL, not HTTPS, etc.)
   * @throws StellarTomlNotFoundInvalidException Stellar.toml file was not found or was malformed.
   * @return FederationServer
   */
  public static FederationServer createForDomain(InternetDomainName domain) {
    HttpUrl stellarTomlUrl;
    StringBuilder uriBuilder = new StringBuilder();
    uriBuilder.append("https://");
    uriBuilder.append(domain.toString());
    uriBuilder.append("/.well-known/stellar.toml");
    stellarTomlUrl = HttpUrl.parse(uriBuilder.toString());
    if (stellarTomlUrl == null) {
      throw new RuntimeException("Unexpected error when building URL"
                                 + " to get stellar.toml file");
    }
    Toml stellarToml = null;
    try {
      stellarToml = HttpClient.executeGetAndHandleResponse(
              stellarTomlUrl,
              new ResponseHandler<Toml>() {
                @Override
                public Toml handleResponse(Response response) throws IOException {
                  if (response.code() >= 300) {
                    throw new StellarTomlNotFoundInvalidException();
                  }

                  ResponseBody body = response.body();
                  if (body == null) {
                    throw new StellarTomlNotFoundInvalidException();
                  }

                  return new Toml().read(body.string());
                }
              });
    } catch (IOException e) {
      throw new ConnectionErrorException();
    }

    String federationServer = stellarToml.getString("FEDERATION_SERVER");
    if (federationServer == null) {
      throw new NoFederationServerException();
    }

    return new FederationServer(federationServer, domain);
  }

  /**
   * Resolves a stellar address using a given federation server.
   * @param address Stellar addres, like <code>bob*stellar.org</code>
   * @throws MalformedAddressException Address is malformed
   * @throws ConnectionErrorException Connection problems
   * @throws NotFoundException Stellar address not found by federation server
   * @throws ServerErrorException Federation server responded with error
   * @return FederationResponse
   */
  public FederationResponse resolveAddress(String address) {
    String[] tokens = address.split("\\*");
    if (tokens.length != 2) {
      throw new MalformedAddressException();
    }

    HttpUrl url = serverUrl.newBuilder().setQueryParameter("type", "name")
                                        .setQueryParameter("q", address)
                                        .build();

    TypeToken type = new TypeToken<FederationResponse>() {};
    ResponseHandler<FederationResponse> responseHandler = new ResponseHandlerClass<FederationResponse>(type);
    try {
      return HttpClient.executeGetAndHandleResponse(url, responseHandler);
    }
    catch (HttpResponseException e) {
      if (e.getStatusCode() == 404) {
        throw new NotFoundException();
      } else {
        throw new ServerErrorException();
      }
    } catch (IOException e) {
      throw new ConnectionErrorException();
    }
  }

  /**
   * Returns a federation server URI.
   * @return URI
   */
  public HttpUrl getServerUrl() {
    return serverUrl;
  }

  /**
   * Returns a domain this server is responsible for.
   * @return InternetDomainName
   */
  public InternetDomainName getDomain() {
    return domain;
  }

  /**
   * To support mocking a client
   * @param httpClient
   */
  static void setHttpClient(OkHttpClient httpClient) {
    FederationServer.httpClient = httpClient;
  }
}

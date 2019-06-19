package tw.clotai.weaklib.net;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Connection provides a convenient interface to fetch content from the web, and parse them into Documents.
 * <p/>
 * To get a new Connection, use {@link org.jsoup.Jsoup#connect(String)}. Connections contain {@link Connection.Request}
 * and {@link Connection.Response} objects. The request objects are reusable as prototype requests.
 * <p/>
 * Request configuration can be made using either the shortcut methods in Connection (e.g. {@link #userAgent(String)}),
 * or by methods in the Connection.Request object directly. All request configuration must be made before the request
 * is executed.
 */
public interface Connection {

    /**
     * GET and POST http methods.
     */
    enum Method {
        GET, POST
    }

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     *
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    Connection url(URL url);

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     *
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    Connection url(String url);

    /**
     * Set the request timeouts (connect and read). If a timeout occurs, an IOException will be thrown. The default
     * timeout is 3 seconds (3000 millis). A timeout of zero is treated as an infinite timeout.
     *
     * @param millis number of milliseconds (thousandths of a second) before timing out connects or reads.
     * @return this Connection, for chaining
     */
    Connection timeout(int millis);

    Connection useCache(boolean useCache);

    /**
     * Set the request referrer (aka "referer") header.
     *
     * @param referrer referrer to use
     * @return this Connection, for chaining
     */
    Connection referrer(String referrer);

    /**
     * Configures the connection to (not) follow server redirects. By default this is <b>true</b>.
     *
     * @param followRedirects true if server redirects should be followed.
     * @return this Connection, for chaining
     */
    Connection followRedirects(boolean followRedirects);

    Connection nativeFollowRedirects(boolean nativeFollowRedirects);

    Connection useragent(String agent);

    Connection useProxy(Proxy useproxy);

    /**
     * Set the request method to use, GET or POST. Default is GET.
     *
     * @param method HTTP request method
     * @return this Connection, for chaining
     */
    Connection method(Method method);

    /**
     * Add a request data parameter. Request parameters are sent in the request query string for GETs, and in the request
     * body for POSTs. A request may have multiple values of the same name.
     *
     * @param key   data key
     * @param value data value
     * @return this Connection, for chaining
     */
    Connection data(String key, String value);

    /**
     * Adds all of the supplied data to the request data parameters
     *
     * @param data map of data parameters
     * @return this Connection, for chaining
     */
    Connection data(Map<String, String> data);

    /**
     * Set a request header.
     *
     * @param name  header name
     * @param value header value
     * @return this Connection, for chaining
     * @see org.jsoup.Connection.Request#headers()
     */
    Connection header(String name, String value);

    /**
     * Set a cookie to be sent in the request.
     *
     * @param name  name of cookie
     * @param value value of cookie
     * @return this Connection, for chaining
     */
    Connection cookie(String name, String value);

    /**
     * Adds each of the supplied cookies to the request.
     *
     * @param cookies map of cookie name -> value pairs
     * @return this Connection, for chaining
     */
    Connection cookies(Map<String, String> cookies);

    Connection charset(String charset);

    /**
     * Execute the request as a GET, and parse the result.
     *
     * @throws java.net.MalformedURLException  if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException             if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException    if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException                     on error
     */
    Connection.Response get() throws IOException;

    /**
     * Execute the request as a POST, and parse the result.
     *
     * @throws java.net.MalformedURLException  if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException             if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException    if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException                     on error
     */
    Connection.Response post() throws IOException;

    /**
     * Execute the request.
     *
     * @return a response object
     * @throws java.net.MalformedURLException  if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException             if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException    if the response mime type is not supported and those errors are not ignored
     * @throws java.net.SocketTimeoutException if the connection times out
     * @throws IOException                     on error
     */
    Connection.Response execute() throws IOException;

    /**
     * Get the request object associated with this connection
     *
     * @return request
     */
    Request request();

    /**
     * Set the connection's request
     *
     * @param request new request object
     * @return this Connection, for chaining
     */
    Connection request(Request request);

    /**
     * Get the response, once the request has been executed
     *
     * @return response
     */
    Response response();

    /**
     * Set the connection's response
     *
     * @param response new response
     * @return this Connection, for chaining
     */
    Connection response(Response response);


    /**
     * Common methods for Requests and Responses
     *
     * @param <T> Type of Base, either Request or Response
     */
    interface Base<T extends Base<?>> {

        /**
         * Get the URL
         *
         * @return URL
         */
        URL url();

        /**
         * Set the URL
         *
         * @param url new URL
         * @return this, for chaining
         */
        T url(URL url);

        String baseURL();

        String oURL();

        /**
         * Get the request method
         *
         * @return method
         */
        Method method();

        /**
         * Set the request method
         *
         * @param method new method
         * @return this, for chaining
         */
        T method(Method method);

        /**
         * Get the value of a header. This is a simplified header model, where a header may only have one value.
         * <p/>
         * Header names are case insensitive.
         *
         * @param name name of header (case insensitive)
         * @return value of header, or null if not set.
         * @see #hasHeader(String)
         * @see #cookie(String)
         */
        String header(String name);

        /**
         * Set a header. This method will overwrite any existing header with the same case insensitive name.
         *
         * @param name  Name of header
         * @param value Value of header
         * @return this, for chaining
         */
        T header(String name, String value);

        /**
         * Check if a header is present
         *
         * @param name name of header (case insensitive)
         * @return if the header is present in this request/response
         */
        boolean hasHeader(String name);

        /**
         * Remove a header by name
         *
         * @param name name of header to remove (case insensitive)
         * @return this, for chaining
         */
        T removeHeader(String name);

        /**
         * Retrieve all of the request/response headers as a map
         *
         * @return headers
         */
        Map<String, String> headers();

        /**
         * Get a cookie value by name from this request/response.
         * <p/>
         * Response objects have a simplified cookie model. Each cookie set in the response is added to the response
         * object's cookie key=value map. The cookie's path, domain, and expiry date are ignored.
         *
         * @param name name of cookie to retrieve.
         * @return value of cookie, or null if not set
         */
        String cookie(String name);

        /**
         * Set a cookie in this request/response.
         *
         * @param name  name of cookie
         * @param value value of cookie
         * @return this, for chaining
         */
        T cookie(String name, String value);

        /**
         * Check if a cookie is present
         *
         * @param name name of cookie
         * @return if the cookie is present in this request/response
         */
        boolean hasCookie(String name);

        /**
         * Remove a cookie by name
         *
         * @param name name of cookie to remove
         * @return this, for chaining
         */
        T removeCookie(String name);

        /**
         * Retrieve all of the request/response cookies as a map
         *
         * @return cookies
         */
        Map<String, String> cookies();
    }

    /**
     * Represents a HTTP request.
     */
    interface Request extends Base<Request> {
        /**
         * Get the request timeout, in milliseconds.
         *
         * @return the timeout in milliseconds.
         */
        int timeout();

        /**
         * Update the request timeout.
         *
         * @param millis timeout, in milliseconds
         * @return this Request, for chaining
         */
        Request timeout(int millis);

        Request useCache(boolean usecache);

        /**
         * Get the maximum body size, in milliseconds.
         *
         * @return the maximum body size, in milliseconds.
         */
        int maxBodySize();

        /**
         * Update the maximum body size, in milliseconds.
         *
         * @param bytes maximum body size, in milliseconds.
         * @return this Request, for chaining
         */
        Request maxBodySize(int bytes);

        /**
         * Get the current followRedirects configuration.
         *
         * @return true if followRedirects is enabled.
         */
        boolean followRedirects();

        boolean nativeFollowRedirects();

        String useragent();

        Proxy useProxy();

        /**
         * Configures the request to (not) follow server redirects. By default this is <b>true</b>.
         *
         * @param followRedirects true if server redirects should be followed.
         * @return this Request, for chaining
         */
        Request followRedirects(boolean followRedirects);

        Request nativeFollowRedirects(boolean followRedirects);

        Request useragent(String agent);

        Request useProxy(Proxy useproxy);

        /**
         * Add a data parameter to the request
         *
         * @param keyval data to add.
         * @return this Request, for chaining
         */
        Request data(KeyVal keyval);

        String charset();

        void charset(String charset);

        /**
         * Get all of the request's data parameters
         *
         * @return collection of keyvals
         */
        Collection<KeyVal> data();
    }

    /**
     * Represents a HTTP response.
     */
    interface Response extends Base<Response> {

        /**
         * Get the status code of the response.
         *
         * @return status code
         */
        int statusCode();

        /**
         * Get the status message of the response.
         *
         * @return status message
         */
        String statusMessage();

        /**
         * Get the character set name of the response.
         *
         * @return character set name
         */
        String charset();

        void charset(String charset);

        /**
         * Get the response content type (e.g. "text/html");
         *
         * @return the response content type
         */
        String contentType();

        /**
         * Get the body of the response as a plain string.
         *
         * @return body
         */
        String body();

        void cookie(HttpCookie c);

        void cookies(List<HttpCookie> cs);

        List<HttpCookie> hcookies();
    }

    /**
     * A Key Value tuple.
     */
    interface KeyVal {

        /**
         * Update the key of a keyval
         *
         * @param key new key
         * @return this KeyVal, for chaining
         */
        KeyVal key(String key);

        /**
         * Get the key of a keyval
         *
         * @return the key
         */
        String key();

        /**
         * Update the value of a keyval
         *
         * @param value the new value
         * @return this KeyVal, for chaining
         */
        KeyVal value(String value);

        /**
         * Get the value of a keyval
         *
         * @return the value
         */
        String value();
    }

}


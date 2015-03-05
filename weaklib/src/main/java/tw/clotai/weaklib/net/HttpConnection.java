package tw.clotai.weaklib.net;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Implementation of {@link Connection}.
 *
 * @see org.jsoup.Jsoup#connect(String)
 */
public class HttpConnection implements Connection {

    //private final static String USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X; en-us) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3";
    private final static String USER_AGENT = null;

    public static Connection newInstance(String url) {
        Connection con = new HttpConnection();

        String nurl = url.replace(" ", "%20");

        con.url(nurl);
        return con;
    }

    public static Connection newInstance(URL url) {
        Connection con = new HttpConnection();
        con.url(url);
        return con;
    }

    private Connection.Request req;
    private Connection.Response res;

    private HttpConnection() {
        req = new Request();
        res = new Response();
    }

    public Connection url(URL url) {
        req.url(url);
        return this;
    }

    public Connection url(String url) {
        Validate.notEmpty(url, "Must supply a valid URL");
        try {
            req.url(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }
        return this;
    }

    public Connection timeout(int millis) {
        req.timeout(millis);
        return this;
    }

    public Connection useCache(boolean useCache) {
        req.useCache(useCache);
        return this;
    }

    public Connection followRedirects(boolean followRedirects) {
        req.followRedirects(followRedirects);
        return this;
    }

    public Connection nativeFollowRedirects(boolean nativeFollowRedirects) {
        req.nativeFollowRedirects(nativeFollowRedirects);
        return this;
    }

    public Connection useragent(String agent) {
        if (agent == null) return this;
        req.useragent(agent);
        return this;
    }

    public Connection useProxy(Proxy useproxy) {
        req.useProxy(useproxy);
        return this;
    }

    public Connection referrer(String referrer) {
        //Validate.notNull(referrer, "Referrer must not be null");
        if (referrer != null) {
            req.header("Referer", referrer);
        }
        return this;
    }

    public Connection method(Method method) {
        req.method(method);
        return this;
    }

    public Connection data(String key, String value) {
        req.data(KeyVal.create(key, value));
        return this;
    }

    public Connection data(Map<String, String> data) {
        Validate.notNull(data, "Data map must not be null");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            req.data(KeyVal.create(entry.getKey(), entry.getValue()));
        }
        return this;
    }

    public Connection header(String name, String value) {
        req.header(name, value);
        return this;
    }

    public Connection cookie(String name, String value) {
        req.cookie(name, value);
        return this;
    }

    public Connection cookies(Map<String, String> cookies) {
        Validate.notNull(cookies, "Cookie map must not be null");
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            req.cookie(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Connection charset(String charset) {
        //Validate.notNull(charset, "Charset must not be null");
        req.charset(charset);
        return this;
    }


    public Connection.Response get() throws IOException {
        req.method(Method.GET);
        if (req.useragent() != null) {
            req.header("User-Agent", req.useragent());
        }
        req.header("Accept-Language", "zh-tw,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        req.header("Accept-Encoding", "gzip, deflate");
        req.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return execute();
    }

    public Connection.Response post() throws IOException {
        req.method(Method.POST);
        if (req.useragent() != null) {
            req.header("User-Agent", req.useragent());
        }
        req.header("Accept-Language", "zh-tw,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        req.header("Accept-Encoding", "gzip, deflate");
        req.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return execute();
    }

    public Connection.Response execute() throws IOException {
        res = Response.execute(req);
        return res;
    }


    public Connection.Request request() {
        return req;
    }

    public Connection request(Connection.Request request) {
        req = request;
        return this;
    }

    public Connection.Response response() {
        return res;
    }

    public Connection response(Connection.Response response) {
        res = response;
        return this;
    }

    @SuppressWarnings("unchecked")
    private static abstract class Base<T extends Connection.Base<T>> implements Connection.Base<T> {
        String baseURL;
        String oURL;
        URL url;
        Method method;
        Map<String, String> headers;
        Map<String, String> cookies;

        private Base() {
            headers = new LinkedHashMap<>();
            cookies = new LinkedHashMap<>();
        }

        public URL url() {
            return url;
        }

        public T url(URL url) {
            Validate.notNull(url, "URL must not be null");
            this.url = url;

            int index = url.toExternalForm().lastIndexOf("/");
            if (index > 7) {
                this.baseURL = url.toExternalForm().substring(0, index);
            } else {
                this.baseURL = url.toExternalForm();
            }

            if (!this.baseURL.endsWith("/")) {
                this.baseURL = this.baseURL() + "/";
            }
            oURL = url.toExternalForm();
            return (T) this;
        }

        public String baseURL() {
            return baseURL;
        }

        @Override
        public String oURL() {
            return oURL;
        }

        public Method method() {
            return method;
        }

        public T method(Method method) {
            Validate.notNull(method, "Method must not be null");
            this.method = method;
            return (T) this;
        }

        public String header(String name) {
            Validate.notNull(name, "Header name must not be null");
            return getHeaderCaseInsensitive(name);
        }

        public T header(String name, String value) {
            Validate.notEmpty(name, "Header name must not be empty");
            Validate.notNull(value, "Header value must not be null");
            removeHeader(name); // ensures we don't get an "accept-encoding" and a "Accept-Encoding"
            headers.put(name, value);
            return (T) this;
        }

        public boolean hasHeader(String name) {
            Validate.notEmpty(name, "Header name must not be empty");
            return (getHeaderCaseInsensitive(name) != null);
        }

        public T removeHeader(String name) {
            Validate.notEmpty(name, "Header name must not be empty");
            Map.Entry<String, String> entry = scanHeaders(name); // remove is case insensitive too
            if (entry != null) {
                headers.remove(entry.getKey()); // ensures correct case
            }
            return (T) this;
        }

        public Map<String, String> headers() {
            return headers;
        }

        private String getHeaderCaseInsensitive(String name) {
            Validate.notNull(name, "Header name must not be null");
            // quick evals for common case of title case, lower case, then scan for mixed
            String value = headers.get(name);
            if (value == null) {
                value = headers.get(name.toLowerCase(Locale.US));
            }
            if (value == null) {
                Map.Entry<String, String> entry = scanHeaders(name);
                if (entry != null) {
                    value = entry.getValue();
                }
            }
            return value;
        }

        private Map.Entry<String, String> scanHeaders(String name) {
            String lc = name.toLowerCase(Locale.US);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().toLowerCase(Locale.US).equals(lc)) {
                    return entry;
                }
            }
            return null;
        }

        public String cookie(String name) {
            Validate.notNull(name, "Cookie name must not be null");
            return cookies.get(name);
        }

        public T cookie(String name, String value) {
            Validate.notEmpty(name, "Cookie name must not be empty");
            Validate.notNull(value, "Cookie value must not be null");
            cookies.put(name, value);
            return (T) this;
        }

        public T cookie(HttpCookie cookie) {
            Validate.notEmpty(cookie);
            return (T) this;
        }

        public boolean hasCookie(String name) {
            Validate.notEmpty("Cookie name must not be empty");
            return cookies.containsKey(name);
        }

        public T removeCookie(String name) {
            Validate.notEmpty("Cookie name must not be empty");
            cookies.remove(name);
            return (T) this;
        }

        public Map<String, String> cookies() {
            return cookies;
        }
    }

    public static class Request extends Base<Connection.Request> implements Connection.Request {
        private int timeoutMilliseconds;
        private int maxBodySizeBytes;
        private boolean followRedirects;
        private boolean nativeFollowRedirects;
        private Proxy useproxy;
        private Collection<Connection.KeyVal> data;
        private String charset;
        private boolean useCache;

        private String agent;

        private Request() {
            timeoutMilliseconds = 30000;
            maxBodySizeBytes = 1024 * 1024; // 1MB
            followRedirects = true;
            nativeFollowRedirects = false;
            useCache = false;
            useproxy = null;
            agent = USER_AGENT;
            data = new ArrayList<Connection.KeyVal>();
            method = Connection.Method.GET;
            headers.put("Accept-Encoding", "gzip");
        }

        public int timeout() {
            return timeoutMilliseconds;
        }

        public String charset() {
            return this.charset;
        }

        public void charset(String charset) {
            if (charset == null) {
                return;
            }
            this.charset = charset;
        }

        public Request timeout(int millis) {
            Validate.isTrue(millis >= 0, "Timeout milliseconds must be 0 (infinite) or greater");
            timeoutMilliseconds = millis;
            return this;
        }

        public Request useCache(boolean usecache) {
            this.useCache = usecache;
            return this;
        }

        public int maxBodySize() {
            return maxBodySizeBytes;
        }

        public Connection.Request maxBodySize(int bytes) {
            Validate.isTrue(bytes >= 0, "maxSize must be 0 (unlimited) or larger");
            maxBodySizeBytes = bytes;
            return this;
        }

        public boolean followRedirects() {
            return followRedirects;
        }

        public boolean nativeFollowRedirects() {
            return nativeFollowRedirects;
        }

        public String useragent() {
            return agent;
        }

        public Proxy useProxy() {
            return this.useproxy;
        }

        public Request followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Request nativeFollowRedirects(boolean nativeFollowRedirects) {
            this.nativeFollowRedirects = nativeFollowRedirects;
            return this;
        }

        public Request useragent(String agent) {
            this.agent = agent;
            return this;
        }

        public Request useProxy(Proxy proxy) {
            this.useproxy = proxy;
            return this;
        }

        public Request data(Connection.KeyVal keyval) {
            Validate.notNull(keyval, "Key val must not be null");
            data.add(keyval);
            return this;
        }

        public Collection<Connection.KeyVal> data() {
            return data;
        }
    }

    public static class Response extends Base<Connection.Response> implements Connection.Response {
        private static final int MAX_REDIRECTS = 5;
        private int statusCode;
        private String statusMessage;
        private ByteBuffer byteData;
        private String resBody = null;
        private String preCharset = null;
        private String charset = null;
        private String contentType;
        private boolean executed = false;
        private int numRedirects = 0;

        @SuppressWarnings("unused")
        private Connection.Request req;

        Response() {
            super();
        }

        private Response(Response previousResponse) throws IOException {
            super();
            if (previousResponse != null) {
                numRedirects = previousResponse.numRedirects + 1;
                if (numRedirects >= MAX_REDIRECTS)
                    throw new IOException(String.format("Too many redirects occurred trying to load URL %s", previousResponse.url()));
            }
        }

        static Response execute(Connection.Request req) throws IOException {
            return execute(req, null);
        }

        static Response execute(Connection.Request req, Response previousResponse) throws IOException {
            Validate.notNull(req, "Request must not be null");
            String protocol = req.url().getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new MalformedURLException("Only http & https protocols supported");
            }

            // set up the request for execution
            if (req.method() == Connection.Method.GET && req.data().size() > 0) {
                serialiseRequestUrl(req); // appends query string
            }

            Response res = null;

            HttpURLConnection conn = createConnection(req);
            try {
                conn.connect();
                if (req.method() == Connection.Method.POST) {
                    writePost(req.data(), conn.getOutputStream(), req.charset());
                }
                int status = conn.getResponseCode();
                boolean needsRedirect = false;
                if (status != HttpURLConnection.HTTP_OK) {
                    if ((status == HttpURLConnection.HTTP_MOVED_TEMP) ||
                            (status == HttpURLConnection.HTTP_MOVED_PERM) ||
                            (status == HttpURLConnection.HTTP_SEE_OTHER) ||
                            (status == 307)) {
                        needsRedirect = true;
                    }
                }
                res = new Response(previousResponse);
                res.setupFromConnection(conn, previousResponse);
                if (needsRedirect && req.followRedirects()) {
                    req.method(Method.GET); // always redirect with a get. any data param from original req are dropped.
                    req.data().clear();

                    /** fix empty space **/
                    String location = res.header("Location");
                    if (location != null && location.startsWith("http:/") && location.charAt(6) != '/') // fix broken Location: http:/temp/AAG_New/en/index.php
                        location = location.substring(6);
                    req.url(new URL(req.url(), location));

                    for (Map.Entry<String, String> cookie : res.cookies.entrySet()) { // add response cookies to request (for e.g. login posts)
                        req.cookie(cookie.getKey(), cookie.getValue());
                    }
                    return execute(req, res);
                }
                res.req = req;

                InputStream bodyStream = null;
                InputStream dataStream = null;
                try {
                    dataStream = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
                    bodyStream = res.hasHeader("Content-Encoding") && res.header("Content-Encoding").equalsIgnoreCase("gzip") ?
                            new BufferedInputStream(new GZIPInputStream(dataStream)) :
                            new BufferedInputStream(dataStream);

                    if (Build.VERSION.SDK_INT < 14) {
                        bodyStream = new DoneHandlerInputStream(bodyStream);
                    }
                    if (req.charset() != null) {
                        res.resBody = DataUtil.readToString(bodyStream, req.charset());
                        res.charset = req.charset();
                    } else {
                        res.byteData = DataUtil.readToByteBuffer(bodyStream, req.maxBodySize());
                        res.charset = DataUtil.getCharsetFromContentType(res.contentType); // may be null, readInputStream deals with it
                    }
                } finally {
                    if (bodyStream != null) bodyStream.close();
                    if (dataStream != null) dataStream.close();
                }

            } finally {
                conn.disconnect();
                conn = null;
            }

            if (res != null) {
                res.baseURL = req.baseURL();
                res.oURL = req.oURL();
                res.executed = true;
            }
            return res;
        }

        public int statusCode() {
            return statusCode;
        }

        public String statusMessage() {
            return statusMessage;
        }

        public String charset() {
            return charset;
        }

        public void charset(String charset) {
            if (charset == null) {
                return;
            }
            if (preCharset == null) {
                preCharset = this.charset;
            }
            this.charset = charset;
        }

        public String contentType() {
            return contentType;
        }

        public String body() {
            Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");

            if (resBody != null) {
                return resBody;
            } else {
                String body = null;
                if (byteData != null) {
                    if (charset == null) {
                        body = Charset.forName(DataUtil.defaultCharset).decode(byteData).toString();
                    } else {

                        body = Charset.forName(charset).decode(byteData).toString();
                    }
                    byteData.rewind();
                }
                return body;
            }
        }

        // set up connection defaults, and details from request
        private static HttpURLConnection createConnection(Connection.Request req) throws IOException {
            HttpURLConnection conn;
            Proxy proxy = req.useProxy();

            if (proxy == null) {
                conn = (HttpURLConnection) req.url().openConnection();
            } else {
                conn = (HttpURLConnection) req.url().openConnection(proxy);
            }

            conn.setRequestMethod(req.method().name());
            conn.setInstanceFollowRedirects(req.nativeFollowRedirects()); // don't rely on native redirection support
            conn.setConnectTimeout(req.timeout());
            conn.setReadTimeout(req.timeout());
            if (req.method() == Method.POST) {
                conn.setDoOutput(true);
                conn.setUseCaches(false);
            }
            if (req.cookies().size() > 0) {
                conn.addRequestProperty("Cookie", getRequestCookieString(req));
            }
            for (Map.Entry<String, String> header : req.headers().entrySet()) {
                conn.addRequestProperty(header.getKey(), header.getValue());
            }

            return conn;
        }

        // set up url, method, header, cookies
        private void setupFromConnection(HttpURLConnection conn, Connection.Response previousResponse) throws IOException {
            method = Connection.Method.valueOf(conn.getRequestMethod());
            url = conn.getURL();
            statusCode = conn.getResponseCode();
            statusMessage = conn.getResponseMessage();
            contentType = conn.getContentType();

            Map<String, List<String>> resHeaders = conn.getHeaderFields();
            processResponseHeaders(resHeaders);

            // if from a redirect, map previous response cookies into this response
            if (previousResponse != null) {
                for (Map.Entry<String, String> prevCookie : previousResponse.cookies().entrySet()) {
                    if (!hasCookie(prevCookie.getKey()))
                        cookie(prevCookie.getKey(), prevCookie.getValue());
                }
            }
        }

        void processResponseHeaders(Map<String, List<String>> resHeaders) {
            String cookiev;
            for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
                String name = entry.getKey();
                if (name == null) {
                    continue; // http/1.1 line
                }

                List<String> values = entry.getValue();
                if (name.equalsIgnoreCase("Set-Cookie")) {
                    List<HttpCookie> cookies;
                    MyCookieParser mCookieParser;
                    for (String value : values) {
                        if (value == null)
                            continue;

                        Log.e("TTTTT", "raw: " + value);
                        mCookieParser = new MyCookieParser(value);
                        cookies = mCookieParser.parse();
                        for (HttpCookie c : cookies) {
                            if (c.hasExpired() || c.getDiscard()) continue;

                            cookiev = c.getValue();
                            if (cookiev == null) cookiev = "";
                            cookie(c.getName(), cookiev);
                            Log.e("TTTTTTTT", c.getDomain() + ": " + c.getName() + " -> " + cookiev);
                        }
                    }
                } else { // only take the first instance of each header
                    if (!values.isEmpty()) {
                        header(name, values.get(0));
                    }
                }
            }
        }

        private static void writePost(Collection<Connection.KeyVal> data, OutputStream outputStream, String charset) throws IOException {
            String postCharset = charset;
            if (postCharset == null) {
                postCharset = DataUtil.defaultCharset;
            }
            OutputStreamWriter w = new OutputStreamWriter(outputStream, postCharset);
            boolean first = true;
            for (Connection.KeyVal keyVal : data) {
                if (!first)
                    w.append('&');
                else
                    first = false;

                w.write(URLEncoder.encode(keyVal.key(), postCharset));
                w.write('=');
                w.write(URLEncoder.encode(keyVal.value(), postCharset));
            }
            w.close();
        }

        private static String getRequestCookieString(Connection.Request req) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> cookie : req.cookies().entrySet()) {
                if (!first)
                    sb.append("; ");
                else
                    first = false;
                sb.append(cookie.getKey()).append('=').append(cookie.getValue());
                // todo: spec says only ascii, no escaping / encoding defined. validate on set? or escape somehow here?
            }
            return sb.toString();
        }

        // for get url reqs, serialise the data map into the url
        private static void serialiseRequestUrl(Connection.Request req) throws IOException {
            URL in = req.url();
            StringBuilder url = new StringBuilder();
            boolean first = true;
            // reconstitute the query, ready for appends
            url
                    .append(in.getProtocol())
                    .append("://")
                    .append(in.getAuthority()) // includes host, port
                    .append(in.getPath())
                    .append("?");
            if (in.getQuery() != null) {
                url.append(in.getQuery());
                first = false;
            }
            for (Connection.KeyVal keyVal : req.data()) {
                if (!first)
                    url.append('&');
                else
                    first = false;
                url
                        .append(URLEncoder.encode(keyVal.key(), DataUtil.defaultCharset))
                        .append('=')
                        .append(URLEncoder.encode(keyVal.value(), DataUtil.defaultCharset));
            }
            req.url(new URL(url.toString()));
            req.data().clear(); // moved into url as get params
        }
    }

    public static class KeyVal implements Connection.KeyVal {
        private String key;
        private String value;

        public static KeyVal create(String key, String value) {
            Validate.notEmpty(key, "Data key must not be empty");
            Validate.notNull(value, "Data value must not be null");
            return new KeyVal(key, value);
        }

        private KeyVal(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public KeyVal key(String key) {
            Validate.notEmpty(key, "Data key must not be empty");
            this.key = key;
            return this;
        }

        public String key() {
            return key;
        }

        public KeyVal value(String value) {
            Validate.notNull(value, "Data value must not be null");
            this.value = value;
            return this;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    final static class DoneHandlerInputStream extends FilterInputStream {
        private boolean done;

        public DoneHandlerInputStream(InputStream stream) {
            super(stream);
        }

        @Override
        public int read(byte[] bytes, int offset, int count) throws IOException {
            if (!done) {
                int result = super.read(bytes, offset, count);
                if (result != -1) {
                    return result;
                }
            }
            done = true;
            return -1;
        }
    }
}

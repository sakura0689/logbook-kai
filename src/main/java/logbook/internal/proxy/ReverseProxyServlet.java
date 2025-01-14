package logbook.internal.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ProxyConfiguration;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;

import logbook.bean.AppConfig;
import logbook.internal.ThreadManager;
import logbook.internal.logger.LoggerHolder;
import logbook.plugin.PluginServices;
import logbook.proxy.ContentListenerSpi;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

/**
 * リバースプロキシ
 *
 */
public final class ReverseProxyServlet extends ProxyServlet {

    private static final long serialVersionUID = 1L;

    /** リスナー */
    private transient List<ContentListenerSpi> listeners;

    /*
     * Hop-by-Hop ヘッダーを除去します
     */
    @Override
    protected void customizeProxyRequest(Request proxyRequest, HttpServletRequest request) {
        proxyRequest.onRequestContent(new RequestContentListener(request));

        if (!AppConfig.get().isUseProxy()) { // アップストリームプロキシがある場合は除外

            // HTTP/1.1 ならkeep-aliveを追加します
            if (proxyRequest.getVersion() == HttpVersion.HTTP_1_1) {
                proxyRequest.header(HttpHeader.CONNECTION, "keep-alive");
            }

            // Pragma: no-cache はプロキシ用なので Cache-Control: no-cache に変換します
            String pragma = proxyRequest.getHeaders().get(HttpHeader.PRAGMA);
            if ((pragma != null) && pragma.equals("no-cache")) {
                proxyRequest.header(HttpHeader.PRAGMA, null);
                if (!proxyRequest.getHeaders().containsKey(HttpHeader.CACHE_CONTROL.asString())) {
                    proxyRequest.header(HttpHeader.CACHE_CONTROL, "no-cache");
                }
            }
        }

        String queryString = ((org.eclipse.jetty.server.Request) request).getQueryString();
        fixQueryString(proxyRequest, queryString);

        super.customizeProxyRequest(proxyRequest, request);
    }

    /*
     * レスポンスが帰ってきた
     */
    @Override
    protected void onResponseContent(HttpServletRequest request, HttpServletResponse response,
            Response proxyResponse,
            byte[] buffer, int offset, int length) throws IOException {

        CaptureHolder holder = (CaptureHolder) request.getAttribute(Filter.CONTENT_HOLDER);
        if (holder == null) {
            holder = new CaptureHolder();
            request.setAttribute(Filter.CONTENT_HOLDER, holder);
        }
        // ストリームに書き込む
        holder.putResponse(buffer);

        super.onResponseContent(request, response, proxyResponse, buffer, offset, length);
    }

    /*
     * レスポンスが完了した
     */
    @Override
    protected void onResponseSuccess(HttpServletRequest request, HttpServletResponse response,
            Response proxyResponse) {
        try {
            if(response.getStatus() == HttpServletResponse.SC_OK) {
                CaptureHolder holder = (CaptureHolder) request.getAttribute(Filter.CONTENT_HOLDER);
                if (holder != null) {
                    RequestMetaDataWrapper req = new RequestMetaDataWrapper();
                    req.set(request);

                    ResponseMetaDataWrapper res = new ResponseMetaDataWrapper();
                    res.set(response);

                    Runnable task = () -> {
                        this.invoke(req, res, holder);
                    };
                    ThreadManager.getExecutorService().submit(task);
                }
            }
        } catch (Exception e) {
            LoggerHolder.get().warn("リバースプロキシ サーブレットで例外が発生 req=" + request, e);
        } finally {
            // Help GC
            request.removeAttribute(Filter.CONTENT_HOLDER);
        }
        super.onResponseSuccess(request, response, proxyResponse);
    }

    /*
     * HttpClientを作成する
     */
    @Override
    protected HttpClient newHttpClient() {
        HttpClient client = new HttpClient() {
            @Override
            protected String normalizeHost(String host) {
                return host;
            }
        };
        // プロキシを設定する
        if (AppConfig.get().isUseProxy()) {
            // ポート
            int port = AppConfig.get().getProxyPort();
            // ホスト
            String host = AppConfig.get().getProxyHost();
            // 設定する
            client.setProxyConfiguration(new ProxyConfiguration(host, port));
        }
        return client;
    }

    /**
     * <p>
     * ライブラリのバグを修正します<br>
     * URLにマルチバイト文字が含まれている場合にURLが正しく組み立てられないバグを修正します
     * </p>
     */
    private static void fixQueryString(Request proxyRequest, String queryString) {
        if (queryString != null && !queryString.isEmpty()) {
            if (proxyRequest instanceof HttpRequest) {
                try {
                    FieldHolder.QUERY_FIELD.set(proxyRequest, queryString);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void invoke(RequestMetaDataWrapper baseReq, ResponseMetaDataWrapper baseRes, CaptureHolder holder) {
        try {
            if (this.listeners == null) {
                this.listeners = PluginServices.instances(ContentListenerSpi.class).collect(Collectors.toList());
            }
            for (ContentListenerSpi listener : this.listeners) {
                RequestMetaDataWrapper req = baseReq.clone();
                req.set(holder.getRequest());

                if (listener.test(req)) {
                    ResponseMetaDataWrapper res = baseRes.clone();
                    res.set(holder.getResponse());

                    Runnable task = () -> {
                        try {
                            listener.accept(req, res);
                        } catch (Exception e) {
                            LoggerHolder.get().warn("リバースプロキシ サーブレットで例外が発生", e);
                        }
                    };
                    ThreadManager.getExecutorService().submit(task);
                }
            }
            holder.clear();
        } catch (Exception e) {
            LoggerHolder.get().warn("リバースプロキシ サーブレットで例外が発生 req=" + baseReq.getRequestURI(), e);
        }
    }

    static class RequestMetaDataWrapper implements RequestMetaData, Cloneable {

        private String contentType;

        private String method;

        private Map<String, List<String>> parameterMap;

        private String queryString;

        private String requestURI;

        private Optional<InputStream> requestBody;

        @Override
        public String getContentType() {
            return this.contentType;
        }

        void setContentType(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public String getMethod() {
            return this.method;
        }

        void setMethod(String method) {
            this.method = method;
        }

        @Override
        public Map<String, List<String>> getParameterMap() {
            return this.parameterMap;
        }

        void setParameterMap(Map<String, List<String>> parameterMap) {
            this.parameterMap = parameterMap;
        }

        @Override
        public String getQueryString() {
            return this.queryString;
        }

        void setQueryString(String queryString) {
            this.queryString = queryString;
        }

        @Override
        public String getRequestURI() {
            return this.requestURI;
        }

        void setRequestURI(String requestURI) {
            this.requestURI = requestURI;
        }

        @Override
        public Optional<InputStream> getRequestBody() {
            return this.requestBody;
        }

        void setRequestBody(Optional<InputStream> requestBody) {
            this.requestBody = requestBody;
        }

        void set(HttpServletRequest req) {
            this.setContentType(req.getContentType());
            this.setMethod(req.getMethod().toString());
            this.setQueryString(req.getQueryString());
            this.setRequestURI(req.getRequestURI());
        }

        void set(InputStream body) {
            String bodystr;
            try (Reader reader = new InputStreamReader(body, StandardCharsets.UTF_8)) {
                int len;
                char[] cbuf = new char[128];
                StringBuilder sb = new StringBuilder();
                while ((len = reader.read(cbuf)) > 0) {
                    sb.append(cbuf, 0, len);
                }
                bodystr = URLDecoder.decode(sb.toString(), "UTF-8");
            } catch (IOException e) {
                bodystr = "";
            }
            Map<String, List<String>> map = new LinkedHashMap<>();
            for (String part : bodystr.split("&")) {
                String key;
                String value;
                int idx = part.indexOf('=');
                if (idx > 0) {
                    key = part.substring(0, idx);
                    value = part.substring(idx + 1, part.length());
                } else {
                    key = part;
                    value = null;
                }
                map.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(value);
            }
            this.setParameterMap(map);
            this.setRequestBody(Optional.of(body));
        }

        @Override
        public RequestMetaDataWrapper clone() {
            RequestMetaDataWrapper clone = new RequestMetaDataWrapper();
            clone.setContentType(this.getContentType());
            clone.setMethod(this.getMethod());
            clone.setQueryString(this.getQueryString());
            clone.setRequestURI(this.getRequestURI());
            clone.setParameterMap(this.getParameterMap());
            clone.setRequestBody(this.getRequestBody());
            return clone;
        }
    }

    static class ResponseMetaDataWrapper implements ResponseMetaData, Cloneable {

        private int status;

        private String contentType;

        private Optional<InputStream> responseBody;

        @Override
        public int getStatus() {
            return this.status;
        }

        void setStatus(int status) {
            this.status = status;
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        void setContentType(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public Optional<InputStream> getResponseBody() {
            return this.responseBody;
        }

        void setResponseBody(Optional<InputStream> responseBody) {
            this.responseBody = responseBody;
        }

        void set(HttpServletResponse res) {
            this.setStatus(res.getStatus());
            this.setContentType(res.getContentType());
        }

        void set(InputStream body) throws IOException {
            this.setResponseBody(Optional.of(ungzip(body)));
        }

        @Override
        public ResponseMetaDataWrapper clone() {
            ResponseMetaDataWrapper clone = new ResponseMetaDataWrapper();
            clone.setStatus(this.getStatus());
            clone.setContentType(this.getContentType());
            clone.setResponseBody(this.getResponseBody());
            return clone;
        }

        private static InputStream ungzip(InputStream body) throws IOException {
            body.mark(Short.BYTES);
            int magicbyte = body.read() << 8 ^ body.read();
            body.reset();
            if (magicbyte == 0x1f8b) {
                return new GZIPInputStream(body);
            }
            return body;
        }
    }

    private static class FieldHolder {
        /** ライブラリバグ対応 (HttpRequest#queryを上書きする) */
        static final Field QUERY_FIELD = getDeclaredField(HttpRequest.class, "query");

        /**
         * private フィールドを取得する
         * @param clazz クラス
         * @param string フィールド名
         * @return フィールドオブジェクト
         */
        private static <T> Field getDeclaredField(Class<T> clazz, String string) {
            try {
                Field field = clazz.getDeclaredField(string);
                field.setAccessible(true);
                return field;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
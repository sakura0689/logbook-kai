package logbook.internal.proxy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

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
import logbook.core.LogBookCoreServices;
import logbook.internal.ThreadManager;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.net.RequestMetaDataWrapper;
import logbook.internal.net.ResponseMetaDataWrapper;
import logbook.proxy.ContentListenerSpi;

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

    /**
     * レスポンスが帰ってきた
     */
    @Override
    protected void onResponseContent(HttpServletRequest request, HttpServletResponse response,
            Response proxyResponse,
            byte[] buffer, int offset, int length) throws IOException {

        CapturedHttpRequestResponse capturedHttpRequestResponse = (CapturedHttpRequestResponse) request.getAttribute(CapturedHttpRequestResponseConst.CONTENT_HOLDER);
        if (capturedHttpRequestResponse == null) {
            capturedHttpRequestResponse = new CapturedHttpRequestResponse();
            request.setAttribute(CapturedHttpRequestResponseConst.CONTENT_HOLDER, capturedHttpRequestResponse);
        }
        // reponse bufferをキャプチャする
        capturedHttpRequestResponse.putOriginResponse(buffer);

        super.onResponseContent(request, response, proxyResponse, buffer, offset, length);
    }

    /**
     * レスポンスが完了した
     */
    @Override
    protected void onResponseSuccess(HttpServletRequest request, HttpServletResponse response,
            Response proxyResponse) {
        try {
            if(response.getStatus() == HttpServletResponse.SC_OK) {
                CapturedHttpRequestResponse capturedHttpRequestResponse = (CapturedHttpRequestResponse) request.getAttribute(CapturedHttpRequestResponseConst.CONTENT_HOLDER);
                if (capturedHttpRequestResponse != null) {
                    RequestMetaDataWrapper req = new RequestMetaDataWrapper();
                    req.set(request);

                    ResponseMetaDataWrapper res = new ResponseMetaDataWrapper();
                    res.set(response);

                    Runnable task = () -> {
                        this.invoke(req, res, capturedHttpRequestResponse);
                    };
                    ThreadManager.getExecutorService().submit(task);
                }
            }
        } catch (Exception e) {
            LoggerHolder.get().warn("リバースプロキシ サーブレットで例外が発生 req=" + request, e);
        } finally {
            // Help GC
            request.removeAttribute(CapturedHttpRequestResponseConst.CONTENT_HOLDER);
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

    /**
     * ContentListenerSpiインターフェースの実装クラスを実行します
     * 
     * @param baseReq WrapしたHttpRequest情報
     * @param baseRes WrapしたHttpResponse情報
     * @param capturedHttpRequestResponse キャプチャしたOriginのHttpRequest/HttpResponse情報
     * 
     * @see logbook.internal.APIListener
     * @see logbook.internal.ImageListener
     */
    private void invoke(RequestMetaDataWrapper baseReq, ResponseMetaDataWrapper baseRes, CapturedHttpRequestResponse capturedHttpRequestResponse) {
        try {
            if (this.listeners == null) {
                this.listeners = LogBookCoreServices.instances(ContentListenerSpi.class).collect(Collectors.toList());
            }
            for (ContentListenerSpi listener : this.listeners) {
                RequestMetaDataWrapper req = baseReq.clone();
                req.set(capturedHttpRequestResponse.getOriginRequest());

                if (listener.test(req)) {
                    ResponseMetaDataWrapper res = baseRes.clone();
                    res.set(capturedHttpRequestResponse.getOriginResponse());

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
            capturedHttpRequestResponse.clear();
        } catch (Exception e) {
            LoggerHolder.get().warn("リバースプロキシ サーブレットで例外が発生 req=" + baseReq.getRequestURI(), e);
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
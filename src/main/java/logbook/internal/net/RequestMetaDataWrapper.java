package logbook.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import logbook.net.RequestMetaData;

/**
 * OriginのHttpRequestからの情報を保持するクラスです
 *
 */
public class RequestMetaDataWrapper implements RequestMetaData, Cloneable {

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

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public Map<String, List<String>> getParameterMap() {
        return this.parameterMap;
    }

    public void setParameterMap(Map<String, List<String>> parameterMap) {
        this.parameterMap = parameterMap;
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getRequestURI() {
        return this.requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public Optional<InputStream> getRequestBody() {
        return this.requestBody;
    }

    public void setRequestBody(Optional<InputStream> requestBody) {
        this.requestBody = requestBody;
    }

    public void set(HttpServletRequest req) {
        this.setContentType(req.getContentType());
        this.setMethod(req.getMethod().toString());
        this.setQueryString(req.getQueryString());
        this.setRequestURI(req.getRequestURI());
    }

    public void set(InputStream body) {
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
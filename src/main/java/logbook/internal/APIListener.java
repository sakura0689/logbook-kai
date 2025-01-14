package logbook.internal;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import logbook.api.API;
import logbook.api.APIListenerSpi;
import logbook.common.Messages;
import logbook.internal.Tuple.Pair;
import logbook.internal.logger.LoggerHolder;
import logbook.plugin.PluginServices;
import logbook.proxy.ContentListenerSpi;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

/**
 * APIを受け取りJSONをAPIListenerSpiを実装したサービスプロバイダに送ります
 *
 */
public final class APIListener implements ContentListenerSpi {

    /**
     * @APIアノテーションが付与された、実行対象URI情報とAPIListenerSpi実装クラスのペア情報一覧
     */
    private final Map<String, List<Pair<String, APIListenerSpi>>> targetEventExec;

    /**
     * @APIアノテーションが付与されていない、全てのURLで実行されるAPIListenerSpi実装クラスの一覧
     */
    private final List<Pair<String, APIListenerSpi>> allEventExec = new ArrayList<>();

    private final boolean isDebugEnabled;

    /** 全てのURLで実行される設定がされているフラグ */
    private final boolean isAllEventExec;
    
    public APIListener() {
        Function<APIListenerSpi, Stream<Pair<String, APIListenerSpi>>> mapper = impl -> {
            API target = impl.getClass().getAnnotation(API.class);
            if (target != null) {
                return Arrays.stream(target.value())
                        .map(k -> Tuple.of(k, impl));
            } else {
                this.allEventExec.add(Tuple.of(null, impl));
            }
            return Stream.empty();
        };
        this.isAllEventExec = !this.allEventExec.isEmpty();
        this.targetEventExec = PluginServices.instances(APIListenerSpi.class)
                .flatMap(mapper)
                .collect(Collectors.groupingBy(Pair::getKey));
        this.isDebugEnabled = LoggerHolder.get().isDebugEnabled();
        
        if (this.isDebugEnabled) {
            LoggerHolder.get().debug("APIListener initialize done");
            LoggerHolder.get().debug("targetEventExec設定情報");
            for (Entry<String, List<Pair<String, APIListenerSpi>>> ent : this.targetEventExec.entrySet()) {
                for (Pair<String, APIListenerSpi> data : ent.getValue()) {
                    LoggerHolder.get().debug(ent.getKey() + " : " + data.getKey() + " : " + data.getValue());
                }
            }
            LoggerHolder.get().debug("allEventExec設定情報");
            for (Pair<String, APIListenerSpi> data : this.allEventExec) {
                LoggerHolder.get().debug(data.getKey() + " : " + data.getValue());
            }
        }
    }

    @Override
    public boolean test(RequestMetaData requestMetaData) {
        String uri = requestMetaData.getRequestURI();
        return uri.startsWith("/kcsapi/") && (this.isAllEventExec || this.targetEventExec.containsKey(uri));
    }

    @Override
    public void accept(RequestMetaData requestMetaData, ResponseMetaData responseMetaData) {
        try {
            // レスポンスのJSONを復号します
            InputStream stream = responseMetaData.getResponseBody().get();
            // レスポンスボディのJSONはsvdata=から始まるので除去します
            int read;
            while (((read = stream.read()) != -1) && (read != '=')) {
            }

            try (JsonReader jsonreader = Json.createReader(stream)) {
                JsonObject json = jsonreader.readObject();

                this.send(requestMetaData, responseMetaData, json);
            }
        } catch (Exception e) {
            LoggerHolder.get().warn(Messages.getString("APIListener.2"), e); //$NON-NLS-1$
            // 例外発生時のレスポンスの内容をログに出力する
            StringBuilder sb = new StringBuilder();
            sb.append("uri=");
            try {
                if (requestMetaData != null) {
                    sb.append(requestMetaData.getRequestURI());
                }
            } catch (Exception e2) {
                sb.append(e2.toString());
            }
            sb.append(",body=");
            try {
                if (responseMetaData != null) {
                    InputStream in = responseMetaData.getResponseBody().orElse(null);
                    if (in != null) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        sb.append(new String(out.toByteArray(), StandardCharsets.UTF_8));
                        in.close();
                    }
                }
            } catch (Exception e2) {
                sb.append(e2.toString());
            }
            LoggerHolder.get().warn(sb.toString());
        }
    }

    void send(RequestMetaData req, ResponseMetaData res, JsonObject json) {
        String uri = req.getRequestURI();
        
        List<Pair<String, APIListenerSpi>> pairs = this.targetEventExec.getOrDefault(uri, Collections.emptyList());
        for (Pair<String, APIListenerSpi> pair : pairs) {
            Runnable task = () -> this.createTask(pair, json, req, res);
            ThreadManager.getExecutorService().submit(task);
        }

        for (Pair<String, APIListenerSpi> pair : this.allEventExec) {
            Runnable task = () -> this.createTask(pair, json, req, res);
            ThreadManager.getExecutorService().submit(task);
        }
    }

    private void createTask(Pair<String, APIListenerSpi> pair, JsonObject json, RequestMetaData req,
            ResponseMetaData res) {
        try {
            if (this.isDebugEnabled) {
                String className = pair.getValue().getClass().getName();
                LoggerHolder.get().debug(Messages.getString("APIListener.0", className, req.getRequestURI()));
            }
            pair.getValue().accept(json, req, res);
        } catch (Exception e) {
            LoggerHolder.get().warn(Messages.getString("APIListener.1"), e); //$NON-NLS-1$
            LoggerHolder.get().warn(json.toString());
        }
    }
}

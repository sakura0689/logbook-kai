package logbook.core;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import logbook.internal.logger.LoggerHolder;
import logbook.plugin.JarBasedPlugin;

/**
 * 航海日誌で実行されるプラグイン含めた実行クラスやリソース情報を提供するサービスです
 *
 * 初期化処理は、logbook.internal.Launcherで実行されます
 * 
 * @see logbook.internal.Launcher
 */
public final class LogBookCoreServices {

    private LogBookCoreServices() {
    }

    private static boolean isInitialized = false;
    
    /**
     * LogBookCoreContainerの初期化処理を行います
     * 
     * @param plugins
     * 
     * @see logbook.internal.Launcher
     */
    public static synchronized void init(List<JarBasedPlugin> plugins) throws Exception {
        if (isInitialized) {
            LoggerHolder.get().error("initが呼べるのは一度だけです");
            throw new Exception("PluginServices.initを呼べるのは一度だけです");
        }
        
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        if (plugins == null) {
            plugins = Collections.emptyList();
        }
        container.init(plugins);
        isInitialized = true;
    }
    
    /**
     * クラスローダーを返却します
     *
     * @return クラスローダー
     */
    public static ClassLoader getClassLoader() {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        return container.getClassLoader();
    }
    
    /**
     * プラグイン一覧を返却します
     *
     * @return クラスローダー
     */
    public static List<JarBasedPlugin> getPlugins() {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        return container.getPlugins();
    }
    
    /**
     * 引数で渡されたクラスの実装サービスプロバイダを取得します。
     * 設定ファイルは、src/main/resource/META-INF/service以下に存在する必要があります
     *
     * @param <T> サービスプロバイダ
     * @param clazz プラグインのインターフェイス
     * @return clazzで指定されたサービスプロバイダインスタンス
     * 
     * @see https://docs.oracle.com/javase/jp/8/docs/api/java/util/ServiceLoader.html
     * @see src/main/resource/META-INF/service
     */
    public static <T> Stream<T> getServiceProviders(Class<T> clazz) {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        ServiceLoader<T> loader = ServiceLoader.load(clazz, container.getClassLoader());

        return StreamSupport.stream(loader.spliterator(), false);
    }
    
    /**
     * 指定された名前を持つリソースを検索します。
     *
     * @param name リソース名
     * @return リソースを読み込むためのURL
     * @see ClassLoader#getResource(String)
     */
    public static URL getResource(String name) {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        ClassLoader classLoader = container.getClassLoader();
        return classLoader.getResource(name);
    }

    /**
     * 指定されたリソースを読み込む入力ストリームを返します。
     *
     * @param name リソース名
     * @return リソースを読み込むための入力ストリーム
     * @see ClassLoader#getResourceAsStream(String)
     */
    public static InputStream getResourceAsStream(String name) {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        ClassLoader classLoader = container.getClassLoader();
        return classLoader.getResourceAsStream(name);
    }

    /**
     * 任務のリソースを取得します。
     * （どのコードが任務のリソースを使用しているかわかりやすくするため専用のメソッドを用意）
     * 
     * @param questNo 任務No
     * @return リソースを読み込むためのURL
     */
    public static URL getQuestResource(int questNo) {
        return getResource("logbook/quest/" + questNo + ".json");
    }

    /**
     * 任務のリソースを読み込む入力ストリームを返します。
     *（どのコードが任務のリソースを使用しているかわかりやすくするため専用のメソッドを用意）
     *
     * @param questNo 任務No
     * @return リソースを読み込むための入力ストリーム
     * @see ClassLoader#getResourceAsStream(String)
     */
    public static InputStream getQuestResourceAsStream(int questNo) {
        return getResourceAsStream("logbook/quest/" + questNo + ".json");
    }
    
    /**
     * コンテナを終了します
     * @throws Exception
     */
    public static synchronized void closeContainer() throws Exception {
        LogBookCoreContainer container = LogBookCoreContainer.getInstance();
        container.close();
    }
}

package logbook.core;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import logbook.internal.logger.LoggerHolder;
import logbook.plugin.JarBasedPlugin;

/**
 * 航海日誌で実行されるプラグイン含めた実行クラス群を管理するクラスです
 * 
 * @note
 * LogBookCoreServices経由でのみアクセスする様にしてください
 */
public class LogBookCoreContainer {

    private static final LogBookCoreContainer container = new LogBookCoreContainer();

    /** plugin jarのリスト情報 */
    private List<JarBasedPlugin> plugins;

    /** pluginのURL情報で初期化されたクラスローダー情報 */
    private URLClassLoader classLoader;

    private boolean initialized;

    private LogBookCoreContainer() {
    }

    /**
     * コンテナを初期化します
     * プラグイン情報がある場合は、プラグインの情報も読み込める様にClassLoaderを初期化します
     *
     * @param plugins プラグインのリスト
     */
    public synchronized void init(List<JarBasedPlugin> plugins) {
        if (!this.initialized) {
            if (LoggerHolder.get().isDebugEnabled()) {
                LoggerHolder.get().debug("コンテナの初期化");
                if (plugins == null || plugins.size() == 0) {
                    LoggerHolder.get().debug("コンテナの初期化:プラグイン情報がないのでDefaultで初期化します");
                } else {
                    for (JarBasedPlugin plugin : plugins) {
                        LoggerHolder.get().debug("プラグインを検知 : " + plugin.getURL());
                    }
                }
            }
            
            URL[] urls = plugins.stream()
                    .map(JarBasedPlugin::getURL)
                    .toArray(URL[]::new);
            this.plugins = new ArrayList<>(plugins);
            this.classLoader = new URLClassLoader(urls);
            this.initialized = true;
        }
    }

    /**
     * コンテナが保持しているClassLoaderを閉じます
     *
     * @throws IOException {@link URLClassLoader#close()}
     */
    public void close() throws IOException {
        this.classLoader.close();
    }

    /**
     * このコンテナーが読み込んでいるプラグインのリストを返します
     * @return プラグインのリスト
     */
    public List<JarBasedPlugin> getPlugins() {
        if (!this.initialized) {
            throw new IllegalStateException("PluginContainer not initialized"); //$NON-NLS-1$
        }
        return this.plugins;
    }

    /**
     * このコンテナーのクラスローダーを返します
     * @return クラスローダー
     */
    public ClassLoader getClassLoader() {
        if (!this.initialized) {
            LoggerHolder.get().warn("PluginContainer not initialized", new IllegalStateException()); //$NON-NLS-1$
            return LogBookCoreContainer.class.getClassLoader();
        }
        return this.classLoader;
    }

    /**
     * コンテナーのインスタンスを返します
     * LogBookCoreServicesから呼び出す様にしてください
     * 
     * @return インスタンス
     */
    public static LogBookCoreContainer getInstance() {
        return container;
    }
}

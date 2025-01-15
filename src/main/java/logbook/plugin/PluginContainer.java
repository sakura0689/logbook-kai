package logbook.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import logbook.internal.logger.LoggerHolder;

/**
 * プラグインを管理します
 *
 */
public class PluginContainer {

    private static final PluginContainer container = new PluginContainer();

    private List<JarBasedPlugin> plugins;

    private URLClassLoader classLoader;

    private boolean initialized;

    private PluginContainer() {
    }

    /**
     * プラグインコンテナを初期化します
     *
     * @param plugins プラグイン
     */
    public synchronized void init(List<JarBasedPlugin> plugins) {
        if (!this.initialized) {
            if (LoggerHolder.get().isDebugEnabled()) {
                LoggerHolder.get().debug("プラグインコンテナの初期化");
                if (plugins == null || plugins.size() == 0) {
                    LoggerHolder.get().debug("プラグインコンテナの初期化:プラグイン情報がないのでDefaultで初期化します");
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
     * プラグインコンテナを閉じます
     *
     * @throws IOException {@link URLClassLoader#close()}
     */
    public void close() throws IOException {
        this.classLoader.close();
    }

    /**
     * このプラグインコンテナーが読み込んでいるプラグインのリストを返します
     * @return プラグインのリスト
     */
    public List<JarBasedPlugin> getPlugins() {
        if (!this.initialized) {
            throw new IllegalStateException("PluginContainer not initialized"); //$NON-NLS-1$
        }
        return this.plugins;
    }

    /**
     * このプラグインコンテナーのクラスローダーを返します
     * @return クラスローダー
     */
    public ClassLoader getClassLoader() {
        if (!this.initialized) {
            LoggerHolder.get().warn("PluginContainer not initialized", new IllegalStateException()); //$NON-NLS-1$
            return PluginContainer.class.getClassLoader();
        }
        return this.classLoader;
    }

    /**
     * プラグインコンテナーのインスタンスを返します
     * @return プラグインコンテナーのインスタンス
     */
    public static PluginContainer getInstance() {
        return container;
    }
}

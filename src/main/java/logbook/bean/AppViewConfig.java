package logbook.bean;

import java.util.List;

import logbook.internal.Config;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ビュー回りの状態
 * 
 * アプリケーションの設定と異なり、マシンを変更した時など引き継がなくてもいい情報はこちらに保存する。
 * 今 AppConfig にあるテーブルのカラム幅の情報等もそのうちこちらにまとめる予定。
 */
@Data
public class AppViewConfig {

    private BattleLogConfig battleLogConfig;

    @Data
    public static class BattleLogConfig {
        private List<CustomUnit> customUnits;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class CustomUnit {
            private long from;
            private long to;
        }
    }

    /**
     * アプリケーションのデフォルト設定ディレクトリから<code>AppViewConfig</code>を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(AppViewConfig.class, AppViewConfig::new)</code>
     * </blockquote>
     *
     * @return <code>AppViewConfig</code>
     */
    public static AppViewConfig get() {
        return Config.getDefault().get(AppViewConfig.class, AppViewConfig::new);
    }
}

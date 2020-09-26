package logbook.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

    private CreateItemLogConfig createItemLogConfig;

    private ResourceChartConfig resourceChartConfig;

    private CaptureConfig captureConfig;

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

    @Data
    public static class CreateItemLogConfig {
        private int index;
    }

    @Data
    @JsonInclude(Include.NON_DEFAULT)
    public static class ResourceChartConfig {
        private int termIndex = 2;
        private Long from;
        private Long to;
        private boolean fuel = true;
        private boolean ammo = true;
        private boolean metal = true;
        private boolean bauxite = true;
        private boolean bucket;
        private boolean burner;
        private boolean research;
        private boolean improve;
        private boolean forceZero;
    }

    @Data
    @JsonInclude(Include.NON_DEFAULT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CaptureConfig {
        private Bounds bounds;

        @Data
        public static class Bounds {
            private int x;
            private int y;
            private int width;
            private int height;
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

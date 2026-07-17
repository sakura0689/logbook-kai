package logbook.internal.kancolle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import logbook.bean.AppConfig;
import logbook.constants.SeaArea;
import logbook.internal.logger.LoggerHolder;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 海域に関するメソッドを集めたクラス
 * 
 */
public final class SeaAreas {

    /** 海域情報 */
    @Data
    @AllArgsConstructor
    public static class SeaAreaInfo {
        private final int area;
        private final String name;
        private final int imageId;

        @Override
        public String toString() {
            return name;
        }
    }

    private static Map<Integer, SeaAreaInfo> cache;

    private SeaAreas() {
    }

    /**
     * 海域情報を取得します。
     * 
     * @param area 海域番号
     * @return 海域情報
     */
    public static SeaAreaInfo fromArea(int area) {
        ensureLoaded();
        return cache.get(area);
    }

    /**
     * お札アイコンのNoを取得します。
     * 
     * @param area 海域番号
     * @return お札アイコンのNo
     */
    public static int getImageId(int area) {
        SeaAreaInfo info = fromArea(area);
        return info != null ? info.getImageId() : 0;
    }

    /**
     * 名前から海域番号を取得します。
     * 
     * @param name 海域名
     * @return 海域番号、見つからない場合は Integer.MAX_VALUE
     */
    public static int getAreaByName(String name) {
        ensureLoaded();
        return cache.values().stream()
                .filter(info -> info.getName().equals(name))
                .map(SeaAreaInfo::getArea)
                .findFirst()
                .orElse(Integer.MAX_VALUE);
    }

    /**
     * キャッシュを破棄し、再読込を促します。
     */
    public static void reload() {
        cache = null;
    }

    private static synchronized void ensureLoaded() {
        if (cache != null) {
            return;
        }

        Map<Integer, SeaAreaInfo> map = new HashMap<>();
        
        // 1. 基本情報をEnumから構築 (1~14)
        for (SeaArea sa : SeaArea.values()) {
            map.put(sa.getArea(), new SeaAreaInfo(sa.getArea(), sa.getName(), sa.getImageId()));
        }

        // 2. カスタムJSONで上書き
        Path customJsonPath = Path.of(AppConfig.get().getResourcesDir(), "common", "common_event", "common_event_custom.json");
        File file = customJsonPath.toFile();
        
        if (file.exists() && file.canRead()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(Feature.ALLOW_COMMENTS);
                
                Map<Integer, Map<String, Object>> customData = mapper.readValue(file, new TypeReference<Map<Integer, Map<String, Object>>>() {});
                if (customData != null) {
                    customData.forEach((area, props) -> {
                        String name = (String) props.get("name");
                        Integer imageId = (Integer) props.get("imageId");
                        
                        if (name != null || imageId != null) {
                            SeaAreaInfo base = map.get(area);
                            String finalName = name != null ? name : (base != null ? base.getName() : "Unknown");
                            int finalImageId = imageId != null ? imageId : (base != null ? base.getImageId() : 0);
                            map.put(area, new SeaAreaInfo(area, finalName, finalImageId));
                        }
                    });
                }
            } catch (IOException e) {
                LoggerHolder.get().error("海域カスタムJSONの読み込みに失敗しました", e);
            }
        }

        cache = map;
    }
}

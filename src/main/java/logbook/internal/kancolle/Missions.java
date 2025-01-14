package logbook.internal.kancolle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import logbook.bean.AppConfig;
import logbook.bean.Mission;
import logbook.bean.MissionCollection;
import logbook.bean.MissionCondition;
import logbook.internal.ReferenceCache;
import logbook.plugin.PluginServices;

/**
 * 遠征
 *
 */
public class Missions {

    /** 画像キャッシュ */
    private static final ReferenceCache<String, Image> CACHE = new ReferenceCache<>(50);

    /**
     * 遠征IDから{@link Mission}を返します。
     * 
     * @param missionId 遠征ID
     * @return {@link Mission}
     */
    public static Mission getMission(Integer missionId) {
        return MissionCollection.get()
                .getMissionMap()
                .get(missionId);
    }

    /**
     * 遠征IDから{@link MissionCondition}を返します。
     * 
     * @param missionId 遠征ID
     * @return {@link MissionCondition}
     * @throws IOException
     */
    public static Optional<MissionCondition> getMissionCondition(Integer missionId) throws IOException {
        Mission mission = Missions.getMission(missionId);

        if (mission == null) {
            return Optional.empty();
        }

        if ("前衛支援任務".equals(mission.getName())) {
            mission = Missions.getMission(33);
        } else if ("艦隊決戦支援任務".equals(mission.getName())) {
            mission = Missions.getMission(34);
        }

        InputStream is = PluginServices
                .getResourceAsStream("logbook/mission/" + mission.getMapareaId() + "/" + mission.getDispNo() + ".json");
        if (is == null) {
            return Optional.empty();
        }
        MissionCondition condition;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(Feature.ALLOW_COMMENTS);
            condition = mapper.readValue(is, MissionCondition.class);
        } finally {
            is.close();
        }

        return Optional.ofNullable(condition);
    }

    /**
     * 遠征の定義から時間を表すテキストを返します。
     * @param mission 遠征定義
     * @return テキスト - 30分、3時間15分、1日など
     */
    public static String getDurationText(Mission mission) {
        if (mission.getTime() != null) {
            int minutes = mission.getTime();
            final int DAY_IN_MINUTE = 60*24;
            StringBuilder sb = new StringBuilder(16);
            if (minutes >= DAY_IN_MINUTE) {
                int days = minutes/DAY_IN_MINUTE;
                minutes -= days*DAY_IN_MINUTE;
                sb.append(days).append("日");
            }
            if (minutes >= 60) {
                int hours = minutes/60;
                minutes -= hours*60;
                sb.append(hours).append("時間");
            }
            if (minutes > 0) {
                sb.append(minutes).append("分");
            }
            return sb.toString();
        }
        return "(不明)";
    }

    public static Image damageTypeIcon(int damageType) {
        if (damageType == 1) {
            return expeditionIcon(68, 24, 18);
        } else if (damageType == 2) {
            return expeditionIcon(69, 24, 18);
        }
        return null;
    }

    /**
     * 遠征画像をロードします。
     *
     * @param id 画像ID
     * @param prefWidth 幅
     * @param prefHeight 高さ
     * @return 画像
     */
    private static Image expeditionIcon(int id, int prefWidth, int prefHeight) {
        Path dir = Paths.get(AppConfig.get().getResourcesDir());
        Path p = dir.resolve(Paths.get("sally", "sally_expedition/sally_expedition_" + id + ".png"));

        return CACHE.get(p.toUri().toString()+"@"+prefWidth+"x"+prefHeight, (url, status) -> {
            Image image = new Image(url.substring(0, url.lastIndexOf("@")));
            if (image.isError()) {
                status.setDoCache(false);
                return null;
            }
            double width = image.getWidth();
            double height = image.getHeight();

            if (width != prefWidth || height != prefHeight) {
                Canvas canvas = new Canvas(prefWidth, prefHeight);
                GraphicsContext gc = canvas.getGraphicsContext2D();

                gc.drawImage(image, 0, 0, prefWidth, prefHeight);
                SnapshotParameters sp = new SnapshotParameters();
                sp.setFill(Color.TRANSPARENT);

                return canvas.snapshot(sp, null);
            }
            return image;
        });
    }
}

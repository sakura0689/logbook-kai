package logbook.api;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import jakarta.json.JsonObject;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import logbook.bean.AppBouyomiConfig;
import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.BattleLog;
import logbook.bean.BattleTypes.CombinedType;
import logbook.bean.DeckPortCollection;
import logbook.bean.MapStartNext;
import logbook.bean.Ship;
import logbook.bean.ShipMst;
import logbook.common.Messages;
import logbook.internal.Tuple;
import logbook.internal.bouyomi.BouyomiChanUtils;
import logbook.internal.bouyomi.BouyomiChanUtils.Type;
import logbook.internal.gui.Tools;
import logbook.internal.kancolle.Ships;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.AudiosUtil;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_map/start
 *
 */
@API("/kcsapi/api_req_map/start")
public class ApiReqMapStart implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {

        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            BattleLog log = new BattleLog();
            log.setCombinedType(CombinedType.toCombinedType(AppCondition.get().getCombinedType()));
            log.getNext().add(MapStartNext.toMapStartNext(data));

            AppCondition condition = AppCondition.get();
            condition.setBattleResult(log);
            condition.setMapStart(Boolean.TRUE);
            condition.setDeckId(Integer.parseInt(req.getParameter("api_deck_id")));
            // ルート情報
            condition.getRoute().add(new StringJoiner("-")
                    .add(data.getJsonNumber("api_maparea_id").toString())
                    .add(data.getJsonNumber("api_mapinfo_no").toString())
                    .add(data.getJsonNumber("api_no").toString())
                    .toString());

            if (AppConfig.get().isAlertBadlyStart() || AppBouyomiConfig.get().isEnable()) {
                // 大破した艦娘
                List<Ship> badlyShips = DeckPortCollection.get()
                        .getDeckPortMap()
                        .get(AppCondition.get().getDeckId())
                        .getBadlyShips();

                // 連合艦隊編成時に第1艦隊出撃中は第2艦隊も見る
                if (condition.isCombinedFlag() && condition.getDeckId() == 1) {
                    badlyShips.addAll(DeckPortCollection.get()
                            .getDeckPortMap()
                            .get(2).getBadlyShips(AppConfig.get().isIgnoreSecondFlagship()));
                }

                if (!badlyShips.isEmpty()) {
                    Platform.runLater(() -> displayAlert(badlyShips));
                    // 棒読みちゃん連携
                    sendBouyomi(badlyShips);
                }
            }
        }
    }

    /**
     * 大破警告
     *
     * @param badlyShips 大破艦
     */
    private static void displayAlert(List<Ship> badlyShips) {
        try {
            Path dir = Paths.get(AppConfig.get().getAlertSoundDir());
            Path p = AudiosUtil.randomAudioFile(dir);
            if (p != null) {
                AudioClip clip = new AudioClip(p.toUri().toString());
                clip.setVolume(AppConfig.get().getSoundLevel() / 100D);
                clip.play();
            }
        } catch (Exception e) {
            LoggerHolder.get().warn("サウンド通知に失敗しました", e);
        }
        for (Ship ship : badlyShips) {
            ImageView node = new ImageView(Ships.shipWithItemImage(ship));

            String message = Messages.getString("ship.badly", Ships.shipMst(ship) //$NON-NLS-1$
                    .map(ShipMst::getName)
                    .orElse(""), ship.getLv());

            Tools.Controls.showNotify(node, "大破警告", message, Duration.seconds(30));
        }
    }

    /**
     * 棒読みちゃん連携
     *
     * @param badlyShips 大破艦
     */
    private static void sendBouyomi(List<Ship> badlyShips) {
        if (AppBouyomiConfig.get().isEnable()) {

            List<ShipMst> shipMsts = badlyShips.stream()
                    .map(ship -> Ships.shipMst(ship).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            String hiragana = shipMsts.stream()
                    .map(ShipMst::getYomi)
                    .collect(Collectors.joining("、"));
            String kanji = shipMsts.stream()
                    .map(ShipMst::getName)
                    .collect(Collectors.joining("、"));

            BouyomiChanUtils.speak(Type.MapStartNextAlert,
                    Tuple.of("${hiraganaNames}", hiragana),
                    Tuple.of("${kanjiNames}", kanji));
        }
    }

}

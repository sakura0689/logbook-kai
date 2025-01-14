package logbook.api;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;

import javafx.application.Platform;
import javafx.util.Duration;
import logbook.bean.AppBouyomiConfig;
import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.BattleLog;
import logbook.bean.BattleResult;
import logbook.bean.BattleTypes.CombinedType;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.internal.Audios;
import logbook.internal.BattleLogs;
import logbook.internal.BouyomiChanUtils;
import logbook.internal.Config;
import logbook.internal.PhaseState;
import logbook.internal.BouyomiChanUtils.Type;
import logbook.internal.gui.Tools;
import logbook.internal.log.BattleResultLogFormat;
import logbook.internal.log.LogWriter;
import logbook.internal.util.DateUtil;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

/**
 * /kcsapi/api_req_combined_battle/battleresult
 *
 */
@API("/kcsapi/api_req_combined_battle/battleresult")
public class ApiReqCombinedBattleBattleresult implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            BattleResult result = BattleResult.toBattleResult(data);
            BattleLog log = AppCondition.get().getBattleResult();
            if (log != null) {
                // 削除
                AppCondition.get().setBattleResult(null);

                AppCondition.get().setBattleResultConfirm(log);

                log.setResult(result);
                // ローデータを設定する
                if (AppConfig.get().isIncludeRawData()) {
                    BattleLog.setRawData(log, BattleLog.RawData::setResult, data, req);
                }
                log.setTime(DateUtil.nowString());
                // 艦隊スナップショットを作る
                if (log.getCombinedType() != CombinedType.未結成 && AppCondition.get().getDeckId() == 1) {
                    BattleLog.snapshot(log, 1, 2);
                } else {
                    BattleLog.snapshot(log, AppCondition.get().getDeckId());
                }
                // 戦闘ログの保存
                BattleLogs.write(log);

                LogWriter.getInstance(BattleResultLogFormat::new)
                        .write(log);
                if (AppConfig.get().isApplyResult()) {
                    // 艦隊を更新
                    PhaseState p = new PhaseState(log);
                    p.apply(log.getBattle());
                    p.apply(log.getMidnight());
                    ShipCollection.get()
                            .getShipMap()
                            .putAll(Stream.of(p.getAfterFriend(), p.getAfterFriendCombined())
                                    .flatMap(List::stream)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toMap(Ship::getId, v -> v)));
                }
            }
            if (result.achievementGimmick1()) {
                Platform.runLater(
                        () -> Tools.Controls.showNotify(null, "ギミック解除", "海域に変化が確認されました。", Duration.seconds(15)));
                // 通知音再生
                if (AppConfig.get().isUseSound()) {
                    Platform.runLater(Audios.playDefaultNotifySound());
                }
                // 棒読みちゃん連携
                if (AppBouyomiConfig.get().isEnable()) {
                    BouyomiChanUtils.speak(Type.AchievementGimmick1);
                }
            }
            if (result.achievementGimmick2()) {
                Platform.runLater(
                        () -> Tools.Controls.showNotify(null, "ギミック解除", "ギミックの達成を確認しました。", Duration.seconds(15)));
                // 通知音再生
                if (AppConfig.get().isUseSound()) {
                    Platform.runLater(Audios.playDefaultNotifySound());
                }
                // 棒読みちゃん連携
                if (AppBouyomiConfig.get().isEnable()) {
                    BouyomiChanUtils.speak(Type.AchievementGimmick2);
                }
            }
        }
        // 戦闘結果APIの前後は他のAPIが呼ばれることがなくconflictの可能性が低いためデータ保存する
        Config.getDefault().store();
    }
}

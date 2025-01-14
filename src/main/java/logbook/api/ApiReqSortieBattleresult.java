package logbook.api;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import javafx.application.Platform;
import javafx.util.Duration;
import logbook.bean.AppBouyomiConfig;
import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.BattleLog;
import logbook.bean.BattleResult;
import logbook.bean.BattleTypes.IFormation;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.internal.Audios;
import logbook.internal.BattleLogs;
import logbook.internal.BouyomiChanUtils;
import logbook.internal.BouyomiChanUtils.Type;
import logbook.internal.Config;
import logbook.internal.Logs;
import logbook.internal.PhaseState;
import logbook.internal.gui.Tools;
import logbook.internal.log.BattleResultLogFormat;
import logbook.internal.log.LogWriter;
import logbook.internal.logger.LoggerHolder;
import logbook.proxy.RequestMetaData;
import logbook.proxy.ResponseMetaData;

/**
 * /kcsapi/api_req_sortie/battleresult
 *
 */
@API("/kcsapi/api_req_sortie/battleresult")
public class ApiReqSortieBattleresult implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        //戦闘結果反映時エラー発生フラグ
        boolean isApplyResultError = false;
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
                log.setTime(Logs.nowString());
                // 出撃艦隊
                Integer dockId = Optional.ofNullable(log.getBattle())
                        .map(IFormation::getDockId)
                        .orElse(1);
                // 艦隊スナップショットを作る
                BattleLog.snapshot(log, dockId);
                // battlelogフォルダへ戦闘ログの保存
                BattleLogs.write(log);
                //海域・ドロップ報告書の保存
                LogWriter.getInstance(BattleResultLogFormat::new)
                        .write(log);
                if (AppConfig.get().isApplyResult()) {
                    try {
                        // [現在の戦闘]結果の反映
                        PhaseState p = new PhaseState(log);
                        p.apply(log.getBattle());
                        p.apply(log.getMidnight());
                        ShipCollection.get()
                                .getShipMap()
                                .putAll(p.getAfterFriend().stream()
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toMap(Ship::getId, v -> v)));
                    } catch (Exception e) {
                        isApplyResultError = true;
                        LoggerHolder.get().warn("battlelog[" + log.getTime() + ".json]書き込み後、[現在の戦闘]結果の反映に失敗しました", e);
                    }
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
        
        if (!isApplyResultError) {
            // 戦闘結果APIの前後は他のAPIが呼ばれることがなくconflictの可能性が低いためデータ保存する
            Config.getDefault().store();
        }
    }
}

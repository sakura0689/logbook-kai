package logbook.api;

import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.json.JsonObject;
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
import logbook.internal.Config;
import logbook.internal.bouyomi.BouyomiChanUtils;
import logbook.internal.bouyomi.BouyomiChanUtils.Type;
import logbook.internal.gui.Tools;
import logbook.internal.kancolle.BattleLogs;
import logbook.internal.kancolle.PhaseState;
import logbook.internal.log.BattleResultLogFormat;
import logbook.internal.log.LogWriter;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.AudiosUtil;
import logbook.internal.util.DateUtil;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_combined_battle/battleresult
 *
 */
@API("/kcsapi/api_req_combined_battle/battleresult")
public class ApiReqCombinedBattleBattleresult implements APIListenerSpi {

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
                log.setTime(DateUtil.nowString());
                // 艦隊スナップショットを作る
                if (log.getCombinedType() != CombinedType.未結成 && AppCondition.get().getDeckId() == 1) {
                    BattleLog.snapshot(log, 1, 2);
                } else {
                    BattleLog.snapshot(log, AppCondition.get().getDeckId());
                }
                // 戦闘ログの保存
                BattleLogs.write(log);
                        
                try {
                    PhaseState p = new PhaseState(log);
                    p.apply(log.getBattle());
                    p.apply(log.getMidnight());

                    if (AppConfig.get().isApplyResult()) {                        
                        // 戦闘終了毎に艦隊情報を更新
                        // 艦娘情報を更新: メインパネルに反映
                        ShipCollection.get()
                                .getShipMap()
                                .putAll(p.getAfterFriend().stream()
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toMap(Ship::getId, v -> v)));
                    }
                    BattleLog resultLog = BattleLog.updatePhaseState(log, p);
                    //海戦ドロップ報告書の保存
                    LogWriter.getInstance(BattleResultLogFormat::new).write(resultLog);
                } catch (Exception e) {
                    isApplyResultError = true;
                    LoggerHolder.get().warn("battlelog[" + log.getTime() + ".json]書き込み後、[現在の戦闘]結果の反映に失敗しました", e);
                    
                    //海戦ドロップ報告書の保存
                    //戦闘のAPIが仕様変更の際、PhaseStateが評価出来ずエラーとなるため、戦闘開始前情報を記録する
                    LogWriter.getInstance(BattleResultLogFormat::new).write(log);
                }
            }
            if (result.achievementGimmick1()) {
                Platform.runLater(
                        () -> Tools.Controls.showNotify(null, "ギミック解除", "海域に変化が確認されました。", Duration.seconds(15)));
                // 通知音再生
                if (AppConfig.get().isUseSound()) {
                    Platform.runLater(AudiosUtil.playDefaultNotifySound());
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
                    Platform.runLater(AudiosUtil.playDefaultNotifySound());
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

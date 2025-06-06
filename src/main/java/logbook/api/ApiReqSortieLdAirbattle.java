package logbook.api;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.json.JsonObject;
import logbook.bean.AppCondition;
import logbook.bean.AppConfig;
import logbook.bean.BattleLog;
import logbook.bean.BattleTypes.IFormation;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.bean.SortieLdAirbattle;
import logbook.internal.kancolle.PhaseState;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_sortie/ld_airbattle
 *
 */
@API("/kcsapi/api_req_sortie/ld_airbattle")
public class ApiReqSortieLdAirbattle implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {

            AppCondition condition = AppCondition.get();
            BattleLog log = condition.getBattleResult();
            if (log != null) {
                condition.setBattleCount(condition.getBattleCount() + 1);
                log.setBattleCount(condition.getBattleCount());
                log.setRoute(condition.getRoute());

                log.setBattle(SortieLdAirbattle.toBattle(data));
                // ローデータを設定する
                if (AppConfig.get().isIncludeRawData()) {
                    BattleLog.setRawData(log, BattleLog.RawData::setBattle, data, req);
                }
                // 出撃艦隊
                Integer dockId = Optional.ofNullable(log.getBattle())
                        .map(IFormation::getDockId)
                        .orElse(1);
                // 艦隊スナップショットを作る
                BattleLog.snapshot(log, dockId);
                if (AppConfig.get().isApplyBattle()) {
                    // 艦隊を更新
                    PhaseState p = new PhaseState(log);
                    p.apply(log.getBattle());
                    ShipCollection.get()
                            .getShipMap()
                            .putAll(p.getAfterFriend().stream()
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toMap(Ship::getId, v -> v)));
                }
            }
        }
    }

}

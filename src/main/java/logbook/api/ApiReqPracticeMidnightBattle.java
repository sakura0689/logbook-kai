package logbook.api;

import jakarta.json.JsonObject;

import logbook.bean.AppCondition;
import logbook.bean.BattleLog;
import logbook.bean.BattleMidnightBattle;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_practice/midnight_battle
 *
 */
@API("/kcsapi/api_req_practice/midnight_battle")
public class ApiReqPracticeMidnightBattle implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {

            BattleLog log = AppCondition.get().getPracticeBattleResult();
            if (log != null) {
                log.setMidnight(BattleMidnightBattle.toBattle(data));
            }
        }
    }

}

package logbook.api;

import jakarta.json.JsonObject;
import logbook.bean.AppExpRecords;
import logbook.bean.Basic;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_get_member/basic
 *
 */
@API("/kcsapi/api_get_member/basic")
public class ApiGetMemberBasic implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            Basic.updateBasic(Basic.get(), data);
            AppExpRecords.get().update(Basic.get());
        }
    }

}

package logbook.api;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import logbook.bean.Useitem;
import logbook.bean.UseitemCollection;
import logbook.internal.util.JsonHelper;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_get_member/useitem
 *
 */
@API("/kcsapi/api_get_member/useitem")
public class ApiGetMemberUseitem implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonArray array = json.getJsonArray("api_data");
        if (array != null) {
            UseitemCollection.get()
                    .setUseitemMap(JsonHelper.toMap(array, Useitem::getId, Useitem::toUseitem));
        }
    }

}

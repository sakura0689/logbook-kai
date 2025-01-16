package logbook.api;

import jakarta.json.JsonObject;
import logbook.bean.DeckPort;
import logbook.bean.DeckPortCollection;
import logbook.internal.util.JsonHelper;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_hensei/preset_select
 *
 */
@API("/kcsapi/api_req_hensei/preset_select")
public class ApiReqHenseiPresetSelect implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            Integer deckId = Integer.valueOf(req.getParameter("api_deck_id"));
            String name = data.getString("api_name");
            DeckPort deckPort = DeckPortCollection.get()
                    .getDeckPortMap()
                    .get(deckId)
                    .clone();
            deckPort.setName(name);
            deckPort.setShip(JsonHelper.toIntegerList(data.getJsonArray("api_ship")));

            DeckPortCollection.get()
                    .getDeckPortMap()
                    .put(deckId, deckPort);
        }
    }

}

package logbook.api;

import jakarta.json.JsonObject;
import logbook.bean.AppQuestCollection;
import logbook.bean.QuestList;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_get_member/questlist
 *
 */
@API("/kcsapi/api_get_member/questlist")
public class ApiGetMemberQuestlist implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        JsonObject data = json.getJsonObject("api_data");
        if (data != null) {
            QuestList quest = QuestList.toQuestList(data);

            AppQuestCollection.get()
                    .update(quest, "0".equals(req.getParameter("api_tab_id")));
        }
    }

}

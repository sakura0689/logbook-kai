package logbook.api;

import java.util.Optional;

import jakarta.json.JsonObject;

import logbook.bean.AppCondition;
import logbook.net.RequestMetaData;
import logbook.net.ResponseMetaData;

/**
 * /kcsapi/api_req_hensei/combined
 *
 */
@API("/kcsapi/api_req_hensei/combined")
public class ApiReqHenseiCombined implements APIListenerSpi {

    @Override
    public void accept(JsonObject json, RequestMetaData req, ResponseMetaData res) {
        Optional.ofNullable(req.getParameter("api_combined_type"))
            .map(Integer::valueOf)
            .ifPresent(type -> {
                AppCondition app = AppCondition.get();
                app.setCombinedType(type);
                app.setCombinedFlag(type != 0);
            });
    }

}

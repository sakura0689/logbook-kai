package logbook.internal.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import logbook.bean.BattleTypes;
import logbook.bean.BattleTypes.Stage1;
import logbook.bean.SlotitemMst;
import logbook.bean.SlotitemMstCollection;
import logbook.internal.LoggerHolder;

/**
 * Stage1 基地航空隊編成情報
 *
 */
public class BattleDetailPhaseStage1AirBase extends VBox {

    private Stage1 stage1;

    private String dispAirBaseText;

    @FXML
    private Label dispAirBase;

    /**
    * Stage1 詳細
    * @param stage1 stage1
    * @param airbase (基地航空隊編成情報)
    */
    public BattleDetailPhaseStage1AirBase(Stage1 stage1, List<BattleTypes.SquadronPlane> airbase) {
        this.stage1 = stage1;
        this.dispAirBaseText = getAirBaseHensei(airbase);
        try {
            FXMLLoader loader = InternalFXMLLoader.load("logbook/gui/battle_detail_phase_stage1_airbase.fxml");
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            LoggerHolder.get().error("FXMLのロードに失敗しました", e);
        }
    }

    /**
     * 基地航空隊情報から編成機のカンマ区切り文字列を返却します
     * 
     * @param airbase
     * @return
     */
    private String getAirBaseHensei(List<BattleTypes.SquadronPlane> airbase) {
        if (airbase == null) {
            return "";
        }
        Map<Integer, SlotitemMst> collection = SlotitemMstCollection.get().getSlotitemMap();
        StringJoiner sj = new StringJoiner(" , ");
        for (BattleTypes.SquadronPlane squadronPlane : airbase) {
            sj.add(collection.get(squadronPlane.getMstId()).getName());
        }
        return sj.toString();
    }
    
    @FXML
    void initialize() {
        if (this.stage1 != null) {
            this.dispAirBase.setText(this.dispAirBaseText);
        }
    }
}

package logbook.internal.gui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import logbook.bean.BattleLog;
import logbook.bean.MapStartNext;
import logbook.internal.kancolle.Mapping;

/**
 * 現在の戦闘、現在の演習、戦闘ログに表示される、戦闘ログ詳細の画面表示情報を構築、保持するクラスです
 * 
 */
public class BattleDetailViewData {

    private BattleLog battleLog;
    
    private MapStartNext mapStartNext;
    
    /**
     * デフォルトコンストラクタ
     */
    public BattleDetailViewData(BattleLog battleLog) {
        this.battleLog = battleLog;
        this.mapStartNext = battleLog.getNext().size() > 0 ? battleLog.getNext().get(battleLog.getNext().size() - 1) : null;
    }
    
    /**
     * ハッシュを返却します
     * @return
     */
    public int getHashCode() {
        return Objects.hash(this.mapStartNext , this.battleLog.getBattle(), this.battleLog.getMidnight(), this.battleLog.getResult());
    }
    
    /**
     * 出撃/進撃情報判定を行います
     * @return true:出撃/進撃情報 false:演習
     * 
     * TODO isPractice情報との違いを確認する
     */
    public boolean isMapStartNextData() {
        if (this.mapStartNext == null) {
            return false;
        }
        return true;
    }
    
    /**
     * マス文字列を返却します
     * @return
     */
    public String getMapCellViewString() {
        boolean boss = isBoss();
        if (this.mapStartNext == null) {
            return "";
        }
        
        String mapCell = this.mapStartNext.getMapareaId()
                + "-" + this.mapStartNext.getMapinfoNo()
                + "-" + Mapping.getCell(this.mapStartNext.getMapareaId(), this.mapStartNext.getMapinfoNo(), this.mapStartNext.getNo())
                + (boss ? "(ボス)" : "");
        return mapCell; 
    }
    
    /**
     * ボス判定
     * @return
     */
    private boolean isBoss() {
        if (this.mapStartNext == null) {
            return false;
        }
        return this.mapStartNext.getNo().equals(this.mapStartNext.getBosscellNo()) || this.mapStartNext.getEventId() == 5;
    }
    
    /**
     * ルート情報文字列を返却します
     * @return ルート情報文字列
     */
    public String getRouteViewString() {
        List<String> routeList = this.battleLog.getRoute();
        if (routeList == null) {
            return "";
        }
        String routeStr = routeList.stream()
                                        .map(Mapping::getCell)
                                        .collect(Collectors.joining("→"))
                                        + Optional.ofNullable(this.battleLog.getBattleCount()).map(v -> "(戦闘" + v + "回)").orElse("");
        return routeStr;
    }
    
    /**
     * 演習かどうか
     * @return
     */
    public boolean isPractice() {
        return this.battleLog.isPractice();
    }
    
    /**
     * このオブジェクトの文字列表現を返却します
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hash:").append(getHashCode()).append(System.lineSeparator());
        sb.append("Info情報{").append(System.lineSeparator());        
        sb.append("マス:").append(getMapCellViewString()).append(System.lineSeparator());
        sb.append("ルート:").append(getRouteViewString()).append(System.lineSeparator());
        sb.append("}").append(System.lineSeparator());
        return sb.toString();
    }
}

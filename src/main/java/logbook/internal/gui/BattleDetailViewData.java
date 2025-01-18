package logbook.internal.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import logbook.bean.BattleLog;
import logbook.bean.BattleResult;
import logbook.bean.BattleTypes;
import logbook.bean.BattleTypes.CombinedType;
import logbook.bean.BattleTypes.IFormation;
import logbook.bean.CombinedBattleEachBattle;
import logbook.bean.MapStartNext;
import logbook.bean.Ship;
import logbook.bean.SlotItem;
import logbook.internal.kancolle.Mapping;
import logbook.internal.kancolle.PhaseState;
import logbook.internal.kancolle.Ships;

/**
 * 現在の戦闘、現在の演習、戦闘ログに表示される、戦闘ログ詳細の画面表示情報を構築、保持するクラスです
 * 
 */
public class BattleDetailViewData {

    private BattleLog battleLog;
    
    /**
     * 出撃/進撃情報
     */
    private MapStartNext mapStartNext;
    
    /**
     * 戦闘フェイズ情報
     */
    private PhaseState phaseState;
    
    /**
     * 情報の更新があるか
     */
    private boolean isNewData = false;
    
    /**
     * デフォルトコンストラクタ
     */
    public BattleDetailViewData(BattleLog battleLog) {
        this.battleLog = battleLog;
        this.mapStartNext = battleLog.getNext().size() > 0 ? battleLog.getNext().get(battleLog.getNext().size() - 1) : null;
        this.isNewData= true;
        
        CombinedType combinedType = battleLog.getCombinedType();
        IFormation battle = battleLog.getBattle();
        Map<Integer, List<Ship>> deckMap = battleLog.getDeckMap();
        Map<Integer, SlotItem> itemMap = battleLog.getItemMap();
        Set<Integer> escape = battleLog.getEscape();

        this.phaseState = new PhaseState(combinedType, battle, deckMap, itemMap, escape);
        if (isPractice()) {
            this.phaseState.getAfterEnemy().forEach(enemy -> enemy.setPractice(true));
        }
    }

    /**
     * コンストラクタ
     * ハッシュが一致している場合、PhaseState情報を作成しません
     */
    public BattleDetailViewData(BattleLog battleLog, int hashCode) {
        this.battleLog = battleLog;
        this.mapStartNext = battleLog.getNext().size() > 0 ? battleLog.getNext().get(battleLog.getNext().size() - 1) : null;
        
        if (getHashCode() == hashCode) {
            return;
        }
        isNewData = true;
                
        CombinedType combinedType = battleLog.getCombinedType();
        IFormation battle = battleLog.getBattle();
        Map<Integer, List<Ship>> deckMap = battleLog.getDeckMap();
        Map<Integer, SlotItem> itemMap = battleLog.getItemMap();
        Set<Integer> escape = battleLog.getEscape();

        this.phaseState = new PhaseState(combinedType, battle, deckMap, itemMap, escape);
        if (isPractice()) {
            this.phaseState.getAfterEnemy().forEach(enemy -> enemy.setPractice(true));
        }
    }
    
    /**
     * ハッシュを返却します
     * @return
     */
    public int getHashCode() {
        return Objects.hash(this.mapStartNext , this.battleLog.getBattle(), this.battleLog.getMidnight(), this.battleLog.getResult());
    }

    /**
     * ハッシュ値が違う場合、更新データ存在する
     * @return 画面更新処理フラグ
     */
    public boolean isNewData() {
        return this.isNewData;
    }
    
    /**
     * 戦闘フェイズ情報を返却します
     * @return
     */
    public PhaseState getPhaseState() {
        return this.phaseState;
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
     * 艦隊行動文字列を返却します
     * @return
     */
    public String getInterceptViewString() {
        return BattleTypes.Intercept.toIntercept(this.battleLog.getBattle().getFormation().get(2)).toString();
    }
    
    /**
     * 味方陣形文字列を返却します
     * @return
     */
    public String getFriendFormationViewString() {
        return BattleTypes.Formation.toFormation(this.battleLog.getBattle().getFormation().get(0)).toString();
    }
    
    /**
     * 敵陣形文字列を返却します
     * @return
     */
    public String getEnemyFormationViewString() {
        return BattleTypes.Formation.toFormation(this.battleLog.getBattle().getFormation().get(1)).toString();
    }

    /**
     * 制空値計文字列を返却します
     * @return
     */
    public String getSeikuViewString() {
        IFormation battle = this.battleLog.getBattle();
        if (battle instanceof CombinedBattleEachBattle) {
            //連合艦隊
            //第1艦隊
            int friend = this.phaseState.getAfterFriend().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(Ships::airSuperiority)
                    .sum();
            //第2艦隊
            int friendCombined = this.phaseState.getAfterFriendCombined().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(Ships::airSuperiority)
                    .sum();
            
            StringBuilder seikuSb = new StringBuilder();
            seikuSb.append(friend + friendCombined);
            seikuSb.append("(").append(friend).append("+").append(friendCombined).append(")");
            return seikuSb.toString();
        } else {
            String seikuString = Integer.toString(this.phaseState.getAfterFriend().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(Ships::airSuperiority)
                    .sum());
            return seikuString;
        }
    }
    
    /**
     * 基礎経験値文字列を返却します
     * @return
     */
    public String getGetBaseExpViewString() {
        BattleResult battleResult = this.battleLog.getResult();
        if (battleResult == null) {
            return "?";
        }
        return String.valueOf(battleResult.getGetBaseExp());
    }
    
    /**
     * 艦娘経験値文字列を返却します
     * @return
     */
    public String getShipExpViewString() {
        BattleResult battleResult = this.battleLog.getResult();
        if (battleResult == null) {
            return "?";
        }
        
        String shipExpString = String.valueOf(battleResult.getGetShipExp().stream()
                .mapToInt(Integer::intValue)
                .filter(i -> i > 0)
                .sum()
                + Optional.ofNullable(battleResult.getGetShipExpCombined())
                        .map(v -> v.stream()
                                .mapToInt(Integer::intValue)
                                .filter(i -> i > 0)
                                .sum())
                        .orElse(0));
        
        return shipExpString;
    }

    /**
     * 提督経験値文字列を返却します
     * @return
     */
    public String getGetExpViewString() {
        BattleResult battleResult = this.battleLog.getResult();
        if (battleResult == null) {
            return "?";
        }
        //戦果
        // 提督経験値 ÷ 10000/7 = 提督経験値 ÷ 1428.5714...
        StringBuilder expSb = new StringBuilder();
        expSb.append(battleResult.getGetExp());
        expSb.append("(戦果:");
        expSb.append(BigDecimal.valueOf(battleResult.getGetExp())
                .divide(BigDecimal.valueOf(1428.571), 3, RoundingMode.FLOOR));
        expSb.append(")");
        
        return expSb.toString();
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
        String SPACE = "  ";
        StringBuilder sb = new StringBuilder();
        sb.append("Hash:").append(getHashCode()).append(System.lineSeparator());
        sb.append("Info情報{").append(System.lineSeparator());        
        sb.append(SPACE).append("マス:").append(getMapCellViewString()).append(System.lineSeparator());
        sb.append(SPACE).append("ルート:").append(getRouteViewString()).append(System.lineSeparator());
        sb.append(SPACE).append("艦隊行動:").append(getInterceptViewString()).append(System.lineSeparator());
        sb.append(SPACE).append("味方陣形:").append(getFriendFormationViewString()).append(System.lineSeparator());
        sb.append(SPACE).append("敵陣形:").append(getEnemyFormationViewString()).append(System.lineSeparator());
        sb.append(SPACE).append("制空値計:").append(getSeikuViewString()).append(System.lineSeparator());
        sb.append(SPACE).append("基礎経験値:").append(getGetBaseExpViewString()).append(System.lineSeparator());
        sb.append(SPACE).append("艦娘経験値:").append(getShipExpViewString()).append(System.lineSeparator());
        sb.append(SPACE).append("提督経験値:").append(getGetExpViewString()).append(System.lineSeparator());
        sb.append("}").append(System.lineSeparator());
        return sb.toString();
    }
}

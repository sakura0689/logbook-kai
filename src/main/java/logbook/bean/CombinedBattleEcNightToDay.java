package logbook.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.bean.BattleTypes.IAirBaseAttack;
import logbook.bean.BattleTypes.ICombinedBattle;
import logbook.bean.BattleTypes.ICombinedEcBattle;
import logbook.bean.BattleTypes.IFormation;
import logbook.bean.BattleTypes.IKouku;
import logbook.bean.BattleTypes.INSupport;
import logbook.bean.BattleTypes.INightToDayBattle;
import logbook.bean.BattleTypes.ISortieHougeki;
import logbook.bean.BattleTypes.ISupport;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * 夜戦→昼戦(vs連合艦隊)
 *
 */
@Data
public class CombinedBattleEcNightToDay implements ICombinedBattle, ICombinedEcBattle, IFormation,
        INightToDayBattle, IAirBaseAttack, IKouku, ISortieHougeki, INSupport, ISupport, Serializable {

    private static final long serialVersionUID = -364877629377359534L;

    /** api_dock_id/api_deck_id */
    private Integer dockId;

    /** api_formation */
    private List<Integer> formation;

    /** api_f_nowhps */
    private List<Integer> fNowhps;

    /** api_f_maxhps */
    private List<Integer> fMaxhps;

    /** api_f_nowhps_combined */
    private List<Integer> fNowhpsCombined;

    /** api_f_maxhps_combined */
    private List<Integer> fMaxhpsCombined;

    /** api_fParam */
    private List<List<Integer>> fParam;

    /** api_fParam_combined */
    private List<List<Integer>> fParamCombined;

    /** api_ship_ke */
    private List<Integer> shipKe;

    /** api_ship_lv */
    private List<Integer> shipLv;

    /** api_ship_ke_combined */
    private List<Integer> shipKeCombined;

    /** api_ship_lv_combined */
    private List<Integer> shipLvCombined;

    /** api_e_nowhps */
    private List<Integer> eNowhps;

    /** api_e_maxhps */
    private List<Integer> eMaxhps;

    /** api_e_nowhps_combined */
    private List<Integer> eNowhpsCombined;

    /** api_e_maxhps_combined */
    private List<Integer> eMaxhpsCombined;

    /** api_eSlot */
    private List<List<Integer>> eSlot;

    /** api_eSlot_combined */
    private List<List<Integer>> eSlotCombined;

    /** api_eParam */
    private List<List<Integer>> eParam;

    /** api_eParam_combined */
    private List<List<Integer>> eParamCombined;

    /** api_touch_plane */
    private List<Integer> touchPlane;

    /** api_flare_pos */
    private List<Integer> flarePos;

    /** api_n_support_flag */
    private Integer nSupportFlag;

    /** api_n_support_info */
    private BattleTypes.SupportInfo nSupportInfo;

    /** api_n_hougeki1 */
    private BattleTypes.MidnightHougeki nHougeki1;

    /** api_n_hougeki2 */
    private BattleTypes.MidnightHougeki nHougeki2;

    /** api_day_flag */
    private Boolean dayFlag;

    /** api_search */
    private List<Integer> search;

    /** api_air_base_injection */
    private BattleTypes.AirBaseAttack airBaseInjection;

    /** api_air_base_attack */
    private List<BattleTypes.AirBaseAttack> airBaseAttack;

    /** api_stage_flag */
    private List<Integer> stageFlag;

    /** api_injection_kouku */
    private BattleTypes.Kouku injectionKouku;

    /** api_kouku */
    private BattleTypes.Kouku kouku;

    /** api_support_flag */
    private Integer supportFlag;

    /** api_support_info */
    private BattleTypes.SupportInfo supportInfo;

    /** api_opening_taisen_flag */
    private Boolean openingTaisenFlag;

    /** api_opening_taisen */
    private BattleTypes.Hougeki openingTaisen;

    /** api_opening_flag */
    private Boolean openingFlag;

    /** api_opening_atack */
    private BattleTypes.Raigeki openingAtack;

    /** api_hourai_flag */
    private List<Integer> houraiFlag;

    /** api_hougeki1 */
    private BattleTypes.Hougeki hougeki1;

    /** api_hougeki2 */
    private BattleTypes.Hougeki hougeki2;

    /** api_hougeki3(未使用) */
    private BattleTypes.Hougeki hougeki3;

    /** api_raigeki */
    private BattleTypes.Raigeki raigeki;

    /** api_midnight_flag(未使用) */
    private Boolean midnightFlag;

    /**
     * JsonObjectから{@link CombinedBattleEcNightToDay}を構築します
     *
     * @param json JsonObject
     * @return {@link CombinedBattleEcNightToDay}
     */
    public static CombinedBattleEcNightToDay toBattle(JsonObject json) {
        CombinedBattleEcNightToDay bean = new CombinedBattleEcNightToDay();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_deck_id", bean::setDockId)
                .setIntegerList("api_formation", bean::setFormation)
                .setIntegerList("api_f_nowhps", bean::setFNowhps)
                .setIntegerList("api_f_maxhps", bean::setFMaxhps)
                .setIntegerList("api_f_nowhps_combined", bean::setFNowhpsCombined)
                .setIntegerList("api_f_maxhps_combined", bean::setFMaxhpsCombined)
                .set("api_fParam", bean::setFParam, JsonHelper.toList(JsonHelper::toIntegerList))
                .set("api_fParam_combined", bean::setFParamCombined, JsonHelper.toList(JsonHelper::toIntegerList))
                .setIntegerList("api_ship_ke", bean::setShipKe)
                .setIntegerList("api_ship_lv", bean::setShipLv)
                .setIntegerList("api_ship_ke_combined", bean::setShipKeCombined)
                .setIntegerList("api_ship_lv_combined", bean::setShipLvCombined)
                .setIntegerList("api_e_nowhps", bean::setENowhps)
                .setIntegerList("api_e_maxhps", bean::setEMaxhps)
                .setIntegerList("api_e_nowhps_combined", bean::setENowhpsCombined)
                .setIntegerList("api_e_maxhps_combined", bean::setEMaxhpsCombined)
                .set("api_eSlot", bean::setESlot, JsonHelper.toList(JsonHelper::toIntegerList))
                .set("api_eSlot_combined", bean::setESlotCombined, JsonHelper.toList(JsonHelper::toIntegerList))
                .set("api_eParam", bean::setEParam, JsonHelper.toList(JsonHelper::toIntegerList))
                .set("api_eParam_combined", bean::setEParamCombined, JsonHelper.toList(JsonHelper::toIntegerList))
                .setIntegerList("api_touch_plane", bean::setTouchPlane)
                .setIntegerList("api_flare_pos", bean::setFlarePos)
                .setInteger("api_n_support_flag", bean::setNSupportFlag)
                .set("api_n_support_info", bean::setNSupportInfo, BattleTypes.SupportInfo::toSupportInfo)
                .set("api_n_hougeki1", bean::setNHougeki1, BattleTypes.MidnightHougeki::toMidnightHougeki)
                .set("api_n_hougeki2", bean::setNHougeki2, BattleTypes.MidnightHougeki::toMidnightHougeki)
                .setBoolean("api_day_flag", bean::setDayFlag)
                .setIntegerList("api_search", bean::setSearch)
                .set("api_air_base_injection", bean::setAirBaseInjection,
                        BattleTypes.AirBaseAttack::toAirBaseAttack)
                .set("api_air_base_attack", bean::setAirBaseAttack,
                        JsonHelper.toList(BattleTypes.AirBaseAttack::toAirBaseAttack))
                .setIntegerList("api_stage_flag", bean::setStageFlag)
                .set("api_injection_kouku", bean::setInjectionKouku, BattleTypes.Kouku::toKouku)
                .set("api_kouku", bean::setKouku, BattleTypes.Kouku::toKouku)
                .setInteger("api_support_flag", bean::setSupportFlag)
                .set("api_support_info", bean::setSupportInfo, BattleTypes.SupportInfo::toSupportInfo)
                .setBoolean("api_opening_taisen_flag", bean::setOpeningTaisenFlag)
                .set("api_opening_taisen", bean::setOpeningTaisen, BattleTypes.Hougeki::toHougeki)
                .setBoolean("api_opening_flag", bean::setOpeningFlag)
                .set("api_opening_atack", bean::setOpeningAtack, BattleTypes.Raigeki::toRaigeki)
                .setIntegerList("api_hourai_flag", bean::setHouraiFlag)
                .set("api_hougeki1", bean::setHougeki1, BattleTypes.Hougeki::toHougeki)
                .set("api_hougeki2", bean::setHougeki2, BattleTypes.Hougeki::toHougeki)
                .set("api_hougeki3", bean::setHougeki3, BattleTypes.Hougeki::toHougeki)
                .set("api_raigeki", bean::setRaigeki, BattleTypes.Raigeki::toRaigeki);
        
        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}

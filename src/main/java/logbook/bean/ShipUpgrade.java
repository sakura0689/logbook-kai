package logbook.bean;

import java.io.Serializable;
import java.util.Set;

import jakarta.json.JsonObject;
import logbook.internal.logger.LoggerHolder;
import logbook.internal.util.JsonHelper;
import logbook.internal.util.UnUsedKeyBindListener;
import lombok.Data;

/**
 * 艦娘改装（アップグレード）情報を表します
 *
 */
@Data
public class ShipUpgrade implements Serializable {

    private static final long serialVersionUID = -2428543534571278156L;

    /** id */
    private Integer id;

    /** api_current_ship_id */
    private Integer currentShipId;

    /** api_original_ship_id */
    private Integer originalShipId;

    /** api_upgrade_type */
    private Integer upgradeType;

    /** api_upgrade_level */
    private Integer upgradeLevel;

    /** api_drawing_count 改装設計図 */
    private Integer drawingCount = 0;

    /** api_catapult_count 試製甲板カタパルト */
    private Integer catapultCount = 0;

    /** api_report_count 戦闘詳報 */
    private Integer reportCount = 0;

    /** api_aviation_mat_count 新型航空兵装資材 */
    private Integer aviationMatCount = 0;

    /** api_arms_mat_count 新型兵装資材 */
    private Integer armsMatCount = 0;

    /** api_tech_count 海外艦最新技術 */
    private Integer techCount = 0;

    /** api_boiler_count 新型高温高圧缶 */
    private Integer boilerCount = 0;

    /** api_sortno */
    private Integer sortno;

    /**
     * JsonObjectから{@link ShipUpgrade}を構築します
     *
     * @param json JsonObject
     * @return {@link ShipUpgrade}
     */
    public static ShipUpgrade toShipUpgrade(JsonObject json) {
        ShipUpgrade bean = new ShipUpgrade();

        UnUsedKeyBindListener unUsedKeyBindListener = null;
        if (LoggerHolder.get().isDebugEnabled()) {
            unUsedKeyBindListener = new UnUsedKeyBindListener(json);
        }

        JsonHelper.bind(json, unUsedKeyBindListener)
                .setInteger("api_id", bean::setId)
                .setInteger("api_current_ship_id", bean::setCurrentShipId)
                .setInteger("api_original_ship_id", bean::setOriginalShipId)
                .setInteger("api_upgrade_type", bean::setUpgradeType)
                .setInteger("api_upgrade_level", bean::setUpgradeLevel)
                .setInteger("api_drawing_count", bean::setDrawingCount)
                .setInteger("api_catapult_count", bean::setCatapultCount)
                .setInteger("api_report_count", bean::setReportCount)
                .setInteger("api_aviation_mat_count", bean::setAviationMatCount)
                .setInteger("api_arms_mat_count", bean::setArmsMatCount)
                .setInteger("api_tech_count", bean::setTechCount)
                .setInteger("api_boiler_count", bean::setBoilerCount)
                .setInteger("api_sortno", bean::setSortno);

        if (LoggerHolder.get().isDebugEnabled()) {
            Set<String> unUsedKey = unUsedKeyBindListener.getUnusedKeys();
            for (String key : unUsedKey) {
                LoggerHolder.get().debug("未使用のKeyを検出 : " + key);
            }
        }

        return bean;
    }
}

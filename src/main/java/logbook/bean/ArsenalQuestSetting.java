package logbook.bean;

import lombok.Data;

/**
 * 工廠任務受領設定
 *
 */
@Data
public class ArsenalQuestSetting {

    /** 対応しないフラグ */
    private boolean discard;

    /** 備考 */
    private String comment;
}

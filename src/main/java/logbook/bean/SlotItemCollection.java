package logbook.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import logbook.internal.Config;
import logbook.internal.gui.ShipTablePane;
import logbook.internal.logger.LoggerHolder;
import lombok.Data;

/**
 * アイテムのコレクション
 *
 * note
 * 新規艦娘入手の際、所有艦娘一覧の装備欄が更新されない問題の対応のため、更新リスナーが存在する
 * 
 * 詳細
 * 新規艦娘入手時のAPIの流れ
 * 1./kcsapi/api_port/port
 *   艦娘の一覧が更新される。この際、slot情報はslotitemidのListが取得できるが、そのslotitemidに相当する装備はこのタイミングでは存在しない。そのため装備1-5は空表示となる。
 *   タブ制御クラス、ShipTablePaneはItemImageCellの更新情報を監視している。
 *   Ship情報にはslot1-5はIntegerで装備番号のみ保持している。
 *   そのため、SlotItemのMaster情報が変更されても、艦娘自体のslot1-5のslotitemidには変更はないため、画面の更新は行われない。
 * 2./kcsapi/api_get_member/slot_item
 *   装備一覧情報が更新される。
 *   この際に、所有艦娘一覧パネルが開かれているなら、装備1-5情報を再表示する
 *   
 * @see ShipController#initialize,onSlotItemUpdated,onWindowHidden
 * @see ShipTablePane#refreshSlotItems
 */
@Data
public class SlotItemCollection implements Serializable {

    private static final long serialVersionUID = -2530569251712024161L;

    /** アイテム */
    private Map<Integer, SlotItem> slotitemMap = new LinkedHashMap<>();
    
    /** 更新リスナー（シリアライズ対象外） */
    private transient List<Runnable> updateListeners = new ArrayList<>();

    /**
     * アプリケーションのデフォルト設定ディレクトリから{@link SlotItemCollection}を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(SlotItemCollection.class, SlotItemCollection::new)</code>
     * </blockquote>
     *
     * @return {@link SlotItemCollection}
     */
    public static SlotItemCollection get() {
        return Config.getDefault().get(SlotItemCollection.class, SlotItemCollection::new);
    }
    
    /**
     * 更新リスナーを追加します。
     * クラスコメント参照
     * 
     * @param listener 呼び出したい処理
     */
    public void addUpdateListener(Runnable listener) {
        if (this.updateListeners == null) {
            this.updateListeners = new ArrayList<>();
        }
        this.updateListeners.add(listener);
    }

    /**
     * 更新リスナーを削除します。
     * クラスコメント参照
     * 
     * @param listener 削除するリスナー
     */
    public void removeUpdateListener(Runnable listener) {
        if (this.updateListeners != null) {
            this.updateListeners.remove(listener);
        }
    }

    /**
     * アイテム情報を更新し、更新リスナーに通知します。
     * クラスコメント参照
     * 
     * @param map 新しいアイテムマップ
     */
    public void setSlotitemMap(Map<Integer, SlotItem> map) {
        this.slotitemMap = map;
        // リスナー通知
        if (this.updateListeners != null) {
            this.updateListeners.forEach(listener -> {
                try {
                    listener.run();
                } catch (Exception e) {
                    LoggerHolder.get().error("アイテム情報更新リスナー処理にてエラー発生", e);
                }
            });
        }
    }
}

package logbook.bean;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import logbook.internal.Config;
import lombok.Data;

/**
 * 戦果のベースとなる提督経験値の記録
 */
@Data
public class AppExpRecords {
    /** この半日の基準戦果 (2時/14時を過ぎて最初の経験値） */
    private Long exp12h;
    /** exp12h を設定した時刻 [ms] */
    private long time12h;
    /** この1日の基準戦果 */
    private Long exp1d;
    /** exp1d を設定した時刻 [ms] */
    private long time1d;

    public void update(Basic basic) {
        // 戦果は JST 2時/14時で切り替わるため UTC+7 を基準とすると計算しやすい
        final ZoneId UTC7 = ZoneId.of("UTC+07:00");
        ZonedDateTime now = ZonedDateTime.now(UTC7);
        long base = getBase(now);
        long base12 = getBase(Instant.ofEpochMilli(this.time12h).atZone(UTC7));
        long exp = Optional.ofNullable(Basic.get()).map(Basic::getExperience).map(Integer::longValue).orElse(0L);
        if (base != base12) {
            if (getBase(now.minusHours(12)) == base12) {
                // 1日用の経験値にシフト
                this.exp1d = this.exp12h;
                this.time1d = this.time12h;
            } else {
                // もっと前になってしまったので今の値を埋める
                this.exp1d = exp;
                this.time1d = now.toInstant().toEpochMilli();
            }
            this.exp12h = exp;
            this.time12h = now.toInstant().toEpochMilli();
        }
    }


    private static long getBase(ZonedDateTime time) {
        ZonedDateTime base = time.truncatedTo(ChronoUnit.HOURS);
        if (base.getHour() < 12) {
            // AM
            base = base.withHour(0);
        } else {
            // PM
            base = base.withHour(12);
        }
        return base.toEpochSecond();
    }

    /**
     * アプリケーションのデフォルト設定ディレクトリから{@link AppExpRecords}を取得します、
     * これは次の記述と同等です
     * <blockquote>
     *     <code>Config.getDefault().get(AppExpRecords.class, AppExpRecords::new)</code>
     * </blockquote>
     *
     * @return {@link AppExpRecords}
     */
    public static AppExpRecords get() {
        return Config.getDefault().get(AppExpRecords.class, AppExpRecords::new);
    }
}

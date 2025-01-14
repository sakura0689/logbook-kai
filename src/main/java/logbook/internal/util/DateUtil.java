package logbook.internal.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ログの表示、書き込みで使用する
 *
 */
public final class DateUtil {

    /** 日付書式 */
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * タイムゾーンをJSTとして現在の日付/時間を取得します
     *
     * @return 現在の日付/時間
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
    }

    /**
     * タイムゾーンをJSTとして現在の日付/時間を"yyyy-MM-dd HH:mm:ss"形式の文字列として取得します
     *
     * @return 現在の日付/時間
     */
    public static String nowString() {
        return DATE_FORMAT.format(now());
    }

}

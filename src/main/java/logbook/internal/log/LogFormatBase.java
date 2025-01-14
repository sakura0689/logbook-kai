package logbook.internal.log;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import logbook.internal.util.DateUtil;

public abstract class LogFormatBase<T> implements LogFormat<T> {

    /** 日付書式 */
    protected static final DateTimeFormatter DATE_FORMAT = DateUtil.DATE_FORMAT;

    /**
     * タイムゾーンをJSTとして現在の日付/時間を取得します
     *
     * @return 現在の日付/時間
     */
    protected static ZonedDateTime now() {
        return DateUtil.now();
    }

    /**
     * タイムゾーンをJSTとして現在の日付/時間を"yyyy-MM-dd HH:mm:ss"形式の文字列として取得します
     *
     * @return 現在の日付/時間
     */
    protected static String nowString() {
        return DateUtil.nowString();
    }
    
    /**
     * 文字列を "" で囲み、" があればエスケープします
     * 
     * @param text 元の文字列
     * @return エスケープ済みの "" で囲まれた文字列
     */
    protected static String wrap(String text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        return "\"" + text.replaceAll("\"", "\"\"") + "\"";
    }
}

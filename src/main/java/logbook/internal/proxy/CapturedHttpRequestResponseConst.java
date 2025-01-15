package logbook.internal.proxy;

/**
 * OriginのRequest/Responseを取得するための定義クラスです
 */
public class CapturedHttpRequestResponseConst {

    /** キャプチャーするリクエストのバイトサイズ上限 */
    public static final int MAX_POST_FIELD_SIZE = 1024 * 1024 * 12;

    /** setAttribute用のキー(CaptureHolder) */
    public static final String CONTENT_HOLDER = "logbook.content-holder";
}

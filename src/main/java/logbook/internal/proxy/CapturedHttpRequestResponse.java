package logbook.internal.proxy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CaptureしたOriginのHttpRequest/HttpResponse情報を保持するクラスです
 */
public class CapturedHttpRequestResponse {

    private List<byte[]> originReq = new ArrayList<>();
    
    private List<byte[]> originRes = new ArrayList<>();

    public void putOriginRequest(byte[] data) {
        if (this.originReq.size() > 0 && Arrays.equals(this.originReq.get(this.originReq.size()-1), data)) {
            // if the data is the same as the last one in this.req array, no need to add this to req as it is duplicated
            return;
        }
        this.originReq.add(data);
    }

    public InputStream getOriginRequest() {
        return new ByteArrayInputStream2(this.originReq);
    }

    public void putOriginResponse(byte[] data) {
        this.originRes.add(data);
    }

    public InputStream getOriginResponse() {
        return new ByteArrayInputStream2(this.originRes);
    }

    public void clear() {
        this.originReq = null;
        this.originRes = null;
    }
}

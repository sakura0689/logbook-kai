package logbook.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin初期化時のException情報を保持するクラスです
 */
public class PluginInitExceptionHolder {
    private List<Exception> exceptionList = new ArrayList<Exception>();
    
    public void putException(Exception e) {
        exceptionList.add(e);
    }
    
    public boolean isInitError() {
        return this.exceptionList.size() > 0;
    }
    
    public List<Exception> getException() {
        return this.exceptionList;
    }
}

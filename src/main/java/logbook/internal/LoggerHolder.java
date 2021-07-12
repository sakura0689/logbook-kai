package logbook.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ロギング
 */
public class LoggerHolder {

    public static LoggerProxy get() {
        String callerClass = getCallerClass(2);
        return new LoggerProxy(callerClass);
    }

    public static class LoggerProxy {

        private Logger logger;

        public LoggerProxy(String callerClass) {
            this.logger = LogManager.getLogger(callerClass);
        }

        public boolean isDebugEnabled() {
            return this.logger.isDebugEnabled();
        }

        public void debug(String message) {
            this.logger.debug(getCallerClassAddMethodLines(2) + message);
        }

        public void debug(String message, Object... params) {
            this.logger.debug(getCallerClassAddMethodLines(2) + message, params);
        }

        public void debug(String message, Throwable t) {
            this.logger.debug(getCallerClassAddMethodLines(2) + message, t);
        }

        public void info(String message) {
            this.logger.info(getCallerClassAddMethodLines(2) + message);
        }

        public void info(String message, Object... params) {
            this.logger.info(getCallerClassAddMethodLines(2) + message, params);
        }

        public void info(String message, Throwable t) {
            this.logger.info(getCallerClassAddMethodLines(2) + message, t);
        }

        public void warn(String message) {
            this.logger.warn(getCallerClassAddMethodLines(2) + message);
        }

        public void warn(String message, Object... params) {
            this.logger.warn(getCallerClassAddMethodLines(2) + message, params);
        }

        public void warn(String message, Throwable t) {
            this.logger.warn(getCallerClassAddMethodLines(2) + message, t);
        }

        public void error(String message) {
            this.logger.error(getCallerClassAddMethodLines(2) + message);
        }

        public void error(String message, Object... params) {
            this.logger.error(getCallerClassAddMethodLines(2) + message, params);
        }

        public void error(String message, Throwable t) {
            this.logger.error(getCallerClassAddMethodLines(2) + message, t);
        }

        public void fatal(String message) {
            this.logger.trace(getCallerClassAddMethodLines(2) + message);
        }

        public void fatal(String message, Object... params) {
            this.logger.trace(getCallerClassAddMethodLines(2) + message, params);
        }

        public void fatal(String message, Throwable t) {
            this.logger.trace(getCallerClassAddMethodLines(2) + message, t);
        }
    }

    static String getCallerClass(final int depth) {
        return getEquivalentStackTraceElement(depth + 1).getClassName();
    }

    static String getCallerClassAddMethodLines(final int depth) {
        StackTraceElement element = getEquivalentStackTraceElement(depth + 1);
        return new StringBuilder(element.getClassName()).append("#").append(element.getMethodName()).append("(").append(element.getLineNumber()).append(") ").toString();
    }

    private static StackTraceElement getEquivalentStackTraceElement(final int depth) {
        final StackTraceElement[] elements = new Throwable().getStackTrace();
        int i = 0;
        for (final StackTraceElement element : elements) {
            if (isValid(element)) {
                if (i == depth) {
                    return element;
                }
                ++i;
            }
        }
        return null;
    }

    private static boolean isValid(final StackTraceElement element) {
        if (element.isNativeMethod()) {
            return false;
        }
        final String cn = element.getClassName();
        if (cn.startsWith("sun.reflect.")) {
            return false;
        }
        final String mn = element.getMethodName();
        if (cn.startsWith("java.lang.reflect.") && (mn.equals("invoke") || mn.equals("newInstance"))) {
            return false;
        }
        if (cn.equals("java.lang.Class") && mn.equals("newInstance")) {
            return false;
        }
        if (cn.equals("java.lang.invoke.MethodHandle") && mn.startsWith("invoke")) {
            return false;
        }
        return true;
    }
}

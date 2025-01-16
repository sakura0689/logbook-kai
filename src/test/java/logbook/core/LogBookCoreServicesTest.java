package logbook.core;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class LogBookCoreServicesTest {

    @Test
    void testInit() throws Exception {
        LogBookCoreServices.init(Collections.emptyList());
    }
}

package logbook.core;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import logbook.api.APIListenerSpi;
import logbook.listener.ContentListenerSpi;
import logbook.plugin.lifecycle.StartUp;
import logbook.proxy.ProxyServerSpi;

class LogBookCoreServicesTest {

    @BeforeEach
    void setup() throws Exception {
        LogBookCoreServices.init(Collections.emptyList());
    }
    
    @Test
    void testGetServiceProviders() throws Exception {
        List<APIListenerSpi> apiListenerSpiList =  LogBookCoreServices.getServiceProviders(APIListenerSpi.class).collect(Collectors.toList());
        Assertions.assertEquals(67, apiListenerSpiList.size());
        
        List<StartUp> startUpList =  LogBookCoreServices.getServiceProviders(StartUp.class).collect(Collectors.toList());
        Assertions.assertEquals(1, startUpList.size());

        List<ContentListenerSpi> contentListenerSpiList =  LogBookCoreServices.getServiceProviders(ContentListenerSpi.class).collect(Collectors.toList());
        Assertions.assertEquals(2, contentListenerSpiList.size());

        List<ProxyServerSpi> proxyServerSpiList =  LogBookCoreServices.getServiceProviders(ProxyServerSpi.class).collect(Collectors.toList());
        Assertions.assertEquals(1, proxyServerSpiList.size());
    }
}

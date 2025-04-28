package com.devappsys.logs_grpc.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigurationTest {

    @Test
    public void testConfigurationInitialization() {
        Configuration config = new Configuration(
                "127.0.0.1", 5000, "com.example.app", "client-123",
                "Samsung", "Galaxy S21", "Android 14", "1.2.3",
                "device-abc","user-xyz"
        );

        assertEquals("127.0.0.1", config.getHost());
        assertEquals(5000, config.getPort());
        assertEquals("client-123", config.getClientId());
        assertEquals("Galaxy S21", config.getModel());
        assertEquals("Android 14", config.getOsVersion());
        assertEquals("1.2.3", config.getAppVersion());
        assertEquals("device-abc", config.getDeviceId());
        assertEquals("com.example.app", config.getPackageName());
        assertEquals("Samsung", config.getManufacturer());

    }
}
package com.devappsys.logs_grpc;

import com.devappsys.logs_grpc.models.Configuration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LogsPluginTest {

//    private Configuration config;
//
//    @Before
//    public void setUp() {
//        config = new Configuration(
//                "127.0.0.1", 5000, "com.example.app", "client-123",
//                "Samsung", "Galaxy S21", "Android 14", "1.2.3", "device-abc",
//                "user-xyz"
//        );
//        resetLogsPlugin();
//    }
//
//    private void resetLogsPlugin() {
//        try {
//            java.lang.reflect.Field instanceField = LogsPlugin.class.getDeclaredField("_instance");
//            instanceField.setAccessible(true);
//            instanceField.set(null, null);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    public void testInitAndGetInstance() {
//        LogsPlugin.init(config);
//        LogsPlugin plugin = LogsPlugin.getInstance();
//        assertNotNull(plugin);
//    }
//
//    @Test(expected = IllegalStateException.class)
//    public void testGetInstanceWithoutInit() {
//        LogsPlugin.getInstance();
//    }
//
//    @Test(expected = IllegalStateException.class)
//    public void testInitTwiceThrowsException() {
//        LogsPlugin.init(config);
//        LogsPlugin.init(config); // Should throw exception (based on stricter init() rule)
//    }
}
package com.devappsys.logs_grpc.grpc;

import com.devappsys.log.ContextOuterClass;
import com.devappsys.log.ContextServiceGrpc;
import com.devappsys.log.Event;
import com.devappsys.log.EventServiceGrpc;
import com.devappsys.log.Log;
import com.devappsys.log.LogServiceGrpc;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClientBlockingImpl implements GrpcClient {
    private static GrpcClientBlockingImpl _instance;
    private String _host;
    private int _port;
    private ManagedChannel _channel;
    private LogServiceGrpc.LogServiceBlockingStub _logStub;
    private EventServiceGrpc.EventServiceBlockingStub _eventStub;
    private ContextServiceGrpc.ContextServiceBlockingStub _contextStub;

    public static GrpcClientBlockingImpl getInstance() {
        if (_instance == null) {
            throw new IllegalStateException("GrpcClient is not initialized");
        }
        return _instance;
    }

    private GrpcClientBlockingImpl(String host, int port) {
        this._host = host;
        this._port = port;

        _channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .maxInboundMessageSize(30*1024*1024)
                .idleTimeout(30, TimeUnit.SECONDS) // Auto-close if idle
                .build();
        _logStub = LogServiceGrpc.newBlockingStub(_channel);
        _eventStub = EventServiceGrpc.newBlockingStub(_channel);
        _contextStub = ContextServiceGrpc.newBlockingStub(_channel);
    }

    public static void init(String host, int port) {
        if (_instance != null) {
            throw new IllegalStateException("GrpcClient is already initialized");
        }
        _instance = new GrpcClientBlockingImpl(host, port);
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }
    @Override

    public void shutdown() {
        if (_instance != null) {
            if (!_instance._channel.isShutdown()) {
                _instance._channel.shutdown();
            }
            _instance = null;
        } else {
            throw new IllegalStateException("GrpcClient is not initialized");
        }
    }
    @Override
    public ContextOuterClass.Response sendLog(Log.LogMessage logMessage) {
        return _logStub.sendLog(logMessage);
    }
    @Override
    public ContextOuterClass.Response sendLogList(Log.LogList logList) {
        return _logStub.sendLogList(logList);
    }
    @Override
    public ContextOuterClass.Response sendEvent(Event.EventMessage eventMessage) {
        return _eventStub.sendEvent(eventMessage);
    }
    @Override
    public ContextOuterClass.Response sendEventList(Event.EventList eventList) {
        return _eventStub.sendEventList(eventList);
    }
    @Override
    public ContextOuterClass.Response sendContext(ContextOuterClass.Context contextMessage) {
        return _contextStub.sendContext(contextMessage);
    }
    @Override
    public ContextOuterClass.Response sendContextList(ContextOuterClass.ContextList contextList) {
        return _contextStub.sendContextList(contextList);
    }

    @Override
    public ContextOuterClass.Response sendLogFile(Log.LogFileRequest logFile) {
        return _logStub.sendLogFile(logFile);
    }

    @Override
    public ContextOuterClass.Response sendEventFile(Event.EventFileRequest eventFile) {
        return _eventStub.sendEventFile(eventFile);
    }

    @Override
    public ContextOuterClass.Response sendContextFile(ContextOuterClass.ContextFileRequest contextFile) {
        return _contextStub.sendContextFile(contextFile);
    }

}

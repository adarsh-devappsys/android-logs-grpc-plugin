package com.devappsys.grpc_logs.api.grpc;

import com.devappsys.log.Common;
import com.devappsys.log.Context;
import com.devappsys.log.Event;
import com.devappsys.log.Log;
import com.devappsys.log.LoggingServiceGrpc;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcClientAsyncImpl {

    private final ManagedChannel channel;
    private final LoggingServiceGrpc.LoggingServiceStub asyncStub;

    public GrpcClientAsyncImpl(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .maxInboundMessageSize(30 * 1024 * 1024)
                .idleTimeout(30, TimeUnit.SECONDS)
                .build();
        this.asyncStub = LoggingServiceGrpc.newStub(channel);
    }

    // === Single Uploads ===

    public void uploadLog(Log.LogMessage request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadLog(request, responseObserver);
    }


    public void uploadEvent(Event.EventMessage request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadEvent(request, responseObserver);
    }


    public void uploadContext(Context.ContextMessage request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadContext(request, responseObserver);
    }

    // === Batch Uploads ===

    public void uploadLogs(Log.LogBatch request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadLogs(request, responseObserver);
    }


    public void uploadEvents(Event.EventBatch request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadEvents(request, responseObserver);
    }


    public void uploadContexts(Context.ContextBatch request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadContexts(request, responseObserver);
    }

    // === Compressed File Uploads ===

    public void uploadLogsFile(Common.CompressedFileUpload request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadLogsFile(request, responseObserver);
    }


    public void uploadEventsFile(Common.CompressedFileUpload request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadEventsFile(request, responseObserver);
    }


    public void uploadContextsFile(Common.CompressedFileUpload request, StreamObserver<Common.UploadResponse> responseObserver) {
        asyncStub.uploadContextsFile(request, responseObserver);
    }

    // === Streaming Uploads ===

    public StreamObserver<Log.LogMessage> streamLogs(StreamObserver<Common.UploadResponse> responseObserver) {
        return asyncStub.streamLogs(responseObserver);
    }


    public StreamObserver<Event.EventMessage> streamEvents(StreamObserver<Common.UploadResponse> responseObserver) {
        return asyncStub.streamEvents(responseObserver);
    }


    public StreamObserver<Context.ContextMessage> streamContexts(StreamObserver<Common.UploadResponse> responseObserver) {
        return asyncStub.streamContexts(responseObserver);
    }

    // === Cleanup ===

    public void shutdown() {
        if (!channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
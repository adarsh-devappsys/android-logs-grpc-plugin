package com.devappsys.logs_grpc.grpc;

import com.devappsys.log.ContextOuterClass;
import com.devappsys.log.Event;
import com.devappsys.log.Log;

public interface GrpcClient {
    ContextOuterClass.Response sendLog(Log.LogMessage logMessage);
    ContextOuterClass.Response sendEvent(Event.EventMessage eventMessage);
    ContextOuterClass.Response sendContext(ContextOuterClass.Context contextMessage);

    ContextOuterClass.Response sendLogList(Log.LogList logList);
    ContextOuterClass.Response sendEventList(Event.EventList eventList);
    ContextOuterClass.Response sendContextList(ContextOuterClass.ContextList contextList);

    void shutdown();
}

package com.devappsys.logs_grpc.local_datasouce;

import com.devappsys.log.ContextOuterClass;
import com.devappsys.log.Event;
import com.devappsys.log.Log;
import com.devappsys.logs_grpc.models.data.ContextModel;
import com.devappsys.logs_grpc.models.data.EventModel;
import com.devappsys.logs_grpc.models.data.LogModel;

import java.util.List;

public interface LocalDatasourceRepo {
    void saveLog(LogModel logModel);
    void saveEvent(EventModel eventModel);
    void saveContext(ContextModel contextModel);
    Log.LogList getLogs();
   Event.EventList getEvents();
    ContextOuterClass.ContextList getContexts();
}

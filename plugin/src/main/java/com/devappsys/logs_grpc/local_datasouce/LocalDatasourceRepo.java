package com.devappsys.logs_grpc.local_datasouce;

import com.devappsys.log.ContextOuterClass;
import com.devappsys.log.Event;
import com.devappsys.log.Log;
import com.devappsys.logs_grpc.models.data.ContextModel;
import com.devappsys.logs_grpc.models.data.EventModel;
import com.devappsys.logs_grpc.models.data.LogModel;

import java.io.File;
import java.util.List;

public interface LocalDatasourceRepo {

    /** Save methods */
    void saveLog(LogModel logModel);
    void saveEvent(EventModel eventModel);
    void saveContext(ContextModel contextModel);

    /** Cleanup: close all open file streams */
    void cleanup();

    /** Check if data exists to upload */
    boolean hasLogsToUpload();
    boolean hasEventsToUpload();
    boolean hasContextsToUpload();

    /** Get File for uploading */
    List<File> getLogsFile();
    List<File> getEventsFile();
    List<File> getContextsFile();

    /** Delete the specific data file after upload */
    void deleteLogsFile(String fileName);
    void deleteEventsFile(String fileName);
    void deleteContextsFile(String fileName);

    /** Delete all stored data (force clean) */
    void deleteAllLogs();
    void deleteAllEvents();
    void deleteAllContexts();

    /** Emergency rotate is method used for rotating files without even considering the file size  */
    void rotateEmergency();
}
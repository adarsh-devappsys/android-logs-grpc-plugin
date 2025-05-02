package com.devappsys.grpc_logs.api.local_datasouce;


import com.devappsys.log.Context;
import com.devappsys.log.Event;
import com.devappsys.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.devappsys.grpc_logs.api.data_models.LogModel;
import com.devappsys.grpc_logs.api.data_models.EventModel;
import com.devappsys.grpc_logs.api.data_models.ContextModel;
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

    /**
     * Get All Counts of stored data
     */
    long getLogsCount();
    long getEventsCount();
    long getContextsCount();

    /**
     * Rotate files if they exceed a certain size
     */
    void rotateLogsFile() throws IOException;
    void rotateEventsFile() throws IOException;
    void rotateContextsFile()throws IOException;
    /** Get all stored data to protobuf */
    Map<String,Log.LogBatch> getAllLogs();
    Map<String,Event.EventBatch> getAllEvents();
    Map<String,Context.ContextBatch> getAllContexts();
    /** Emergency rotate is method used for rotating files without even considering the file size  */
    void rotateEmergency();
}
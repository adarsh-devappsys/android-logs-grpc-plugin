package com.devappsys.logs_grpc.local_datasouce;

import android.content.Context;
import android.os.Build;

import com.devappsys.logs_grpc.models.data.ContextModel;
import com.devappsys.logs_grpc.models.data.EventModel;
import com.devappsys.logs_grpc.models.data.LogModel;
import com.devappsys.log.ContextOuterClass;
import com.devappsys.log.Event;
import com.devappsys.log.Log;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class FileDatasourceImpl implements LocalDatasourceRepo {

    private static final String LOG_FILE = "logs_data.dat";
    private static final String EVENT_FILE = "events_data.dat";
    private static final String CONTEXT_FILE = "contexts_data.dat";

    private final Context mContext;

    // Constructor accepts context
    public FileDatasourceImpl(Context context) {
        this.mContext = context;
    }

    @Override
    public void saveLog(LogModel logModel) {
        // Convert LogModel to Protobuf LogMessage and write to file
        Log.LogMessage logMessage = logModel.toProtobuf();
        writeToFile(LOG_FILE, logMessage.toByteArray());
    }

    @Override
    public void saveEvent(EventModel eventModel) {
        // Convert EventModel to Protobuf EventMessage and write to file
        Event.EventMessage eventMessage = eventModel.toProtobuf();
        writeToFile(EVENT_FILE, eventMessage.toByteArray());
    }

    @Override
    public void saveContext(ContextModel contextModel) {
        // Convert ContextModel to Protobuf Context and write to file
        ContextOuterClass.Context context = contextModel.toProtobuf();
        writeToFile(CONTEXT_FILE, context.toByteArray());
    }



    // Helper method to write a byte array to a file in internal storage
    private void writeToFile(String filename, byte[] data) {
        FileOutputStream fos = null;
        try {
            fos = mContext.openFileOutput(filename, Context.MODE_APPEND);
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Log.LogList getLogs() {
        byte[] data = readFromFile(LOG_FILE);
        if (data.length == 0) {
            return Log.LogList.getDefaultInstance();  // Return empty LogList
        }

        try {
            return Log.LogList.parseFrom(data);  // Parse and return Log.LogList
        } catch (IOException e) {
            e.printStackTrace();
            return Log.LogList.getDefaultInstance();  // Return default empty LogList on error
        }
    }

    @Override
    public Event.EventList getEvents() {
        byte[] data = readFromFile(EVENT_FILE);
        if (data.length == 0) {
            return Event.EventList.getDefaultInstance();  // Return empty EventList
        }

        try {
            return Event.EventList.parseFrom(data);  // Parse and return Event.EventList
        } catch (IOException e) {
            e.printStackTrace();
            return Event.EventList.getDefaultInstance();  // Return default empty EventList on error
        }
    }

    @Override
    public ContextOuterClass.ContextList getContexts() {
        byte[] data = readFromFile(CONTEXT_FILE);
        if (data.length == 0) {
            return ContextOuterClass.ContextList.getDefaultInstance();  // Return empty ContextList
        }

        try {
            return ContextOuterClass.ContextList.parseFrom(data);  // Parse and return ContextList
        } catch (IOException e) {
            e.printStackTrace();
            return ContextOuterClass.ContextList.getDefaultInstance();  // Return default empty ContextList on error
        }
    }

    // Helper method to read byte data from a file in internal storage
    private byte[] readFromFile(String filename) {
        FileInputStream fis = null;
        try {
            fis = mContext.openFileInput(filename);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return fis.readAllBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];  // Return empty byte array on error
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new byte[0];
    }
}
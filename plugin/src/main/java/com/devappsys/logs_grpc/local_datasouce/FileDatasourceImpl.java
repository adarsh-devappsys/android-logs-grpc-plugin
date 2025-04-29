package com.devappsys.logs_grpc.local_datasouce;

import android.content.Context;

import com.devappsys.logs_grpc.models.data.ContextModel;
import com.devappsys.logs_grpc.models.data.EventModel;
import com.devappsys.logs_grpc.models.data.LogModel;
import com.devappsys.log.ContextOuterClass;
import com.devappsys.log.Event;
import com.devappsys.log.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileDatasourceImpl implements LocalDatasourceRepo {

    private static final String ROOT_DIR = "logs_storage";
    private static final String LOG_DIR = "logs";
    private static final String EVENT_DIR = "events";
    private static final String CONTEXT_DIR = "contexts";

    private static final String LOG_FILE = "logs_data.dat";
    private static final String EVENT_FILE = "events_data.dat";
    private static final String CONTEXT_FILE = "contexts_data.dat";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final Context mContext;

    private BufferedOutputStream logOutputStream;
    private BufferedOutputStream eventOutputStream;
    private BufferedOutputStream contextOutputStream;

    private static FileDatasourceImpl instance;

    public static synchronized FileDatasourceImpl getInstance(Context context) {
        if (instance == null) {
            instance = new FileDatasourceImpl(context);
        }
        return instance;
    }
    private FileDatasourceImpl(Context context) {
        this.mContext = context;
        initStorage();
    }

    private void initStorage() {
        try {
            File root = new File(mContext.getFilesDir(), ROOT_DIR);
            if (!root.exists()) root.mkdirs();

            File logDir = new File(root, LOG_DIR);
            if (!logDir.exists()) logDir.mkdirs();

            File eventDir = new File(root, EVENT_DIR);
            if (!eventDir.exists()) eventDir.mkdirs();

            File contextDir = new File(root, CONTEXT_DIR);
            if (!contextDir.exists()) contextDir.mkdirs();

            logOutputStream = new BufferedOutputStream(new FileOutputStream(new File(logDir, LOG_FILE), true));
            eventOutputStream = new BufferedOutputStream(new FileOutputStream(new File(eventDir, EVENT_FILE), true));
            contextOutputStream = new BufferedOutputStream(new FileOutputStream(new File(contextDir, CONTEXT_FILE), true));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getDir(String subDir) {
        return new File(new File(mContext.getFilesDir(), ROOT_DIR), subDir);
    }

    private File getLogFile() {
        return new File(getDir(LOG_DIR), LOG_FILE);
    }

    private File getEventFile() {
        return new File(getDir(EVENT_DIR), EVENT_FILE);
    }

    private File getContextFile() {
        return new File(getDir(CONTEXT_DIR), CONTEXT_FILE);
    }

    @Override
    public void saveLog(LogModel logModel) {
        try {
            logOutputStream.write(logModel.toProtobuf().toByteArray());
            logOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveEvent(EventModel eventModel) {
        try {
            if (getEventFile().length() >= MAX_FILE_SIZE) {
                rotateEventFile();
            }
            eventOutputStream.write(eventModel.toProtobuf().toByteArray());
            eventOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveContext(ContextModel contextModel) {
        try {
            contextOutputStream.write(contextModel.toProtobuf().toByteArray());
            contextOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(){
        try {
            if (logOutputStream != null) {
                logOutputStream.close();
            }
            if (eventOutputStream != null) {
                eventOutputStream.close();
            }
            if (contextOutputStream != null) {
                contextOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasLogsToUpload() {
        File[] files = getDir(LOG_DIR).listFiles((dir, name) ->
                name.startsWith("logs_data_") && !name.equals(LOG_FILE));
        return files != null && files.length > 0;
    }

    @Override
    public boolean hasEventsToUpload() {
        File[] files = getDir(EVENT_DIR).listFiles((dir, name) ->
                name.startsWith("events_data_") && !name.equals(EVENT_FILE));
        return files != null && files.length > 0;
    }

    @Override
    public boolean hasContextsToUpload() {
        File[] files = getDir(CONTEXT_DIR).listFiles((dir, name) ->
                name.startsWith("contexts_data_") && !name.equals(CONTEXT_FILE));
        return files != null && files.length > 0;
    }

    @Override
    public List<File> getLogsFile() {
        List<File> files = new ArrayList<>();

        File[] allFiles = getDir(LOG_DIR).listFiles((dir, name) ->
                name.startsWith("logs_data_") && !name.equals(LOG_FILE));

        if (allFiles != null) {
            Collections.addAll(files, allFiles);
        }

        return files;
    }

    @Override
    public List<File> getEventsFile() {
        List<File> files = new ArrayList<>();

        File[] allFiles = getDir(EVENT_DIR).listFiles((dir, name) ->
                name.startsWith("events_data_") && !name.equals(EVENT_FILE));

        if (allFiles != null) {
            Collections.addAll(files, allFiles);
        }

        return files;
    }

    @Override
    public List<File> getContextsFile() {
        List<File> files = new ArrayList<>();

        File[] allFiles = getDir(CONTEXT_DIR).listFiles((dir, name) ->
                name.startsWith("contexts_data_") && !name.equals(CONTEXT_FILE));

        if (allFiles != null) {
            Collections.addAll(files, allFiles);
        }

        return files;
    }

    @Override
    public void deleteLogsFile(String fileName) {
        File file = new File(getDir(LOG_DIR), fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void deleteEventsFile(String fileName) {
        File file = new File(getDir(EVENT_DIR), fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void deleteContextsFile(String fileName) {
        File file = new File(getDir(CONTEXT_DIR), fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void deleteAllLogs() {
        File[] files = getDir(LOG_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Override
    public void deleteAllEvents() {
        File[] files = getDir(EVENT_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Override
    public void deleteAllContexts() {
        File[] files = getDir(CONTEXT_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Override
    public void rotateEmergency() {
        try {
            rotateContextsFile();
            rotateLogsFile();
            rotateEventFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rotateEventFile() throws IOException {
        if (eventOutputStream != null) {
            eventOutputStream.close();
        }

        File current = getEventFile();
        String newName = "events_data_" + System.currentTimeMillis() + ".dat";
        File renamed = new File(getDir(EVENT_DIR), newName);
        current.renameTo(renamed);

        eventOutputStream = new BufferedOutputStream(new FileOutputStream(current, false)); // Start new file
    }

    private void rotateLogsFile()throws IOException{
        if (logOutputStream != null) {
            logOutputStream.close();
        }

        File current = getLogFile();
        String newName = "logs_data_" + System.currentTimeMillis() + ".dat";
        File renamed = new File(getDir(LOG_DIR), newName);
        current.renameTo(renamed);

        logOutputStream = new BufferedOutputStream(new FileOutputStream(current, false)); // Start new file
    }

    private void rotateContextsFile()throws IOException{
        if (contextOutputStream != null) {
            contextOutputStream.close();
        }

        File current = getContextFile();
        String newName = "contexts_data_" + System.currentTimeMillis() + ".dat";
        File renamed = new File(getDir(CONTEXT_DIR), newName);
        current.renameTo(renamed);

        contextOutputStream = new BufferedOutputStream(new FileOutputStream(current, false)); // Start new file
    }
}
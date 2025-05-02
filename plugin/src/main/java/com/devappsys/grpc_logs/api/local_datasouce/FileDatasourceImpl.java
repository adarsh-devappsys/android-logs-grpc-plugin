package com.devappsys.grpc_logs.api.local_datasouce;

import android.content.Context;

import com.devappsys.log.Event;
import com.devappsys.log.Log;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devappsys.grpc_logs.api.data_models.LogModel;
import com.devappsys.grpc_logs.api.data_models.EventModel;
import com.devappsys.grpc_logs.api.data_models.ContextModel;
import com.google.protobuf.CodedInputStream;

public class FileDatasourceImpl implements LocalDatasourceRepo {

    private static final String ROOT_DIR = "logs_storage";
    private static final String LOG_DIR = "logs";
    private static final String EVENT_DIR = "events";
    private static final String CONTEXT_DIR = "contexts";

    private static final String LOG_FILE = "logs_data.dat";
    private static final String EVENT_FILE = "events_data.dat";
    private static final String CONTEXT_FILE = "contexts_data.dat";

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
    public synchronized void saveLog(LogModel logModel) {
        try {
            logOutputStream.write(logModel.toProtobuf().toByteArray());
            logOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void saveEvent(EventModel eventModel) {
        try {
            eventOutputStream.write(eventModel.toProtobuf().toByteArray());
            eventOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void saveContext(ContextModel contextModel) {
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
    public synchronized void deleteLogsFile(String fileName) {
        new File(getDir(LOG_DIR), fileName).delete();
    }

    @Override
    public synchronized void deleteEventsFile(String fileName) {
        new File(getDir(EVENT_DIR), fileName).delete();
    }

    @Override
    public synchronized void deleteContextsFile(String fileName) {
        new File(getDir(CONTEXT_DIR), fileName).delete();
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
    public long getLogsCount() {
        List<File> files = new ArrayList<>(getLogsFile()); // rotated files
//        files.add(getLogFile());                           // current active file
        return countProtobufMessages(files, Log.LogMessage.parser());
    }

    @Override
    public long getEventsCount() {
        List<File> files = new ArrayList<>(getEventsFile());
//        files.add(getEventFile());
        return countProtobufMessages(files, Event.EventMessage.parser());
    }

    @Override
    public long getContextsCount() {
        List<File> files = new ArrayList<>(getContextsFile());
//        files.add(getContextFile());
        return countProtobufMessages(files, com.devappsys.log.Context.ContextMessage.parser());
    }

    // === Generic parser for any protobuf list ===
    private <T extends com.google.protobuf.Message> long countProtobufMessages(List<File> files, com.google.protobuf.Parser<T> parser) {
        long count = 0;
        for (File file : files) {
            try (InputStream in = new FileInputStream(file)) {
                CodedInputStream codedInput = CodedInputStream.newInstance(in);
                while (!codedInput.isAtEnd()) {
                    int length = codedInput.readRawVarint32(); // read message size
                    codedInput.readRawBytes(length);           // skip message
                    count++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public Map<String, Log.LogBatch> getAllLogs() {
        Map<String, Log.LogBatch> fileToLogBatchMap = new HashMap<>();
        List<File> files = getLogsFile(); // Rotated log files

        for (File file : files) {
            List<Log.LogMessage> messages = new ArrayList<>();

            try (FileInputStream fis = new FileInputStream(file)) {
                CodedInputStream codedInput = CodedInputStream.newInstance(fis);
                while (!codedInput.isAtEnd()) {
                    int size = codedInput.readRawVarint32();
                    byte[] data = codedInput.readRawBytes(size);
                    Log.LogMessage logMessage = Log.LogMessage.parser().parseFrom(data);
                    messages.add(logMessage);
                }
                Log.LogBatch batch = Log.LogBatch.newBuilder().addAllLogs(messages).build();
                fileToLogBatchMap.put(file.getName(), batch);
            } catch (IOException e) {
                e.printStackTrace(); // Handle errors properly
            }
        }

        return fileToLogBatchMap;
    }

    @Override
    public Map<String, Event.EventBatch> getAllEvents() {
        Map<String, Event.EventBatch> fileToEventBatchMap = new HashMap<>();
        List<File> files = getEventsFile(); // Rotated event files

        for (File file : files) {
            List<Event.EventMessage> messages = new ArrayList<>();

            try (FileInputStream fis = new FileInputStream(file)) {
                CodedInputStream codedInput = CodedInputStream.newInstance(fis);
                while (!codedInput.isAtEnd()) {
                    int size = codedInput.readRawVarint32();
                    byte[] data = codedInput.readRawBytes(size);
                    Event.EventMessage event = Event.EventMessage.parser().parseFrom(data);
                    messages.add(event);
                }
                Event.EventBatch batch = Event.EventBatch.newBuilder().addAllEvents(messages).build();
                fileToEventBatchMap.put(file.getName(), batch);
            } catch (IOException e) {
                e.printStackTrace(); // Handle as needed
            }
        }

        return fileToEventBatchMap;
    }
    @Override
    public Map<String, com.devappsys.log.Context.ContextBatch> getAllContexts() {
        Map<String, com.devappsys.log.Context.ContextBatch> fileToContextBatchMap = new HashMap<>();
        List<File> files = getContextsFile(); // Rotated context files

        for (File file : files) {
            List<com.devappsys.log.Context.ContextMessage> messages = new ArrayList<>();

            try (FileInputStream fis = new FileInputStream(file)) {
                CodedInputStream codedInput = CodedInputStream.newInstance(fis);
                while (!codedInput.isAtEnd()) {
                    int size = codedInput.readRawVarint32();
                    byte[] data = codedInput.readRawBytes(size);
                    com.devappsys.log.Context.ContextMessage context =
                            com.devappsys.log.Context.ContextMessage.parser().parseFrom(data);
                    messages.add(context);
                }
                com.devappsys.log.Context.ContextBatch batch =
                        com.devappsys.log.Context.ContextBatch.newBuilder().addAllContexts(messages).build();
                fileToContextBatchMap.put(file.getName(), batch);
            } catch (IOException e) {
                e.printStackTrace(); // Handle properly
            }
        }

        return fileToContextBatchMap;
    }


    private <T extends com.google.protobuf.Message> List<T> parseMessages(
            List<File> files,
            com.google.protobuf.Parser<T> parser
    ) {
        List<T> messages = new ArrayList<>();
        List<File> allFiles = new ArrayList<>(files);

        for (File file : allFiles) {
            try (FileInputStream fis = new FileInputStream(file)) {
                CodedInputStream codedInput = CodedInputStream.newInstance(fis);
                while (!codedInput.isAtEnd()) {
                    int size = codedInput.readRawVarint32();
                    byte[] data = codedInput.readRawBytes(size);
                    T message = parser.parseFrom(data);
                    messages.add(message);
                }
            } catch (IOException e) {
                e.printStackTrace(); // Log it properly in production
            }
        }
        return messages;
    }
    @Override
    public void rotateEmergency() {
        try {
            rotateContextsFile();
            rotateLogsFile();
            rotateEventsFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rotateEventsFile() throws IOException {
        if (eventOutputStream != null) {
            eventOutputStream.close();
        }

        File current = getEventFile();
        String newName = "events_data_" + System.currentTimeMillis() + ".dat";
        File renamed = new File(getDir(EVENT_DIR), newName);
        current.renameTo(renamed);

        eventOutputStream = new BufferedOutputStream(new FileOutputStream(current, false)); // Start new file


    }

    public void rotateLogsFile() throws IOException {
        if (logOutputStream != null) {
            logOutputStream.close();
        }

        File current = getLogFile();
        String newName = "logs_data_" + System.currentTimeMillis() + ".dat";
        File renamed = new File(getDir(LOG_DIR), newName);
        current.renameTo(renamed);

        logOutputStream = new BufferedOutputStream(new FileOutputStream(current, false)); // Start new file


    }

    public void rotateContextsFile()throws IOException{
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
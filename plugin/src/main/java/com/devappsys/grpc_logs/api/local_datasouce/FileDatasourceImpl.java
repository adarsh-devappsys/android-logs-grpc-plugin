package com.devappsys.grpc_logs.api.local_datasouce;

import android.content.Context;

import com.devappsys.grpc_logs.util.Logger;
import com.devappsys.log.Event;
import com.devappsys.log.Log;
import com.devappsys.grpc_logs.api.data_models.LogModel;
import com.devappsys.grpc_logs.api.data_models.EventModel;
import com.devappsys.grpc_logs.api.data_models.ContextModel;
import com.google.protobuf.CodedInputStream;

import java.io.*;
import java.util.*;

public class FileDatasourceImpl implements LocalDatasourceRepo {

    private static final String ROOT_DIR = "logs_storage";
    private static final String LOG_DIR = "logs";
    private static final String EVENT_DIR = "events";
    private static final String CONTEXT_DIR = "contexts";

    private static final String LOG_FILE = "logs_data.bin";
    private static final String EVENT_FILE = "events_data.bin";
    private static final String CONTEXT_FILE = "contexts_data.bin";

    private final Context mContext;

    private BufferedOutputStream logOutputStream;
    private BufferedOutputStream eventOutputStream;
    private BufferedOutputStream contextOutputStream;

    private static FileDatasourceImpl instance;
    private  Logger logger;

    public static synchronized FileDatasourceImpl getInstance(Context context) {
        if (instance == null) {
            instance = new FileDatasourceImpl(context);
        }
        return instance;
    }

    private FileDatasourceImpl(Context context) {
        this.mContext = context;
        this.logger = new Logger(true);
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

    // === Method Implementations ===

    @Override
    public synchronized void saveLog(LogModel logModel) {
        try {
            if (logOutputStream == null) {
                logOutputStream = new BufferedOutputStream(new FileOutputStream(getLogFile(), true));
            }
            logModel.toProtobuf().writeDelimitedTo(logOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void saveEvent(EventModel eventModel) {
        try {
            if (eventOutputStream == null) {
                eventOutputStream = new BufferedOutputStream(new FileOutputStream(getEventFile(), true));
            }
           eventModel.toProtobuf().writeDelimitedTo(eventOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void saveContext(ContextModel contextModel) {
        try {
            if (contextOutputStream == null) {
                contextOutputStream = new BufferedOutputStream(new FileOutputStream(getContextFile(), true));
            }
           contextModel.toProtobuf().writeDelimitedTo(contextOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void cleanup(){
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

    // === Log/Events/Context Checking Methods ===
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
        return getFilesFromDir(LOG_DIR, "logs_data_", LOG_FILE);
    }

    @Override
    public List<File> getEventsFile() {
        return getFilesFromDir(EVENT_DIR, "events_data_", EVENT_FILE);
    }

    @Override
    public List<File> getContextsFile() {
        return getFilesFromDir(CONTEXT_DIR, "contexts_data_", CONTEXT_FILE);
    }

    @Override
    public synchronized void deleteLogsFile(String fileName) {
        File logFile = new File(getDir(LOG_DIR), fileName);
        if (logFile.exists()&&!logFile.delete()) {
            logger.e("Failed to delete log file: " + fileName);
        }
    }

    @Override
    public synchronized void deleteEventsFile(String fileName) {
        File eventFile = new File(getDir(EVENT_DIR), fileName);
       if (eventFile.exists() && !eventFile.delete()) {
            logger.e("Failed to delete event file: " + fileName);
        }
    }

    @Override
    public synchronized void deleteContextsFile(String fileName) {
        File contextFile = new File(getDir(CONTEXT_DIR), fileName);
        if (contextFile.exists() && !contextFile.delete()) {
            logger.e("Failed to delete context file: " + fileName);
        }
    }

    @Override
    public void deleteAllLogs() {
        File[] logFiles = getDir(LOG_DIR).listFiles();
        if (logFiles != null) {
            for (File file : logFiles) {
                file.delete();
            }
        }
    }

    @Override
    public void deleteAllEvents() {
        File[] eventFiles = getDir(EVENT_DIR).listFiles();
        if (eventFiles != null) {
            for (File file : eventFiles) {
                file.delete();
            }
        }
    }

    @Override
    public void deleteAllContexts() {
        File[] contextFiles = getDir(CONTEXT_DIR).listFiles();
        if (contextFiles != null) {
            for (File file : contextFiles) {
                file.delete();
            }
        }
    }

    @Override
    public long getLogsCount() {
        List<File> files = new ArrayList<>(getLogsFile());
        return countProtobufMessages(files, Log.LogMessage.parser());
    }

    @Override
    public long getEventsCount() {
        List<File> files = new ArrayList<>(getEventsFile());
        return countProtobufMessages(files, Event.EventMessage.parser());
    }

    @Override
    public long getContextsCount() {
        List<File> files = new ArrayList<>(getContextsFile());
        return countProtobufMessages(files, com.devappsys.log.Context.ContextMessage.parser());
    }

    // === Rotation Methods ===
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
    public synchronized void rotateLogsFile() throws IOException {
        if (logOutputStream != null) {
            logOutputStream.close();
        }

        File current = getLogFile();
        if (current.length() == 0) {
            logOutputStream = new BufferedOutputStream(new FileOutputStream(current, true));
            return;
        }

        String newName = "logs_data_" + System.currentTimeMillis() + ".bin";
        File renamed = new File(getDir(LOG_DIR), newName);
        if (current.renameTo(renamed)) {
            logOutputStream = new BufferedOutputStream(new FileOutputStream(current, false));
        } else {
            throw new IOException("Failed to rotate log file");
        }
    }

    @Override
    public void rotateEventsFile() throws IOException {
        if (eventOutputStream != null) {
            eventOutputStream.close();
        }

        File current = getEventFile();
        if (current.length() == 0) {
            eventOutputStream = new BufferedOutputStream(new FileOutputStream(current, true));
            return;
        }
        String newName = "events_data_" + System.currentTimeMillis() + ".bin";
        File renamed = new File(getDir(EVENT_DIR), newName);
        if(current.renameTo(renamed)) {
            eventOutputStream = new BufferedOutputStream(new FileOutputStream(current, false));
        } else {
            throw new IOException("Failed to rotate event file");
        }

    }

    @Override
    public void rotateContextsFile() throws IOException {
        if (contextOutputStream != null) {
            contextOutputStream.close();
        }
        File current = getContextFile();
        if (current.length() == 0) {
            contextOutputStream = new BufferedOutputStream(new FileOutputStream(current, true));
            return;
        }
        String newName = "contexts_data_" + System.currentTimeMillis() + ".bin";
        File renamed = new File(getDir(CONTEXT_DIR), newName);
        if(current.renameTo(renamed)) {
            contextOutputStream = new BufferedOutputStream(new FileOutputStream(current, false));
        } else {
            throw new IOException("Failed to rotate context file");
        }
    }

    @Override
    public Map<String, Log.LogBatch> getAllLogs() {
        Map<String, Log.LogBatch> logBatchMap = new HashMap<>();

        File logsDir = getDir(LOG_DIR);  // Fix: define the directory first

        if (!logsDir.exists() || !logsDir.isDirectory()) {
            return logBatchMap;
        }

        File[] files = logsDir.listFiles((dir, name) -> name.startsWith("logs_data_") && !name.equals(LOG_FILE));
        if (files == null) return logBatchMap;

        for (File file : files) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Log.LogBatch.Builder builder = Log.LogBatch.newBuilder();
                Log.LogMessage log;
                while ((log = Log.LogMessage.parseDelimitedFrom(fis)) != null) {
                    builder.addLogs(log);
                }
                logBatchMap.put(file.getName(), builder.build());
            } catch (IOException e) {
                logger.e("Failed to parse logs from file: " + file.getAbsolutePath());
            }
        }

        return logBatchMap;
    }

    @Override
    public Map<String, Event.EventBatch> getAllEvents() {
        Map<String, Event.EventBatch> eventBatchMap = new HashMap<>();

        File eventsDir = getDir(EVENT_DIR);  // Fix: define the directory first

        if (!eventsDir.exists() || !eventsDir.isDirectory()) {
            return eventBatchMap;
        }

        File[] files = eventsDir.listFiles((dir, name) -> name.startsWith("events_data_") && !name.equals(EVENT_FILE));
        if (files == null) return eventBatchMap;

        for (File file : files) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Event.EventBatch.Builder builder = Event.EventBatch.newBuilder();
                Event.EventMessage event;
                while ((event = Event.EventMessage.parseDelimitedFrom(fis)) != null) {
                    builder.addEvents(event);
                }
                eventBatchMap.put(file.getName(), builder.build());
            } catch (IOException e) {
                logger.e("Failed to parse events from file: " + file.getAbsolutePath());
            }
        }

        return eventBatchMap;
    }

    @Override
    public Map<String, com.devappsys.log.Context.ContextBatch> getAllContexts() {
        Map<String, com.devappsys.log.Context.ContextBatch> contextBatchMap = new HashMap<>();

        File contextsDir = getDir(CONTEXT_DIR);  // Fix: define the directory first

        if (!contextsDir.exists() || !contextsDir.isDirectory()) {
            return contextBatchMap;
        }

        File[] files = contextsDir.listFiles((dir, name) -> name.startsWith("contexts_data_") && !name.equals(CONTEXT_FILE));
        if (files == null) return contextBatchMap;

        for (File file : files) {
            try (FileInputStream fis = new FileInputStream(file)) {
                com.devappsys.log.Context.ContextBatch.Builder builder = com.devappsys.log.Context.ContextBatch.newBuilder();
                com.devappsys.log.Context.ContextMessage context;
                while ((context = com.devappsys.log.Context.ContextMessage.parseDelimitedFrom(fis)) != null) {
                    builder.addContexts(context);
                }
                contextBatchMap.put(file.getName(), builder.build());
            } catch (IOException e) {
                logger.e("Failed to parse context from file: " + file.getAbsolutePath());
            }
        }

        return contextBatchMap;
    }

    // === Helper Methods ===

    private List<File> getFilesFromDir(String subDir, String prefix, String mainFile) {
        List<File> files = new ArrayList<>();
        File[] allFiles = getDir(subDir).listFiles((dir, name) -> name.startsWith(prefix) && !name.equals(mainFile));
        if (allFiles != null) {
            Collections.addAll(files, allFiles);
        }
        return files;
    }

    private File getDir(String subDir) {
        File dir = new File(new File(mContext.getFilesDir(), ROOT_DIR), subDir);
        if (!dir.exists() && !dir.mkdirs()) {
            logger.e("Failed to create directory: " + dir.getAbsolutePath());
        }
        return dir;
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

    // === Protobuf Parsing Helper ===
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
}
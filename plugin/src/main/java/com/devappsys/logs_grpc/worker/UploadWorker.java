package com.devappsys.logs_grpc.worker;

import static com.devappsys.logs_grpc.LogsPlugin.KEY_CLIENT_ID;
import static com.devappsys.logs_grpc.LogsPlugin.KEY_HOST;
import static com.devappsys.logs_grpc.LogsPlugin.KEY_PACKAGE_NAME;
import static com.devappsys.logs_grpc.LogsPlugin.KEY_PORT;
import static com.devappsys.logs_grpc.LogsPlugin.PREF_NAME;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.devappsys.log.ContextOuterClass;
import com.devappsys.logs_grpc.LogsPlugin;
import com.devappsys.logs_grpc.grpc.GrpcClient;
import com.devappsys.logs_grpc.local_datasouce.LocalDatasourceRepo;
import com.devappsys.logs_grpc.models.Configuration;
import com.google.protobuf.ByteString;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class UploadWorker extends Worker {

    private boolean isEmergency;
    public static final String IS_EMERGENCY = "is_emergency";
    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ForegroundInfo getForegroundInfo() {
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "log_channel")
                .setContentTitle("Syncing logs")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .build();
        return new ForegroundInfo(1001, notification);
    }

    @NonNull
    @Override
    public Result doWork() {
        isEmergency = getInputData().getBoolean(IS_EMERGENCY,false);
        // check if logs plugin is initialized
        LogsPlugin logsPlugin;
        if (!LogsPlugin.isInitialized()) {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

            LogsPlugin.init(getApplicationContext(), new Configuration(
                    prefs.getString(KEY_HOST, ""),
                    prefs.getInt(KEY_PORT, 50051),
                    prefs.getString(KEY_PACKAGE_NAME, ""),
                    prefs.getString(KEY_CLIENT_ID, ""),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ));
        }
        logsPlugin = LogsPlugin.getInstance();

        GrpcClient grpcClient = logsPlugin.getGrpcClient();
        LocalDatasourceRepo localDatasourceRepo = logsPlugin.getLocalDatasourceRepo();
        String sessionId = logsPlugin.getSessionId();
        if(isEmergency){
            localDatasourceRepo.rotateEmergency();
        }
        if (localDatasourceRepo.hasContextsToUpload()) {
            List<File> contextFiles = localDatasourceRepo.getContextsFile();
            for (File file : contextFiles) {
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    ByteString fileContent = ByteString.copyFrom(fileBytes);
                    ContextOuterClass.ContextFileRequest contextFile = ContextOuterClass.ContextFileRequest.newBuilder()
                            .setSessionID(sessionId)
                            .setFileName(file.getName())
                            .setFileContent(fileContent)
                            .build();
                    ContextOuterClass.Response response=  grpcClient.sendContextFile(contextFile);
                    if(response.getSuccess()) {
                        localDatasourceRepo.deleteContextsFile(file.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (localDatasourceRepo.hasLogsToUpload()) {
            List<File> logFiles = localDatasourceRepo.getLogsFile();
            for (File file : logFiles) {
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    ByteString fileContent = ByteString.copyFrom(fileBytes);
                    com.devappsys.log.Log.LogFileRequest logFile = com.devappsys.log.Log.LogFileRequest.newBuilder()
                            .setSessionID(sessionId)
                            .setFileName(file.getName())
                            .setFileContent(fileContent)
                            .build();
                    ContextOuterClass.Response response=   grpcClient.sendLogFile(logFile);
                    if(response.getSuccess()) {
                        localDatasourceRepo.deleteLogsFile(file.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (localDatasourceRepo.hasEventsToUpload()) {
            List<File> eventFiles = localDatasourceRepo.getEventsFile();
            for (File file : eventFiles) {
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    ByteString fileContent = ByteString.copyFrom(fileBytes);
                    com.devappsys.log.Event.EventFileRequest eventFile = com.devappsys.log.Event.EventFileRequest.newBuilder()
                            .setSessionID(sessionId)
                            .setFileName(file.getName())
                            .setFileContent(fileContent)
                            .build();
                  ContextOuterClass.Response response= grpcClient.sendEventFile(eventFile);
                  if(response.getSuccess()) {
                      localDatasourceRepo.deleteEventsFile(file.getName());
                  }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return Result.success();
    }
}

package com.cmss.jffmpeg.cli;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FfmpegTask {

    private final Logger logger = LoggerFactory.getLogger(FfmpegTask.class);

    private ProcessBuilder pb;
    private Process process;
    private OutmsgHandler msgReader;
    private volatile boolean isRunning;

    private CameraInfo cameraInfo;
    private String taskId;
    private String steamUri;

    public FfmpegTask(CameraInfo cameraInfo, TaskArgs args) {
        this.cameraInfo = cameraInfo;

        List<String> cmd = new ArrayList<String>();
        cmd.add(args.getFfmpegPath());
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(args.getRtspUri());
        cmd.add("-c:v");
        cmd.add("copy");
        cmd.add("-c:a");
        cmd.add("copy");
        cmd.add("-f");
        cmd.add("flv");
        cmd.add("-s");
        cmd.add(args.getResolution());
        String pushUri = args.getPushUri();
        if (pushUri != null && pushUri.endsWith("/")) {
            pushUri += cameraInfo.getCamId();
        } else {
            pushUri += "/" + cameraInfo.getCamId();
        }
        cmd.add(pushUri);
        steamUri = pushUri;
        pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        // Task ID
        if (StringUtils.isBlank(cameraInfo.getIp4Addr())) {
            throw new RuntimeException("相机参数不完整：ip4地址不能为空");
        }
        this.taskId = generateTaskId(cameraInfo.getIp4Addr(), args.getResolution());
    }

    public static String generateTaskId(String camIp, String resolution) {
        // 可以做MD5
        return camIp + "@" + resolution;
    }

    public static TaskArgs argsBuilder() {
        return new TaskArgs();
    }

    public void start() {
        if (isRunning) {
            return;
        }
        try {
            process = pb.start();
            isRunning = true;

            msgReader = new OutmsgHandler(process.getInputStream());
            new Thread(msgReader).start();
        } catch (IOException e) {
            logger.error(Throwables.getStackTraceAsString(e));
            stop();
        }
    }

    public void stop() {
        if (process != null) {
            if (msgReader != null) {
                msgReader.stop();
            }
            if (isRunning) {
                process.destroy();
                isRunning = false;
            }
        }
    }

    public CameraInfo getCameraInfo() {
        return cameraInfo;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getSteamUri() {
        return steamUri;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 任务参数
     */
    public static class TaskArgs {
        private String ffmpegPath;
        private String rtspUri;
        private String resolution;
        private String pushUri;

        public String getFfmpegPath() {
            return ffmpegPath;
        }

        public TaskArgs setFfmpegPath(String ffmpegPath) {
            this.ffmpegPath = ffmpegPath;
            return this;
        }

        public String getRtspUri() {
            return rtspUri;
        }

        public TaskArgs setRtspUri(String rtspUri) {
            this.rtspUri = rtspUri;
            return this;
        }

        public String getResolution() {
            return resolution;
        }

        public TaskArgs setResolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        public String getPushUri() {
            return pushUri;
        }

        public TaskArgs setPushUri(String pushUri) {
            this.pushUri = pushUri;
            return this;
        }
    }
}
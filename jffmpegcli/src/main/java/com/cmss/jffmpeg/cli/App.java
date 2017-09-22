package com.cmss.jffmpeg.cli;

import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) throws InterruptedException {
        FfmpegTaskManager ffmpegMgr = new FfmpegTaskManager();

        CameraInfo camInfo = new CameraInfo();
        camInfo.setIp4Addr("192.168.0.14");
        camInfo.setCamId("cam01");
        camInfo.setCamName("cam01");
        camInfo.setRtstUri("rtsp://192.168.0.14/media/video1");

        FfmpegTask.TaskArgs taskArgs = FfmpegTask.argsBuilder();
        taskArgs.setFfmpegPath("/usr/bin/ffmpeg")
                .setRtspUri("rtsp://192.168.0.14/media/video1")
                .setResolution("1920*1080")
                .setPushUri("rtmp://192.168.0.10:1935/myapp");


        FfmpegTask ffmpegTask = new FfmpegTask(camInfo, taskArgs);
        ffmpegMgr.execute(ffmpegTask);

        System.out.println("----------------------------------------------------");
        System.out.println(JSON.toJSONString(ffmpegMgr.getTaskMap()));
        System.out.println(JSON.toJSONString(ffmpegMgr.getIp4Addrs()));

        TimeUnit.SECONDS.sleep(30);

        ffmpegMgr.destroy(ffmpegTask.getTaskId());
        System.out.println("----------------------------------------------------");
        System.out.println(JSON.toJSONString(ffmpegMgr.getTaskMap()));
        System.out.println(JSON.toJSONString(ffmpegMgr.getIp4Addrs()));

        TimeUnit.SECONDS.sleep(10);
        System.out.println("app stopped...");
    }
}

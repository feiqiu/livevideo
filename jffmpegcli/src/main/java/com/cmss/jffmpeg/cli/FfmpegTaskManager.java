package com.cmss.jffmpeg.cli;

import java.util.*;

public class FfmpegTaskManager {

    private Map<String, FfmpegTask> taskMap = new HashMap<String, FfmpegTask>();
    private Set<String> ip4Addrs = new HashSet<String>();

    /**
     * 执行任务
     * @param task
     */
    public void execute(FfmpegTask task) {
        // stop and remove old task if exists
        FfmpegTask oldTask = taskMap.get(task.getTaskId());
        if (oldTask != null) {
            oldTask.stop();
        }

        // start and add task
        task.start();
        taskMap.put(task.getTaskId(), task);
        ip4Addrs.add(task.getCameraInfo().getIp4Addr());
    }

    /**
     * 销毁任务
     * @param taskId
     */
    public void destroy(String taskId) {
        FfmpegTask task = taskMap.get(taskId);
        if (task != null) {
            task.stop();
            taskMap.remove(taskId);
            ip4Addrs.remove(task.getCameraInfo().getIp4Addr());
        }
    }

    /**
     * 通过相机IP销毁任务
     * @param camIp4Addr
     */
    public void destroyByCamIp(String camIp4Addr) {
        boolean exist = ip4Addrs.contains(camIp4Addr);
        if (exist) {
            Iterator<String> itor = taskMap.keySet().iterator();
            while (itor.hasNext()) {
                String taskId = itor.next();
                if (taskId.startsWith(camIp4Addr + "@")) {
                    destroy(taskId);
                }
            }
            ip4Addrs.remove(camIp4Addr);
        }
    }

    public FfmpegTask getTask(String taskId) {
        return taskMap.get(taskId);
    }

    public Map<String, FfmpegTask> getTaskMap() {
        return taskMap;
    }

    public Set<String> getIp4Addrs() {
        return ip4Addrs;
    }
}

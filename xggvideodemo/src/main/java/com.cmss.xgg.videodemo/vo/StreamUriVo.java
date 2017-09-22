package com.cmss.xgg.videodemo.vo;

public class StreamUriVo {

    private String taskId;
    private String streamUri;

    public StreamUriVo(){
    }

    public StreamUriVo(String taskId, String streamUri) {
        this.taskId = taskId;
        this.streamUri = streamUri;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStreamUri() {
        return streamUri;
    }

    public void setStreamUri(String streamUri) {
        this.streamUri = streamUri;
    }
}

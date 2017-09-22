package com.cmss.jffmpeg.cli;

public class CameraInfo {

    private String ip4Addr;
    private String camId;
    private String camName;
    private String rtstUri;

    public String getIp4Addr() {
        return ip4Addr;
    }

    public void setIp4Addr(String ip4Addr) {
        this.ip4Addr = ip4Addr;
    }

    public String getCamId() {
        return camId;
    }

    public void setCamId(String camId) {
        this.camId = camId;
    }

    public String getCamName() {
        return camName;
    }

    public void setCamName(String camName) {
        this.camName = camName;
    }

    public String getRtstUri() {
        return rtstUri;
    }

    public void setRtstUri(String rtstUri) {
        this.rtstUri = rtstUri;
    }
}

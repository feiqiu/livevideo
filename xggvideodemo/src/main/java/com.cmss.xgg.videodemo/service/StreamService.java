package com.cmss.xgg.videodemo.service;

import com.cmss.jffmpeg.cli.CameraInfo;
import com.cmss.jffmpeg.cli.FfmpegTask;
import com.cmss.jffmpeg.cli.FfmpegTaskManager;
import com.cmss.xgg.videodemo.vo.StreamUriVo;
import com.google.common.base.Throwables;
import de.onvif.soap.OnvifDevice;
import org.apache.commons.lang3.StringUtils;
import org.onvif.ver10.schema.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.xml.soap.SOAPException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * 流服务
 */
@Service
public class StreamService {

    public final Logger logger = LoggerFactory.getLogger(StreamService.class);

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    private static final String DEFAULT_RESOLUTION = "1920*1080";

    private FfmpegTaskManager ffmpegMgr = new FfmpegTaskManager();

    /**
     * 获取RTMP流地址
     * @param cameraIp
     * @param user
     * @param password
     * @param camId 摄像机ID，需唯一
     * @param camName
     * @return
     */
    public StreamUriVo getRtmpUri(String cameraIp, String user, String password, String camId, String camName) {
        List<String> rtspUris = getRtspUri(cameraIp, user, password);
        if (CollectionUtils.isEmpty(rtspUris)) {
            return null;
        }

        FfmpegTask ffmpegTask = ffmpegMgr.getTask(FfmpegTask.generateTaskId(cameraIp, DEFAULT_RESOLUTION));
        if (ffmpegTask != null && ffmpegTask.isRunning()) {
            return new StreamUriVo(ffmpegTask.getTaskId(), ffmpegTask.getSteamUri());
        }

        String rtspUri = rtspUris.get(0);

        CameraInfo camInfo = new CameraInfo();
        camInfo.setIp4Addr(cameraIp);
        camInfo.setCamId(camId);
        camInfo.setCamName(camName);
        camInfo.setRtstUri(rtspUri);

        FfmpegTask.TaskArgs taskArgs = FfmpegTask.argsBuilder();
        taskArgs.setFfmpegPath(ffmpegPath)
                .setRtspUri(rtspUri)
                .setResolution(DEFAULT_RESOLUTION)
                .setPushUri("rtmp://192.168.0.10:1935/myapp"); // 暂时写死，实际环境可能会通过服务获取

        ffmpegTask = new FfmpegTask(camInfo, taskArgs);
        ffmpegMgr.execute(ffmpegTask);

        if (ffmpegTask.isRunning()) {
            return new StreamUriVo(ffmpegTask.getTaskId(), ffmpegTask.getSteamUri());
        }
        return null;
    }

    /**
     * 获取RTSP流地址
     * @param hostIp
     * @param user
     * @param password
     * @return
     */
    public List<String> getRtspUri(String hostIp, String user, String password) {
        try {
            OnvifDevice cam = new OnvifDevice(hostIp, user, password);
            List<Profile> profiles = cam.getDevices().getProfiles();
            if (CollectionUtils.isEmpty(profiles)) {
                return new ArrayList<>(0);
            }

            List<String> rtspUris = new ArrayList<String>(profiles.size());
            for (Profile profile : profiles) {
                String rtspStreamUri = cam.getMedia().getRTSPStreamUri(profile.getToken());
                if (StringUtils.isBlank(rtspStreamUri)) {
                    continue;
                }
                rtspUris.add(rtspStreamUri);
            }

            return rtspUris;
        } catch (ConnectException | SOAPException e) {
            logger.error(Throwables.getStackTraceAsString(e));
            return new ArrayList<>(0);
        }
    }

    /**
     * 获取设备profile列表
     * @param hostIp
     * @param user
     * @param password
     * @return
     */
    public List<Profile> getProfiles(String hostIp, String user, String password) {
        OnvifDevice cam = null;
        try {
            cam = new OnvifDevice(hostIp, user, password);
            return cam.getDevices().getProfiles();
        } catch (ConnectException e) {
            logger.error(Throwables.getStackTraceAsString(e));
        } catch (SOAPException e) {
            logger.error(Throwables.getStackTraceAsString(e));
        }
        return new ArrayList<Profile>(0);
    }
}

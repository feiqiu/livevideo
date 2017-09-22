package com.cmss.xgg.videodemo.controller;

import com.cmss.xgg.videodemo.param.CamInfo;
import com.cmss.xgg.videodemo.service.StreamService;
import com.cmss.xgg.videodemo.vo.StreamUriVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class VideoStreamController {

    @Autowired
    private StreamService streamService;

    @RequestMapping("/")
    public String index(Model model){
        model.addAttribute("camInfo", new CamInfo());
        return "index";
    }

    @RequestMapping(value = "video/play", method = RequestMethod.POST)
    public String playVideo(@ModelAttribute("camInfo") CamInfo camInfo, Model model) {
        StreamUriVo streamVo = streamService.getRtmpUri(camInfo.getIp4Address(), camInfo.getUser(),
                camInfo.getPassword(), camInfo.getCamId(), camInfo.getCamName());
        model.addAttribute("cameraName", camInfo.getCamName());
        model.addAttribute("streamVo", streamVo);
        return "playvideo";
    }
}

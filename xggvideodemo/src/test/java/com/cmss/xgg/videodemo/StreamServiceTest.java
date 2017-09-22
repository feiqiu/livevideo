package com.cmss.xgg.videodemo;

import com.cmss.xgg.videodemo.service.StreamService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class StreamServiceTest {

    @Autowired
    StreamService streamService;

    @Test
    public void testGetRtspUris() {
        System.out.println(streamService.getRtspUri("192.168.0.14", "admin", "admin"));
    }
}

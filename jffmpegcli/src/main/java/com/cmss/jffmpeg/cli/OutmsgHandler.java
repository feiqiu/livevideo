package com.cmss.jffmpeg.cli;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 打印进程输出流
 */
public class OutmsgHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(OutmsgHandler.class);

    private BufferedReader reader;
    private volatile boolean stop = false;

    OutmsgHandler(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public void stop() {
        this.stop = true;
    }

    public void run() {
        String msg = null;
        try {
            while (!stop) {
                if ((msg = reader.readLine()) != null) {
                    logger.info(msg);
                }
            }
            if (stop) {
                reader.close();
            }
        } catch (IOException e) {
            logger.error(Throwables.getStackTraceAsString(e));
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error(Throwables.getStackTraceAsString(e));
            }
        }
    }
}

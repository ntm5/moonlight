package com.moonlight.shipbattle.logging;

import java.util.Arrays;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

import com.moonlight.shipbattle.Main;

import java.util.logging.Logger;

public class Logging
{
    private Logger logger;
    private static Logging instance;
    
    public Logging() {
        this.logger = null;
        Logging.instance = this;
    }
    
    public void init() throws IOException {
        this.logger = Logger.getLogger("shipbattle");
        final File dir = new File(Main.getMain().getDataFolder().getAbsolutePath() + "/logs");
        if (!dir.exists()) {
            dir.mkdir();
        }
        final Date d = new Date();
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(d);
        String name = null;
        for (int i = 0; i < 256; ++i) {
            name = dir.getAbsolutePath() + "/" + date + "-" + i + ".log";
            final File file = new File(name);
            if (!file.exists()) {
                break;
            }
        }
        final FileHandler fileHandler = new FileHandler(name);
        fileHandler.setFormatter(new SBFormatter());
        this.logger.addHandler(fileHandler);
        this.logger.setUseParentHandlers(false);
    }
    
    public void out() {
        Arrays.stream(this.logger.getHandlers()).forEach(handler -> {
            handler.close();
            this.logger.removeHandler(handler);
        });
    }
    
    public static Logger getLogger() {
        return Logging.instance.logger;
    }
    
    public static Logging getInstance() {
        return Logging.instance;
    }
}

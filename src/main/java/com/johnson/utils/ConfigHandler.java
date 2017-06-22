package com.johnson.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by johnson on 19/06/2017.
 */
public enum ConfigHandler {
    INSTANCE;
    public final Logger logger = LoggerFactory.getLogger(ConfigHandler.class);
    private Properties config;
    private File file = null;

    ConfigHandler() {
        config = new Properties();
        try {
            String parent = new File("").getParent();
            file = new File(parent, "config.properties");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    config.load(fileInputStream);
                } finally {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("配置文件读入失败", e.getMessage());
        }
    }

    public Properties getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                config.store(fileOutputStream, "保存配置");
            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        } catch (IOException e) {
            logger.error("保存配置失败", e.getMessage());
        }
    }

}

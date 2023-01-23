package com.moonlight.shipbattle.configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import com.moonlight.shipbattle.Main;

import java.io.File;
import java.util.Properties;

public class LangConfiguration
{
    private static final Properties config;
    private static final Properties defaults;
    private static File propertiesFile;
    
    static void load() throws IOException {
        if (LangConfiguration.propertiesFile == null) {
            LangConfiguration.propertiesFile = new File(Main.getMain().getDataFolder(), "strings.properties");
        }
        if (!LangConfiguration.propertiesFile.exists()) {
            Main.getMain().saveResource("strings.properties", false);
        }
        LangConfiguration.config.load(new InputStreamReader(new FileInputStream(LangConfiguration.propertiesFile), "UTF-8"));
        LangConfiguration.defaults.load(new InputStreamReader(Main.getMain().getResource("strings.properties"), "UTF-8"));
    }
    
    public static String getString(final String key) {
        String value = LangConfiguration.config.getProperty(key);
        if (value != null) {
            return value;
        }
        value = LangConfiguration.defaults.getProperty(key);
        if (value != null) {
            return value;
        }
        return "§cSz\u00f6veg nem tal\u00e1lhat\u00f3: '" + key + "'";
    }
    
    static {
        config = new Properties();
        defaults = new Properties();
    }
}

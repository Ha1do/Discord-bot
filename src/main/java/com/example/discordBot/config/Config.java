package com.example.discordBot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class Config
{
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static
    {
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE))
        {
            if (inputStream == null)
            {
                logger.error("Failed to load configuration file: {}", CONFIG_FILE);
            }
            else
            {
                properties.load(inputStream);
                logger.info("Loaded default configuration file: {}", CONFIG_FILE);
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to load configuration file: ", e);
        }
    }

    public static String getBotToken()
    {
        return properties.getProperty("botToken");
    }
}

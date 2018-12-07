package org.himalay.commandline;

import java.util.List;

public interface Log4jConfiguratorMXBean {
    /**
     * list of all the logger names and their levels
     * @return List of loggers
     */
    List<String> getLoggers();
 
    /**
     * Get the log level for a given logger
     * 
     * @param logger The logger name
     * @return the log level
     */
    String getLogLevel(String logger);
 
    /**
     * 
     * Set the log level for a given logger
     * @param logger The logger name
     * @param level  Level to set to
     */
    void setLogLevel(String logger, String level);
}
package org.himalay.commandline;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
public class Log4jConfigurator implements Log4jConfiguratorMXBean {
    public List<String> getLoggers() {
        List<String> list = new ArrayList<String>();
		def lm = Class.forName("org.apache.log4j.LogManager");
        for (Enumeration e = lm.getCurrentLoggers();
             e.hasMoreElements(); ) {
 
            def log =  e.nextElement();
            if (log.getLevel() != null) {
                list.add(log.getName() + " = " + log.getLevel().toString());
            }else{
				list.add(log.getName() + " = " + log.getEffectiveLevel().toString());
			}
        }
        return list;
    }
 
    public String getLogLevel(String logger) {
        String level = "unavailable";
 
        if (StringUtils.isNotBlank(logger)) {
            def log = Class.forName("org.apache.log4j.Logger");
			log = log.getLogger(logger)
            if (log != null) {
				def lvl = log.getLevel()
				if ( lvl == null){
					lvl = log.getEffectiveLevel()
				}
                level = lvl.toString();
            }
        }
        return level;
    }
    public void setLogLevel(String logger, String level) {
        if (StringUtils.isNotBlank(logger)  &&  StringUtils.isNotBlank(level)) {
            def log = Class.forName("org.apache.log4j.Logger");
			log = log.getLogger(logger)
            if (log != null) {
                log.setLevel(Class.forName("org.apache.log4j.Level").toLevel(level.toUpperCase()));
            }
        }
    }
 
}

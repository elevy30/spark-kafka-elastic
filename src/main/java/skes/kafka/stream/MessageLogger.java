package skes.kafka.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;

/**
 * Created by eyallevy on 07/01/2018
 */
public class MessageLogger {

    static Logger logger = LoggerFactory.getLogger(KsTopologyBuilder.class);

    public static void log(LogLevel logLevel, String message, Object... objects) {
        switch (logLevel) {
            case DEBUG:
                logger.debug(message, objects);
                break;
            case ERROR:
                logger.error(message, objects);
                break;
            case FATAL:
                logger.error(message, objects);
                break;
            case INFO:
                logger.info(message, objects);
                break;
            case TRACE:
                logger.trace(message, objects);
                break;
            case WARN:
                logger.warn(message, objects);
                break;
            default:
                logger.debug(message, objects);
                break;
        }
    }

}


package com.sample;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4j2Example {

    // Logger 생성 (LogManager 통해 생성)
    private static final Logger logger = LogManager.getLogger(Log4j2Example.class);

    public static void main(String[] args) {
        logger.info("애플리케이션 시작");
        logger.debug("디버그 메시지: {}", "추가 데이터");
        logger.warn("경고 메시지");
        try {
            int result = 10 / 0;
        } catch (Exception e) {
            logger.error("에러 발생", e);
        }
    }
}

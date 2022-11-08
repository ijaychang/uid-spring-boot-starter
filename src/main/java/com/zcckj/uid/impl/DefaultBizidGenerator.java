
package com.zcckj.uid.impl;

import com.zcckj.uid.BizidGenerator;
import com.zcckj.uid.exception.UidGenerateException;
import com.zcckj.uid.worker.WorkerIdAssigner;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Setter
public class DefaultBizidGenerator implements BizidGenerator, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBizidGenerator.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Spring property
     */
    protected WorkerIdAssigner workerIdAssigner;

    protected long workerId;

    /**
     * Random object used by random method. This has to be not local to the
     * random method so as to not return the same value in the same millisecond.
     */
    private static final Random RANDOM = new Random();

    @Override
    public String getUID() throws UidGenerateException {
        return String.format("%s%06d%06d", LocalDateTime.now().format(DATE_TIME_FORMATTER), workerId, RandomUtils.nextInt(0, 1000000));
    }

    @Override
    public String parseUID(String uid) {
        // format as string
        String datetime = uid.substring(0, 14);
        String random = uid.substring(20, 26);
        return String.format("{\"UID\":\"%s\",\"dateTime\":\"%s\",\"workerId\":\"%06d\",\"random\":\"%s\"}",
                uid, datetime, workerId, random);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // initialize worker id
        long workerId = workerIdAssigner.assignWorkerId();
        LOGGER.info("Initialized workerID:{}", workerId);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10000; i++) {
            System.out.println(LocalDateTime.now().format(DATE_TIME_FORMATTER) + String.format("%06d", RANDOM.nextInt(1000000)));
        }
    }
}

package com.zcckj.uid;

import com.zcckj.uid.impl.DefaultUidGenerator;
import com.zcckj.uid.worker.DisposableWorkerIdAssigner;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/***
 *
 *
 * +------+----------------------+----------------+-----------+
 * | sign |     delta seconds    | worker node id | sequence  |
 * +------+----------------------+----------------+-----------+
 *   1bit          28bits              22bits         13bits
 *
 */

public class UidSpringBootStarterApplicationTests {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    DateTimeFormatter YEAR_MONTH_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

	@Test
	public void test1() {
        DisposableWorkerIdAssigner workerIdAssigner = new DisposableWorkerIdAssigner();
        workerIdAssigner.setZookeeperConnection("171.188.0.161:2181");
        workerIdAssigner.setServicePort(8080);
        DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
        defaultUidGenerator.setEpochStr("2014-12-31");
        defaultUidGenerator.setTimeBits(37);
        defaultUidGenerator.setWorkerBits(20);
        defaultUidGenerator.setSeqBits(6);
        defaultUidGenerator.setWorkerIdAssigner(workerIdAssigner);
        try {
            defaultUidGenerator.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int year = 2015 ; year <= 2020 ; year ++) {
            for(int month = 1 ; month <= 12 ; month ++) {
                String monthStr = month < 10 ? "0"+month : month+"";
                String yearMonthStr = year + "-"+monthStr;
                YearMonth yearMonth = YearMonth.parse(yearMonthStr);
                LocalDateTime startDayOfYearMonthLocalDateTime = yearMonth.atDay(1).atStartOfDay();
                long epochMilli1 = startDayOfYearMonthLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long beginId = defaultUidGenerator.getHistoryUID(epochMilli1);

                LocalDateTime endDayOfYearMonthLocalDateTime = yearMonth.atEndOfMonth().atTime(23,59,59,999999999);
                long epochMilli2 = endDayOfYearMonthLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long endId = defaultUidGenerator.getHistoryUID(epochMilli2);
                System.out.println(yearMonthStr+" "+beginId+" "+endId);
            }
        }

    }

}

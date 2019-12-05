package com.zcckj.uid;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.zcckj.uid.impl.DefaultUidGenerator;
import com.zcckj.uid.worker.DisposableWorkerIdAssigner;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class UidSpringBootStarterApplicationTests {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    DateTimeFormatter YEAR_MONTH_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");


    @Test
    public void test00(){
        LocalDateTime localDateTime1 = LocalDateTime.parse("2019-01-01 00:00:00", DATE_TIME_FORMATTER);
        System.out.println(localDateTime1.atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli());
        LocalDateTime localDateTime2 = LocalDateTime.parse("2019-11-31 23:59:59", DATE_TIME_FORMATTER);

        System.out.println(localDateTime2.atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli());
    }


    @Test
    public void test0() {
        long maxDeltaSecond = ~(-1L << 28);
        System.out.println(maxDeltaSecond);

        BigDecimal bigDecimal=new BigDecimal(563.77866D);
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
        System.out.println(bigDecimal);

        double shortDistance = 234234234.3343434D;
        BigDecimal distance = new BigDecimal(shortDistance);
        distance = distance.divide(new BigDecimal(1000.00D)).setScale(2);
        System.out.println(distance);

    }

    @Test
    public void test1() {
        DisposableWorkerIdAssigner workerIdAssigner = new DisposableWorkerIdAssigner();
        workerIdAssigner.setZookeeperConnection("171.188.0.161:2181");
        workerIdAssigner.setServicePort(8088);
        workerIdAssigner.setServiceIp("10.1.80.62");
        String epochStr = "2014-12-31";
        /***
         *
         * +------+----------------------+----------------+-----------+
         * | sign |     delta seconds    | worker node id | sequence  |
         * +------+----------------------+----------------+-----------+
         *   1bit          32bits              21bits         10bits
         */
        int timeBits = 32,workerBits = 21,seqBits = 10;
        DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
        defaultUidGenerator.setEpochStr(epochStr);
        defaultUidGenerator.setTimeBits(timeBits);
        defaultUidGenerator.setWorkerBits(workerBits);
        defaultUidGenerator.setSeqBits(seqBits);
        defaultUidGenerator.setWorkerIdAssigner(workerIdAssigner);
        try {
            defaultUidGenerator.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 可以用多久
        LocalDateTime epochLocalDateTime = LocalDateTime.parse("2014-12-31 00:00:00",DATE_TIME_FORMATTER);
        long epochSecond = TimeUnit.SECONDS.toSeconds(epochLocalDateTime.toEpochSecond(ZoneOffset.of("+8")));
        long maxSecond = (long) Math.pow(2.0D,timeBits);
        long years = (maxSecond-epochSecond)/ (3600 * 24 * 365);
        System.out.println("By this config uid can use "+years+" years");

        ZoneId zoneId = ZoneId.systemDefault();
        System.out.println(zoneId.getDisplayName(TextStyle.FULL, Locale.getDefault()));

        for (int year = 2015; year <= 2020; year++) {

            for (int month = 1; month <= 12; month++) {
                String monthStr = month < 10 ? "0" + month : month + "";
                String yearMonthStr = year + "-" + monthStr;
                YearMonth yearMonth = YearMonth.parse(yearMonthStr);
                LocalDateTime startDayOfYearMonthLocalDateTime = yearMonth.atDay(1).atStartOfDay();
                long epochMilli1 = startDayOfYearMonthLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()
                        .toEpochMilli();
                long beginId = defaultUidGenerator.getHistoryUIDOfBeginSequence(epochMilli1);
                String parsedBeginId = defaultUidGenerator.parseUID(beginId);

                LocalDateTime endDayOfYearMonthLocalDateTime = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);
                long epochMilli2 = endDayOfYearMonthLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()
                        .toEpochMilli();
                long endId = defaultUidGenerator.getEndHistoryUIDOfEndSequence(epochMilli2);
                String parsedEndId = defaultUidGenerator.parseUID(endId);

                System.out.println(year + " " + " " + monthStr + " " + beginId + " " + endId+" " + parsedBeginId+" "+parsedEndId);
            }
        }

    }

}

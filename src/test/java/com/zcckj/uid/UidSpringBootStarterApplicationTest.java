package com.zcckj.uid;

import com.zcckj.uid.annotation.EnableUID;
import com.zcckj.uid.impl.DefaultUidGenerator;
import com.zcckj.uid.worker.DisposableWorkerIdAssigner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

@EnableUID
@SpringBootApplication
public class UidSpringBootStarterApplicationTest implements CommandLineRunner {
    @Autowired
    private UidGenerator uidGenerator;

    public static void main(String[] args) {
        SpringApplication.run(UidSpringBootStarterApplicationTest.class,args);
    }


    @Override
    public void run(String... args) throws Exception {
        for(int i = 0  ; i < 100 ; i ++){
            long uid = uidGenerator.getUID();
            System.out.println(uid);
        }
    }
}

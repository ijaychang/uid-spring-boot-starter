package com.zcckj.uid;

import com.zcckj.uid.annotation.EnableUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@EnableUID
@SpringBootApplication
public class SpringBootStarterApplicationTest implements CommandLineRunner {
    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private BizidGenerator bizidGenerator;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterApplicationTest.class,args);
    }


    @Override
    public void run(String... args) throws Exception {
        for(int j = 0 ; j < 10 ; j ++){
            long begin = System.currentTimeMillis();
            for (int i = 0; i < 500; i++) {
                //long uid = uidGenerator.getUID();
                String uid = bizidGenerator.getUID("ZX");
                System.out.println(uid);
                Thread.sleep(50L);
            }
            System.out.println(System.currentTimeMillis() - begin);
        }
    }



}

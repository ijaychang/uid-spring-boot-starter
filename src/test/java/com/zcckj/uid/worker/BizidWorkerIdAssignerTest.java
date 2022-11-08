package com.zcckj.uid.worker;

import com.zcckj.uid.BizidGenerator;
import com.zcckj.uid.annotation.EnableUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableUID
@SpringBootApplication
public class BizidWorkerIdAssignerTest {

    @Autowired
    private BizidGenerator bizidGenerator;

    @Test
    public void assignWorkerId() {
        BizidWorkerIdAssigner bizidWorkerIdAssigner = new BizidWorkerIdAssigner();
        bizidWorkerIdAssigner.setServiceIp("10.1.80.62");
        bizidWorkerIdAssigner.setServicePort(8088);
        bizidWorkerIdAssigner.setZookeeperConnection("171.188.0.161:2181");
        long workId = bizidWorkerIdAssigner.assignWorkerId();
        System.out.println(workId);
    }

}
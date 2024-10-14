package cn.jaychang.uid.worker;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Represents an implementation of {@link WorkerIdAssigner}, 
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author fsren
 */
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {
    private static final String UID_NAMESPACE = "uid-generator";
    /** 用于分配workId*/
    private static final String SEQ_ZNODE = "/workId/sequence";

    private static final int SESSION_TIMEOUT = 5000;
    private static final int CONNECTION_TIMEOUT = 5000;

    private static final Logger LOGGER = LoggerFactory.getLogger(DisposableWorkerIdAssigner.class);

    private String zookeeperConnection;

    /** 业务服务提供服务的IP地址*/
    private String serviceIp;

    /** 业务服务提供服务的端口号*/
    private int servicePort;

    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     *
     * @return assigned worker id
     */
    @Override
    public long assignWorkerId() {
        long znodeSeq = znodeSeq();
        Assert.isTrue(znodeSeq != -1L, "znodeSeq must not equals -1L");
        LOGGER.info("Add worker node:" + znodeSeq);
        return znodeSeq;
    }


    public long znodeSeq() {
        Assert.isTrue(servicePort != 0, "servicePort must be assign");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        try {
            final CuratorFramework client = CuratorFrameworkFactory.builder().connectString(this.zookeeperConnection)
                    .sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy)
                    .namespace(UID_NAMESPACE).build();
            client.start();
            final String serviceIpPort = serviceIp + "#" + servicePort;
            final String sequencePathPrefix = SEQ_ZNODE + "/" + serviceIpPort + "-";
            Stat stat = client.checkExists().creatingParentsIfNeeded().forPath(SEQ_ZNODE);
            String sequenceNodePath = null;
            if (stat == null) {
                sequenceNodePath = createPersistentSequenceNode(client, sequencePathPrefix);
            } else {
                List<String> seqNodePaths = client.getChildren().forPath(SEQ_ZNODE);
                if (CollectionUtils.isEmpty(seqNodePaths)) {
                    sequenceNodePath = createPersistentSequenceNode(client, sequencePathPrefix);
                } else {
                    Map<String, String> serviceIpPortAndWorkIdMap = seqNodePaths.parallelStream().collect(Collectors
                            .toMap(seqNodePath -> transfer(seqNodePath, 0), seqNodePath -> transfer(seqNodePath, 1)));
                    String workIdStr = serviceIpPortAndWorkIdMap.get(serviceIpPort);
                    if (StringUtils.isEmpty(workIdStr)) {
                        sequenceNodePath = createPersistentSequenceNode(client, sequencePathPrefix);
                    } else {
                        sequenceNodePath = sequencePathPrefix + workIdStr;
                        checkTimeMillisIsValid(client,sequenceNodePath);
                    }
                }
            }

            String workIdStr = sequenceNodePath.split("-")[1];
            // 定时上报服务最新的时间(更新上报时间戳)
            scheduledUploadData(client,sequenceNodePath);
            // 注册shutdown钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> client.close()));
            return Long.parseLong(workIdStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("AlibabaThreadPoolCreation")
    private void scheduledUploadData(CuratorFramework client, String sequenceNodePath) {
        // 每3s上报当前节点的时间
        new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "uid-schedule-upload-time");
            thread.setDaemon(true);
            return thread;
        }).scheduleAtFixedRate(() -> updateNewData(client, sequenceNodePath),1,3L,TimeUnit.SECONDS);
    }

    private void updateNewData(CuratorFramework client, String sequenceNodePath) {
        try {
            client.setData().forPath(sequenceNodePath, String.valueOf(System.currentTimeMillis()).getBytes());
        } catch (Exception e) {
        }
    }

    private static String transfer(String sourceStr, int index) {
        return sourceStr.split("-")[index];
    }

    private void checkTimeMillisIsValid(CuratorFramework client,String sequenceNodePath) throws Exception {
        // 检查当前时间戳秒数是否大于等于存储在sequenceNodePath的秒数
        byte[] bytes = client.getData().forPath(sequenceNodePath);
        long timeMillisStoreInSeqNode = Long.parseLong(new String(bytes));
        // 该节点的时间不能小于最后一次上报的时间
        if(System.currentTimeMillis() < timeMillisStoreInSeqNode){
            throw new RuntimeException("Current timeMillis should bigger than timeMillisStoreInSeqNode");
        }
    }

    private String createPersistentSequenceNode(CuratorFramework client, String sequencePathPrefix) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath(sequencePathPrefix, String.valueOf(System.currentTimeMillis()).getBytes());
    }

    public void setZookeeperConnection(String zookeeperConnection) {
        this.zookeeperConnection = zookeeperConnection;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public String getServiceIp() {
        return serviceIp;
    }

    public void setServiceIp(String serviceIp) {
        this.serviceIp = serviceIp;
    }
}

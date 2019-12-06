package com.zcckj.uid.worker;

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


        try (CuratorFramework client = CuratorFrameworkFactory.builder().connectString(this.zookeeperConnection)
                .sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy)
                .namespace(UID_NAMESPACE).build();) {
            client.start();
            final String serviceIpPort = serviceIp + "#" + servicePort;
            final String sequencePathPrefix = SEQ_ZNODE + "/" + serviceIpPort + "-";
            // 先检查服务IP+服务端口号节点是否存在
            Stat stat = client.checkExists().creatingParentsIfNeeded().forPath(SEQ_ZNODE);
            String sequenceNodePath = null;
            if (stat == null) {
                sequenceNodePath = createPersistentSequenceNode(client, sequencePathPrefix);
            } else {
                List<String> seqNodePaths = client.getChildren().forPath(SEQ_ZNODE);
                if (CollectionUtils.isEmpty(seqNodePaths)) {
                    sequenceNodePath = createPersistentSequenceNode(client, sequencePathPrefix);
                } else {
                    Map<String, String> ipPortAndWorkIdMap = seqNodePaths.parallelStream().collect(Collectors
                            .toMap(seqNodePath -> transfer(seqNodePath, 0), seqNodePath -> transfer(seqNodePath, 1)));
                    String workIdStr = ipPortAndWorkIdMap.get(serviceIpPort);
                    if (StringUtils.isEmpty(workIdStr)) {
                        sequenceNodePath = createPersistentSequenceNode(client, sequencePathPrefix);
                    } else {
                        sequenceNodePath = serviceIpPort + "-" + workIdStr;
                    }
                }
            }
            String workIdStr = sequenceNodePath.split("-")[1];
            return Long.parseLong(workIdStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String transfer(String sourceStr, int index) {
        return sourceStr.split("-")[index];
    }

    private String createPersistentSequenceNode(CuratorFramework client, String sequencePathPrefix) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath(sequencePathPrefix);
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

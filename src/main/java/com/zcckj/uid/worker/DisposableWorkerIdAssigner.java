package com.zcckj.uid.worker;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

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
	/** 用于存储服务IP-服务端口对应的workId值*/
    private static final String STORAGE_ZNODE = "/workId/storage";

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
		Assert.isTrue(znodeSeq != -1L,"znodeSeq must not equals -1L");
		LOGGER.info("Add worker node:" + znodeSeq);
		return znodeSeq;
	}


	public long znodeSeq() {
        Assert.isTrue(servicePort != 0,"servicePort must be assign");
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString(this.zookeeperConnection)
				.sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy)
				.namespace(UID_NAMESPACE).build();
		client.start();
		try {
		    final String serviceIpPort = getServiceIp()+"#"+servicePort;
            final String storagePath = STORAGE_ZNODE + "/" + serviceIpPort;
            final String sequencePathPrefix = SEQ_ZNODE+"/"+ serviceIpPort + "-";
		    // 先检查服务IP+服务端口号节点是否存在
            Stat stat = client.checkExists().creatingParentsIfNeeded().forPath(storagePath);
            if(stat != null){
                byte[] bytes = client.getData().forPath(storagePath);
                long workId = Long.parseLong(new String(bytes));
                return workId;
            }
            String sequenceNodePath = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(sequencePathPrefix);
            String workIdStr = sequenceNodePath.split("-")[1];
            long workId = Long.parseLong(workIdStr);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(storagePath,String.valueOf(workId).getBytes());
            return workId;
		} catch (Exception e) {
		    throw new RuntimeException(e);
		} finally {
			client.close();
		}
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

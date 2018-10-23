package com.zcckj.uid.worker;

import org.apache.commons.lang3.RandomUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an implementation of {@link WorkerIdAssigner}, 
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author fsren
 */
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {
	public static final String SEQ_ZNODE = "/parent";
	public static final int SESSION_TIMEOUT = 5000;
	public static final int CONNECTION_TIMEOUT = 5000;
	private static final Logger LOGGER = LoggerFactory.getLogger(DisposableWorkerIdAssigner.class);
	private String zookeeperConnection;

	/**
	 * Assign worker id base on database.<p>
	 * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
	 * Otherwise, the node runs on an actual machine.
	 *
	 * @return assigned worker id
	 */
	public long assignWorkerId() {
		long znodeSeq = znodeSeq();
		LOGGER.info("Add worker node:" + znodeSeq);
		return znodeSeq;
	}


	public long znodeSeq() {
		long versionAsSeq = RandomUtils.nextLong(0, 100000);
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString(this.zookeeperConnection)
				.sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy)
				.namespace("uid-generator").build();
		client.start();

		try {

			if (client.checkExists().forPath(SEQ_ZNODE) == null) {
				client.create().forPath(SEQ_ZNODE, new byte[0]);
			} else {
				client.setData().forPath(SEQ_ZNODE, new byte[0]);
			}
			Stat stat = new Stat();
			client.getData().storingStatIn(stat).forPath(SEQ_ZNODE);
			versionAsSeq = stat.getVersion();
			return versionAsSeq;
		} catch (Exception ignored) {
		} finally {
			client.close();
		}
		return versionAsSeq;
	}

	public void setZookeeperConnection(String zookeeperConnection) {
		this.zookeeperConnection = zookeeperConnection;
	}

}

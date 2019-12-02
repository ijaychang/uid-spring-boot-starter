package com.zcckj.uid.worker;

import com.zcckj.uid.utils.NetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Represents an implementation of {@link WorkerIdAssigner}, 
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author fsren
 */
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {
	public static final String SEQ_ZNODE = "/workId";
	public static final int SESSION_TIMEOUT = 5000;
	public static final int CONNECTION_TIMEOUT = 5000;
	private static final Logger LOGGER = LoggerFactory.getLogger(DisposableWorkerIdAssigner.class);
	private String zookeeperConnection;

    private final String USER_HOME_DIR_KEY_NAME = "user.home";

    private final String WORK_ID_KEY_NAME = "workId";

    private final String WORK_ID_CACHE_PROPERITES_FILE_PATH_PREFIX = System.getProperty(USER_HOME_DIR_KEY_NAME)+"/uid-work-id-";

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
		LOGGER.info("Add worker node:" + znodeSeq);
		return znodeSeq;
	}


	public long znodeSeq() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString(this.zookeeperConnection)
				.sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy)
				.namespace("uid-generator").build();
		client.start();
        String ipPort = NetUtils.getLocalAddress()+":"+servicePort;
		try {
            File wordIdCacheFile = new File(WORK_ID_CACHE_PROPERITES_FILE_PATH_PREFIX+ipPort.replace(":","+")+".properties");
            if(!wordIdCacheFile.exists()){
                File userHomeDir = new File(System.getProperty(USER_HOME_DIR_KEY_NAME));
                if(!userHomeDir.exists()){
                    userHomeDir.mkdirs();
                }
                if(!wordIdCacheFile.exists()){
                    wordIdCacheFile.createNewFile();
                }

                Assert.isTrue(servicePort != 0,"servicePort must be assign");
                // 持久顺序节点 IP:PORT-
                String path = SEQ_ZNODE+"/"+ ipPort +"-";
                String seqNodepath = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                        .forPath(path);
                String workIdStr = seqNodepath.split("-")[1];
                Properties workIdProperties = PropertiesLoaderUtils.loadProperties(new FileSystemResource(wordIdCacheFile));
                workIdProperties.setProperty(WORK_ID_KEY_NAME,workIdStr);
                try(OutputStream outputStream = new FileOutputStream(wordIdCacheFile);) {
                    workIdProperties.store(outputStream,"workId cache in this file");
                }
                return Long.parseLong(workIdStr);
            }

            Properties workIdProperties = PropertiesLoaderUtils.loadProperties(new FileSystemResource(wordIdCacheFile));
            String workIdStr = workIdProperties.getProperty(WORK_ID_KEY_NAME);
			Assert.isTrue(StringUtils.isNotBlank(workIdStr),"workId not exists in "+wordIdCacheFile.getAbsolutePath());
            return Long.parseLong(workIdStr);
		} catch (Exception e) {
		    throw new RuntimeException(e);
		} finally {
			client.close();
		}
	}

	public void setZookeeperConnection(String zookeeperConnection) {
		this.zookeeperConnection = zookeeperConnection;
	}

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }
}

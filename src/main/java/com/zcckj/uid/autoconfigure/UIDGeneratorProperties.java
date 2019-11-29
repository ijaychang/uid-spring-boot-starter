package com.zcckj.uid.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: fsren
 *
 * @Package com.zcckj.uid.boot
 *
 * @Date: 2018/10/22 19:36
 *
 * @Description:
 *  <p>
 *
 *  </p>
 */
@ConfigurationProperties(prefix = "uid")
@Getter
@Setter
public class UIDGeneratorProperties {

	private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

	/** Bits allocate */
	private int timeBits = 23;
	private int workerBits = 31;
	private int seqBits = 9;

	/** zookeeper address */
	private String zookeeperConnection = "localhost:2181";

    /** servicePort */
	private int servicePort = 8080;

	/** UID Generator Type */
	private UIDGeneratorType type = UIDGeneratorType.DEFAULT;

	/** Customer epoch, unit as second. For example 2018-10-19 (ms: 1539878400000)*/
	private String epochStr = "2018-10-19";
}

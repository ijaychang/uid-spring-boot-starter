package com.zcckj.uid.autoconfigure;

import com.zcckj.uid.UidGenerator;
import com.zcckj.uid.impl.CachedUidGenerator;
import com.zcckj.uid.impl.DefaultUidGenerator;
import com.zcckj.uid.worker.DisposableWorkerIdAssigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: fsren
 *
 * @Package com.zcckj.uid.autoconfigure
 *
 * @Date: 2018/10/22 20:59
 *
 * @Description:
 *  <p>
 *
 *  </p>
 */
@Configuration
@EnableConfigurationProperties(UIDGeneratorProperties.class)//指定类的配置
@ConditionalOnClass(UidGenerator.class)
public class UIDGeneratorAutoConfiguration {

	/**
	 * 自定义配置
	 */
	private UIDGeneratorProperties properties;

	@Autowired
	public UIDGeneratorAutoConfiguration(UIDGeneratorProperties properties) {
		this.properties = properties;
	}

	@Bean
	public UidGenerator createUidGenerator() {
		DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner();
		disposableWorkerIdAssigner.setZookeeperConnection(properties.getZookeeperConnection());

		DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
		if (properties.getType() == UIDGeneratorType.CACHED) {
			defaultUidGenerator = new CachedUidGenerator();
		}
		defaultUidGenerator.setSeqBits(properties.getSeqBits());
		defaultUidGenerator.setTimeBits(properties.getTimeBits());
		defaultUidGenerator.setWorkerBits(properties.getWorkerBits());
		defaultUidGenerator.setEpochStr(properties.getEpochStr());
		defaultUidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner);
		return defaultUidGenerator;
	}

}

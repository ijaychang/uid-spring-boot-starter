package com.zcckj.uid.autoconfigure;

import com.zcckj.uid.UidGenerator;
import com.zcckj.uid.annotation.EnableUID;
import com.zcckj.uid.impl.CachedUidGenerator;
import com.zcckj.uid.impl.DefaultUidGenerator;
import com.zcckj.uid.utils.InetUtils;
import com.zcckj.uid.utils.InetUtilsProperties;
import com.zcckj.uid.worker.DisposableWorkerIdAssigner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @auther: fsren
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
@ConditionalOnBean(annotation = EnableUID.class)
@EnableConfigurationProperties({UIDGeneratorProperties.class,InetUtilsProperties.class})
@ConditionalOnClass(UidGenerator.class)
public class UIDGeneratorAutoConfiguration {

	/**
	 * 自定义配置
	 */
	private UIDGeneratorProperties uidGeneratorProperties;

    /**
     * 自定义配置
     */
    private InetUtilsProperties inetUtilsProperties;


    private InetUtils inetUtils;

	@Autowired
	public UIDGeneratorAutoConfiguration(UIDGeneratorProperties uidGeneratorProperties,InetUtilsProperties inetUtilsProperties) {
		this.uidGeneratorProperties = uidGeneratorProperties;
		this.inetUtilsProperties = inetUtilsProperties;
        inetUtils = new InetUtils(inetUtilsProperties);
	}



	@Bean
    @ConditionalOnMissingBean
	public UidGenerator createUidGenerator() {
		DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner();
		disposableWorkerIdAssigner.setZookeeperConnection(uidGeneratorProperties.getZookeeperConnection());
        disposableWorkerIdAssigner.setServicePort(uidGeneratorProperties.getServicePort());

        // 未指定服务IP地址则使用inetUtils获取
        if(StringUtils.isBlank(disposableWorkerIdAssigner.getServiceIp())) {
            disposableWorkerIdAssigner.setServiceIp(inetUtils.findFirstNonLoopbackAddress().getHostAddress());
        }

		DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
		if (uidGeneratorProperties.getType() == UIDGeneratorType.CACHED) {
			defaultUidGenerator = new CachedUidGenerator();
		}
		defaultUidGenerator.setSeqBits(uidGeneratorProperties.getSeqBits());
		defaultUidGenerator.setTimeBits(uidGeneratorProperties.getTimeBits());
		defaultUidGenerator.setWorkerBits(uidGeneratorProperties.getWorkerBits());
		defaultUidGenerator.setEpochStr(uidGeneratorProperties.getEpochStr());
		defaultUidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner);
		return defaultUidGenerator;
	}

}

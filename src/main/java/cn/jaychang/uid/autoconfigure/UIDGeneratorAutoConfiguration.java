package cn.jaychang.uid.autoconfigure;

import cn.jaychang.uid.BizidGenerator;
import cn.jaychang.uid.UidGenerator;
import cn.jaychang.uid.worker.ZkWorkerIdAssigner;
import cn.jaychang.uid.annotation.EnableUID;
import cn.jaychang.uid.impl.CachedUidGenerator;
import cn.jaychang.uid.impl.DefaultBizidGenerator;
import cn.jaychang.uid.impl.DefaultUidGenerator;
import cn.jaychang.uid.utils.InetUtils;
import cn.jaychang.uid.worker.BizidWorkerIdAssigner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;

/**
 * @auther: fsren
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
@EnableConfigurationProperties({UIDGeneratorProperties.class, InetUtilsProperties.class})
@ConditionalOnClass({UidGenerator.class, BizidGenerator.class})
public class UIDGeneratorAutoConfiguration{

    /**
     * 自定义配置
     */
    private UIDGeneratorProperties uidGeneratorProperties;

    /**
     * 自定义配置
     */
    private InetUtilsProperties inetUtilsProperties;

    private InetUtils inetUtils;

    @Value("${server.port:8080}")
    private int servicePort;

    private static final String LOCAL_LOOP_BACK_IP = "127.0.0.1";

    @Autowired
    public UIDGeneratorAutoConfiguration(UIDGeneratorProperties uidGeneratorProperties,
            InetUtilsProperties inetUtilsProperties) {
        this.uidGeneratorProperties = uidGeneratorProperties;
        this.inetUtilsProperties = inetUtilsProperties;
        inetUtils = new InetUtils(inetUtilsProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public UidGenerator createUidGenerator() {
        ZkWorkerIdAssigner zkWorkerIdAssigner = new ZkWorkerIdAssigner();
        zkWorkerIdAssigner.setZookeeperConnection(uidGeneratorProperties.getZookeeperConnection());
        // 单元测试时serverPort时-1此时会影响zk的持久有序节点key名的判断，故这里将-1改为99999
        if (this.servicePort == -1) {
            this.servicePort = 99999;
        }
        servicePort = Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        zkWorkerIdAssigner.setServicePort(servicePort);

        // 未指定服务IP地址则使用inetUtils获取
        if (StringUtils.isBlank(zkWorkerIdAssigner.getServiceIp())) {
            String defaultIpAddress = inetUtilsProperties.getDefaultIpAddress();
            if (StringUtils.isNotBlank(defaultIpAddress) && !LOCAL_LOOP_BACK_IP.equals(defaultIpAddress)) {
                zkWorkerIdAssigner.setServiceIp(inetUtilsProperties.getDefaultIpAddress());
            } else {
                zkWorkerIdAssigner.setServiceIp(inetUtils.findFirstNonLoopbackAddress().getHostAddress());
            }
        }

        DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
        if (uidGeneratorProperties.getType() == UIDGeneratorType.CACHED) {
            defaultUidGenerator = new CachedUidGenerator();
        }
        defaultUidGenerator.setSeqBits(uidGeneratorProperties.getSeqBits());
        defaultUidGenerator.setTimeBits(uidGeneratorProperties.getTimeBits());
        defaultUidGenerator.setWorkerBits(uidGeneratorProperties.getWorkerBits());
        defaultUidGenerator.setEpochStr(uidGeneratorProperties.getEpochStr());
        defaultUidGenerator.setWorkerIdAssigner(zkWorkerIdAssigner);
        return defaultUidGenerator;
    }

    @Bean
    @ConditionalOnMissingBean
    public BizidGenerator createBizidGenerator() {
        BizidWorkerIdAssigner bizidWorkerIdAssigner = new BizidWorkerIdAssigner();
        bizidWorkerIdAssigner.setZookeeperConnection(uidGeneratorProperties.getZookeeperConnection());
        if (this.servicePort == -1) {
            this.servicePort = 99999;
        }
        bizidWorkerIdAssigner.setServicePort(servicePort);

        // 未指定服务IP地址则使用inetUtils获取
        if (StringUtils.isBlank(bizidWorkerIdAssigner.getServiceIp())) {
            String defaultIpAddress = inetUtilsProperties.getDefaultIpAddress();
            if (StringUtils.isNotBlank(defaultIpAddress) && !LOCAL_LOOP_BACK_IP.equals(defaultIpAddress)) {
                bizidWorkerIdAssigner.setServiceIp(inetUtilsProperties.getDefaultIpAddress());
            } else {
                bizidWorkerIdAssigner.setServiceIp(inetUtils.findFirstNonLoopbackAddress().getHostAddress());
            }
        }

        DefaultBizidGenerator bizidGenerator = new DefaultBizidGenerator();

        bizidGenerator.setWorkerIdAssigner(bizidWorkerIdAssigner);
        return bizidGenerator;
    }
}

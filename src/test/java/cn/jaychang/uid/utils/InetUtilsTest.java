package cn.jaychang.uid.utils;

import cn.jaychang.uid.autoconfigure.InetUtilsProperties;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Arrays;

/**
 *
 * <p>
 *  TODO 类作用描述
 * </p>
 *
 * @author zhangjie
 * @since 2019-12-03
 */
public class InetUtilsTest {

    @Test
    public void findFirstNonLoopbackHostInfo() {
        InetUtilsProperties inetUtilsProperties = new InetUtilsProperties();
        inetUtilsProperties.setPreferredNetworks(Arrays.asList("10.1.80"));
        InetUtils inetUtils = new InetUtils(inetUtilsProperties);
        InetAddress firstNonLoopbackAddress = inetUtils.findFirstNonLoopbackAddress();
        System.out.println(firstNonLoopbackAddress.getHostAddress());
    }

    @Test
    public void findFirstNonLoopbackAddress() {
    }

    @Test
    public void isPreferredAddress() {
    }

    @Test
    public void ignoreInterface() {
    }

    @Test
    public void convertAddress() {
    }
}
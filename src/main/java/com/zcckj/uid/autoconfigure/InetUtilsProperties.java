/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zcckj.uid.autoconfigure;

import com.zcckj.uid.utils.InetUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Properties for {@link InetUtils}.
 *
 * @author Spencer Gibb
 */
@ConfigurationProperties(InetUtilsProperties.PREFIX)
public class InetUtilsProperties {

    /**
     * Prefix for the Inet Utils properties.
     */
    public static final String PREFIX = "uid.serviceIp";


    /**
     * The default IP address. Used in case of errors.
     */
    private String defaultIpAddress = "127.0.0.1";


    /**
     * List of Java regular expressions for network interfaces that will be ignored.
     */
    private List<String> ignoredInterfaces = new ArrayList<>();

    /**
     * Whether to use only interfaces with site local addresses. See
     * {@link InetAddress#isSiteLocalAddress()} for more details.
     */
    private boolean useOnlySiteLocalInterfaces = false;

    /**
     * List of Java regular expressions for network addresses that will be preferred.
     */
    private List<String> preferredNetworks = new ArrayList<>();

    public static String getPREFIX() {
        return PREFIX;
    }


    public String getDefaultIpAddress() {
        return this.defaultIpAddress;
    }

    public void setDefaultIpAddress(String defaultIpAddress) {
        this.defaultIpAddress = defaultIpAddress;
    }

    public List<String> getIgnoredInterfaces() {
        return this.ignoredInterfaces;
    }

    public void setIgnoredInterfaces(List<String> ignoredInterfaces) {
        this.ignoredInterfaces = ignoredInterfaces;
    }

    public boolean isUseOnlySiteLocalInterfaces() {
        return this.useOnlySiteLocalInterfaces;
    }

    public void setUseOnlySiteLocalInterfaces(boolean useOnlySiteLocalInterfaces) {
        this.useOnlySiteLocalInterfaces = useOnlySiteLocalInterfaces;
    }

    public List<String> getPreferredNetworks() {
        return this.preferredNetworks;
    }

    public void setPreferredNetworks(List<String> preferredNetworks) {
        this.preferredNetworks = preferredNetworks;
    }

}

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

package com.zcckj.uid.utils;

import com.zcckj.uid.autoconfigure.InetUtilsProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Spencer Gibb
 */
public class InetUtils implements Closeable {

	// TODO: maybe shutdown the thread pool if it isn't being used?
	private final ExecutorService executorService;

	private final InetUtilsProperties properties;

	private final Log log = LogFactory.getLog(InetUtils.class);

	public InetUtils(final InetUtilsProperties properties) {
		this.properties = properties;
		this.executorService = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r);
			thread.setName(InetUtilsProperties.PREFIX);
			thread.setDaemon(true);
			return thread;
		});
	}

	@Override
	public void close() {
		this.executorService.shutdown();
	}

	public InetAddress findFirstNonLoopbackAddress() {
		InetAddress result = null;
		try {
			int lowest = Integer.MAX_VALUE;
			for (Enumeration<NetworkInterface> nics = NetworkInterface
					.getNetworkInterfaces(); nics.hasMoreElements();) {
				NetworkInterface ifc = nics.nextElement();
				if (ifc.isUp()) {
					this.log.trace("Testing interface: " + ifc.getDisplayName());
					if (ifc.getIndex() < lowest || result == null) {
						lowest = ifc.getIndex();
					}
					else if (result != null) {
						continue;
					}

					// @formatter:off
					if (!ignoreInterface(ifc.getDisplayName())) {
						for (Enumeration<InetAddress> addrs = ifc
								.getInetAddresses(); addrs.hasMoreElements();) {
							InetAddress address = addrs.nextElement();
							if (address instanceof Inet4Address
									&& !address.isLoopbackAddress()
                                    && !address.isAnyLocalAddress()
                                    && !address.isLinkLocalAddress()
									&& isPreferredAddress(address)) {
								this.log.trace("Found non-loopback interface: "
										+ ifc.getDisplayName());
								result = address;
							}
						}
					}
					// @formatter:on
				}
			}
		}
		catch (IOException ex) {
			this.log.error("Cannot get first non-loopback address", ex);
		}

		if (result != null) {
			return result;
		}

		try {
			return InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
			this.log.warn("Unable to retrieve localhost");
		}

		return null;
	}

	// For testing.
	boolean isPreferredAddress(InetAddress address) {

		if (this.properties.isUseOnlySiteLocalInterfaces()) {
			final boolean siteLocalAddress = address.isSiteLocalAddress();
			if (!siteLocalAddress) {
				this.log.trace("Ignoring address: " + address.getHostAddress());
			}
			return siteLocalAddress;
		}
		final List<String> preferredNetworks = this.properties.getPreferredNetworks();
		if (preferredNetworks.isEmpty()) {
			return true;
		}
		for (String regex : preferredNetworks) {
			final String hostAddress = address.getHostAddress();
			if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
				return true;
			}
		}
		this.log.trace("Ignoring address: " + address.getHostAddress());
		return false;
	}

	// For testing
	boolean ignoreInterface(String interfaceName) {
		for (String regex : this.properties.getIgnoredInterfaces()) {
			if (interfaceName.matches(regex)) {
				this.log.trace("Ignoring interface: " + interfaceName);
				return true;
			}
		}
		return false;
	}

}

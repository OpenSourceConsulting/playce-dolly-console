/* 
 * Athena Peacock Dolly - DataGrid based Clustering 
 * 
 * Copyright (C) 2013 Open Source Consulting, Inc. All rights reserved by Open Source Consulting, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * Sang-cheon Park	2014. 3. 31.		First Draft.
 */
package com.athena.dolly.console.module.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.athena.dolly.console.module.core.DollyConfig;
import com.athena.dolly.console.module.hotrod.ConfigurationException;
import com.athena.dolly.console.module.jmx.vo.MemoryVo;

/**
 * <pre>
 * 
 * </pre>
 * @author Sang-cheon Park
 * @version 1.0
 */
@Component
public class JmxClientManager implements InitializingBean {
	
    private static final Logger logger = LoggerFactory.getLogger(JmxClientManager.class);

	private DollyConfig config;
	private static Map<String, JmxClient> jmxClientMap;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (DollyConfig.properties == null || config == null) {
    		try {
                config = new DollyConfig().load();
			} catch (ConfigurationException e) {
				logger.error("[Dolly] Configuration error : ", e);
			}
    	}
		
		String[] jmxServers = config.getJmxServers();
		String user = config.getUser();
		String passwd = config.getPasswd();
		
		jmxClientMap = new HashMap<String, JmxClient>();
		
		JmxClient jmxClient = null;
		for (int i = 0; i < jmxServers.length; i++) {
			jmxClient = new JmxClient(jmxServers[i], user, passwd);
			jmxClientMap.put(i + "", jmxClient);
		}
		
		logger.debug("JMX Client Info : [{}]" + jmxClientMap);
	}

	/**
	 * <pre>
	 * 
	 * </pre>
	 * @param nodeName
	 * @return
	 */
	public static JmxClient getJmxClient(String nodeName) {
		return jmxClientMap.get(nodeName);
	}//end of getJmxClient()
	
	/**
	 * <pre>
	 * 
	 * </pre>
	 * @param nodeName
	 * @return
	 */
	public static boolean isValidNodeName(String nodeName) {
		return jmxClientMap.containsKey(nodeName);
	}//end of isValidNodeName()
	
	public static MemoryVo getMenoryUsage(String nodeName) {
		JmxClient jmxClient = jmxClientMap.get(nodeName);
        
        MemoryVo memory = null;
		try {
			ObjectName objectName=new ObjectName("java.lang:type=Memory");			
			
			MBeanServerConnection connection = jmxClient.getJmxConnector().getMBeanServerConnection();
			CompositeDataSupport heapMemoryUsage = (CompositeDataSupport)connection.getAttribute(objectName, "HeapMemoryUsage");
			
			memory = new MemoryVo();
	        memory.setCommitted((Long)heapMemoryUsage.get("committed"));
	        memory.setInit((Long)heapMemoryUsage.get("init"));
	        memory.setMax((Long)heapMemoryUsage.get("max"));
	        memory.setUsed((Long)heapMemoryUsage.get("used"));
		} catch (Exception e) {
			logger.error("unhandled exception has errored : ", e);
		}
        
        return memory;
	}
}
//end of JmxClientManager.java
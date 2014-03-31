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
 * Sang-cheon Park	2013. 12. 5.		First Draft.
 */
package com.athena.dolly.console.module.core;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.athena.dolly.console.module.hotrod.ConfigurationException;

/**
 * <pre>
 * Infinispan 관련 설정이 저장된 파일을 로드하고 파싱한다.
 * </pre>
 * @author Sang-cheon Park
 * @version 1.0
 */
public class DollyConfig {

	private static final String CONFIG_FILE = "dolly.properties";
	private static final String JMX_SERVER_LIST = "infinispan.jmx.server.list";
	private static final String JMX_USER = "infinispan.jmx.user";
    private static final String JMX_PASSWD = "infinispan.jmx.passwd";
    
    public static Properties properties;
    
    private String[] jmxServers;
    private String user;
    private String passwd;
    
	/**
	 * <pre>
	 * 프로퍼티 파일을 로드하고 파싱한다.
	 * </pre>
	 * @return
	 * @throws ConfigurationException
	 */
	public DollyConfig load() throws ConfigurationException {
		properties = loadConfigFile();
        parseConfigFile(properties);
        return this;
    }//end of load()

    /**
     * <pre>
     * System Property 또는 classpath에 존재하는 프로퍼티 파일을 로드한다.
     * </pre>
     * @return
     * @throws ConfigurationException
     */
    private Properties loadConfigFile() throws ConfigurationException {
    	InputStream configResource = null;
    	
        try {
        	String configFile = System.getProperty(CONFIG_FILE);
        	
        	if (configFile != null && !"".equals(configFile)) {
        		configResource = new BufferedInputStream(new FileInputStream(configFile));
        	} else {
        		configResource = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
        	}
        	
            if (configResource == null) {
                throw new FileNotFoundException("Could not locate " + CONFIG_FILE + " in the classpath or System Poroperty(-Ddolly.properties=Full Qualified File Name) path.");
            }
            
            Properties config = new Properties();
            config.load(configResource);

            configResource.close();
        	
            return config;
        } catch (IOException e) {
            throw new ConfigurationException("Could not load the configuration file (" + CONFIG_FILE + "). " +
                    "Please make sure it exists at the root of the classpath or System Poroperty(-Ddolly.properties=Full Qualified File Name) path.", e);
        }
    }//end of loadConfigFile()
    
    /**
     * <pre>
     * 프로퍼티 파일을 파싱한다.
     * </pre>
     * @param config
     * @throws ConfigurationException
     */
    private void parseConfigFile(Properties config) throws ConfigurationException {
    	extractJmxServerList(config);
    	extractJmxUser(config);
    	extractJmxPasswd(config);
    }//end of parseConfigFile()

    /**
     * <pre>
     * JMX 서버 목록을 확인한다.
     * </pre>
     * @param config
     */
    private void extractJmxServerList(Properties config) {
    	String serverList = config.getProperty(JMX_SERVER_LIST, null);
    	
    	if (StringUtils.isNotEmpty(serverList)) {
    		this.jmxServers = serverList.split(";");
    	}
    }//end of extractJmxServerList()

    /**
     * <pre>
     * JMX 접속을 위한 사용자 명을 확인한다.
     * </pre>
     * @param config
     */
    private void extractJmxUser(Properties config) {
    	this.user = config.getProperty(JMX_USER, null);
    }//end of extractJmxUser()

    /**
     * <pre>
     * JMX 접속을 위한 사용자 비밀번호를 확인한다.
     * </pre>
     * @param config
     */
    private void extractJmxPasswd(Properties config) {
    	this.passwd = config.getProperty(JMX_PASSWD, null);
    }//end of extractJmxPasswd()

	/**
	 * @return the jmxServers
	 */
	public String[] getJmxServers() {
		return jmxServers;
	}

	/**
	 * @param jmxServers the jmxServers to set
	 */
	public void setJmxServers(String[] jmxServers) {
		this.jmxServers = jmxServers;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the passwd
	 */
	public String getPasswd() {
		return passwd;
	}

	/**
	 * @param passwd the passwd to set
	 */
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
}
//end of DollyConfig.java
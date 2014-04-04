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
package com.athena.dolly.console.module.hotrod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.ServerStatistics;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.athena.dolly.console.module.core.DollyConfig;

/**
 * <pre>
 * Infinispan Data Grid Server에 대한 접속, 데이터 입력/조회/삭제 기능을 수행하는 관리 클래스
 * </pre>
 * @author Sang-cheon Park
 * @version 1.0
 */
public class DollyManager {
	
    private static final Logger logger = LoggerFactory.getLogger(DollyManager.class);

    private static DollyManager _instance;
	private static RemoteCache<String, Object> cache;
	private static DollyConfig config;

    /**
     * <pre>
     * Singleton 형태의 DollyManager 인스턴스를 가져온다.
     * </pre>
     * @return
     */
    public synchronized static DollyManager getInstance() {
        if (_instance == null) {
        	init();
            _instance = new DollyManager();
        }
        
        return _instance;
    }//end of getInstance()
    
    /**
     * <pre>
     * 주어진 프로퍼티를 이용하여 Infinispan Data Grid Server에 접속하여 RemoteCache object를 가져온다. 
     * </pre>
     */
    private static void init() {
    	if (DollyConfig.properties == null || config == null) {
    		try {
                config = new DollyConfig().load();
			} catch (ConfigurationException e) {
	            System.err.println("[Dolly] Configuration error : " + e.getMessage());
	            e.printStackTrace();
			}
    	}
    	
    	logger.debug("[Dolly] infinispan.client.hotrod.server_list={}", DollyConfig.properties.getProperty("infinispan.client.hotrod.server_list"));
    	
		ConfigurationBuilder builder = new ConfigurationBuilder();
	    cache = new RemoteCacheManager(builder.withProperties(DollyConfig.properties).build()).getCache();
    }//end of init()
    
    /**
     * <pre>
     * RemoteCache를 중지한다.
     * </pre>
     */
    public void destory() {
	    cache.stop();
    }//end of destory()

	/**
	 * <pre>
	 * RemoteCache에 주어진 Key / Value로 저장한다. 
	 * </pre>
	 * @param cacheKey
	 * @param value
	 */
	public synchronized void setValue(String cacheKey, Object value) {
		cache.put(cacheKey, value, -1, TimeUnit.DAYS, -1, TimeUnit.DAYS);
    }//end of setValue()
    
	/**
	 * <pre>
	 * RemoteCache에 Map 형태의 Data를 저장하기 위해 사용하며, 주어진 Cache Key / Data Key / Data Value로 저장한다.
	 * </pre>
	 * @param cacheKey
	 * @param dataKey
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public synchronized void setValue(String cacheKey, String dataKey, Object value) {
		if (dataKey != null) {
	    	Map<String, Object> attribute = (Map<String, Object>)cache.get(cacheKey);
			
			if (attribute == null) {
				attribute = new ConcurrentHashMap<String, Object>();
			}
			
			attribute.put(dataKey, value);
			cache.put(cacheKey, attribute, -1, TimeUnit.DAYS, -1, TimeUnit.DAYS);
		}
    }//end of setValue()
    
	/**
	 * <pre>
	 * RemoteCache에서 주어진 Key에 해당하는 Data를 조회한다.
	 * </pre>
	 * @param cacheKey
	 * @return
	 */
	public Object getValue(String cacheKey) {
    	return cache.get(cacheKey);
    }//end of getValue()
    
    /**
     * <pre>
     * RemoteCache에서 Map 형태의 Data를 조회하기 위해 사용하며, 주어진 Cache Key / Data Key에 해당하는 Data를 조회한다. 
     * </pre>
     * @param cacheKey
     * @param dataKey
     * @return
     */
    @SuppressWarnings("unchecked")
	public Object getValue(String cacheKey, String dataKey) {
    	Map<String, Object> attribute = (Map<String, Object>)cache.get(cacheKey);
		
		if (attribute == null) {
			return null;
		} else {
			return attribute.get(dataKey);
		}
    }//end of getValue()
    
    /**
     * <pre>
	 * RemoteCache에서 주어진 Key에 해당하는 Data를 제거한다.
     * </pre>
     * @param cacheKey
     */
	public synchronized void removeValue(String cacheKey) {
		cache.remove(cacheKey);
    }//end of removeValue()
    
    /**
     * <pre>
     * RemoteCache에서 Map 형태의 Data를 제거하기 위해 사용하며, 주어진 Cache Key / Data Key에 해당하는 Data를 제거한다. 
     * </pre>
     * @param cacheKey
     * @param dataKey
     */
    @SuppressWarnings("unchecked")
	public synchronized void removeValue(String cacheKey, String dataKey) {
    	Map<String, Object> attribute = (Map<String, Object>)cache.get(cacheKey);
		
		if (attribute != null) {
			attribute.remove(dataKey);
		}
    }//end of removeValue()
    
    /**
     * <pre>
     * RemoteCache에 저장된 Cache Key를 조회한다.
     * </pre>
     * @param cacheKey
     * @return
     */
	public List<SessionKey> getKeys() {
		List<SessionKey> sessionKeyList = new ArrayList<SessionKey>();
		Enumeration<String> cacheKeys = Collections.enumeration(cache.keySet());
	    SessionKey sessionKey = null;
	    while (cacheKeys.hasMoreElements()) {
	    	sessionKey = new SessionKey();
	    	sessionKey.setKey(cacheKeys.nextElement());
	    	sessionKeyList.add(sessionKey);
	    }
	    
	    return sessionKeyList;
    }//end of getKeys()
    
    /**
     * <pre>
     * RemoteCache에서 주어진 Cache Key에 해당하는 Data가 Map 형태일 경우 해당 Map의 키 목록을 Enumeration 형태로 조회한다.
     * </pre>
     * @param cacheKey
     * @return
     */
    @SuppressWarnings("unchecked")
	public Enumeration<String> getValueNames(String cacheKey) {
    	Map<String, Object> attribute = (Map<String, Object>)cache.get(cacheKey);
		
		if (attribute == null) {
			return null;
		} else {
			return Collections.enumeration(attribute.keySet());
		}
    }//end of getValueNames()
    
	/**
	 * <pre>
	 * Debug용으로써 RemoteCache 내에 존재하는 Data를 출력한다.
	 * </pre>
	 */
	public void printAllCache() {
	    System.out.println("ProtocolVersion : " + cache.getProtocolVersion());
	    System.out.println("Version : " + cache.getVersion());
	    System.out.println("isEmpty : " + cache.isEmpty());
	    System.out.println("Size : " + cache.size());
	    
	    Enumeration<String> cacheKeys = Collections.enumeration(cache.keySet());
	    String cacheKey = null;
    	Object data = null;
	    int i = 1;
	    while (cacheKeys.hasMoreElements()) {
	    	cacheKey = cacheKeys.nextElement();
    		System.out.println("================== Element index [" + i + "] ==================");
    		System.out.println("Cache Key : " + cacheKey);
	    	
	    	data = cache.get(cacheKey);
	    	
	    	if (data != null) {
    			System.out.println("Cache Data : " + data.toString());
	    	} else {
	    		System.out.println("Cache Data is NULL.");
	    	}
    		
    		System.out.println("");
    		i++;  
	    }
    }//end of printAllCache()
	
	/**
	 * <pre>
	 * Infinispan의 속성, 통계정보 및 저장된 키 목록을 조회한다.
	 * </pre>
	 * @return
	 */
	public DollyStats getStats() {
		DollyStats stats = new DollyStats();
		
		stats.setProtocolVersion(cache.getProtocolVersion());
		stats.setVersion(cache.getVersion());
		stats.setName(cache.getName());
		stats.setIsEmpty(cache.isEmpty());
		stats.setSize(cache.size());
		
		ServerStatistics statistics = cache.stats();
		stats.setTimeSinceStart(statistics.getStatistic(ServerStatistics.TIME_SINCE_START));
		stats.setCurrentNumberOfEntries(statistics.getStatistic(ServerStatistics.CURRENT_NR_OF_ENTRIES));
		stats.setTotalNumberOfEntries(statistics.getStatistic(ServerStatistics.TOTAL_NR_OF_ENTRIES));
		stats.setStores(statistics.getStatistic(ServerStatistics.STORES));
		stats.setRetrievals(statistics.getStatistic(ServerStatistics.RETRIEVALS));
		stats.setHits(statistics.getStatistic(ServerStatistics.HITS));
		stats.setMisses(statistics.getStatistic(ServerStatistics.MISSES));
		stats.setRemoveHits(statistics.getStatistic(ServerStatistics.REMOVE_HITS));
		stats.setRemoveMisses(statistics.getStatistic(ServerStatistics.REMOVE_MISSES));
		
		return stats;
	}//end of getStats()
	
	public static void main(String[] args) {
		UserVo user = null;
		for (int i = 0; i < 100; i++) {
			user = new UserVo();
			user.setUserId("osci_" + i);
			user.setEmail("support" + i + "@osck.kr");
			DollyManager.getInstance().setValue(UUID.randomUUID().toString(), user);
		}
		
		DollyManager.getInstance().printAllCache();
		DollyStats stat = DollyManager.getInstance().getStats();
		
		logger.debug("{}", stat);
		
		
		
		//DollyManager.getInstance().removeValue("cachekey");
	}
}
//end of DollyManager.java

class UserVo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String userId = "osci";
	private String firstName = "Open Source";
	private String lastName = "Consulting";
	private String email = "support@osci.kr";
	private int age = 30;
	private String address1 = "Address 1";
	private String address2 = "Address 2";
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}
	/**
	 * @param age the age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}
	/**
	 * @return the address1
	 */
	public String getAddress1() {
		return address1;
	}
	/**
	 * @param address1 the address1 to set
	 */
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	/**
	 * @return the address2
	 */
	public String getAddress2() {
		return address2;
	}
	/**
	 * @param address2 the address2 to set
	 */
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
}
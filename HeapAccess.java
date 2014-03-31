package com.athena.dolly.console.module.jmx;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.*;

import org.infinispan.interceptors.CacheMgmtInterceptor;
import org.infinispan.manager.DefaultCacheManager;
import org.jboss.remotingjmx.RemotingConnectorServer;
import org.xnio.Options;

/*
 * 
infinispan-cli-client-6.0.0.Final.jar
infinispan-commons-6.0.0.Final.jar
infinispan-core-6.0.0.Final.jar
jboss-as-jmx-7.2.0.Final.jar
jboss-as-osgi-jmx-7.2.0.Final.jar
jboss-as-system-jmx-7.2.0.Final.jar
jboss-logging-3.1.2.GA.jar
jboss-marshalling-1.3.18.GA.jar
jboss-marshalling-river-1.3.18.GA.jar
jboss-remoting-3.2.14.GA.jar
jboss-transaction-api_1.1_spec-1.0.1.Final.jar
remoting-jmx-1.1.0.Final.jar
xnio-api-3.0.7.GA.jar
xnio-nio-3.0.7.GA.jar 
 */

public class HeapAccess {
	
    public static void main(String[] args) throws Exception {
        String host = "192.168.0.172";  // Your JBoss Bind Address default is localhost
        int port = 10199;  // JBoss remoting port

        String urlString ="service:jmx:remoting-jmx://" + host + ":" + port;
        System.out.println("		\n\n\t****  urlString: "+urlString);

        HashMap   environment = new HashMap();
        
        String[]  credentials = new String[] {"test", "!test123"};
        environment.put (JMXConnector.CREDENTIALS, credentials);
        
        environment.put ("java.naming.factory.url.pkgs", "org.jboss.ejb.client.naming");
        environment.put ("remote.connectionprovider.create.options.org.xnio.Options.SSL_STARTTLS", false);

        
        JMXServiceURL serviceURL = new JMXServiceURL(urlString);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, environment);
        MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

        ObjectName objectName=new ObjectName("java.lang:type=Memory");
        CompositeDataSupport heapMemoryUsage =(CompositeDataSupport)connection.getAttribute(objectName, "HeapMemoryUsage");
        
        System.out.println("	committed                = " + heapMemoryUsage.get("committed"));
        System.out.println("	init                     = " + heapMemoryUsage.get("init"));
        System.out.println("	max                      = " + heapMemoryUsage.get("max"));
        System.out.println("	used                     = " + heapMemoryUsage.get("used"));

//        
//        objectName = new ObjectName("jboss.infinispan:type=Cache,name=\"default(dist_sync)\",manager=\"clustered\",component=Statistics");
//        System.out.println(connection.isRegistered(objectName));
//
//
//        
//        System.out.println(        	connection.getDefaultDomain()) ;
//        System.out.println(        	connection.getMBeanCount()) ;
//        System.out.println(        	connection.getMBeanInfo(objectName)) ;
//        
//        
//        for (int i=0; i<connection.getDomains().length;i++)
//        {
//        	System.out.println("key = " + connection.getDomains()[i].toString());
//        }
        

//      for (int i=0; i<connection.getMBeanInfo(objectName).getAttributes().length;i++)
//      {

      	MBeanInfo heapMemoryUsage2 = connection.getMBeanInfo(objectName);
      	
      	System.out.println(heapMemoryUsage2.getClass().getName());
      	
      	System.out.println(heapMemoryUsage2.getDescription());
      	System.out.println(heapMemoryUsage2.getAttributes().length);
      	
        for (int z=0; z<heapMemoryUsage2.getAttributes().length;z++)
        {
          	System.out.println("-----------------------");
          	
          	
          	
          	System.out.println(heapMemoryUsage2.getAttributes()[z].getName());
          	System.out.println(heapMemoryUsage2.getAttributes()[z].getType());
          	System.out.println(heapMemoryUsage2.getAttributes()[z].getClass().getName());
          	System.out.println(heapMemoryUsage2.getAttributes()[z].getDescriptor().toString());
        }
      	
      	
      	System.out.println(heapMemoryUsage2.getDescriptor().toString());      
    	  
//      }
//        
        
        
      
        
        
        jmxConnector.close();
        
    }
}

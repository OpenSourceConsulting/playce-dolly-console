/* 
 * Athena Peacock Dolly - DataGrid based Clustering 
 * 
 * Copyright (C) 2014 Open Source Consulting, Inc. All rights reserved by Open Source Consulting, Inc.
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
 * Sang-cheon Park	2014. 3. 27.		First Draft.
 */
package com.athena.dolly.console.web.contorller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.athena.dolly.console.module.hotrod.DollyManager;
import com.athena.dolly.console.module.hotrod.DollyStats;
import com.athena.dolly.console.module.hotrod.SessionKey;
import com.athena.dolly.console.module.jmx.JmxClientManager;
import com.athena.dolly.console.module.jmx.vo.MemoryVo;
import com.athena.dolly.console.web.exception.ResourceNotFoundException;

/**
 * <pre>
 * 
 * </pre>
 * @author Sang-cheon Park
 * @version 1.0
 */
@Controller
@RequestMapping("/")
public class ConsoleController {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleController.class);

    @RequestMapping(value="/getStat", method=RequestMethod.GET)
    @ResponseStatus(value=HttpStatus.OK)
    @ResponseBody
    public DollyStats getStat(HttpServletRequest request) {
    	
    	DollyStats stat = DollyManager.getInstance().getStats();
    	
    	if (stat == null) {
    		throw new ResourceNotFoundException("Resource Not Found at [" + request.getRequestURI() + "]");
        }
    	
    	return stat;
    }   

    @RequestMapping(value="/getSessionKeyList", method=RequestMethod.GET)
    @ResponseBody
    public List<SessionKey> getSessionKeyList(HttpServletRequest request) {
    	return DollyManager.getInstance().getKeys();
    }

    @RequestMapping(value="/getSessionData", method=RequestMethod.GET)
    @ResponseStatus(value=HttpStatus.OK)
    @ResponseBody
    public Object getSessionData(HttpServletRequest request, @QueryParam("key") String key) {
    	
    	if (key == null) {
    		throw new ResourceNotFoundException("Resource Not Found at [" + request.getRequestURI() + "]");
    	}
    	
    	return DollyManager.getInstance().getValue(key);
    }

    @RequestMapping(value="/deleteSessionData", method=RequestMethod.GET)
    @ResponseStatus(value=HttpStatus.OK)
    @ResponseBody
    public String deleteSessionData(HttpServletRequest request, @QueryParam("key") String key) {
    	
    	if (key == null) {
    		throw new ResourceNotFoundException("Resource Not Found at [" + request.getRequestURI() + "]");
    	}
    	
    	DollyManager.getInstance().removeValue(key);
    	
    	return "success";
    }
    
    @RequestMapping(value="/memory/{nodeName}", method=RequestMethod.GET)
    @ResponseStatus(value=HttpStatus.OK)
    @ResponseBody
    public MemoryVo momory(HttpServletRequest request, @PathVariable String nodeName) {
    	
    	if (!JmxClientManager.isValidNodeName(nodeName)) {
    		throw new ResourceNotFoundException("Resource Not Found at [" + request.getRequestURI() + "]");
    	}
    	
    	return JmxClientManager.getMenoryUsage(nodeName);
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
	public Response handleCustomException(ResourceNotFoundException ex) {
    	logger.error("ResourceNotFoundException has occurred. : ", ex);
		return Response.status(Status.NOT_FOUND).build();
	}
}
//end of ConsoleController.java
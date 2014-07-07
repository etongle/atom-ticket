/**
 * Author: obullxl@gmail.com
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.obullxl.ticket.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.obullxl.ticket.TicketService;
import com.github.obullxl.ticket.api.TicketException;

/**
 * Ticket工具类
 * 
 * @author obullxl@gmail.com
 * @version $Id: TicketUtils.java, V1.0.1 2013年11月25日 上午11:10:57 $
 */
public class TicketUtils {
    /** 票据容器 */
    private static final ConcurrentMap<String, TicketService> tickets = new ConcurrentHashMap<String, TicketService>();

    /**
     * 初始化
     */
    public void init() {
        tickets.clear();
    }

    /**
     * 注册服务到容器中
     */
    public static void regist(TicketService service) {
        regist(service.findName(), service);
    }

    /**
     * 注册服务到容器中
     */
    public static void regist(String name, TicketService service) {
        tickets.put(name, service);
    }
    
    /**
     * 获取票据服务
     */
    public static TicketService fetch(String name) {
        return tickets.get(name);
    }

    /**
     * 获取一个票据值
     */
    public static long nextValue(String name) throws TicketException {
        return fetch(name).nextValue();
    }
    
    /**
     * 获取一组票据值
     */
    public static long[] nextValues(String name, int count) throws TicketException {
        return fetch(name).nextValues(count);
    }

}

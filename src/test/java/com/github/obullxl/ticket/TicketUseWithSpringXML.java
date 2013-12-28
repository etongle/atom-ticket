/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.github.obullxl.ticket;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * SpringXML配置方式使用方式
 * 
 * @author obullxl@gmail.com
 * @version $Id: TicketUseWithSpringXML.java, 2012-10-19 下午9:42:49 Exp $
 */
public class TicketUseWithSpringXML {

    /**
     * 请首先执行SQL文件，以创建数据库和数据表，或者修改XML数据源的配置
     * <p/>
     * SQL文件：database.sql
     * <p/>
     * XML配置：how-to-use.xml
     */
    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/how-to-use.xml");
        TicketService ticket = context.getBean(TicketService.class);
        for (int i = 0; i < 100; i++) {
            System.out.println(ticket.nextValue());
        }
    }

}

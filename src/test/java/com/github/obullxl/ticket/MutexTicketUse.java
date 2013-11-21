/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.github.obullxl.ticket;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 单元测试
 * 
 * @author obullxl@gmail.com
 * @version $Id: AtomicTicket.java, 2012-10-19 下午9:42:49 Exp $
 */
public class MutexTicketUse {
    
    public void test_ticket() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/how-to-use.xml");
        MutexTicket ticket = context.getBean(MutexTicket.class);
        for (int i = 0; i < 100; i++) {
            System.out.println(ticket.nextValue());
        }
    }

}

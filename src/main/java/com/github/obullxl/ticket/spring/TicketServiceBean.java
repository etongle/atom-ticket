/**
 * Author: obullxl@gmail.com
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.obullxl.ticket.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.obullxl.ticket.TicketService;
import com.github.obullxl.ticket.utils.TicketUtils;

/**
 * 票据服务Spring组件
 * <p/>
 * <b>功能：</b>注册票据服务到{@code TicketUtils}工具类中！
 * 
 * @author obullxl@gmail.com
 * @version $Id: TicketServiceBean.java, V1.0.1 2013年11月25日 上午11:20:52 $
 */
public class TicketServiceBean implements ApplicationContextAware {

    /** 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        String[] names = context.getBeanNamesForType(TicketService.class);
        if (names == null || names.length < 1) {
            return;
        }

        for (String name : names) {
            TicketService service = context.getBean(name, TicketService.class);
            TicketUtils.regist(service);
        }
    }

}

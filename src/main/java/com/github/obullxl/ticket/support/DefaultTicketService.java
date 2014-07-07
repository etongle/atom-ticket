/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.github.obullxl.ticket.support;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;

import com.github.obullxl.lang.utils.LogUtils;
import com.github.obullxl.ticket.TicketService;
import com.github.obullxl.ticket.api.TicketDAO;
import com.github.obullxl.ticket.api.TicketException;
import com.github.obullxl.ticket.api.TicketRange;

/**
 * 票据默认实现
 * 
 * @author obullxl@gmail.com
 * @version $Id: DefaultTicketService.java, 2012-10-19 下午9:50:27 Exp $
 */
public class DefaultTicketService implements TicketService {
    private static final Logger  logger = LogUtils.get();

    /** LOCK */
    private final Lock           lock   = new ReentrantLock();

    /** 序列名称 */
    private String               name;

    /** DAO */
    private TicketDAO            ticketDAO;

    /** 初始化标志 */
    private boolean              initTicket;

    /** 序列 */
    private volatile TicketRange currentTicket;

    /**
     * 初始化
     */
    public void init() {
        Validate.notNull(this.name, "票据名称注入失败！");
        Validate.notNull(this.ticketDAO, "接口[TicketDAO]注入失败！");

        if (this.initTicket) {
            boolean init = this.ticketDAO.initTicket(this.name);
            if (!init) {
                String msg = "序列[" + this.name + "]初始化失败！";
                logger.error(msg, new RuntimeException(msg));
            }
        }
    }

    /** 
     * @see com.github.obullxl.ticket.TicketService#findName()
     */
    public String findName() {
        return this.name;
    }

    /**
     * @see com.github.obullxl.ticket.TicketService#nextValue()
     */
    public long nextValue() throws TicketException {
        if (this.currentTicket == null) {
            this.lock.lock();
            try {
                if (this.currentTicket == null) {
                    this.currentTicket = this.ticketDAO.nextRange(name);
                }
            } finally {
                this.lock.unlock();
            }
        }

        long value = this.currentTicket.getAndIncrement();
        if (value == -1) {
            this.lock.lock();
            try {
                for (;;) {
                    this.currentTicket = this.ticketDAO.nextRange(this.name);

                    value = this.currentTicket.getAndIncrement();
                    if (value < 1) {
                        continue;
                    }

                    break;
                }
            } finally {
                this.lock.unlock();
            }
        }

        if (value < 0) {
            throw new TicketException("Sequence value overflow, value = " + value);
        }

        return value;
    }

    /** 
     * @see com.github.obullxl.ticket.TicketService#nextValues(int)
     */
    public long[] nextValues(int count) throws TicketException {
        count = Math.max(count, 1);
        count = Math.min(count, 1000);

        long[] result = new long[count];

        for (int i = 0; i < count; i++) {
            result[i] = this.nextValue();
        }

        return result;
    }

    // ~~~~~~~~~~ getters and setters ~~~~~~~~~~~ //

    public void setName(String name) {
        this.name = name;
    }

    public void setTicketDAO(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    public void setInitTicket(boolean initTicket) {
        this.initTicket = initTicket;
    }

}

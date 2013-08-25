/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.atom.core.ticket;

/**
 * 票据
 * 
 * @author obullxl@gmail.com
 * @version $Id: MutexTicket.java, 2012-10-19 下午9:47:53 Exp $
 */
public interface MutexTicket {

    /**
     * 取得序列下一个值
     */
    public long nextValue() throws TicketException;

}

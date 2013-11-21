/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.github.obullxl.ticket.api;


/**
 * 票据DAO
 * 
 * @author obullxl@gmail.com
 * @version $Id: TicketDAO.java, 2012-10-19 下午9:49:05 Exp $
 */
public interface TicketDAO {

    /**
     * 取得下一个可用的序列区间
     *
     * @param name 序列名称
     * @return 返回下一个可用的序列区间
     */
   public TicketRange nextRange(String name) throws TicketException;

}

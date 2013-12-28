/**
 * Author: obullxl@gmail.com
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.obullxl.ticket.fmt;

/**
 * 票据号编码转换
 * 
 * @author obullxl@gmail.com
 * @version $Id: TicketEncode.java, V1.0.1 2013年12月28日 上午9:29:04 $
 */
public interface TicketEncode {

    /**
     * 编码转换
     */
    public String encode(long ticket);

}

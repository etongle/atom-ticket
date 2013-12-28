/**
 * Author: obullxl@gmail.com
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.obullxl.ticket.support;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.github.obullxl.lang.utils.DateUtils;
import com.github.obullxl.ticket.fmt.TicketEncode;

/**
 * 票据号编码转换默认实现
 * 
 * @author obullxl@gmail.com
 * @version $Id: DefaultTicketEncode.java, V1.0.1 2013年12月28日 上午9:30:29 $
 */
public class DefaultTicketEncode implements TicketEncode {

    /**
     * @see com.github.obullxl.ticket.fmt.TicketEncode#encode(long)
     */
    public String encode(long ticket) {
        String no = StringUtils.leftPad(Long.toString(ticket), 10, "0");
        return "E" + DateUtils.toStringDS(new Date()) + no;
    }

}

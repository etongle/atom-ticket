/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.atom.core.ticket;

/**
 * 票据异常
 * 
 * @author obullxl@gmail.com
 * @version $Id: TicketException.java, 2012-10-19 下午9:46:51 Exp $
 */
public class TicketException extends Exception {
    private static final long serialVersionUID = -2844751099881749528L;

    public TicketException() {
    }

    public TicketException(String message) {
        super(message);
    }

    public TicketException(String message, Throwable cause) {
        super(message, cause);
    }

    public TicketException(Throwable cause) {
        super(cause);
    }

}

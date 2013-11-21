/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.github.obullxl.ticket.api;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 票据区间
 * 
 * @author obullxl@gmail.com
 * @version $Id: AtomicTicket.java, 2012-10-19 下午9:42:49 Exp $
 */
public class TicketRange {
    private final long minValue;
    private final long maxValue;

    private final AtomicLong value;
    
    public TicketRange(long minValue, long maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = new AtomicLong(minValue);
    }

    public long getAndIncrement() {
        long currentValue = value.getAndIncrement();
        if (currentValue > maxValue) {
            return -1;
        }

        return currentValue;
    }

    public long getMinValue() {
        return minValue;
    }

    public long getMaxValue() {
        return maxValue;
    }
    
}

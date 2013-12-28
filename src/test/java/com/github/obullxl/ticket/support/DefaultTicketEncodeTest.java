/**
 * Author: obullxl@gmail.com
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.obullxl.ticket.support;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.github.obullxl.lang.utils.DateUtils;

/**
 * DefaultTicketEncode测试
 * 
 * @author obullxl@gmail.com
 * @version $Id: DefaultTicketEncodeTest.java, V1.0.1 2013年12月28日 上午9:34:00 $
 */
public class DefaultTicketEncodeTest {
    private DefaultTicketEncode encode = new DefaultTicketEncode();

    /**
     * encode
     */
    @Test
    public void test_encode() {
        long id = 909;
        String date = DateUtils.toStringDS(new Date());
        String actual = encode.encode(id);

        Assert.assertEquals(actual, "E" + date + "0000000909");
    }

}

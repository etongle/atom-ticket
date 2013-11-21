/**
 * Author: obullxl@gmail.com
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.obullxl.ticket;

import org.apache.commons.dbcp.BasicDataSource;

import com.github.obullxl.ticket.support.DefaultTicketDAO;
import com.github.obullxl.ticket.support.DefaultTicketService;

/**
 * Java代码的方式使用方式
 * 
 * @author obullxl@gmail.com
 * @version $Id: TicketUserWithJavaApp.java, V1.0.1 2013年11月21日 下午2:54:46 $
 */
public class TicketUserWithJavaCode {

    /**
     * 请首先执行SQL文件，以创建数据库和数据表
     * <p/>
     * SQL文件：database.sql
     */
    public static void main(String[] args) throws Exception {
        BasicDataSource ds = null;
        try {
            ds = new BasicDataSource();
            ds.setDriverClassName("com.mysql.jdbc.Driver");
            ds.setUrl("jdbc:mysql://127.0.01:3306/mplat?useUnicode=true&amp;characterEncoding=UTF8");
            ds.setUsername("mplat");
            ds.setPassword("mplat");

            DefaultTicketDAO dao = new DefaultTicketDAO();
            dao.setDataSource(ds);
            dao.setRetryTimes(50);
            dao.setStep(100);
            dao.setTableName("adm_mutex_ticket");
            dao.setNameColumnName("name");
            dao.setValueColumnName("value");
            dao.setStampColumnName("stamp");

            DefaultTicketService service = new DefaultTicketService();
            service.setName("USER-INFO-ID");
            service.setTicketDAO(dao);

            for (int i = 0; i < 100; i++) {
                System.out.println(service.nextValue());
            }
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
    }

}

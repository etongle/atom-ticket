/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.atom.core.ticket.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

import com.atom.core.ticket.AtomicTicket;
import com.atom.core.ticket.TicketDAO;
import com.atom.core.ticket.TicketException;

/**
 * 票据DAO默认实现
 * 
 * @author obullxl@gmail.com
 * @version $Id: DefaultTicketDAO.java, 2012-10-19 下午9:56:05 Exp $
 */
public class DefaultTicketDAO implements TicketDAO, InitializingBean {
    private static final int     MIN_STEP              = 1;
    private static final int     MAX_STEP              = 100000;
    private static final int     DEFAULT_STEP          = 1000;
    private static final int     DEFAULT_RETRY_TIMES   = 150;

    private static final String  DEFAULT_TABLE_NAME    = "adm_mutex_ticket";
    private static final String  DEFAULT_NAME_CN_NAME  = "name";
    private static final String  DEFAULT_VALUE_CN_NAME = "value";
    private static final String  DEFAULT_STAMP_CN_NAME = "stamp";

    private static final long    DELTA                 = 100000000L;

    private DataSource           dataSource;

    /** 重试次数 */
    private int                  retryTimes            = DEFAULT_RETRY_TIMES;

    /** 步长 */
    private int                  step                  = DEFAULT_STEP;

    /** 序列所在的表名 */
    private String               tableName             = DEFAULT_TABLE_NAME;

    /** 存储序列名称的列名 */
    private String               nameColumnName        = DEFAULT_NAME_CN_NAME;

    /** 存储序列值的列名 */
    private String               valueColumnName       = DEFAULT_VALUE_CN_NAME;

    /** 存储序列最后更新时间的列名 */
    private String               stampColumnName       = DEFAULT_STAMP_CN_NAME;

    private final Lock           selectLock            = new ReentrantLock();
    private volatile String      selectSQL;

    private final Lock           updateLock            = new ReentrantLock();
    private volatile String      updateSQL;

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.notNull(this.dataSource, "数据源注入失败！");
    }

    /**
     * @see com.atom.core.ticket.TicketDAO#nextRange(java.lang.String)
     */
    public AtomicTicket nextRange(String name) throws TicketException {
        if (name == null) {
            throw new IllegalArgumentException("序列名称不能为空");
        }

        long oldValue;
        long newValue;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        for (int i = 0; i < this.retryTimes + 1; ++i) {
            try {
                conn = this.dataSource.getConnection();
                pstmt = conn.prepareStatement(this.findSelectSQL());
                pstmt.setString(1, name);
                rs = pstmt.executeQuery();
                rs.next();
                oldValue = rs.getLong(1);

                if (oldValue < 0) {
                    StringBuilder message = new StringBuilder();
                    message.append("Sequence value cannot be less than zero, value = ").append(oldValue);
                    message.append(", please check table ").append(this.tableName);

                    throw new TicketException(message.toString());
                }

                if (oldValue > Long.MAX_VALUE - DELTA) {
                    StringBuilder message = new StringBuilder();
                    message.append("Sequence value overflow, value = ").append(oldValue);
                    message.append(", please check table ").append(this.tableName);

                    throw new TicketException(message.toString());
                }

                newValue = oldValue + this.step;
            } catch (SQLException e) {
                throw new TicketException(e);
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(conn);
            }

            try {
                conn = this.dataSource.getConnection();
                pstmt = conn.prepareStatement(this.findUpdateSQL());
                pstmt.setLong(1, newValue);
                pstmt.setLong(2, System.currentTimeMillis());
                pstmt.setString(3, name);
                pstmt.setLong(4, oldValue);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    continue;
                }

                return new AtomicTicket(oldValue + 1, newValue);
            } catch (SQLException e) {
                throw new TicketException(e);
            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(conn);
            }
        }

        throw new TicketException("Retried too many times, retryTimes = " + retryTimes);
    }

    private String findSelectSQL() {
        if (this.selectSQL == null) {
            this.selectLock.lock();
            try {
                if (this.selectSQL == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("select ").append(this.valueColumnName);
                    buffer.append(" from ").append(this.tableName);
                    buffer.append(" where ").append(this.nameColumnName).append(" = ?");

                    this.selectSQL = buffer.toString();
                }
            } finally {
                this.selectLock.unlock();
            }
        }

        return this.selectSQL;
    }

    private String findUpdateSQL() {
        if (this.updateSQL == null) {
            this.updateLock.lock();
            try {
                if (this.updateSQL == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("update ").append(this.tableName);
                    buffer.append(" set ").append(this.valueColumnName).append(" = ?, ");
                    buffer.append(this.stampColumnName).append(" = ? where ");
                    buffer.append(this.nameColumnName).append(" = ? and ");
                    buffer.append(this.valueColumnName).append(" = ?");

                    this.updateSQL = buffer.toString();
                }
            } finally {
                this.updateLock.unlock();
            }
        }

        return this.updateSQL;
    }

    // ~~~~~~~~~~~~~ getters and setters ~~~~~~~~~~~~~ //

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setRetryTimes(int retryTimes) {
        if (retryTimes < 0) {
            throw new IllegalArgumentException("Property retryTimes cannot be less than zero, retryTimes = " + retryTimes);
        }

        this.retryTimes = retryTimes;
    }

    public void setStep(int step) {
        if (step < MIN_STEP || step > MAX_STEP) {
            StringBuilder message = new StringBuilder();
            message.append("Property step out of range [").append(MIN_STEP);
            message.append(",").append(MAX_STEP).append("], step = ").append(step);

            throw new IllegalArgumentException(message.toString());
        }

        this.step = step;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setNameColumnName(String nameColumnName) {
        this.nameColumnName = nameColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public void setStampColumnName(String stampColumnName) {
        this.stampColumnName = stampColumnName;
    }

}

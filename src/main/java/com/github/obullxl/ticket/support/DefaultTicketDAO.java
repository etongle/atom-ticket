/**
 * aBoy.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.github.obullxl.ticket.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;

import com.github.obullxl.lang.utils.DBUtils;
import com.github.obullxl.lang.utils.LogUtils;
import com.github.obullxl.ticket.api.TicketDAO;
import com.github.obullxl.ticket.api.TicketException;
import com.github.obullxl.ticket.api.TicketRange;

/**
 * 票据DAO默认实现
 * 
 * @author obullxl@gmail.com
 * @version $Id: DefaultTicketDAO.java, 2012-10-19 下午9:56:05 Exp $
 */
public class DefaultTicketDAO implements TicketDAO {
    /** Logger */
    private static final Logger logger                = LogUtils.get();

    private static final int    DEFAULT_RETRY_TIMES   = 10;

    private static final String DEFAULT_TABLE_NAME    = "atom_ticket";
    private static final String DEFAULT_NAME_CN_NAME  = "name";
    private static final String DEFAULT_VERN_CN_NAME  = "version";
    private static final String DEFAULT_STEP_CN_NAME  = "step";
    private static final String DEFAULT_VALUE_CN_NAME = "ticket";
    private static final String DEFAULT_MINV_CN_NAME  = "minv";
    private static final String DEFAULT_MAXV_CN_NAME  = "maxv";
    private static final String DEFAULT_CYCLE_CN_NAME = "cycle";

    /** 数据源 */
    private DataSource          dataSource;

    /** 重试次数 */
    private int                 retryTimes            = DEFAULT_RETRY_TIMES;

    /** 序列所在的表名 */
    private String              tableName             = DEFAULT_TABLE_NAME;

    /** 存储序列名称的列名 */
    private String              nameColumnName        = DEFAULT_NAME_CN_NAME;

    /** 存储序列版本的列名 */
    private String              versionColumnName     = DEFAULT_VERN_CN_NAME;

    /** 存储序列步长的列名 */
    private String              stepColumnName        = DEFAULT_STEP_CN_NAME;

    /** 存储序列值的列名 */
    private String              valueColumnName       = DEFAULT_VALUE_CN_NAME;

    /** 存储序列最小值的列名 */
    private String              minvColumnName        = DEFAULT_MINV_CN_NAME;

    /** 存储序列最大值的列名 */
    private String              maxvColumnName        = DEFAULT_MAXV_CN_NAME;

    /** 存储序列是否转圈的列名 */
    private String              cycleColumnName       = DEFAULT_CYCLE_CN_NAME;

    // ~~~~~~~~~~~~~ 线程锁 ~~~~~~~~~~~~ //

    private final Lock          selectLock            = new ReentrantLock();
    private volatile String     selectSQL;

    private final Lock          updateLock            = new ReentrantLock();
    private volatile String     updateSQL;

    /**
     * 初始化
     */
    public void init() {
        Validate.notNull(this.dataSource, "数据源注入失败！");

        this.selectSQL = this.findSelectSQL();
        logger.warn("[统一编号]-SelectSQL: " + this.selectSQL);

        this.updateSQL = this.findUpdateSQL();
        logger.warn("[统一编号]-UpdateSQL: " + this.updateSQL);
    }

    /** 
     * @see com.github.obullxl.ticket.api.TicketDAO#initTicket(java.lang.String)
     * <br/>
     * SQL: INSERT INTO atom_ticket VALUES ('TB-PUBLIC-ID', 5, 10, 1, 1, 9999999999, 'TRUE');
     */
    public boolean initTicket(String name) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(this.tableName).append("(");
        sql.append(this.nameColumnName);
        sql.append(",").append(this.versionColumnName);
        sql.append(",").append(this.stepColumnName);
        sql.append(",").append(this.valueColumnName);
        sql.append(",").append(this.minvColumnName);
        sql.append(",").append(this.maxvColumnName);
        sql.append(",").append(this.cycleColumnName);
        sql.append(") VALUES(?, ?, ?, ?, ?, ?, ?)");

        logger.warn("[统一编号]-票据[{}]初始化SQL: {}", name, sql.toString());

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;
        for (int i = 0; i < 3; i++) {
            try {
                conn = this.dataSource.getConnection();

                pstmt = conn.prepareStatement(this.findSelectSQL());
                pstmt.setString(1, name);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    // 已经存在
                    logger.warn("[统一编号]-票据[{}]已经存在，无需初始化！", name);
                    return true;
                }

                // 不存在则初始化
                pstmt2 = conn.prepareStatement(sql.toString());
                pstmt2.setString(1, name);
                pstmt2.setLong(2, 1);
                pstmt2.setLong(3, 10);
                pstmt2.setLong(4, 1);
                pstmt2.setLong(5, 1);
                pstmt2.setLong(6, 9999999999L);
                pstmt2.setString(7, "TRUE");

                int count = pstmt2.executeUpdate();
                logger.warn("[统一编号]-票据[{}]初始化完成[{}].", name, count);

                return true;
            } catch (Exception e) {
                logger.error("[统一编号]-票据[{}]初始化异常！", name, e);
            } finally {
                DBUtils.closeQuietly(rs);
                DBUtils.closeQuietly(pstmt);
                DBUtils.closeQuietly(pstmt2);
                DBUtils.closeQuietly(conn);
            }
        }

        // 重试均失败
        return false;
    }

    /**
     * @see com.github.obullxl.ticket.api.TicketDAO#nextRange(java.lang.String)
     */
    public TicketRange nextRange(String name) throws TicketException {
        if (name == null) {
            throw new IllegalArgumentException("序列名称不能为空");
        }

        long oldValue; // 数据库值
        long newValue; // 数据库需要更新的值
        long startValue; // 序列开始数据值

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;

        for (int i = 0; i < this.retryTimes + 1; ++i) {
            try {
                conn = this.dataSource.getConnection();

                pstmt = conn.prepareStatement(this.findSelectSQL());
                pstmt.setString(1, name);
                rs = pstmt.executeQuery();
                rs.next();

                long stepValue = rs.getLong(this.stepColumnName);
                if (stepValue < 1) {
                    StringBuilder message = new StringBuilder();
                    message.append("Step value cannot be less than zero, step=").append(stepValue);
                    message.append(", please check table ").append(this.tableName);
                    throw new TicketException(message.toString());
                }

                long minValue = rs.getLong(this.minvColumnName);
                if (minValue < 1) {
                    StringBuilder message = new StringBuilder();
                    message.append("Min value cannot be less than zero, min=").append(minValue);
                    message.append(", please check table ").append(this.tableName);
                    throw new TicketException(message.toString());
                }

                long maxValue = rs.getLong(this.maxvColumnName);
                if (maxValue < 1) {
                    StringBuilder message = new StringBuilder();
                    message.append("Max value cannot be less than zero, max=").append(maxValue);
                    message.append(", please check table ").append(this.tableName);
                    throw new TicketException(message.toString());
                }

                if (minValue >= maxValue) {
                    StringBuilder message = new StringBuilder();
                    message.append("Max value must be greater than min value, max=").append(maxValue);
                    message.append(", min=").append(minValue).append(", please check table ").append(this.tableName);
                    throw new TicketException(message.toString());
                }

                oldValue = rs.getLong(this.valueColumnName);
                startValue = oldValue;
                if (oldValue < 0) {
                    StringBuilder message = new StringBuilder();
                    message.append("Sequence value cannot be less than zero, value=").append(oldValue);
                    message.append(", please check table ").append(this.tableName);
                    throw new TicketException(message.toString());
                }

                // 转圈设置
                boolean cycleFlag = BooleanUtils.toBoolean(rs.getString(this.cycleColumnName));
                if (oldValue >= maxValue) {
                    if (cycleFlag) {
                        startValue = minValue - 1;
                        newValue = minValue + stepValue;
                    } else {
                        StringBuilder message = new StringBuilder();
                        message.append("Sequence value excude max value, value=").append(oldValue);
                        message.append(", please check table ").append(this.tableName);
                        throw new TicketException(message.toString());
                    }
                } else {
                    newValue = oldValue + stepValue;
                    if (newValue > maxValue) {
                        newValue = maxValue;
                    }
                }

                // 更新数据值
                long versionValue = rs.getLong(this.versionColumnName);

                pstmt2 = conn.prepareStatement(this.findUpdateSQL());
                pstmt2.setLong(1, newValue);
                pstmt2.setString(2, name);
                pstmt2.setLong(3, versionValue);
                pstmt2.setLong(4, oldValue);

                int affectedRows = pstmt2.executeUpdate();
                if (affectedRows == 0) {
                    // 再次重试
                    continue;
                }

                // 获取成功
                return new TicketRange(startValue + 1, newValue);
            } catch (SQLException e) {
                throw new TicketException(e);
            } finally {
                DBUtils.closeQuietly(rs);
                DBUtils.closeQuietly(pstmt);
                DBUtils.closeQuietly(pstmt2);
                DBUtils.closeQuietly(conn);
            }
        }

        throw new TicketException("Retried too many times, retryTimes=" + retryTimes);
    }

    /**
     * SELECT查询SQL
     * <p/>
     * SELECT * FROM table WHERE name=?
     */
    private String findSelectSQL() {
        if (this.selectSQL == null) {
            this.selectLock.lock();
            try {
                if (this.selectSQL == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("SELECT * FROM ").append(this.tableName);
                    buffer.append(" WHERE ").append(this.nameColumnName).append("=?");

                    this.selectSQL = buffer.toString();
                }
            } finally {
                this.selectLock.unlock();
            }
        }

        return this.selectSQL;
    }

    /**
     * UPDATE更新SQL
     * <p/>
     * UPDATE table SET value=?, version=version+1 WHERE name=? AND version=? AND value=?
     */
    private String findUpdateSQL() {
        if (this.updateSQL == null) {
            this.updateLock.lock();
            try {
                if (this.updateSQL == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("UPDATE ").append(this.tableName);
                    buffer.append(" SET ");
                    buffer.append(this.valueColumnName).append("=?, ");
                    buffer.append(this.versionColumnName).append("=").append(this.versionColumnName).append("+1");
                    buffer.append(" WHERE ");
                    buffer.append(this.nameColumnName).append("=? AND ");
                    buffer.append(this.versionColumnName).append("=? AND ");
                    buffer.append(this.valueColumnName).append("=?");

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

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setNameColumnName(String nameColumnName) {
        this.nameColumnName = nameColumnName;
    }

    public void setVersionColumnName(String versionColumnName) {
        this.versionColumnName = versionColumnName;
    }

    public void setStepColumnName(String stepColumnName) {
        this.stepColumnName = stepColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public void setMinvColumnName(String minvColumnName) {
        this.minvColumnName = minvColumnName;
    }

    public void setMaxvColumnName(String maxvColumnName) {
        this.maxvColumnName = maxvColumnName;
    }

    public void setCycleColumnName(String cycleColumnName) {
        this.cycleColumnName = cycleColumnName;
    }

}

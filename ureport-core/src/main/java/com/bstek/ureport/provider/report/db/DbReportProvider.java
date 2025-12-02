/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.provider.report.db;

import cn.hutool.core.util.IdUtil;
import com.bstek.ureport.UReportProperties;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@ConditionalOnBean(DataSource.class)
public class DbReportProvider implements ReportProvider {

    private static final String PREFIX = "db:";

    private final String tableName;

    private final String columnId;
    private final String columnName;
    private final String columnContent;

    private final String columnUpdateTime;


    public DbReportProvider(UReportProperties p, JdbcTemplate jdbcTemplate) {
        UReportProperties.DbConfig dbConfig = p.getDbConfig();
        this.tableName = dbConfig.getTableName();
        this.columnId = dbConfig.getColumnId();
        this.columnName = dbConfig.getColumnName();
        this.columnContent = dbConfig.getColumnContent();
        this.columnUpdateTime = dbConfig.getColumnUpdateTime();
        this.jdbcTemplate =jdbcTemplate;

        Assert.hasText(tableName, "Table name cannot be empty");
        Assert.hasText(columnId, "Column id cannot be empty");
        Assert.hasText(columnName, "Column name cannot be empty");
        Assert.hasText(columnContent, "Column content cannot be empty");
        Assert.hasText(columnUpdateTime, "Column updateTime cannot be empty");

        this.init();
    }

    private JdbcTemplate jdbcTemplate;

    private void init() {
        try {
            // Check if table exists by querying it
            jdbcTemplate.queryForRowSet("SELECT count(*) FROM " + tableName);
        } catch (Exception e) {
            // Table doesn't exist, create it
            log.info("Table doesn't exist, create it");
            String ddl = "create table " + tableName + "(" +
                    columnId + " char(32) primary key ," +
                    columnName + " varchar(80)," +
                    columnContent + " longtext," +
                    columnUpdateTime + " timestamp)";
            log.info("ddl: {}", ddl);
            jdbcTemplate.execute(ddl);
        }
    }

    @Override
    public String loadReport(String file) {
        try {
            Map<String, Object> map = jdbcTemplate.queryForMap("select * from " + tableName + " where " + columnName + "=?", file);

            return (String) map.get(columnContent);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteReport(String file) {
        jdbcTemplate.update("delete from " + tableName + " where " + columnName + "=?", file);
    }

    @Override
    public List<ReportFile> getReportFiles() {
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select * from " + tableName + " order by " + columnUpdateTime + " desc ");

        List<ReportFile> list = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            String name = (String) map.get(columnName);
            if(name.startsWith(PREFIX)){
                name = name.substring(PREFIX.length());
            }
            Timestamp updateTime = (Timestamp) map.get(columnUpdateTime);
            list.add(new ReportFile(name, updateTime));
        }

        return list;
    }

    @Override
    public String getName() {
        return "数据库";
    }

    @Override
    public void saveReport(String file, String content) {
        String old = this.loadReport(file);
        if (old == null) {
            String sql = "insert into " + tableName + "(" + columnId + "," + columnName + "," + columnContent + "," + columnUpdateTime + ") values(?,?,?,?)";
            jdbcTemplate.update(sql, IdUtil.fastSimpleUUID(), file, content, new Date());
            return;
        }
        String sql = "update " + tableName + " set " + columnContent + "=?, " + columnUpdateTime + "=? where " + columnName + "=?";
        jdbcTemplate.update(sql, content, new Date(), file);
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}

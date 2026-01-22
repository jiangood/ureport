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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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
        UReportProperties.DbConfig dbConfig = p.getDb();
        this.tableName = dbConfig.getTableName();
        this.columnId = dbConfig.getColumnId();
        this.columnName = dbConfig.getColumnName();
        this.columnContent = dbConfig.getColumnContent();
        this.columnUpdateTime = dbConfig.getColumnUpdateTime();
        this.jdbcTemplate = jdbcTemplate;

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
        file = cleanPrefix(file);
        log.debug("加载基于数据库的文件： {}", file);
        file = StrUtil.removePrefix(file, PREFIX);
        log.debug("移除前缀后 {}", file);

        String sql = "select * from " + tableName + " where " + columnName + "=?";
        log.debug("生成的SQL：{}", sql);

        // queryForMap会抛出异常，这里简单用List 表示
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, file);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        Map<String, Object> map = list.get(0);

        return (String) map.get(columnContent);

    }

    @Override
    public void deleteReport(String file) {
        log.debug("加载基于数据库的文件： {}", file);
        file = cleanPrefix(file);
        log.debug("移除前缀后 {}", file);

        jdbcTemplate.update("delete from " + tableName + " where " + columnName + "=?", file);
    }

    private static String cleanPrefix(String file) {
        file = StrUtil.removePrefix(file, PREFIX);
        return file;
    }

    @Override
    public List<ReportFile> getReportFiles() {
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select * from " + tableName + " order by " + columnUpdateTime + " desc ");

        List<ReportFile> list = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            String name = (String) map.get(columnName);
            if (name.startsWith(PREFIX)) {
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
        file = cleanPrefix(file);
        log.info("准备保存 {}", file);
        String old = this.loadReport(file);
        if (old == null) {
            String sql = "insert into " + tableName + "(" + columnId + "," + columnName + "," + columnContent + "," + columnUpdateTime + ") values(?,?,?,?)";
            log.info("插入记录SQL: {}", sql);
            int rows = jdbcTemplate.update(sql, IdUtil.fastSimpleUUID(), file, content, new Date());
            log.info("影响行数:{}", rows);
            Assert.state(rows == 1, "保存报表失败");
            return;
        }
        String sql = "update " + tableName + " set " + columnContent + "=?, " + columnUpdateTime + "=? where " + columnName + "=?";
        log.info("更新记录 SQL: {} ", sql);
        int rows = jdbcTemplate.update(sql, content, new Date(), file);
        log.info("影响行数:{}", rows);
        Assert.state(rows == 1, "更新报表失败");
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}

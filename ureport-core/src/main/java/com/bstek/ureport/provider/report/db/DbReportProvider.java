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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
@ConditionalOnBean(DataSource.class)
@NoArgsConstructor
public class DbReportProvider implements ReportProvider {


    private String tableName;

    private String columnId;
    private String columnName;
    private String columnContent;

    private String columnUpdateTime;
    private String prefix = "db:";

    public DbReportProvider(UReportProperties p) {
        UReportProperties.DbConfig dbConfig = p.getDbConfig();
        this.tableName = dbConfig.getTableName();
        this.columnId = dbConfig.getColumnId();
        this.columnName = dbConfig.getColumnName();
        this.columnContent = dbConfig.getColumnContent();
        this.columnUpdateTime = dbConfig.getColumnUpdateTime();
    }

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    void init() {
        try {
            // Check if table exists by querying it
            jdbcTemplate.queryForRowSet("SELECT count(*) FROM " + tableName);
        } catch (Exception e) {
            // Table doesn't exist, create it
            String ddl = "create table " + tableName + "(" +
                    columnId + " int primary key ," +
                    columnName + " varchar(80)," +
                    columnContent + " longtext," +
                    columnUpdateTime + " timestamp)";
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
            Long updateTime = (Long) map.get(columnUpdateTime);
            list.add(new ReportFile(name, new Date(updateTime)));
        }

        return list;
    }

    @Override
    public String getName() {
        return "数据库";
    }

    @Override
    public void saveReport(String file, String content) {
        String id = IdUtil.simpleUUID();
        String old = this.loadReport(file);
        if (old == null) {
            String sql = "insert into " + tableName + "(" + columnId + "," + columnName + "," + columnContent + "," + columnUpdateTime + ") values(?,?,?,?)";
            jdbcTemplate.update(sql, file, content, new Date());
        }

    }

    @Override
    public String getPrefix() {
        return prefix;
    }
}

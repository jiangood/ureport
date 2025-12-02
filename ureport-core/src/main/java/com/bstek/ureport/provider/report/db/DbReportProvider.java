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
@ConditionalOnMissingBean(DbReportProvider.class)
@NoArgsConstructor
public class DbReportProvider implements ReportProvider {

    private static final String DEFAULT_TABLE_NAME = "sys_ureport";

    private static final String DEFAULT_COLUMN_ID = "id";
    private static final String DEFAULT_COLUMN_NAME = "file";
    private static final String DEFAULT_COLUMN_CONTENT = "content";
    private static final String DEFAULT_COLUMN_UPDATE_TIME = "update_time";


    private String tableName = DEFAULT_TABLE_NAME;

    private String columnId = DEFAULT_COLUMN_ID;
    private String columnName = DEFAULT_COLUMN_NAME;
    private String columnContent = DEFAULT_COLUMN_CONTENT;

    private String columnUpdateTime = DEFAULT_COLUMN_UPDATE_TIME;
    private String prefix = "db:";

    public DbReportProvider(String tableName, String columnId, String columnName, String columnContent, String columnUpdateTime) {
        this.tableName = tableName;
        this.columnId = columnId;
        this.columnName = columnName;
        this.columnContent = columnContent;
        this.columnUpdateTime = columnUpdateTime;
    }

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    void init(){
        try {
            // Check if table exists by querying it
            jdbcTemplate.queryForRowSet("SELECT count(*) FROM " + tableName );
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
        Map<String, Object> map = jdbcTemplate.queryForMap("select * from " + tableName + " where " + columnName + "=?", file);
        String content = (String) map.get(columnContent);

        return content;
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
        String sql = "insert into " + tableName + "(" + columnName + "," + columnContent + "," + columnUpdateTime + ") values(?,?,?)";
        jdbcTemplate.update(sql, file, content, new Date());
    }

    @Override
    public String getPrefix() {
        return prefix;
    }
}

package com.bstek.ureport;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ureport")
@Data
public class UReportProperties {


    /**
     * 文件存储目录
     */
    private String fileDir = "/ureport";

    /**
     * 可选， 数据库配置，默认使用jdbc数据源
     */

    private DbConfig db = new DbConfig();


    @Data
    public static class DbConfig {


        private String tableName = "sys_ureport";

        private String columnId = "id";
        private String columnName = "file_name";
        private String columnContent = "content";
        private String columnUpdateTime = "update_time";
    }
}

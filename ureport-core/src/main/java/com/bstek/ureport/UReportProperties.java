package com.bstek.ureport;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ureport")
@Data
public class UReportProperties {


    /**
     * 文件存储目录
     */
    private String fileStoreDir = "ureport-files";

    /**
     * 可选， 数据库配置，默认使用jdbc数据源
     */

    private DbConfig dbConfig = new DbConfig();


    @Data
    public static class DbConfig {


        private String tableName = "sys_ureport";

        private String columnId = "id";
        private String columnName = "file";
        private String columnContent = "content";
        private String columnUpdateTime = "update_time";
    }
}

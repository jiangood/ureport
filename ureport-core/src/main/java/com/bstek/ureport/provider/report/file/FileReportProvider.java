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
package com.bstek.ureport.provider.report.file;

import cn.hutool.core.io.FileUtil;
import com.bstek.ureport.UReportProperties;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;


@Component
public class FileReportProvider implements ReportProvider {
    private static final String PREFIX = "file:";

    private final File storeDir;

    public FileReportProvider(UReportProperties props) throws IOException {
        storeDir = new File(props.getFileDir());
        FileUtils.forceMkdir(storeDir);
    }


    @Override
    public String loadReport(String file) {
        File finalFile = getFinalFile(file);
        return FileUtil.readUtf8String(finalFile);
    }

    @Override
    public void deleteReport(String file) {
        File finalFile = getFinalFile(file);
        FileUtil.del(finalFile);
    }

    @Override
    public List<ReportFile> getReportFiles() {
        Collection<File> files = FileUtils.listFiles(storeDir, null, true);
        return files.stream()
                .map(f -> new ReportFile(f.getName(), parseFileDate(f)))
                .sorted(Comparator.comparing(ReportFile::getUpdateDate))
                .toList();
    }

    @Override
    public String getName() {
        return "文件夹 (" + storeDir.getAbsolutePath() + ")";
    }

    @Override
    public void saveReport(String file, String content) {
        File finalFile = getFinalFile(file);
        FileUtil.writeUtf8String(content, finalFile);
    }

    private File getFinalFile(String file) {
        if (file.startsWith(PREFIX)) {
            file = file.substring(PREFIX.length());
        }
        return new File(storeDir, file);
    }

    private Date parseFileDate(File file) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(file.lastModified());
        return calendar.getTime();
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}

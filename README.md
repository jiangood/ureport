# ureport-spring
基于开源项目ureport2的springboot3.x版

ureport用起来挺顺手，为了简化修复bug及新增功能，将其修改为spring版本

![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/ureport-console) 

## 使用方式
### 1 在原有项目中引入依赖
```
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>ureport-console</artifactId>
    <version>version</version>
</dependency>
```

### 2.启动项目
访问 http://localhost:8080/ureport/designer 

### 3 可选配值
配置文件中 application.yml 增加配置,ureport开头，ide会自动提示

[UReportProperties.java](ureport-core%2Fsrc%2Fmain%2Fjava%2Fcom%2Fbstek%2Fureport%2FUReportProperties.java)
## 示例项目
可参考示例项目
https://github.com/jiangood/ureport-example

## 文档
https://www.w3cschool.cn/ureport/

# 新增函数
## 字符串函数
- 转数字 toInt
- 减法（参考减法-） sub


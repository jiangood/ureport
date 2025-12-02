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
### 2 配值（可选，不配置默认使用默认配置）
参考配置类，[src/main/java/com/bstek/ureport/UReportProperties.java]()
   
 在你的springboot配置文件 如application.yml 中添加以ureport开头的配置
### 3.启动项目
访问 http://localhost:8080/ureport/designer 

## 示例项目
可参考示例项目
https://github.com/jiangood/ureport-example

## 文档
https://www.w3cschool.cn/ureport/

# 新增函数
## 字符串函数
- 转数字 toInt
- 减法（参考减法-） sub


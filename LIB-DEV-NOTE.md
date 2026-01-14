# 库开发的一些记录，好记性不如烂笔头

## 随着springboot的升级，词法分析库 antlr4 升级到 4.13.0, 否者和jpa冲突了

1 当然是修改 pom.xml

2 重新生成相关类，主要是 com.bstek.ureport.dsl 包下面
https://github.com/antlr/antlr4/blob/master/doc/java-target.md

可使用 idea插件生成。 安装插件后，点击 .g4 文件。 
右键先配置，填写包名。
右键选择生成，最后将生成的类复制过去
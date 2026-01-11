HoYo-Achievement-Server
===========

## ℹ️ 简介
HoYo-Achievement 项目的后端服务核心。

本项目基于 Java 开发，采用 RESTful API 架构，为前端提供业务接口支持，并负责解析和同步 [HoYo-Achievement-Data](https://github.com/ShawnSjl/HoYo-Achievement-Data) 中的游戏数据。

## ✨ 主要功能
用户系统: 
  - 目前由管理员负责创建用户。

  - 一个用户可至多创建10个游戏成就账号（本地或云端）。

成就管理:
  - 对于已登录用户，支持用户成就状态同步（已完成/未完成/进度）至数据库。

  - 对于未登录用户，支持浏览器本地数据缓存。

  - 支持成就的导入和导出。

## ⚙️ 技术栈
语言: Java 21

框架: Spring Boot 3.x

数据库: MySQL

ORM: MyBatis-Plus / Spring Data JPA

工具: Maven, Lombok

## 🚀 快速开始
环境要求
- JDK 17+
- MySQL 8.0+

创建数据库
```SQL
create schema hoyo_achievement collate utf8mb4_0900_ai_ci;
```

下载数据
- 从[HoYo-Achievement-Data](https://github.com/ShawnSjl/HoYo-Achievement-Data)中克隆数据到本地文件夹

运行
```sh
export DB_PWD=your_actual_password

java -jar HoYo_Achievement_Server.jar \
  --spring.datasource.url="jdbc:mysql://localhost:3306/hoyo_achievement?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai" \
  --spring.datasource.username=root \
  --spring.datasource.password=${DB_PWD} \
  --app.data-folder="/path/to/data" \
  --server.port=8686
```
⚠️请自行修改以上参数中的内容

## 🔐 管理员账号
当程序运行时，如果发现没有初始化管理员账号，则会立即生成一个。

默认账号：`root`

默认随机密码：在log中显示
- 可以通过在启动时添加`--app.admin.initial-password=xxxxx`来指定初始密码

## 📝 日志管理
默认情况下，日志会输出到控制台。如果你需要将日志保存到文件并按天自动切割（每天生成一个新文件，只保留最近30天），请执行以下操作：

在 HoYo_Achievement_Server.jar 的同级目录下，创建一个名为 logback-spring.xml 的文件。模板内容：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="./logs" />
    <property name="APP_NAME" value="hoyo_server" />

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APP_NAME}.log</file>
        
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APP_NAME}.%d{yyyy-MM-dd}.log</fileNamePattern>
            
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>

        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```
在启动前，添加参数`--logging.config=./logback-spring.xml`，Spring Boot 会检测并应用该配置。日志将生成在 ./logs 目录下。

## 🛠️ 相关仓库
1. 前端：[HoYo-Achievement-Web](https://github.com/ShawnSjl/HoYo-Achievement-Web)
2. 数据：[HoYo-Achievement-Data](https://github.com/ShawnSjl/HoYo-Achievement-Data)

# ⚠️ 免责声明
本仓库仅用于整理和存储游戏相关数据，供玩家工具使用。

所有的游戏文本、图标、数据版权均归 miHoYo / HoYoverse 所有。

本项目与 HoYoverse 没有任何官方关联。

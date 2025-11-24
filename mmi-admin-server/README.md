# mmi-admin-server

## Requirement 
* openjdk:11
* spring-boot 2.6.2


## Introduction 
1. mmi-admin-server is a server provide management features of User, role, UserGroup and so on. 

## Configuration
1. I18n 
```
kp:
  uaa:
    resourceserver:
      authorizationPaths: /token/login,/inner/user/ids
      permitAllPaths: /error
  i18n:
    basename: i18n/uaaServer,i18n/admin
    defaultLocale:
      lang: en
      country: US
    supportLocales:
      - lang: zh
        country: CN
      - lang: en
        country: US
```
2. mybatis
```
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    auto-mapping-behavior: full
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath*:mapper/*.xml
mybatis:
  mapper:
    dialect: mysql
    location: sg.ncs.kp.uaa.server.mapper,sg.ncs.kp.admin.mapper

```
3. spring, datasource, redis
```
spring:
  application:
    name: mmi-admin
    version: @version@
    code: 01
  datasource:
    url: jdbc:mysql://${DB_IP:172.31.2.239}:${DB_PORT:3307}/${DB_NAME:mmi-uaa}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2b8&allowMultiQueries=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:qAcP@ssw0rd4Mys@L}
    driver-class-name: com.mysql.cj.jdbc.Driver
    autoReconnect: true
  redis:
    timeout: 6000
    sentinel:
      master: ${REDIS_MASTER_NAME:redis-master}
      nodes: ${REDIS_SENTINEL_NODES:172.31.2.207:26379}
    database: 0
    password: ${REDIS_PASSWORD:redispass}
``` 
4. eureka
```
eureka:
  instance:
    hostname: ${EUREKA_HOST:127.0.0.1}
    prefer-ip-address: true
  client:
    registerWithEureka: true
    fetchRegistry: true
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${EUREKA_PORT:26500}/eureka/
```
5. oss
```
oss:
  type: local
  path: ${OSS_PATH:D://}
```

## Introduce permission related table and api 
### 数据库
1. 权限信息表:kp_permission
2. 字段：  
    1.id 主键  
    2.type 权限类型 1：模块 2：菜单 3：页面 4：按钮  
    3.name 权限名称，用来对外展示  
    4.authority_key 权限标识，用于程序接口权限控制（唯一）  
    5.sort 排序，升序 默认999  
    6.status 状态 1：启用 0.禁用  
    7.level 权限等级，用于控制该权限哪个等级得用户可见 1.superAdmin 2.admin 3.user  
    8.parent_id 该权限所属得父级权限id  
    9.uri 前端页面跳转路径  

### 接口
1. [/user/menus](https://gitlab.kaisquare.com/kaipro/kp-admin/-/blob/main/api/0.2.0/openapi.yaml#/User%20Api/get_user_menus)  
        该接口返回的权限信息不包含按钮（type=4）的数据。由于i18n的缘故，用于展示的菜单名称不会维护在数据库里，而是前端根据authority_key以及i18n的配置动态获取的。
2. [/user/getMyself](https://gitlab.kaisquare.com/kaipro/kp-admin/-/blob/main/api/0.2.0/openapi.yaml#/User%20Api/get_user_getMyself)  
        该接口可以获取到当前登录用户的信息，包括用户的基本信息，角色信息，权限信息等。其中可以从返回的permissions属性中获取到用户所具备的所有authority_key（包括按钮）。  
```json
{
  "permissions": [
    "identifiedPersonByAttributesList",
    "poiInterRegionalAnalysisList",
    "alarm",
    "poiTrack",
    "satelliteImageAnalytic",
    "humanAnalysis",
    "face",
    "frequentlyAccessRegionAnalysisEdit",
    "channelGroup",
    "......"
  ]
}
```
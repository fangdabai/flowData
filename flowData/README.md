## readme



只是基本的SpringBoot项目，拿来即用

用maven搭建的springboot项目，集成了springdoc、swagger、mybatis，只是搭建了框架，待完善代码





# ==--------------------------------==









# 待整理





## 给log4j2穿上彩色衣服

https://developer.aliyun.com/article/1115656



## MyBatis-Plus官网

https://baomidou.com/getting-started/config/





## 使用 JPA 和 MyBatis-Plus 项目树形结构对比图

**1、使用 JPA 的项目树形结构**

~~~
springboot-jpa-example/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── myproject/
│   │   │               ├── Application.java           # 启动类
│   │   │               ├── controller/               # 控制器层
│   │   │               │   └── MyController.java     # 控制器类
│   │   │               ├── service/                  # 服务层
│   │   │               │   └── MyService.java        # 服务类
│   │   │               ├── repository/               # 数据访问层（JPA使用 repository）
│   │   │               │   └── MyRepository.java     # JPA的 Repository
│   │   │               ├── model/                    # 实体类
│   │   │               │   └── MyEntity.java         # JPA 实体类（对应数据库表）
│   │   │               └── config/                   # 配置类
│   │   │                   └── AppConfig.java         # 配置类
│   │   ├── resources/
│   │   │   ├── application.properties                  # 配置文件
│   │   │   └── templates/                              # 模板文件（如 Thymeleaf 使用）
│   │   └── static/                                     # 静态资源文件
├── pom.xml                                              # 项目依赖管理文件
└── README.md                                            # 项目说明文件

~~~

**2、使用 MyBatis-Plus 的项目树形结构**

~~~
springboot-mybatisplus-example/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── myproject/
│   │   │               ├── Application.java           # 启动类
│   │   │               ├── controller/               # 控制器层
│   │   │               │   └── MyController.java     # 控制器类
│   │   │               ├── service/                  # 服务层
│   │   │               │   └── MyService.java        # 服务类
│   │   │               ├── mapper/                   # 数据访问层（MyBatis-Plus 使用 mapper）
│   │   │               │   └── MyMapper.java         # MyBatis-Plus Mapper
│   │   │               ├── model/                    # 实体类
│   │   │               │   └── MyEntity.java         # MyBatis-Plus 实体类（对应数据库表）
│   │   │               └── config/                   # 配置类
│   │   │                   └── MybatisPlusConfig.java # MyBatis-Plus 配置类
│   │   ├── resources/
│   │   │   ├── application.properties                  # 配置文件
│   │   │   └── mapper/                                 # MyBatis 映射文件（如果使用 XML）
│   │   │       └── MyMapper.xml                        # MyBatis 映射文件（可选）
│   │   └── static/                                     # 静态资源文件
├── pom.xml                                              # 项目依赖管理文件
└── README.md                                            # 项目说明文件

~~~

**3、主要区别**
数据访问层 (repository vs mapper)

JPA: 使用 repository 来表示数据访问层，继承 JpaRepository 或 CrudRepository 来进行数据库操作。
MyBatis-Plus: 使用 mapper 来表示数据访问层，继承 BaseMapper 或通过 XML 配置进行 SQL 映射。
实体类 (model / entity)

在 JPA 中，实体类通常使用 @Entity 注解来标记，同时使用 @Id 标记主键。
在 MyBatis-Plus 中，实体类使用 @TableName 和 @TableId 来标记数据库表和主键。
配置类 (config)

JPA: 配置类通常是 Spring Boot 配置，可能包括 JPA 配置，数据源配置等。
MyBatis-Plus: 除了基本的配置文件，还可能包括 MyBatis-Plus 特有的配置类（如分页插件、性能分析插件等）。







## 数据库逆向-生成mapper







## 上传到GitHub

我的idea未更新导致无法通过idea上传，于是先使用基础的方式

git add .

git commit -m "提交备注"

git push


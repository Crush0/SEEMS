# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

SEEMS (Ship Energy Efficiency Management System) 是一个船舶能效管理系统，采用前后端分离架构。

### 技术栈

**后端 (seems-backend)**
- Spring Boot 3.3.3
- Java 17
- MyBatis Plus 3.5.8
- MySQL 数据库
- Redis 缓存
- Socket.IO (netty-socketio 2.0.11) - 实时数据推送
- JWT 认证 (jjwt 0.12.6)
- Apache POI 5.3.0 - Excel 报表生成

**前端 (seems-frontend)**
- Vue 3 + TypeScript
- Vite 3 构建工具
- Arco Design 组件库
- Pinia 状态管理
- ECharts 图表库
- Socket.io-client 实时通信
- 高德地图 (@amap/amap-jsapi-loader)
- Three.js 3D 渲染

**脚本 (seems-script)**
- Python 数据分析脚本

## 常用开发命令

### 后端开发

```bash
# 进入后端目录
cd seems-backend

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn clean package

# 运行应用（开发环境）
mvn spring-boot:run

# 运行打包后的 JAR
java -jar target/SEEMS-0.0.1-SNAPSHOT.jar
```

### 前端开发

```bash
# 进入前端目录
cd seems-frontend

# 安装依赖
npm install

# 开发环境运行（支持热更新）
npm run dev

# 构建生产环境版本
npm run build

# 预览生产构建
npm run preview

# 类型检查
npm run type:check

# 代码检查和修复
npm run lint-fix

# 查看构建分析报告
npm run report
```

## 项目架构

### 后端架构

```
seems-backend/
├── src/main/java/cn/edu/just/ytc/seems/
│   ├── controller/          # REST API 控制器
│   ├── service/            # 业务逻辑层
│   │   └── Impl/           # 业务逻辑实现
│   ├── mapper/             # MyBatis 数据访问层
│   ├── pojo/
│   │   ├── entity/         # 数据库实体
│   │   ├── dto/            # 数据传输对象
│   │   └── vo/             # 视图对象
│   ├── config/             # Spring 配置类
│   ├── analyze/            # 数据分析工具类
│   ├── handler/            # Socket.IO 消息处理器
│   ├── executor/           # Python 脚本执行器
│   ├── annotation/         # 自定义注解（如权限控制）
│   ├── aop/                # AOP 切面（如权限检查）
│   ├── interceptor/        # 拦截器（如用户信息）
│   ├── exception/          # 自定义异常
│   ├── utils/              # 工具类
│   └── run/                # 后台服务运行器
└── src/main/resources/
    ├── application.yml     # 主配置文件
    └── static/             # 静态资源
```

### 前端架构

```
seems-frontend/
├── src/
│   ├── api/                # API 接口封装
│   ├── assets/             # 静态资源
│   ├── components/         # 通用组件
│   ├── config/             # 配置文件
│   ├── layout/             # 布局组件
│   ├── router/             # 路由配置
│   │   ├── routes/modules/ # 路由模块
│   │   └── guard/          # 路由守卫
│   ├── store/              # Pinia 状态管理
│   ├── utils/              # 工具函数
│   ├── views/              # 页面组件
│   │   ├── dashboard/      # 仪表盘（工作台、轨迹、监控）
│   │   ├── visualization/  # 数据可视化
│   │   ├── report/         # 报表管理
│   │   ├── personnel/      # 人员管理
│   │   └── user/           # 用户管理
│   └── types/              # TypeScript 类型定义
└── config/                 # Vite 构建配置
```

### 核心功能模块

**数据流程**
1. 船舶传感器数据 → Redis 缓存 → Python 脚本分析 → MySQL 存储
2. 实时数据通过 Socket.IO 推送到前端
3. 前端使用 ECharts 展示图表分析结果

**权限控制**
- 基于 JWT 的用户认证
- 自定义 `@HasPermission` 注解进行权限检查
- AOP 切面处理权限逻辑

**数据分析**
- Python 脚本执行器 (`ScriptExecutor`)
- 能耗计算 (`EnergyConsumptionCalc`)
- GPS 数据计算 (`GPSCalc`)
- 航行统计 (`SpeedStats`)
- 中午报告生成 (`NoonReportGenerator`)

## 开发注意事项

### 后端开发

1. **数据库配置**
   - 默认数据库：`seems` @ localhost:3306
   - 使用 MyBatis Plus 的自动建表功能 (`@EnableAutoTable`)
   - 所有实体类需继承 `BaseEntity`

2. **实时通信**
   - Socket.IO 端口：33000
   - 消息处理器：`SocketHandler`、`AliMsgHandler`
   - Redis 订阅发布用于实时数据推送

3. **Python 脚本集成**
   - 脚本路径：`seems-script/run.py`
   - Python 环境：`seems-script/.venv/Scripts/python.exe`
   - 在 `application.yml` 中配置脚本路径

4. **API 前缀**
   - 所有 API 路径前缀：`/api`
   - 例如：`http://localhost:8080/api/user/info`

### 前端开发

1. **路由和权限**
   - 路由守卫：`router/guard/` 目录
   - 权限控制基于用户角色（admin, user 等）
   - 使用 `requiresAuth` 和 `roles` 字段控制访问

2. **状态管理**
   - 用户状态：`store/modules/user/`
   - 船舶数据：`store/modules/ship-data/`
   - 应用配置：`store/modules/app/`

3. **API 调用**
   - 使用 Axios 封装的 API 请求
   - 自动携带 JWT Token
   - 统一错误处理（`api/interceptor.ts`）

4. **实时数据**
   - Socket.IO 连接管理
   - 船舶实时数据展示在监控页面
   - 使用 Pinia store 管理实时数据状态

### 环境配置

**后端 (application.yml)**
- 数据库连接信息
- Redis 配置
- Socket.IO 端口配置
- 异步线程池配置
- Python 脚本路径

**前端 (环境变量)**
- `VITE_API_BASE_URL`: 后端 API 地址
- 在 `config/vite.config.*.ts` 中配置环境变量

## 故障排查

1. **后端无法启动**
   - 检查 MySQL 和 Redis 服务是否运行
   - 检查端口占用（8080, 33000）
   - 查看 `application.yml` 配置

2. **前端构建失败**
   - 确保 Node.js 版本 >= 14.0.0
   - 删除 `node_modules` 重新安装依赖
   - 检查 TypeScript 类型错误

3. **实时数据不更新**
   - 检查 Socket.IO 连接状态
   - 确认 Redis 消息通道是否正常
   - 检查 Python 脚本是否正常运行

## 代码规范

- 后端使用 Lombok 简化代码
- 前端使用 ESLint + Prettier 代码格式化
- 提交代码前运行 `npm run lint-fix` 修复格式问题
- 使用 Husky 进行 Git 提交前检查

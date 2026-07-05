# Docker

本目录保存本地开发依赖配置。

## 服务

`docker-compose.yml` 包含：

- MySQL 8.0，数据库名 `agent_study`
- Redis 7.2，开启 AOF 持久化

## 启动

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study
docker compose -f .\docker\docker-compose.yml up -d
```

## 停止

```powershell
docker compose -f .\docker\docker-compose.yml down
```

如需删除本地数据卷：

```powershell
docker compose -f .\docker\docker-compose.yml down -v
```

## 默认连接信息

- MySQL: `localhost:3306`
- Database: `agent_study`
- User: `root`
- Password: `agentstudy`
- Redis: `localhost:6379`

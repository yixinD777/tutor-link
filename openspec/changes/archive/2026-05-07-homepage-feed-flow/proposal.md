## Why

首页核心功能链路不通：搜索框点击无搜索能力、科目 Tab 筛选因参数不匹配完全失效、无分页加载更多。用户进入首页后看到的是固定 6 条数据，无法交互、无法筛选、无法翻页，体验等同于静态页面。

## What Changes

- 修复首页科目 Tab 筛选：将硬编码的科目名字符串改为从 `/subjects` 接口获取 subjectId，使筛选真正生效
- 新增关键词搜索能力：后端 `TutorSearchService` 增加 keyword 参数，对 university/major/intro 做模糊匹配；首页搜索框输入后跳转列表页并携带关键词自动搜索
- 首页改为 feed 流无限滚动：去掉写死的 `size=6`，增加 `onReachBottom` 分页加载
- 新增地区和价格区间筛选：首页增加地区 picker 和价格 picker，调用后端已有的 `province/city/hourlyRateMin/hourlyRateMax` 参数
- 首页去掉"更多 ›"入口（feed 流模式下不再需要）
- 批量插入 300+ 条测试家教数据，支撑 feed 流滚动体验

## Capabilities

### New Capabilities
- `tutor-keyword-search`: 后端关键词搜索能力，对家教档案的 university/major/intro 字段进行模糊匹配
- `homepage-feed-flow`: 首页 feed 流分页加载 + 多维筛选（科目/地区/价格），替换当前固定 6 条的静态展示
- `bulk-tutor-seed-data`: 存储过程批量生成 300+ 条测试家教数据

### Modified Capabilities

## Impact

- **前端**：`pages/index/index.vue` 模板和逻辑大幅改造；`pages/tutor/list.vue` 增加接收 keyword 查询参数
- **后端**：`TutorSearchService` 增加 keyword 模糊查询；`TutorController` 增加 keyword 参数
- **数据库**：新增 Flyway 迁移脚本，存储过程批量插入测试数据
- **API**：`GET /api/v1/tutors` 增加 `keyword` 可选参数（向后兼容）

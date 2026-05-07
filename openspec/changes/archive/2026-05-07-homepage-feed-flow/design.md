## Context

当前首页家教展示是静态的 6 条固定数据，核心交互链路不通：

- 搜索框点击仅跳转列表页，不携带关键词
- 科目 Tab 传 `subject="数学"` 字符串，后端只认 `subjectId` (Long)，筛选无效
- 无分页加载，用户滑到底即结束
- 后端 `TutorSearchService` 已支持 subjectId/province/city/hourlyRate 筛选，但缺少 keyword 模糊搜索

后端现有接口能力较完整，主要问题集中在前端参数传递和缺失的 keyword 搜索。

## Goals / Non-Goals

**Goals:**
- 首页搜索框输入关键词后跳转列表页并自动搜索
- 科目 Tab 使用 subjectId 筛选，真正生效
- 首页改为 feed 流无限滚动
- 首页增加地区和价格区间筛选
- 后端增加 keyword 模糊搜索（university/major/intro）
- 批量插入 300+ 条测试数据

**Non-Goals:**
- 不做 OFFSET 深翻页优化（cursor-based 分页），当前量级不需要
- 不做科目筛选从两步查询改为 JOIN 的优化
- 不做 fillUserInfo 的 JOIN 优化
- 不做 province/city 索引优化
- 不做搜索结果缓存

## Decisions

### D1: 关键词搜索使用 LIKE 模糊匹配

**选择**: `LIKE '%keyword%'` 对 university、major、intro 三个字段做 OR 匹配

**替代方案**:
- Elasticsearch 全文检索：当前数据量（<1万）完全不需要，引入 ES 增加运维复杂度
- MySQL FULLTEXT 索引：需要修改表引擎和建索引，LIKE 在小数据量下够用

**理由**: 300-1000 条数据量下 LIKE 性能完全可接受。后续数据增长到万级时可切换 ES。

### D2: 首页搜索框采用"跳转带参"模式

**选择**: 点击搜索框跳转列表页，若已输入关键词则通过 URL query 传递

**替代方案**:
- 首页内嵌搜索：需要重构首页布局，且搜索输入体验不如独立页面

**理由**: 列表页已有完整的搜索+筛选+分页逻辑，跳转带参是最小改动方案，复用现有能力。

### D3: Feed 流使用 OFFSET 分页而非 cursor

**选择**: 继续使用 MyBatis-Plus 的 `selectPage`（OFFSET 分页）

**替代方案**:
- Cursor-based (keyset pagination): 用 `WHERE avg_rating < last_rating OR (avg_rating = last_rating AND id < last_id)` 避免深翻页退化

**理由**: 当前数据量下 OFFSET 完全够用。cursor 方案在多排序维度（rating/price/order_count）下实现复杂，且需要改后端返回格式。留作后续优化。

### D4: 地区筛选从 tutor_profile 去重获取

**选择**: 新增 `/api/v1/tutors/regions` 接口，返回已有家教覆盖的 province/city 列表

**替代方案**:
- 从 area 字典表获取：area 表是行政区划全量数据，大多数地区没有家教，会产生大量空选项

**理由**: 只展示有家教的地区，用户体验更好。

### D5: 批量测试数据使用存储过程生成

**选择**: Flyway 迁移脚本中用 MySQL 存储过程随机生成 300+ 条 user + tutor_profile + tutor_subject 记录

**替代方案**:
- 手写 INSERT：几百条不现实
- Java 代码初始化：需要写 runner，且依赖 Spring 上下文

**理由**: 存储过程在 Flyway 迁移时自动执行，一次到位，不依赖应用层。

## Risks / Trade-offs

- [LIKE 模糊搜索前缀 % 无法走索引] → 数据量 <1 万时可接受，万级时切换 ES 或 FULLTEXT
- [OFFSET 深翻页在万级数据时退化] → 当前量级无影响，标记为技术债，后续可切换 cursor-based
- [存储过程生成的测试数据昵称可能重复] → 使用组合规则（姓+名+数字）保证唯一性
- [地区筛选新增接口] → 低风险，简单去重查询

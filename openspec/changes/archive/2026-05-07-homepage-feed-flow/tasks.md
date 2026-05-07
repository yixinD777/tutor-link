## 1. 批量测试数据

- [x] 1.1 创建 V17__seed_bulk_tutors.sql Flyway 迁移脚本，使用存储过程生成 300+ 条 user + tutor_profile + tutor_subject 记录
- [x] 1.2 执行迁移脚本，验证数据插入正确（至少 300 条认证家教、8+ 城市、6 科目均有覆盖）
- [x] 1.3 删除存储过程，确认无残留

## 2. 后端关键词搜索

- [x] 2.1 TutorSearchService.searchTutors 增加 String keyword 参数
- [x] 2.2 在 LambdaQueryWrapper 中实现 keyword 模糊匹配逻辑（university/major/intro 三字段 OR，LIKE '%keyword%'，转义 % 和 _）
- [x] 2.3 TutorController.searchTutors 增加 @RequestParam(required = false) String keyword 参数，透传至 service
- [x] 2.4 验证：keyword 为空时不影响原有查询；keyword="北大" 返回匹配结果；keyword 与 subjectId/price 等组合筛选正常

## 3. 后端地区接口

- [x] 3.1 TutorSearchService 新增 getAvailableRegions() 方法，从 tutor_profile 查询 certification_status=2 的去重 province/city 列表，按 province 分组返回
- [x] 3.2 TutorController 新增 GET /api/v1/tutors/regions 端点，返回 ApiResult<List<RegionVO>>
- [x] 3.3 创建 RegionVO（province + List<String> cities）
- [x] 3.4 验证接口返回格式正确

## 4. 首页 feed 流改造

- [x] 4.1 首页科目 Tab 从硬编码字符串改为从 /subjects 接口动态加载，使用 subjectId 作为筛选参数
- [x] 4.2 首页 loadTutors 修复：将 params.subject 改为 params.subjectId，值从动态加载的科目列表中查找对应 ID
- [x] 4.3 首页增加 onReachBottom 触发加载更多，实现 page 递增 + tutors 数组追加 + hasMore 判断
- [x] 4.4 首页 size 从 6 改为 10，去掉"更多 ›"入口
- [x] 4.5 首页新增地区筛选 picker，从 /tutors/regions 接口获取数据，传 province/city 参数
- [x] 4.6 首页新增价格区间筛选 picker（¥0-80 / ¥80-120 / ¥120-160 / ¥160+），转换单位为分后传 hourlyRateMin/hourlyRateMax
- [x] 4.7 首页搜索框改为：有输入时跳转列表页并携带 keyword 参数，无输入时跳转列表页

## 5. 列表页接收关键词

- [x] 5.1 tutor/list.vue 的 onShow 中读取页面 query.keyword，若有值则填入搜索框并自动触发搜索
- [x] 5.2 列表页 loadTutors 方法支持 keyword 参数传给 searchTutors API

## 6. 验证

- [x] 6.1 首页 feed 流：滑到底自动加载更多，直到显示"没有更多了"
- [x] 6.2 首页科目 Tab 筛选生效，切换后结果变化
- [x] 6.3 首页地区/价格筛选生效
- [x] 6.4 首页搜索框输入关键词跳转列表页并自动搜索
- [x] 6.5 后端 keyword 搜索对 university/major/intro 均可匹配

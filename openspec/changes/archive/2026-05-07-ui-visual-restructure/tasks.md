## 1. Design Token 更新 (warm-color-system)

- [x] 1.1 更新 `uni.scss` — 替换主色 token: `$color-primary` → `#FF6B6B`, `$color-primary-light` → `#FFF0F0`, `$color-primary-dark` → `#E85555`; 新增 `$color-secondary: #FF8E53`, `$color-gradient: linear-gradient(135deg, #FF6B6B, #FF8E53)`
- [x] 1.2 更新 `uni.scss` — 替换阴影 token 为暖色调: `$shadow-card` → `0 4rpx 16rpx rgba(255, 107, 107, 0.08)`, `$shadow-float` → `0 4rpx 16rpx rgba(255, 107, 107, 0.4)`
- [x] 1.3 更新 `uni.scss` — 语义色 status token 适配新主色: `$color-status-info-bg` / `$color-status-info-text` 自动跟随新 `$color-primary`

## 2. Tl* 组件配色跟随

- [x] 2.1 更新 `TlButton.vue` — primary 类型使用新 `$color-primary`; 新增 gradient 类型支持 `$color-gradient` 背景
- [x] 2.2 更新 `TlCard.vue` — elevated 模式阴影使用新 `$shadow-card`
- [x] 2.3 更新 `TlStatusBadge.vue` — info 主题色自动跟随新 `$color-primary`
- [x] 2.4 更新 `TlTag.vue` — subject 类型使用新 `$color-primary` 色系
- [x] 2.5 更新 `TlChatBubble.vue` — self 气泡背景使用新 `$color-primary`
- [x] 2.6 更新 `TlScrollTabs.vue` — active 下划线使用新 `$color-primary`
- [x] 2.7 更新 `TlSearchBar.vue` — 聚焦边框色使用新 `$color-primary`
- [x] 2.8 更新 `TlSwitch.vue` — activeColor 默认值使用新 `$color-primary`

## 3. 首页视觉重构 (最高优先级)

- [x] 3.0 使用 `open-design` skill 对首页进行 UI 设计 — 输入当前首页代码 + 小红书风格 + 珊瑚暖色系要求，获取设计建议
- [x] 3.1 首页模板重构 — 添加渐变 hero 区域(320rpx): `$color-gradient` 背景 + slogan + 搜索栏, 底部圆角过渡
- [x] 3.2 首页模板重构 — 快捷入口改为渐变底大圆角卡片(找家教/发需求/AI顾问)
- [x] 3.3 首页模板重构 — 推荐家教区域从单列 TlCard 列表改为双列 grid 卡片布局
- [x] 3.4 首页样式重构 — 编写双列网格样式 `.tutor-grid` + `.grid-card` (头像居中+名字+评分+学校+价格)
- [x] 3.5 首页样式重构 — 渐变 hero 区域样式 (白色搜索框/文字, 底部圆角, 内容区白色上移重叠)

## 4. 登录页视觉重构

- [x] 4.0 使用 `open-design` skill 对登录页进行 UI 设计
- [x] 4.1 登录页模板重构 — 全屏 `$color-gradient` 背景, 白色品牌标题, 半透明表单输入框
- [x] 4.2 登录页样式重构 — 白色文字/半透明输入框/角色选择卡片半透明底/微信登录突出样式

## 5. 家教详情页视觉重构

- [x] 5.0 使用 `open-design` skill 对家教详情页进行 UI 设计
- [x] 5.1 家教详情页模板重构 — 顶部添加渐变 hero (280rpx): 大头像居中 + 白色姓名/学校/评分
- [x] 5.2 家教详情页样式重构 — hero 区域样式 + 信息卡片区域微调间距

## 6. 家教列表页视觉重构

- [x] 6.0 使用 `open-design` skill 对家教列表页进行 UI 设计
- [x] 6.1 家教列表页模板重构 — 从单列列表改为双列 grid 卡片
- [x] 6.2 家教列表页样式重构 — grid 布局样式 + 筛选栏吸顶胶囊样式

## 7. 个人中心视觉重构

- [x] 7.0 使用 `open-design` skill 对个人中心进行 UI 设计
- [x] 7.1 个人中心模板重构 — 顶部渐变 hero (300rpx): 头像 + 昵称 + 数据面板(订单数/评价数)
- [x] 7.2 个人中心样式重构 — hero 区域样式 + 菜单列表白色圆角卡片

## 8. 其他页面适配

- [x] 8.1 订单详情页 — 状态栏渐变色适配新主色
- [x] 8.2 聊天详情页 — 气泡更圆润(radius-xl) + 输入栏样式微调
- [x] 8.3 订单列表/排期/试课/评价等页面 — 状态色/主色跟随新 token 自然生效, 检查有无硬编码色值需手动替换

## 9. 验证

- [x] 9.1 H5 模式编译通过 (`npm run dev:h5`) 无 SCSS 编译错误
- [x] 9.2 微信小程序模式编译通过 (`npm run build:mp-weixin`) 无报错
- [x] 9.3 全站视觉检查 — 确认无残留冷蓝色(#4A90D9)硬编码

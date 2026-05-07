## 1. 设计令牌重塑 (premium-design-tokens)

- [x] 1.1 更新 `uni.scss` — 主色: `$color-primary` → `#2C5BF6`, `$color-primary-light` → `#EEF2FF`, `$color-primary-dark` → `#1A4FD4`; 删除 `$color-secondary` 和 `$color-gradient`
- [x] 1.2 更新 `uni.scss` — 中性色: 文字色→暖灰(`#111827`/`#374151`/`#6B7280`/`#9CA3AF`), 背景色→`#F9FAFB`/`#F3F4F6`, 语义色微调(`#22C55E`/`#F59E0B`/`#EF4444`)
- [x] 1.3 更新 `uni.scss` — 阴影中性化: `$shadow-card` → `0 2rpx 8rpx rgba(0,0,0,0.04), 0 8rpx 24rpx rgba(0,0,0,0.06)`, `$shadow-float` → `0 4rpx 16rpx rgba(0,0,0,0.08), 0 16rpx 48rpx rgba(0,0,0,0.04)`, `$shadow-bar` → `0 -1rpx 0 rgba(0,0,0,0.06)`
- [x] 1.4 更新 `uni.scss` — 间距扩展: `$spacing-page` → `32rpx`, 新增 `$spacing-4xl: 80rpx`, `$spacing-5xl: 120rpx`
- [x] 1.5 更新 `uni.scss` — 排版: 新增 `$font-weight-light: 300`; 新增 `$radius-2xl: 24rpx`; 更新 `$color-bg-hover: #EEF2FF`
- [x] 1.6 更新 `pages.json` — tabBar `selectedColor` → `#2C5BF6`
- [x] 1.7 更新 `styles/_tokens.scss` — 同步备份文件的主色/中性色/阴影值

## 2. Tl* 组件升级

- [x] 2.1 更新 `TlButton.vue` — 删除 `gradient` 类型; 新增 `active:scale(0.97)` 微交互 + `transition: transform 0.15s ease`
- [x] 2.2 更新 `TlCard.vue` — elevated 阴影使用新中性阴影; 默认 padding 增加
- [x] 2.3 更新 `TlStatusBadge.vue` — info 主题跟随新主色
- [x] 2.4 更新 `TlTag.vue` — subject 类型跟随新主色; 调整为更克制的样式
- [x] 2.5 更新 `TlChatBubble.vue` — self 气泡背景使用新 `$color-primary`; 圆角保持 `$radius-xl`
- [x] 2.6 更新 `TlScrollTabs.vue` — active 胶囊使用新 `$color-primary`
- [x] 2.7 更新 `TlSearchBar.vue` — 搜索框圆角增大到 `$radius-xl`; 背景改为 `$color-bg-input`
- [x] 2.8 更新 `TlSwitch.vue` — activeColor 跟随新主色
- [x] 2.9 更新 `TlEmpty.vue` — 使用精致 SVG 线性图标替代 emoji 图标
- [x] 2.10 升级 `TlLoading.vue` — 新增 `mode="skeleton"` 骨架屏模式, 支持 `rows` prop, CSS shimmer 动画
- [x] 2.11 更新 `TlAvatar.vue` — 渐变背景底改为中性色 `$color-bg-input`
- [x] 2.12 更新 `TlInfoRow.vue` — 调整 label 使用 `$font-weight-regular`, value 使用 `$font-weight-medium`

## 3. 首页视觉升级 (最高优先级)

- [ ] 3.0 使用 `open-design` skill 对首页进行高端风格 UI 设计
- [x] 3.1 首页 hero 区域 — 从渐变背景改为白色/极浅灰背景 + 大标题(700) + 细副标题(300) + 搜索框
- [x] 3.2 首页快捷入口 — 从彩色渐变卡片改为中性圆角图标 + 文字，更克制
- [x] 3.3 首页推荐家教 — 双列网格保留但配色收敛(头像底改为中性色，价格色从 danger 改为 primary)
- [x] 3.4 首页间距升级 — `$spacing-page: 32rpx`，section 间距从 `$spacing-xl` 提升到 `$spacing-2xl`+

## 4. 登录页视觉升级

- [ ] 4.0 使用 `open-design` skill 对登录页进行高端风格 UI 设计
- [x] 4.1 登录页 — 从全屏渐变改为白底 + 品牌蓝大标题 + 精致输入框 + 主色登录按钮
- [x] 4.2 登录页 — 角色选择卡片改为中性浅灰底 + 蓝色选中边框，不再用半透明

## 5. 家教详情页视觉升级

- [ ] 5.0 使用 `open-design` skill 对家教详情页进行高端风格 UI 设计
- [x] 5.1 家教详情 hero — 从渐变背景改为白色 + 大头像 + 深色标题 + 细灰色元信息
- [x] 5.2 家教详情卡片 — 信息网格间距增加，section-label 使用 bold，content 使用 light/regular

## 6. 家教列表页视觉升级

- [x] 6.1 家教列表 — 双列网格头像底色改为中性，价格色收敛为 primary，增加页面间距
- [x] 6.2 家教列表 — 筛选栏胶囊样式更精致(中性色底 + primary 选中)

## 7. 个人中心视觉升级

- [x] 7.1 个人中心 hero — 从渐变背景改为白底 + 头像 + 大标题 + 细数据面板
- [x] 7.2 个人中心菜单 — 增加行间距，箭头改为更精致的 chevron

## 8. 其他页面适配

- [x] 8.1 订单详情 — 状态栏渐变改为 `$color-primary` 纯色 + 圆角底部
- [x] 8.2 聊天详情 — self 气泡跟随新 primary，富消息卡片边框色调整
- [x] 8.3 全部页面 — 检查移除所有渐变背景引用(`$color-gradient`)，替换为 `$color-primary` 纯色
- [x] 8.4 骨架屏 — 订单详情/家教详情等页面 loading 改为 `TlLoading mode="skeleton"`

## 9. 微交互 & 动效

- [x] 9.1 更新 `uni.scss` — 新增 `@keyframes shimmer` 骨架屏扫光动画 + `@keyframes glow-drift` 光晕漂移动画
- [x] 9.2 列表项入场 — 首页/列表页循环项添加 `slideUp` + stagger animation-delay
- [x] 9.3 页面过渡 — App.vue 全局页面 `fadeIn` 动画

## 10. 动态背景效果

- [x] 10.1 更新 `uni.scss` — 新增 glow-drift mixin (`hero-glow`) 用于 hero 区域渐变光晕
- [x] 10.2 首页 hero — 添加 `::before` 光晕漂移动画
- [x] 10.3 家教详情 hero — 添加 `::before` 光晕漂移动画
- [x] 10.4 个人中心 hero — 添加 `::before` 光晕漂移动画
- [x] 10.5 登录页 — 主色背景 + 光晕漂移动画
- [x] 10.6 全局噪点纹理 — 在 `App.vue` 添加 SVG feTurbulence 噪点 overlay (`opacity: 0.03`, `pointer-events: none`)
- [x] 10.7 TlButton primary — 添加 shimmer 微光扫过效果

## 11. 验证

- [x] 11.1 H5 模式编译通过 (`npm run dev:h5`) 无 SCSS 编译错误
- [x] 11.2 微信小程序模式编译通过 (`npm run build:mp-weixin`) 无报错
- [x] 11.3 全站视觉检查 — 确认无残留珊瑚色(#FF6B6B)硬编码，无残留渐变引用

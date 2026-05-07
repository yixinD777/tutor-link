## Context

Tutor-Link 当前使用珊瑚暖色系(#FF6B6B/#FF8E53 渐变)，风格偏社交/小红书。用户明确要求转向高端教育产品风格：极简克制、留白充裕、品质感强，类似 Apple 教育类产品。当前存在的具体问题：

- 配色过多：珊瑚红+橙色渐变+紫色+绿色+黄色，6种语义色+2种功能色全部在用，视觉噪音大
- 间距过紧：`$spacing-page: 20rpx`、`$spacing-xl: 30rpx`，页面元素挤在一起
- 字重对比弱：700/500/400 三档，实际使用中 500 和 400 视觉差异不大
- 阴影偏色：`rgba(255,107,107,0.08)` 带有色调，不够中性精致
- 缺少骨架屏：只有 spinner loading，无骨架屏
- 无微交互：按钮无点击反馈，列表无入场动画

技术栈：uni-app (Vue 3 + Vite + SCSS)，设计 token 集中在 `uni.scss`，19 个 Tl* 组件，easycom 自动注册。

## Goals / Non-Goals

**Goals:**
- 主色切换为沉稳蓝 #2C5BF6，去除渐变，全局配色收敛为1主色+中性色
- 间距体系扩展，页面元素留足呼吸空间
- 排版字重对比加强，标题粗+正文细
- 阴影中性化，卡片浮起感精致
- 骨架屏加载组件
- 微交互体系（按钮缩放、列表入场、页面过渡）
- 视觉上能感受到"这不是普通小程序，这是有品质感的产品"

**Non-Goals:**
- 不改后端 API / 数据结构
- 不改路由 / 页面路径
- 不增加新页面
- 不做暗色模式
- 不做深色/浅色主题切换
- 不改管理后台(tutor-link-admin)

## Decisions

### D1: 主色 — 沉稳蓝 #2C5BF6

**选择**: `#2C5BF6` (明亮但沉稳的蓝色)

**替代方案**:
- A. `#4A6CF7` (偏紫的蓝，更时尚但偏轻)
- B. `#1E3A5F` (深海军蓝，太沉闷)
- C. 保留珊瑚暖色 (与目标风格完全冲突)

**理由**: #2C5BF6 是明亮但不轻浮的蓝色，既有专业教育产品的信赖感，又有现代感。与中性灰搭配时克制而精致。去掉渐变是因为高端风格追求的是"一色到底"的纯粹感，而非视觉丰富度。

**完整色板**:
```
主色:
  $color-primary:        #2C5BF6
  $color-primary-light:  #EEF2FF
  $color-primary-dark:   #1A4FD4

语义色 (仅在状态场景使用，不作为装饰色):
  $color-success:  #22C55E  (保留)
  $color-warning:  #F59E0B  (从 #FF9500 微调，更沉稳)
  $color-danger:   #EF4444  (从 #FF3B30 微调，稍柔和)

功能色 (极少量点缀):
  $color-purple:   #7B5CF5  (仅 AI 顾问)
  $color-wechat:   #07C160  (仅微信登录按钮)

中性色:
  $color-text-primary:    #111827  (从 #1A1A1A 微调，更暖的深灰)
  $color-text-regular:    #374151  (从 #333333 微调)
  $color-text-secondary:  #6B7280  (从 #999999 微调，更深，不用太浅的灰)
  $color-text-placeholder:#9CA3AF  (从 #BBBBBB 微调)

背景:
  $color-bg-page:  #F9FAFB  (从 #F5F5F5 微调，更白净)
  $color-bg-card:  #FFFFFF
  $color-bg-input: #F3F4F6  (从 #F5F5F5 微调，与页面背景有更明显区分)
```

### D2: 间距体系 — 翻倍扩展

**选择**: 新增 `$spacing-4xl: 80rpx`、`$spacing-5xl: 120rpx`，`$spacing-page: 32rpx`

**替代方案**: 保持现有间距但增加 section margin

**理由**: 高端感的核心是"空间换品质"。当前 `$spacing-page: 20rpx` 太紧，页面两侧没有呼吸空间。增加到 32rpx 是第一步；section 之间的间距也需要从 30rpx 提升到 48rpx+。

```
新增/调整:
  $spacing-page: 32rpx  (从 20rpx)
  $spacing-4xl: 80rpx   (新增)
  $spacing-5xl: 120rpx  (新增)
```

### D3: 排版 — 字重对比加强

**选择**: 新增 `$font-weight-light: 300`，标题统一 700 + 正文 400

**替代方案**: 引入 display font (如衬线体)

**理由**: 在微信小程序环境下，自定义字体加载慢且不稳定。利用系统字体的 300/400/700 三档，700 vs 400 的对比已经足够强烈。关键是在页面中真正使用这种对比 — 标题大+粗，正文小+细，而不是都是 500。

### D4: 阴影 — 中性柔和

**选择**: 纯中性灰阴影 `rgba(0,0,0,0.04)` ~ `rgba(0,0,0,0.08)`

**替代方案**: 继续使用有色调阴影

**理由**: 有色调阴影(如 `rgba(255,107,107,0.08)`) 在珊瑚暖色系下尚可，但与沉稳蓝搭配会显得突兀。中性阴影是高端产品的标配 — Apple、Notion、Linear 全部使用纯灰阴影。

```
$shadow-card:   0 2rpx 8rpx rgba(0, 0, 0, 0.04), 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
$shadow-float:  0 4rpx 16rpx rgba(0, 0, 0, 0.08), 0 16rpx 48rpx rgba(0, 0, 0, 0.04);
$shadow-bar:    0 -1rpx 0 rgba(0, 0, 0, 0.06);
$shadow-popup:  0 8rpx 32rpx rgba(0, 0, 0, 0.12);
```

### D5: 骨架屏实现 — CSS 渐变动画

**选择**: 使用 CSS `linear-gradient` + `animation` 实现骨架屏闪烁效果

**替代方案**: 用 SVG 绘制完整骨架

**理由**: CSS 渐变方案零依赖、性能好、在 uni-app 全平台兼容。通过 `background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%)` + `background-size: 200% 100%` + `animation: shimmer` 实现扫光效果。

### D6: 微交互 — CSS transform + transition

**选择**: 纯 CSS `transform: scale()` + `transition` 实现按钮缩放和列表入场

**替代方案**: JS 手动控制或 Lottie 动画

**理由**: CSS transform 在 uni-app 全平台兼容且性能最优。按钮 `active:scale(0.97)` + `transition: transform 0.15s` 即可。列表入场使用 `@keyframes slideUp` + `animation-delay` 实现错开入场。

### D7: 使用 open-design skill

**选择**: 每个核心页面重构前调用 open-design skill 获取设计指导

**替代方案**: 纯手动设计

**理由**: 用户明确要求使用 open-design skill，且该 skill 可提供移动端原型参考，确保设计落地质量。

### D8: 动态背景效果 — 渐变光晕 + 噪点纹理 + 微光扫过

**选择**: 三种效果组合使用：
1. **动态渐变光晕** — hero 区域使用 `@keyframes glow-drift` 让色块缓慢流动，取代纯色背景
2. **噪点纹理叠加** — 全局通过 SVG `feTurbulence` 生成噪点，以 `opacity: 0.03` 轻叠加在页面背景上，增加纸质感
3. **微光扫过** — 登录按钮、骨架屏、卡片边缘使用 `@keyframes shimmer` 扫光动画

**替代方案**: Canvas 粒子效果 / Lottie 动画背景

**理由**: 三种效果均为纯 CSS/SVG 实现，零依赖、性能优、uni-app 全平台兼容。动态渐变光晕给 hero 区域"呼吸感"，噪点纹理给全局"质感"，微光扫过给交互元素"精致感"。三者叠加不花哨，符合极简克制风格。

**实现细节**:

1. **动态渐变光晕**:
```scss
// hero 区域使用
.hero {
  background: $color-primary;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 200%;
    height: 200%;
    top: -50%;
    left: -50%;
    background: radial-gradient(circle at 30% 40%, rgba(255,255,255,0.15) 0%, transparent 50%),
                radial-gradient(circle at 70% 60%, rgba(255,255,255,0.1) 0%, transparent 40%);
    animation: glow-drift 8s ease-in-out infinite alternate;
  }
}

@keyframes glow-drift {
  0%   { transform: translate(0, 0) scale(1); }
  100% { transform: translate(30rpx, -20rpx) scale(1.05); }
}
```

2. **噪点纹理叠加**:
```scss
// App.vue 全局伪元素
.page::after {
  content: '';
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image: url("data:image/svg+xml,...feTurbulence...");
  opacity: 0.03;
  pointer-events: none;
  z-index: 9999;
}
```

3. **微光扫过**:
```scss
@keyframes shimmer {
  0%   { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

// 用于按钮、骨架屏
.shimmer-effect {
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.2) 50%, transparent 100%);
  background-size: 200% 100%;
  animation: shimmer 2s ease-in-out infinite;
}
```

## Risks / Trade-offs

**[沉稳蓝可能偏冷]** → 家长端可能觉得不够亲切 → 通过大圆角+充足留白+暖灰文字(#111827 偏暖)平衡

**[间距翻倍信息密度降低]** → 每屏展示内容减少 → 高端感优先，详情页承载更多信息；列表页可增加分页

**[去渐变视觉冲击力下降]** → 从"一眼亮眼"变成"越看越舒服" → 这是目标风格的取舍，高端产品追求的是耐看而非刺激

**[改动面大]** → token + 组件 + 页面全改 → 分优先级：token → 组件 → 首页/登录 → 其他页面，每步可验证

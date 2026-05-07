## Context

Tutor-Link 当前的前端 UI 使用冷蓝色主色(#4A90D9)，布局以单列列表为主，视觉风格偏工具化。19 个 Tl* 组件已建立 but 配色和布局仍停留在"功能可用"阶段。用户明确要求转向小红书风格 — 珊瑚暖色系、双列网格、渐变背景、圆角饱满感。

技术栈：uni-app (Vue 3 + Vite + SCSS)，设计 token 集中在 `uni.scss`，easycom 自动注册 Tl* 组件。

## Goals / Non-Goals

**Goals:**
- 全站主色从冷蓝切换到珊瑚暖色(#FF6B6B→#FF8E53 渐变)
- 首页重构为"发现页"：渐变头部 + 双列网格卡片
- 登录页、家教详情页、个人中心增加渐变 hero 区域
- 所有 Tl* 组件配色跟随新 token
- 视觉上能明显感受到"这不是原来那个了"

**Non-Goals:**
- 不改后端 API / 数据结构
- 不改路由 / 页面路径
- 不增加新页面
- 不做暗色模式
- 不做 SSR / 性能优化
- 不改管理后台(tutor-link-admin)

## Decisions

### D1: 主色方案 — 珊瑚暖色系

**选择**: `#FF6B6B`(珊瑚红) → `#FF8E53`(暖橙) 渐变

**替代方案**: 靛蓝紫 `#6366F1→#8B5CF6` (更保守但不够暖)

**理由**: 用户明确要求小红书风格，珊瑚暖色是核心视觉锚点。虽然家长端需要信任感，但通过圆角+留白+卡片阴影可以弥补；而冷色系无论如何调整都无法达到"温暖亲切"的感觉。

**色板定义**:
```
$color-primary:       #FF6B6B
$color-primary-light: #FFF0F0
$color-primary-dark:  #E85555
$color-secondary:     #FF8E53  (渐变终点色)
$color-gradient:      linear-gradient(135deg, #FF6B6B, #FF8E53)

$color-success:  #34C759 (保留)
$color-warning:  #FF9500 (保留)
$color-danger:   #FF3B30 (保留)
$color-purple:   #7B5CF5 (保留，AI 顾问专用)
$color-wechat:   #07C160 (保留)

$color-text-primary:    #1A1A1A
$color-text-regular:    #333333
$color-text-secondary:  #999999
$color-text-placeholder:#BBBBBB

$color-bg-page: #F5F5F5
$color-bg-card: #FFFFFF
$shadow-card:   0 4rpx 16rpx rgba(255, 107, 107, 0.08)
```

### D2: 首页布局 — 双列网格

**选择**: CSS Grid `grid-template-columns: 1fr 1fr` + gap

**替代方案**: CSS Columns 瀑布流(真正的交错高度)

**理由**: uni-app 的 scroll-view + CSS columns 在小程序端有兼容性问题。CSS Grid 方案更稳定，卡片等高也符合家教卡片的信息量(不需要像小红书那样因图片比例导致高度差异)。如果未来需要真正瀑布流，可引入虚拟列表组件。

**卡片结构**:
```
┌──────────────────┐
│  ┌──────────────┐ │
│  │   头像区域    │ │  ← 120rpx 圆形头像 + 渐变背景底
│  │   (居中大)    │ │
│  └──────────────┘ │
│  张同学 ★4.9      │  ← 名字 + 评分
│  北大 · 数学      │  ← 学校 · 科目
│  ¥80-120/时       │  ← 价格(突出)
└──────────────────┘
```

### D3: 渐变 Hero 区域实现

**选择**: 固定高度 view + CSS gradient 背景

**替代方案**: SVG 背景 / Canvas 绘制

**理由**: CSS gradient 在 uni-app 全平台兼容，性能好，代码量最少。通过 `$color-gradient` 变量统一管理，一处修改全站生效。

**各页面 Hero 规格**:
| 页面 | 高度 | 内容 |
|------|------|------|
| 首页 | 320rpx | 搜索栏 + slogan |
| 登录页 | 100vh | 全屏渐变 + 品牌 |
| 家教详情 | 280rpx | 头像 + 基本信息 |
| 个人中心 | 300rpx | 头像 + 昵称 + 数据 |

### D4: 组件更新策略 — Token 驱动

**选择**: 只改 `uni.scss` 中的 token 值 + 组件中引用 token 的地方自然生效

**替代方案**: 逐个组件手动改色值

**理由**: 前一轮重构已将所有硬编码色值替换为 SCSS token。改 token 即可完成 80% 的配色切换。剩余 20% 是内联渐变/阴影等需要手动调整的地方。

### D5: UI 设计工具 — open-design skill

**选择**: 在实施过程中使用 `open-design` skill 辅助页面 UI 设计

**替代方案**: 纯手动编写样式

**理由**: 用户已安装 open-design skill，该 skill 可在页面级别提供 UI 优化建议和设计方案。在每个核心页面(首页、登录页、家教详情等)重构时，先通过 open-design 生成设计方案，再落地到代码。这比纯手动试错更高效，也能保证视觉质量的一致性。

**使用方式**: 在实施 tasks 中每个页面的视觉重构步骤前，先调用 `/open-design` 获取该页面的设计建议，然后根据建议 + design.md 中的色板/布局规范编写代码。

## Risks / Trade-offs

**[暖色信任感]** → 家长端可能觉得"不够正式" → 通过卡片阴影、留白节奏、清晰信息层级弥补专业感；订单/支付相关页面使用更克制的排版

**[双列网格信息密度]** → 卡片宽度减半，每个家教展示信息更少 → 精简到核心 3 要素(名字+评分、学校科目、价格)，详情页承载更多信息

**[小程序端兼容]** → 部分渐变/阴影在小程序渲染可能有差异 → 使用 rpx 单位 + 标准 CSS 属性，避免使用 backdrop-filter 等不兼容属性

**[改动面大]** → 19 组件 + 19 页面全改 → 分优先级实施：token → 组件 → 首页 → 其他页面，每步可验证

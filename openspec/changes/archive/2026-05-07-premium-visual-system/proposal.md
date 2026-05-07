## Why

当前家教直连平台的视觉风格经过两轮迭代（冷蓝 CRUD → 珊瑚暖色小红书风），但仍有根本性问题：配色过多（珊瑚红+橙色渐变+紫色+绿色+黄色混用）、间距偏紧缺乏呼吸感、字重对比弱导致信息层级模糊、卡片和按钮缺少精致的阴影与圆角体系。整体气质停留在"功能型应用"，而非"高端教育产品"。需要从底层设计令牌开始，全面重塑为极简克制、留白充裕、品质感强的视觉体系，类似 Apple 教育类产品或高端留学咨询平台。

## What Changes

- **BREAKING**: 全站主色从珊瑚红(#FF6B6B)切换为沉稳蓝(#2C5BF6)，去除渐变，配色收敛为1主色+中性色为主
- 全面升级设计令牌：新增 `$font-weight-light(300)`、`$spacing-4xl(80rpx)`、`$spacing-5xl(120rpx)` 大间距，`$radius-2xl(24rpx)` 大圆角
- 阴影体系重构：从有色调阴影切换为中性柔和投影(`rgba(0,0,0,0.04)`~`0.08`)，卡片浮起感更精致
- 排版升级：标题使用 `$font-weight-bold(700)` + 正文 `$font-weight-regular(400)`，字重对比从 700/500 提升到 700/400；行高默认 `$line-height-relaxed(1.6)`
- 页面间距翻倍：`$spacing-page` 从 20rpx → 32rpx，section 间距从 30rpx → 48rpx，用空间换品质感
- TlEmpty 升级为精致 SVG 线性插画空状态
- TlLoading 新增骨架屏模式
- TlButton 新增点击缩放微交互(`active: scale(0.97)`)
- 列表项入场动画(`slideUp` stagger)
- 动态渐变光晕背景：hero 区域色块缓慢流动，取代纯色背景，增加呼吸感
- 全局噪点纹理叠加：SVG feTurbulence 噪点以极低透明度覆盖，增加纸质感
- 微光扫过效果：按钮/骨架屏等元素 shimmer 扫光动画，增加精致感
- 所有页面移除内联 scoped CSS，样式全部通过 token + mixin 实现

## Capabilities

### New Capabilities
- `premium-design-tokens`: 沉稳蓝主色+中性色克制配色体系，包含完整的颜色、字体、间距、圆角、阴影、动效令牌
- `skeleton-loading`: 骨架屏加载组件，替代粗糙的 loading spinner
- `micro-interactions`: 微交互体系 — 按钮缩放反馈、列表入场动画、页面过渡
- `dynamic-backgrounds`: 动态背景效果 — 渐变光晕漂移、噪点纹理叠加、微光扫过动画

### Modified Capabilities

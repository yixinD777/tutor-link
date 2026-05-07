## ADDED Requirements

### Requirement: Premium color token system
The system SHALL replace all coral warm color tokens in `uni.scss` with premium blue-neutral tokens. The primary color SHALL be `#2C5BF6`, primary-light SHALL be `#EEF2FF`, primary-dark SHALL be `#1A4FD4`. The `$color-gradient` token SHALL be removed. The `$color-secondary` token SHALL be removed. Text colors SHALL use warm-tinted grays (`#111827`, `#374151`, `#6B7280`, `#9CA3AF`). Background page SHALL be `#F9FAFB`. Background input SHALL be `#F3F4F6`.

#### Scenario: Token replacement propagates to all components
- **WHEN** the `$color-primary` value in `uni.scss` is changed to `#2C5BF6`
- **THEN** all Tl* components that reference `$color-primary` SHALL render with the new blue color without any per-component code changes

#### Scenario: Gradient removal
- **WHEN** any component previously used `$color-gradient`
- **THEN** it SHALL use `$color-primary` solid color instead

### Requirement: Premium shadow system
All shadows SHALL use neutral gray rgba values. `$shadow-card` SHALL use layered neutral shadows: `0 2rpx 8rpx rgba(0,0,0,0.04), 0 8rpx 24rpx rgba(0,0,0,0.06)`. No shadow SHALL contain colored rgba values.

#### Scenario: Card shadow rendering
- **WHEN** a TlCard with `elevated` prop renders
- **THEN** the box-shadow SHALL be layered neutral gray, with no color tint

### Requirement: Extended spacing system
The system SHALL add `$spacing-4xl: 80rpx` and `$spacing-5xl: 120rpx` tokens. `$spacing-page` SHALL be increased from `20rpx` to `32rpx`.

#### Scenario: Page breathing room
- **WHEN** a page uses `$spacing-page` for horizontal padding
- **THEN** the padding SHALL be 32rpx on each side

### Requirement: Enhanced typography contrast
The system SHALL add `$font-weight-light: 300`. Page titles SHALL use `$font-weight-bold(700)` and body text SHALL use `$font-weight-regular(400)`. The use of `$font-weight-medium(500)` SHALL be limited to navigation and tab labels only.

#### Scenario: Title vs body weight contrast
- **WHEN** a page renders a section title and body text
- **THEN** the title SHALL use font-weight 700 and the body SHALL use font-weight 400

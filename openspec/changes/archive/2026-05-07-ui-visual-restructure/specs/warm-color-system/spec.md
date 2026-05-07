## ADDED Requirements

### Requirement: Warm color design tokens
The system SHALL replace all cold-blue color tokens in `uni.scss` with coral warm color tokens. The primary color SHALL be `#FF6B6B`, primary-light SHALL be `#FFF0F0`, primary-dark SHALL be `#E85555`. A new `$color-secondary` token SHALL be `#FF8E53` for gradient endpoints. A new `$color-gradient` token SHALL be `linear-gradient(135deg, #FF6B6B, #FF8E53)`. Card shadow SHALL use warm-tinted rgba values.

#### Scenario: Token replacement propagates to all components
- **WHEN** the `$color-primary` value in `uni.scss` is changed to `#FF6B6B`
- **THEN** all Tl* components that reference `$color-primary` SHALL render with the new coral color without any per-component code changes

#### Scenario: Gradient token usage
- **WHEN** a component applies `background: $color-gradient`
- **THEN** it SHALL render a 135-degree gradient from `#FF6B6B` to `#FF8E53`

### Requirement: Semantic color consistency
The system SHALL preserve existing semantic colors (success, warning, danger, purple, wechat) unchanged. Status badge theme mappings SHALL remain functionally correct with the new primary color.

#### Scenario: Status badge with new primary
- **WHEN** a TlStatusBadge renders with type="info" or status mapped to the info theme
- **THEN** it SHALL use `$color-primary`(#FF6B6B) as the text color and `$color-primary-light`(#FFF0F0) as the background

### Requirement: Shadow warm tint
Card and floating shadows SHALL shift from blue-tinted rgba to warm-tinted rgba. `$shadow-card` SHALL use `rgba(255, 107, 107, 0.08)` instead of `rgba(74, 144, 217, 0.10)`.

#### Scenario: Card shadow rendering
- **WHEN** a TlCard with `elevated` prop renders
- **THEN** the box-shadow SHALL have a warm-tinted rgba value matching the new primary color

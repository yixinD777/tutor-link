## ADDED Requirements

### Requirement: Hero gradient glow animation
Hero sections (home page, tutor detail, profile) SHALL have a slowly drifting gradient glow overlay. The glow SHALL be implemented via `::before` pseudo-element with radial gradients using `rgba(255,255,255,0.1~0.15)`, animated with `@keyframes glow-drift` (8s ease-in-out infinite alternate, translate + scale). The glow SHALL not interfere with touch events (`pointer-events: none`).

#### Scenario: Home page hero glow
- **WHEN** the home page hero section renders
- **THEN** a subtle white radial gradient overlay SHALL slowly drift across the primary-color background

### Requirement: Global noise texture overlay
All pages SHALL have a barely-visible noise texture overlay for paper-like quality. The noise SHALL be generated via SVG `feTurbulence` inline data URL, applied as a fixed pseudo-element with `opacity: 0.03` and `pointer-events: none`. The z-index SHALL be above all content but below popups and modals.

#### Scenario: Noise texture visibility
- **WHEN** any page renders
- **THEN** a subtle grain/noise texture SHALL be barely visible on the background, adding tactile quality without distracting from content

### Requirement: Shimmer sweep effect
Buttons with `type="primary"` and skeleton loading elements SHALL have a periodic shimmer sweep. The shimmer SHALL use `linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent)` with `background-size: 200% 100%` animated via `@keyframes shimmer` over 2s infinite. The shimmer SHALL only activate on primary buttons, not on outline/text variants.

#### Scenario: Primary button shimmer
- **WHEN** a TlButton with type="primary" renders
- **THEN** a subtle light sweep SHALL periodically pass across the button surface

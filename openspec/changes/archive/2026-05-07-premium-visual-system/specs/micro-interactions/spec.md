## ADDED Requirements

### Requirement: Button press feedback
TlButton SHALL have a scale-down effect on active state. When pressed, the button SHALL scale to `0.97` with a `0.15s ease` transition. When released, it SHALL scale back to `1` with the same transition.

#### Scenario: Button press interaction
- **WHEN** user taps and holds a TlButton
- **THEN** the button SHALL visually scale down to 97% of its size within 0.15s

### Requirement: List item staggered entrance
List items rendered in a loop (tutor grid, order list, etc.) SHALL have a `slideUp` entrance animation with staggered delay. Each item SHALL fade in and slide up 40rpx over 0.3s, with a 50ms delay increment per item (capped at 500ms total delay).

#### Scenario: Tutor grid entrance
- **WHEN** the tutor grid on the home page loads with 6 tutors
- **THEN** each tutor card SHALL animate in with an increasing delay (0ms, 50ms, 100ms, 150ms, 200ms, 250ms)

### Requirement: Page transition animation
Page content SHALL have a subtle `fadeIn` animation on mount. The entire page content area SHALL fade from `opacity: 0` to `opacity: 1` over 0.2s when the page is first rendered.

#### Scenario: Page entrance
- **WHEN** user navigates to a new page
- **THEN** the page content SHALL smoothly fade in over 0.2s

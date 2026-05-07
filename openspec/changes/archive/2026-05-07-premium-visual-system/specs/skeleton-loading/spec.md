## ADDED Requirements

### Requirement: Skeleton loading component
The TlLoading component SHALL support a `skeleton` mode that renders a shimmer-animated placeholder matching the shape of content that will load. The skeleton SHALL use CSS gradient animation (`linear-gradient` sweep from `#F3F4F6` through `#E5E7EB` back to `#F3F4F6`) with `background-size: 200% 100%` animated over 1.5s infinite.

#### Scenario: Skeleton mode rendering
- **WHEN** TlLoading is used with `mode="skeleton"` prop
- **THEN** it SHALL display a shimmer-animated placeholder instead of a spinner

#### Scenario: Skeleton shape variants
- **WHEN** TlLoading skeleton is used with `rows` prop (default: 3)
- **THEN** it SHALL render that many placeholder rows with decreasing widths (100%, 80%, 60%)

### Requirement: Skeleton replaces spinner on page load
All page-level loading states (tutor detail, order detail, etc.) SHALL use skeleton mode instead of spinner when initially loading content.

#### Scenario: Tutor detail page skeleton
- **WHEN** the tutor detail page is loading
- **THEN** a skeleton matching the page layout (avatar, name, info grid, cards) SHALL be displayed

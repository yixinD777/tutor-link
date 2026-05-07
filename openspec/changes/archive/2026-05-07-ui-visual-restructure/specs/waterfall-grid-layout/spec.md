## ADDED Requirements

### Requirement: Two-column grid layout for tutor cards
The system SHALL display tutor recommendation cards in a two-column grid layout using CSS Grid (`grid-template-columns: 1fr 1fr`) with a gap of `$spacing-md`. Each card SHALL contain: a centered circular avatar area with gradient background, tutor name with star rating, university and subject line, and hourly rate price.

#### Scenario: Home page tutor grid rendering
- **WHEN** a parent user views the home page with tutor recommendations
- **THEN** tutors SHALL be displayed in a 2-column grid, each card showing avatar, name+rating, school·subject, and price

#### Scenario: Empty state in grid
- **WHEN** there are no tutors to display
- **THEN** a TlEmpty component SHALL be displayed spanning the full width of the grid area

### Requirement: Grid card visual style
Each grid card SHALL have white background, `$radius-xl`(20rpx) border radius, `$shadow-card` box shadow, and `$spacing-lg` padding. The avatar area SHALL be 120rpx circular with a gradient background circle behind it. The price text SHALL use `$color-danger` and `$font-size-base` with bold weight.

#### Scenario: Card appearance
- **WHEN** a grid card renders
- **THEN** it SHALL have rounded corners(20rpx), warm-tinted shadow, centered avatar with gradient backdrop, and prominent price in red

### Requirement: Tutor list page grid
The tutor list page (`/pages/tutor/list.vue`) SHALL also use the two-column grid layout for tutor cards, with sticky filter pills at the top.

#### Scenario: List page grid rendering
- **WHEN** user navigates to the tutor list page
- **THEN** tutor cards SHALL display in the same 2-column grid format as the home page recommendations

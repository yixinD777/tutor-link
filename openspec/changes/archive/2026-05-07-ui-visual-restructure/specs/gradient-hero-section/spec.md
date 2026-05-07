## ADDED Requirements

### Requirement: Home page gradient hero section
The home page SHALL have a gradient hero section at the top using `$color-gradient` as background, with a minimum height of 320rpx. It SHALL contain the search bar and a brand slogan text. The hero SHALL have a bottom border-radius for a curved edge effect.

#### Scenario: Home page hero rendering
- **WHEN** a user opens the home page
- **THEN** the top 320rpx area SHALL display a coral-to-orange gradient background containing the search bar and slogan text, with rounded bottom corners

### Requirement: Login page full-screen gradient
The login page SHALL use a full-screen `$color-gradient` background. The brand title SHALL be displayed in white at the top. Form inputs SHALL have white/semi-transparent backgrounds. The WeChat login button SHALL be visually prominent with a white background and green text.

#### Scenario: Login page gradient rendering
- **WHEN** user navigates to the login page
- **THEN** the entire page background SHALL be a coral-to-orange gradient, with white text branding at top and semi-transparent form inputs

### Requirement: Tutor detail gradient hero
The tutor detail page SHALL have a gradient hero section at the top (280rpx height) with the tutor's large avatar centered and basic info (name, university, rating) displayed in white text below it.

#### Scenario: Tutor detail hero rendering
- **WHEN** user opens a tutor detail page
- **THEN** the top 280rpx SHALL show a gradient background with centered avatar and white text info overlay

### Requirement: Profile page gradient hero
The profile page SHALL have a gradient hero section (300rpx height) with the user's avatar, nickname, and a data panel (order count, rating, etc.) displayed in white text.

#### Scenario: Profile hero rendering
- **WHEN** user views their profile page
- **THEN** the top 300rpx SHALL show a gradient background with avatar, name, and stats in white text

### Requirement: Gradient section component pattern
All gradient hero sections SHALL use the same `$color-gradient` variable and consistent bottom-border-radius (`$radius-xl`). The transition from hero to content area SHALL be seamless with no visible gap.

#### Scenario: Consistent gradient across pages
- **WHEN** viewing any page with a gradient hero
- **THEN** the gradient direction and colors SHALL match the global `$color-gradient` token

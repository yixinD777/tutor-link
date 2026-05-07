## ADDED Requirements

### Requirement: Homepage feed flow with infinite scroll
The homepage tutor grid SHALL load an initial page of results and append more results as the user scrolls to the bottom, using `onReachBottom` to trigger the next page request.

#### Scenario: Initial load
- **WHEN** a parent user opens the homepage
- **THEN** the system SHALL load the first page of 10 tutors sorted by rating

#### Scenario: Scroll to bottom loads more
- **WHEN** the user scrolls to the bottom of the tutor grid
- **THEN** the system SHALL request the next page and append results to the existing list

#### Scenario: No more results
- **WHEN** the API returns fewer records than the page size
- **THEN** the system SHALL display "没有更多了" and stop requesting further pages

### Requirement: Subject tab filter uses subjectId
The homepage subject tabs SHALL fetch the subject list from `/api/v1/subjects` and use `subjectId` (Long) as the filter parameter, not the subject name string.

#### Scenario: Tab filter sends subjectId
- **WHEN** the user taps the "数学" tab
- **THEN** the system SHALL look up the subjectId for "数学" from the fetched subject list and send it as the `subjectId` query parameter

#### Scenario: "全部" tab clears subject filter
- **WHEN** the user taps the "全部" tab
- **THEN** the system SHALL send the request without a `subjectId` parameter

### Requirement: Homepage region filter
The homepage SHALL provide a region picker that allows filtering tutors by province and city. The available regions SHALL be fetched from a dedicated API endpoint.

#### Scenario: Select a city
- **WHEN** the user selects "北京市" from the region picker
- **THEN** the system SHALL send `province=北京市&city=北京市` as query parameters

#### Scenario: Clear region filter
- **WHEN** the user taps the region pill without a selection (or selects "全部地区")
- **THEN** the system SHALL send the request without province/city parameters

### Requirement: Homepage price range filter
The homepage SHALL provide a price picker with predefined ranges that maps to `hourlyRateMin` and `hourlyRateMax` query parameters.

#### Scenario: Select price range
- **WHEN** the user selects "¥80-120/时"
- **THEN** the system SHALL send `hourlyRateMin=8000&hourlyRateMax=12000` (converting yuan to cents)

#### Scenario: Clear price filter
- **WHEN** the user taps the price pill without a selection (or selects "不限")
- **THEN** the system SHALL send the request without hourly rate parameters

### Requirement: Search box navigates to tutor list with keyword
The homepage search box SHALL navigate to the tutor list page when tapped. If the user has typed a keyword, it SHALL be passed as a URL query parameter.

#### Scenario: Tap search box without input
- **WHEN** the user taps the search box with no text entered
- **THEN** the system SHALL navigate to `/pages/tutor/list` (tab switch)

#### Scenario: Tap search box with input
- **WHEN** the user has typed "北大" in the search box and taps it
- **THEN** the system SHALL navigate to `/pages/tutor/list?keyword=北大`

### Requirement: Tutor list page accepts keyword parameter
The tutor list page SHALL read the `keyword` query parameter from the URL on show and automatically trigger a search with that keyword.

#### Scenario: Arriving with keyword
- **WHEN** the tutor list page receives `?keyword=北大`
- **THEN** the system SHALL set the search input to "北大" and trigger the search immediately

### Requirement: Remove "更多" link from homepage
The homepage SHALL NOT display the "更多 ›" link next to the "推荐家教" section header, as the feed flow makes it unnecessary.

#### Scenario: Homepage section header
- **WHEN** the homepage renders the tutor section
- **THEN** only the section title "推荐家教" SHALL be displayed, without a "更多" link

### Requirement: Regions API endpoint
The backend SHALL provide a `GET /api/v1/tutors/regions` endpoint that returns a list of province/city combinations where certified tutors exist.

#### Scenario: Fetch available regions
- **WHEN** a request is made to `GET /api/v1/tutors/regions`
- **THEN** the system SHALL return distinct province/city pairs from `tutor_profile` where `certification_status = 2`, grouped by province

#### Scenario: Response format
- **WHEN** the endpoint returns data
- **THEN** each item SHALL contain `province` and an array of `cities`

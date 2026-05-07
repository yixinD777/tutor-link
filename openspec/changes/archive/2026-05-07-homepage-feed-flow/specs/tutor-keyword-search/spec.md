## ADDED Requirements

### Requirement: Keyword fuzzy search on tutor profiles
The system SHALL support a `keyword` query parameter on `GET /api/v1/tutors` that performs case-insensitive LIKE matching against `university`, `major`, and `intro` fields of `tutor_profile`, combined with OR logic.

#### Scenario: Search by university name
- **WHEN** a request is made with `keyword=北大`
- **THEN** the system SHALL return tutors whose `university` contains "北大", regardless of other filter criteria

#### Scenario: Search by major
- **WHEN** a request is made with `keyword=计算机`
- **THEN** the system SHALL return tutors whose `major` contains "计算机"

#### Scenario: Search by intro content
- **WHEN** a request is made with `keyword=竞赛`
- **THEN** the system SHALL return tutors whose `intro` contains "竞赛"

#### Scenario: Keyword combined with other filters
- **WHEN** a request is made with `keyword=数学&subjectId=9001&city=北京市`
- **THEN** the system SHALL return tutors matching the keyword AND the subject filter AND the city filter

#### Scenario: Empty keyword is ignored
- **WHEN** a request is made with `keyword=` (empty string) or no keyword parameter
- **THEN** the system SHALL behave as if no keyword was provided, returning results based on other filters only

### Requirement: Keyword parameter in TutorController
The `TutorController.searchTutors` endpoint SHALL accept an optional `keyword` parameter of type `String` via `@RequestParam(required = false)`.

#### Scenario: Keyword passed to service layer
- **WHEN** the endpoint receives `keyword=物理`
- **THEN** the value SHALL be passed to `TutorSearchService.searchTutors` as a parameter

### Requirement: Keyword applied in TutorSearchService
The `TutorSearchService.searchTutors` method SHALL accept a `String keyword` parameter and, when non-null and non-blank, add the following conditions to the query wrapper:
- `university LIKE '%keyword%'` OR `major LIKE '%keyword%'` OR `intro LIKE '%keyword%'`

#### Scenario: Keyword with special characters
- **WHEN** a keyword contains SQL special characters like `%` or `_`
- **THEN** the system SHALL escape them to prevent unintended LIKE pattern matching

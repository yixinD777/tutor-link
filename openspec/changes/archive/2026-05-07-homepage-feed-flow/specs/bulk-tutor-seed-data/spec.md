## ADDED Requirements

### Requirement: Bulk seed data via Flyway migration
The system SHALL include a Flyway migration script (V17) that generates 300+ test tutor records using a MySQL stored procedure, covering diverse universities, majors, subjects, and regions.

#### Scenario: Migration execution
- **WHEN** Flyway runs the V17 migration
- **THEN** the database SHALL contain at least 300 new `user` + `tutor_profile` + `tutor_subject` records with `certification_status = 2`

### Requirement: Seed data covers multiple regions
The generated tutor profiles SHALL cover at least 8 different cities across China (e.g., 北京、上海、广州、深圳、杭州、南京、武汉、成都).

#### Scenario: Regional diversity
- **WHEN** the migration completes
- **THEN** tutors SHALL exist in at least 8 distinct cities with valid province/city/district values

### Requirement: Seed data covers all subjects
The generated tutor_subject associations SHALL cover all 6 subjects in the `subject` table (数学、英语、物理、化学、语文、编程).

#### Scenario: Subject diversity
- **WHEN** the migration completes
- **THEN** each of the 6 subjects SHALL have at least 30 associated tutors

### Requirement: Seed data has realistic distribution
The generated data SHALL have:
- Realistic Chinese names (common surname + given name combinations)
- Real university names (30+ institutions from 985/211 lists)
- Rating distribution weighted toward higher scores (4.0-5.0 range)
- Hourly rate range of ¥50-200 with reasonable distribution
- Each tutor has 1-2 subject associations

#### Scenario: Data realism
- **WHEN** querying the seeded data
- **THEN** values SHALL appear realistic and varied, not obviously synthetic

### Requirement: Stored procedure is self-cleaning
The migration script SHALL drop the stored procedure after execution, leaving no persistent database objects.

#### Scenario: No leftover procedures
- **WHEN** the migration completes
- **THEN** the stored procedure used for data generation SHALL NOT exist in the database

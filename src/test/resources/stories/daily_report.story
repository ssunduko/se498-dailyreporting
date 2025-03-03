Meta:
@author Sergey Sundukovskiy

Narrative:
As a construction site manager
I want to create and submit daily reports
So that I can track progress and get approval for completed work

Scenario: Creating a new daily report
Given I am an authenticated user with username 'manager'
When I create a new daily report for project 'proj123' for date '2023-06-01'
Then the report should be created successfully
And the report status should be 'DRAFT'

Scenario: Adding activities to a report
Given I have a daily report with ID 'report123' for project 'proj123'
When I add an activity with description 'Foundation work' and category 'Concrete' to the report
Then the activity should be added successfully to the report
And the report should have 1 activity

Scenario: Submitting a report for approval
Given I have a daily report with ID 'report123' with status 'DRAFT'
And the report has at least one activity
When I submit the report for approval
Then the report status should be updated to 'SUBMITTED'

Scenario: Approving a submitted report
Given I have a daily report with ID 'report123' with status 'SUBMITTED'
When the supervisor approves the report
Then the report status should be updated to 'APPROVED'

Scenario: Rejecting a submitted report
Given I have a daily report with ID 'report123' with status 'SUBMITTED'
When the supervisor rejects the report with reason 'Incomplete information'
Then the report status should be updated to 'REJECTED'
And the report notes should contain the rejection reason

Scenario: Updating activity progress
Given I have an activity with ID 'activity123' in report 'report123'
When I update the activity progress to 75 percent
Then the activity progress should be updated successfully
And the report progress should be recalculated
# How to manually test the app

In general, this isn't hard:

- I want to login and have my tasks fetched for me ✅
- I want to use the app successfully when offline ✅
- I want my edited stuff to be fetched again on the next login ✅
- And I want my apps to work together close to real time ✅

Test backend synchronization of everything:
Checklist templates:

- id
- Title
- Description
- CreatedAt
- Tasks
- Task id
- Template id
- Title
- ParentId
- Subtasks
- sort position
- Reminders
- reminderId: UUID,
- templateId: UUID,
- startDateUtc: LocalDateTime,
- isRecurring: Boolean,
- recurrencePattern: String?
  Checklists:
- checklistId
- templateId
- notes
- createdAt
- Tasks
- checkboxId
- checklistId
- checkboxTitle
- isChecked
- parentId
  Operations: create everything, modify everything, delete everything

Have a mechanism of rejecting commands
(accepted commands, rejected commands[reason])

Migrations:
-Simplest - ReminderEntity:
-insert dummy data
-add a uuid field
-get all data using a cursor
-for each, set a uuid
-do table migration to change the type of main id
-remove the old column
-rename the new column
-test
-Less simple - self-reference of Checkbox
-insert dummy data
-add a uuid field
-add a parent_uuid field
-get all data using a cursor
-for each, set a uuid
-for each, set a parent_uuid subqueried with parent_id
-do table migration to change the types of id fields
-test
-self-reference of TemplateCheckbox - done!
-reference Checkbox -> Checklist
-reference Checklist -> Template
-reference TemplateCheckbox -> Template

Test migration (incl reminders)!!
Migration to commands:

1. Remove data
3. Logout with local data removal lines commented out
4. Login again - should upload all that data correctly

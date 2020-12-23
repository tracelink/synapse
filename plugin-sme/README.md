# Synapse Plugin: Subject Matter Experts
This plugin adds the ability to assign Subject Matter Experts to a Project in Synapse. This is useful information to understand where resourcing might be best used or most needed. This is a Database Plugin.

## License
[MIT License](https://opensource.org/licenses/MIT)

## Table of Contents
- [Plugin Display Name](#plugin-display-name)
- [Sidebar Views](#sidebar-views)
- [Scorecard Column](#scorecard-column)
- [Jobs](#jobs)
- [Privileges](#privileges)
- [Database Schema Name](#database-schema-name)
- [Database Migrations Location](#database-migrations-location)

## Plugin Display Name
Subject Matter Experts

## Sidebar Views

### SME List
Provides a UI to add a Subject Matter Expert's Name and assign them to one or more Synapse Projects.

## Scorecard Column
`Subject Matter Experts` that contains a ratio of Projects with a SME assigned to all Projects for each Product Line, or the name of the SME for the particular Project in the Project view.

## Jobs
N/A

## Privileges
N/A

## Database Schema Name
`sme`

## Database Migrations Location
`db/sme`

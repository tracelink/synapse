# Synapse Plugin: Demo
This is a plugin to demonstrate the options available to implementors of a plugin. It demonstrates the following functionality:
- Add data to the scorecard, including links to plugin pages
- Handle plugin authorization privileges
- Schedule a periodic job
- Manage database migrations
- Create and interact with custom database tables.

This is a Database Plugin.

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
Demo Plugin

## Sidebar Views

### Configure Demo
A very simple page that is restricted to the `demoAdmin` privileged user. This allows that user to manually assign a number of vulnerablilities to Synapse Projects.

## Scorecard Column
This plugin provides a simple `Demo Column` that shows the assigned number of vulnerabilities to the Project or to all Projects in the Product Line.

## Jobs
Every few seconds logs the number of vulnerabilities found across all Projects.

## Privileges
`demoAdmin` to grant access to the `Configure Demo` sidebar view.

## Database Schema Name
`demo`

## Database Migrations Location
`db/demo`

# Synapse Plugin: Veracode Static Analysis
This plugin integrates with [Veracode Static Analysis](https://www.veracode.com/products/binary-static-analysis-sast) to pull down scans and flaws found in scans for any configured app/sandbox. Admins can then map the SAST apps/sandboxes to Synapse Projects in order to create Dashboards and a Scorecard value to understand where there is room for improvement and where there is a solid security understanding. This is a Database Plugin.

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
Veracode SAST

## Sidebar Views

### Dashboard
The Dashboard view provides a view of several high-level stats and a graph showing the number of different severity issues or the CWE Ids of issues found over time for all applications or a chosen Product Line, Project Filter, or Project.

### Flaw Reports
The Flaw Reports page allows a user with the `VeracodeSastFlawViewer` privilege to see the detailed flaw report information for a given Project as well as the high-level results of the most recent scan of each Project.

### Configurations
The Configurations view is used by admins to configure a client to interact with Veracode and to configure at what thresholds a Scorecard Value should turn green, yellow, or red.

### Mappings
This view allows an admin to map a Veracode application to a Synapse Project to fill out the Scorecard.

## Scorecard Column
`Veracode SAST` displays the policy score for a Product Line or Project.

## Jobs
`Veracode SAST Updater` runs once an hour to pull in any new Veracode violations and update all metrics.

## Privileges
`VeracodeSastFlawViewer` is used to allow access to view flaws.

## Database Schema Name
`veracode_sast_schema`

## Database Migrations Location
`db/veracodesast`

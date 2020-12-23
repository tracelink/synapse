# Synapse Plugin: Veracode Dynamic Analysis
This plugin integrates with [Veracode Dynamic Analysis](https://www.veracode.com/products/dynamic-analysis-dast) to pull down scans and flaws found in scans for any configured app/sandbox. Admins can then map the DAST apps/sandboxes to Synapse Product Lines in order to create Dashboards and a Scorecard value to understand where there is room for improvement and where there is a solid security understanding. This is a Database Plugin.

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
Veracode DAST

## Sidebar Views

### Dashboard
The Dashboard view provides a view of several high-level stats and a graph showing the number of different severity issues or the CWE Ids of issues found over time for all applications or a chosen Product Line, Project Filter, or Project.

### Flaw Reports
The Flaw Reports page allows a user with the `VeracodeDastFlawViewer` privilege to see the detailed flaw report information for a given Project as well as the high-level results of the most recent scan of each Project.

### Configurations
The Configurations view is used by admins to configure a client to interact with Veracode and to configure at what thresholds a Scorecard Value should turn green, yellow, or red.

### Mappings
This view allows an admin to map a Veracode application to a Synapse Project to fill out the Scorecard.

## Scorecard Column
`Veracode DAST` displays the policy score for a Product Line or Project.

## Jobs
`Veracode DAST Updater` runs once an hour to pull in any new Veracode violations and update all metrics.

## Privileges
`VeracodeDastFlawViewer` is used to allow access to view flaws.

## Database Schema Name
`veracode_dast_schema`

## Database Migrations Location
`db/veracodedast`

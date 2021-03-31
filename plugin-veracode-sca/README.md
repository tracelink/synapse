# Synapse Plugin: Veracode Source Composition Analysis

This plugin integrates
with [Veracode Software Composition Analysis](https://www.veracode.com/products/software-composition-analysis)
to pull down scans and flaws found in scans for any configured app/sandbox. Admins can then map the
SCA apps/sandboxes to Synapse Projects in order to create Dashboards and Scorecard values to
understand where there is room for improvement and where there is a solid security understanding.
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

Veracode SAST

## Sidebar Views

### Dashboard

The Dashboard view provides a view of several high-level stats, and a graph showing the number of
different severity issues, or the CWE Ids of issues found over time for all applications or a chosen
Product Line, Project Filter, or Project.

### Issues

The Issues page allows a user with the `ViewVeracodeSCAIssues` privilege to see the detailed issue
report information for a given Project as well as the high-level results of the most recent scan of
each Project.

### Configurations

The Configurations view is used by admins to configure a client to interact with Veracode and to
configure at what thresholds a Scorecard Value should turn green, yellow, or red.

### Manage Data

The Manage Data view allows an admin to delete SCA workspaces or projects to clean up data in
Synapse. The view also allows the admin user to exclude an SCA workspace or project from Synapse
displays and metrics in the event that a workspace or project is for scratch purposes.

### Mappings

This view allows an admin to map a Veracode application to a Synapse Project to fill out the
Scorecard.

## Scorecard Column

`Veracode SCA` displays the policy score for a Product Line or Project.

## Jobs

`Fetch Veracode SCA Data` runs once an hour to pull in any new Veracode violations and update all
metrics.

## Privileges

`ViewVeracodeSCAIssues` is used to allow access to view flaws.

## Database Schema Name

`veracode_sca_schema`

## Database Migrations Location

`db/veracode/sca`

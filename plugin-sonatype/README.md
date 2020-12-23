# Synapse Plugin: Sonatype Nexus IQ
This plugin integrates with a [Sonatype Nexus IQ](https://www.sonatype.com/nexus-iq-server) server in order to pull down Source Composition Analysis (SCA) findings. Admins can then map the SCA apps to Synapse Projects in order to create Dashboards and a Scorecard value to understand where there is room for improvement and where there is a solid security understanding. This is a Database Plugin.

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
Sonatype Nexus IQ

## Sidebar Views

### Dashboard
The Dashboard view provides a view of several high-level stats and a graph showing the number of different severity issues found over time for a chosen Product Line, Project Filter, or Project.

### Configurations
The Configurations view is used by admins to configure a client to interact with a Sonatype Nexus IQ instance and also configure at what thresholds a Scorecard Value should turn green, yellow, or red.

### Mappings
This view allows an admin to map a Sonatype application to a Synapse Project to fill out the Scorecard.

## Scorecard Column
`Sonatype Violations` displays the High, Medium, and Low stats for a Product Line or Project.

## Jobs
`Fetch Sonatype Data` runs once an hour to pull in any new Sonatype violations and update all metrics.

## Privileges
N/A

## Database Schema Name
`sonatype_schema`

## Database Migrations Location
`db/sonatype`

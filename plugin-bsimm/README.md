# Synapse Plugin: BSIMM
The [Building Security In Maturity Model](https://www.bsimm.com/) is a tool used to judge the efficacy and maturity of a security program within a company. This plugin provides a way to import the questionnaires from the BSIMM and answer them on a per-Product Line basis. It also provides a dashboard that gives a radar graph view comparing a Product Line to the comparison data given by BSIMM. This is a Database Plugin.

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
BSIMM

## Sidebar Views

### Overview
The Overview contains several high-level stats about the state of BSIMM ratings across all Product Lines. It also includes a chart area that allows users to see the radar graph of maturity ratings for a Product Line and its comparisons.

### Surveys
The Surveys UI allows users to view previous maturity survey results. Users with the `BSIMMResponder` privilege may also copy survey results and complete a survey questionnaire. Users with the `Synapse Admin` privilege may also download the survey model XML document, import a survey's question from an XML document, and delete both the survey and any result.

## Scorecard Column
This plugin provides a view into the latest survey result for a given Product Line, which is a score out of 3.

## Jobs
N/A

## Privileges
This plugin provides the `BSIMMResponder` privilege that allows a user to respond to a survey and copy other survey results to a new Product Line.

## Database Schema Name
`bsimm`

## Database Migrations Location
`db/bsimm`

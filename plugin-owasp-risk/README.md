# Synapse Plugin: OWASP Risk
This plugin is based on the [OWASP Risk Rating Methodology](https://owasp.org/www-community/OWASP_Risk_Rating_Methodology) and based on the excellent [OWASP risk calculator](https://owasp-risk-rating.com/) site in order to create a repeatable and understandable risk calculation for judging the severity of a security issue. This plugin also demonstrates the method for bypassing the required authentication/authorization of Synapse to expose a single page to unauthenticated users. This is a Standard Plugin.

## License
[MIT License](https://opensource.org/licenses/MIT)

## Table of Contents
- [Plugin Display Name](#plugin-display-name)
- [Sidebar Views](#sidebar-views)
- [Scorecard Column](#scorecard-column)
- [Jobs](#jobs)
- [Privileges](#privileges)

## Plugin Display Name
OWASP Risk Rating

## Sidebar Views

### Risk Rating
The Risk Rating view allows a user to select the impact and likelihood values for a given issue in a client-side-only implementation of the OWASP Risk Rating. This is automatically mapped onto a 5x5 grid of severity. This view is publicly accessible and does not require any authentication to use it. 

## Scorecard Column
N/A

## Jobs
N/A

## Privileges
No additional privileges

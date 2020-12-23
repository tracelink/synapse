# Synapse
![Synapse Logo](synapse-core/src/main/resources/static/images/synapse_logo.png)  
A metrics aggregation and reporting server to create quick views of company Projects and Product Lines. Collected data is displayed in a Scorecard to give a high-level overview of which Projects need help and which Projects are doing great.

The server can include multiple Plugins to generate metrics from disparate sources and collect data into the main reporting engine.

## Status
[Badges go here]

## License
[MIT License](https://opensource.org/licenses/MIT)

## Table of Contents
- [General Info](#general-info)
- [How TraceLink uses Synapse](#how-tracelink-uses-synapse)
- [Technologies](#technologies)
- [Installation](#installation)
- [Authentication](#authentication)
- [What is a Plugin](#what-is-a-plugin)
- [Plugins Available](#plugins-available)
- [Customization](#customization)
- [Contributions](#contributions)
- [Authors](#authors)
- [License](#license)

## General Info
Synapse is a Spring Boot + Postgresql based project that uses a Plugin Architecture to add functionality. 

The core of Synapse (synapse-core and synapse-web) is a web application that handles grouping Projects (Libraries or Applications) into Product Lines (as defined by a company) as well as building a Scorecard to display overarching metrics for those Projects and Product Lines. Additionally, it handles Authentication and Authorization of users, basic UI framing, Logging, running periodic Jobs, and managing Plugins.

## How TraceLink uses Synapse
TraceLink AppSec collects information from various Security tools and sources, such as Third Party DAST, SAST, SCA tools, Internal Security tools, Issue Trackers, and manually maintained resource allocations in order to provide an answer to the question "What is our security stance?" for each application, library, team, and product line in our offering. The Scorecard provides an answer to this question from a high-level perspective, while the individual plugins enable drilling down into more detail to examine areas that may need prioritization.

## Technologies
Spring Boot - For web server technology  
Flyway - For data migration  
Postgresql - Tested with Postgres 12.1  
Thymeleaf - For UI template rendering  

## Installation
From source code, type `mvn clean package` in the root directory. After packaging, the server jar is located in `synapse-web/target/synapse-web*.jar`.

To execute this jar, it requires a `JDBC_URL`, `JDBC_USERNAME`, and `JDBC_PASSWORD` or a link to a configuration properties file as described in the [Spring Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config) to supply these variables. An example can be seen [here](./synapse-web/src/main/resources/application-prd.yaml).

By default, the server runs on port 8080 and will configure the database tables automatically, including any necessary migrations from version to version.

## Authentication
There are two built-in ways to authenticate to Synapse.
1. "Local" Authentication - Usernames and hashed passwords are stored in the database and users authenticate to Synapse directly. This is configured by default.
2. SSO Authentication - Users login to a separate system which grants access to Synapse via Open ID Connect. Configuration is handled via application properties, and requires a `CLIENT_ID`, `CLIENT_SECRET`, and `ISSUER_URI` supplied on the command line or in a file as in [here](./synapse-web/src/main/resources/application-prd.yaml).

## Authorization
Synapse supports defining custom roles and privileges in order to restrict access to resources on the server. Individual privileges are grouped into roles, which can then be assigned to users. A user with zero roles represents the most basic user with the least amount of privilege. When users register with Synapse, they are not assigned any roles. 

There is only one default role built into Synapse: the Admin role. The Admin role contains a default privilege (appropriately called the Admin privilege), which gives access to Synapse core features such as product management, user management, and logging. When Synapse is deployed, **all** custom privileges stored in the database are also automatically added to the Admin role. This gives any user with the Admin role the ability to access all functionality in every plugin. Needless to say, the Admin role should be used sparingly, and custom roles and privileges should be implemented to handle authorization of typical users.  
 
## What is a Plugin?
Plugins are the centerpiece of Synapse functionality. A plugin collects (and potentially stores) data to display in Synapse. Often, data will be added to the Synapse Scorecard to give a high-level impression of the collected information. Plugins may also implement a set of UI views, which present another opportunity for displaying collected data. There are two broad categories of Plugins: standard plugins and database-enabled plugins.

### Standard Plugins
Standard plugins support the following functionality:

- Register zero or more URL routes to be exposed in the Synapse web application
- Register UI Views corresponding to those routes, which will become available in the Synapse navigation bar
- Register and use zero or more newly defined privileges
- Register zero or more Scorecard columns to display high-level info for the plugin
- Register zero or more periodic, asynchronous jobs (e.g. to pull data from another source) 

### Database Plugins
Database plugins support all the functionality of the standard plugin, and:
 
- Register a database schema owned by the plugin
- Register a database migrations location that allows implementors to manage database tables
- Access JPA Repositories and Entities to manage rows in the database tables for this plugin's schema

**Note:** We recommend that all database plugins maintain their own schema and migrations location. This allows each plugin to be fully independent of both Synapse Core and other plugins, so that plugins can be easily added or removed from a deployment.

## Plugins Available
| Plugin | Description | Documentation Link |
|--------|-------------|--------------------|
| Demo   | A plugin to demonstrate the options available to implementors of a plugin and describe how to create a Scorecard interaction, links to Synapse dashboards, handle plugin authorization privileges, do periodic job scheduling, do database migrations, and handle custom database tables. | [plugin-demo](./plugin-demo/README.md)|
| BSIMM  | The [Building Security In Maturity Model](https://www.bsimm.com/) is a tool used to judge the efficacy and maturity of a security program within a company. This plugin provides a way to import the questionnaires from the BSIMM and answer them on a per-Product Line basis. It also provides a dashboard that gives a radar graph view comparing a Product Line to the comparison data given by BSIMM. | [plugin-bsimm](./plugin-bsimm/README.md)|
| Risk Rating | This plugin is based on the [OWASP Risk Rating Methodology](https://owasp.org/www-community/OWASP_Risk_Rating_Methodology) and based on the excellent [OWASP risk calculator](https://owasp-risk-rating.com/) site in order to create a repeatable and understandable risk calculation for judging the severity of a security issue. This plugin also demonstrates the method for bypassing the required authentication/authorization of Synapse to expose a single page to unauthenticated users. | [plugin-owasp-risk](./plugin-owasp-risk/README.md)
| Subject Matter Expert | This plugin adds the ability to assign Subject Matter Experts to a Project in Synapse. This is useful information to understand where resourcing might be best used or most needed. | [plugin-sme](./plugin-sme/README.md)
| Sonatype SCA | This plugin integrates with a [Sonatype Nexus IQ](https://www.sonatype.com/nexus-iq-server) server in order to pull down Source Composition Analysis (SCA) findings. Admins can then map the SCA apps to Synapse Projects in order to create Dashboards and a Scorecard value to understand where there is room for improvement and where there is a solid security understanding. | [plugin-owasp-risk](./plugin-sonatype/README.md)
| Veracode DAST | This plugin integrates with [Veracode Dynamic Analysis](https://www.veracode.com/products/dynamic-analysis-dast) to pull down scans and flaws found in scans for any configured app/sandbox. Admins can then map the DAST apps/sandboxes to Synapse Product Lines in order to create Dashboards and a Scorecard value to understand where there is room for improvement and where there is a solid security understanding.  | [plugin-veracode-dast](./plugin-veracode-dast/README.md)
| Veracode SAST | This plugin integrates with [Veracode Static Analysis](https://www.veracode.com/products/binary-static-analysis-sast) to pull down scans and flaws found in scans for any configured app/sandbox. Admins can then map the SAST apps/sandboxes to Synapse Projects in order to create Dashboards and a Scorecard value to understand where there is room for improvement and where there is a solid security understanding. | [plugin-veracode-sast](./plugin-veracode-sast/README.md)
| Veracode SCA | This plugin integrates with [Veracode Software Composition Analysis](https://www.veracode.com/products/software-composition-analysis) to pull down scans and flaws found in scans for any configured app/sandbox. Admins can then map the SCA apps/sandboxes to Synapse Projects in order to create Dashboards and a Scorecard value to understand where there is room for improvement and where there is a solid security understanding. | [plugin-veracode-sca](./plugin-veracode-sca/README.md)


## Customization
There are a number of ways to customize your own installation of Synapse.

### Choosing Plugins

An installation can be modified to include a subset of the provided plugins, or your own custom plugins by either modifying the pom file of [synapse-web](./synapse-web/pom.xml) or providing references to your custom plugin jars on the classpath. Any plugins found in your execution's classpath will be automatically included and installed in your Synapse deployment.

### Creating Custom Plugins

While we have provided a number of available plugins for others to choose from, it is likely there are plenty of other plugins that can and should be created. You may create a custom plugin following the model in [plugin-demo](./plugin-demo) or any of the other plugins, as needed. If you feel that a custom plugin might be useful to others, please consider [contributing](#contributions) to this project.

## Database Column Encryption
Synapse provides a system of encryption for database values that are sensitive and should not be stored in plaintext. Synapse uses a Key-Encryption-Key (KEK) scheme of encryption, where a master KEK is provided at the time of deployment and is used to encrypt and decrypt a collection of data encryption keys (DEKs). The DEKs in turn encrypt and decrypt actual database values that need additional protection.

### Configuring Database Columns for Encryption
To configure encryption on a particular database column, simply add an `@Convert` annotation to the field of any entity class, and specify a converter class inside the annotation. For Synapse encryption to work, the converter class must extend the `AbstractEncryptedAttributeConverter`. Synapse has a `StringEncryptedAttributeConverter` to handle the most common case of string encryption, but custom converters can be added to support additional use cases. Note that each converter class corresponds to a single DEK. Implementers may choose to use a single converter for all encrypted columns, or extend base converters to enable different converters (and therefore keys) for different entities and/or columns. See the provided plugins for additional examples of how to use the `StringEncryptedAttributeConverter` and custom encryption converters.

An encryption converter can be added to a column at any time, whether it existed in a previous deployment of Synapse or not. If there is existing, plaintext data in that column of the database when Synapse starts up, it will be automatically encrypted using the converter.

### Configuring Encryption Type and Supplying the KEK
Once the database columns requiring encryption have been specified, Synapse needs a KEK in order to start encryption. The key is supplied through the application properties defined in the synapse-web project. An example of this is shown in [application-prdtest.yaml](./synapse-web/src/main/resources/application-prdtest.yaml). First set `synapse.encryption.type: environment` (which simply indicates that the KEKs are provided via environment variables), and then provide a current KEK keystore in PKCS12 format, along with a keystore password. When Synapse starts up, it will configure encryption with the provided KEK.

If none of the `synapse.encryption.*` properties are set, Synapse defaults to `synapse.encryption.type: none`, and will not perform any encryption on database columns.

### Configuring KEK Rotation
To rotate the KEK, set the `synapse.encryption.environment.currentKeyStorePath` and `synapse.encryption.environment.currentKeyStorePassword` with the values for the new KEK. Then provide the old keystore path and password using the properties `synapse.encryption.environment.previousKeyStorePath` and `synapse.encryption.environment.previousKeyStorePassword`. When Synapse starts up, it will rotate the KEK so that all DEKs are encrypted with the new KEK. 

The next time Synapse is deployed, the old keystore path and password can be dropped as they are no longer needed. Be sure to check the Synapse log for errors during KEK rotation and see that the KEK rotation completes. If Synapse goes down during KEK rotation, restart Synapse with the same current and previous keystores and Synapse should be able to recover.

### Configuring Database Decryption
To decrypt all database values that have been encrypted by Synapse, provide the current keystore path and password as usual, then specify one additional property: `synapse.encryption.environment.decryptMode: true`. When Synapse starts up, it will decrypt all columns that are currently encrypted in the database, and will delete any DEKs the next time Synapse is shut down. For the following deployment of Synapse, all `synapse.encryption.*` variables can be removed from the application.properties file to disable encryption entirely.

### Managing DEK Rotations
Synapse contains an admin page to view the current DEKs and manage their rotations. DEKs can be rotated manually (if, for example, there is reason to believe they are compromised), or a schedule can be set to automatically rotate all keys after a certain number of days. 

## Contributions
Contributions are welcome in the form of Pull Requests and any suggestions are welcome as Issues.

If you are contributing a Pull Request, we ask that you follow some of our practices:
- Please use one of our [formatters](./formatters)
- Please ensure that all existing tests pass and any code written has at least 90% code coverage.
- Please provide a README file that follows our plugin README examples

When creating a new Plugin, please ensure that static content, templates and any other resources live in a special folder in their respective packages to avoid name collisions. Please also ensure that you do not unintentionally re-use an existing schema name, Plugin Display Name, or URI as this will clash with the other Plugin.

## Authors
[Chris Smith](https://github.com/tophersmith)  
[Maddie Cool](https://github.com/madisoncool)  
[Brigid Horan](https://github.com/brigidhoran)

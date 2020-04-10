# Multi JDBC Source Service Plugin

This project implements a plugin for the [LDAP Synchronization Connector](https://lsc-project.org/).

## Goal

The goal of this plugin is to provide a possibility to handle multiple source records when reading from a jdbc source. 
Usually when a not unique key value is read from the database, lsc skips this record with the message `Only a single record can be returned from a getObject request !`. 

I had to implement a synchronization where records needed to read from a CSV file an put into a database but the key exists multiple times. Business logic exists to select the right record but LSC is currently not able to select the "right" record when multiple records exist in the source.

## Configuration

To use a plug-in, you need to place the provided 'lsc-multi-jdbc-src-service-1.0.xsd' into your lsc etc diretory. 

In the lsc.xml xml header, add the reference to the xsd:

```
<lsc xmlns="http://lsc-project.org/XSD/lsc-core-2.1.xsd"
	xmlns:multijdbc="http://lsc-project.org/XSD/lsc-multi-jdbc-src-service-1.0.xsd"
	revision="0">
```

You can now use the `pluginSourceService`

The configuration options are identical to the standard DatabaseSourceService as this plug-in is based on this. The only new configuration element is the "multivaluehook" where you can add javascript code to select the right value. 
The available records are handed over in the variable srcBeanList. `List<Map<String, Object>>`
The function must return the selected entry from the list. `Map<String, Object>`


```
<pluginSourceService
	implementationClass="com.becketal.lsc.plugins.connectors.multijdbcsrc.MultiJdbcSrcService">
	<name>MySyncTask-src</name>
	<connection reference="jdbcCSV" />
	<multijdbc:MultiJdbcSrcServiceConfig>
		<multijdbc:requestNameForList>getGIDUniqueSrcListCSV</multijdbc:requestNameForList>
		<multijdbc:requestNameForObject>getSrcGIDCSV</multijdbc:requestNameForObject>
		<multijdbc:requestNameForClean>getSrcGIDCleanCSV</multijdbc:requestNameForClean>
		<multijdbc:multivaluehook><![CDATA[js:
			java.lang.System.out.println("MultiValueHook always select 1st entry")
			srcBeanList.get(0)
		]]></multijdbc:multivaluehook>
	</multijdbc:MultiJdbcSrcServiceConfig>
</pluginSourceService>
```

The plug-in must reference a databaseConnection.

## Usage

See the sample for a working example on how to use the plug-in

## Open Topics

1. Currently the javascirpt interpreter can not handle custom java libraries or include files as the other javascript functions inside LSC are capable of.  
You need to write the full code of your javascript function inside the lsc.xml multivaluehook. 
2. Currently no test code exists.


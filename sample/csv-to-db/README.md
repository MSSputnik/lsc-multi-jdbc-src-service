# Multi JDBC Source Service configuration example

This configuration shows and example on how to importa an CSV file into and database and de-duplicate records during this process.

## Requirements

To read a CSV file, you an use the [HSQLDB Database](http://hsqldb.org/) engine or the [CSV jdbc](http://csvjdbc.sourceforge.net/) reader . There might be other solutions around which I'm not aware of.

The example uses the CSV jdbc reader.  
Download the jar and place it into the lib directory of your LSC installation.

As destination a Postgres SQL database is used. You can any jdbc enabled database. Just adopt your LSC configuration accordingly.

## Configuration

To use a plug-in, you need to place the provided 'lsc-multi-jdbc-src-service-1.0.xsd' into your lsc etc diretory. 

In the lsc.xml xml header, add the reference to the xsd:

```
<lsc xmlns="http://lsc-project.org/XSD/lsc-core-2.1.xsd"
	xmlns:multijdbc="http://lsc-project.org/XSD/lsc-multi-jdbc-src-service-1.0.xsd"
	revision="0">
```

Then you can use the `pluginSourceService`

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

## Run
To run the code, you need to add `-DLSC.PLUGINS.PACKAGEPATH=com.becketal.lsc.plugins.connectors.multijdbcsrc.generated` to your lsc command, so that the configuration will correctly load the plug-in configuration.

 



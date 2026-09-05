# hibersap-maven-plugin

This Maven plugin was inspired by and partly reuses code of the [Forge Hibersap plugin](https://github.com/forge/plugin-hibersap).

The intention is to generate Hibersap BAPI classes by accessing the SAP `RFC_READ_TABLE` module.

## Depends on

| Addon | Exported | Optional | Comment |
|---|---|---|---|
| `org.hibersap.*` | no | no | |
| `org.hibersap:com.sap.conn.jco.sapjco3` | no | no | JCo 3 is provided by SAP. It contains a native lib and a Java wrapper. It must be provided in a local Maven repo |

## Setup

Make sure the sapjco native library is on `java.library.path`. This can be set via the environment variable `MAVEN_OPTS`.

### sap-connection.properties

A file named `sap-connection.properties` must contain the following connection details:

```properties
#JCo properties
jco.context=org.hibersap.execution.jco.JCoContext
jco.client.client=001
jco.client.user=sapuser
jco.client.passwd=saphost
jco.client.lang=en
jco.client.ashost=saphost.example.com
jco.client.sysnr=00
jco.destination.pool_capacity=1

#Session-manager properties
session-manager.name=SM001
```

Details can be seen in the sister project <https://github.com/hurzelpurzel/sapgw-sample>.

### Configuration properties

| Property | Optional | Comment |
|---|---|---|
| `connectionProperties` | no | Path to the directory where `sap-connection.properties` can be found. Must end with a path separator (`/` or `\`) |
| `namePattern` | no | Comma-separated list of BAPIs, or a pattern containing `*` and/or `?` to search for Function Modules of interest |
| `maxResults` | yes | Maximum amount of Function Modules to be generated. Default is `20` |
| `javaPackage` | yes | Base package of the generated sources. Default is `org.hibersap.model` |
| `outputDir` | no | Folder to use for generated Java classes |

### Add configuration to pom.xml

To use this plugin, add it as a plugin in the `pom.xml` and configure it:

```xml
<plugin>
    <groupId>org.hibersap</groupId>
    <artifactId>generator</artifactId>
    <version>2.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>generateSapEntities</id>
            <phase>generate-sources</phase>
            <configuration>
                <connectionProperties>${project.basedir}/../</connectionProperties>
                <namePattern>Z*</namePattern>
                <javaPackage>net.atos.gw.hibersap.model</javaPackage>
                <outputDir>${project.basedir}/target/generated-sources/</outputDir>
            </configuration>
            <goals>
                <goal>generateSapEntities</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Features

Generate Hibersap Model (BAPI) objects from a given SAP System using the RFC dictionary.

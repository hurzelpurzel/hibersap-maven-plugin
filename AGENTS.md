# AGENTS.md

Maven plugin (`org.hibersap:generator`) that generates Hibersap BAPI model classes by querying a live SAP system via `RFC_READ_TABLE`. Java 21, `maven-plugin` packaging, LGPL-3. Partly reuses the Forge Hibersap plugin.

## Build & verify

- `mvn test` runs JUnit 4 + Hamcrest unit tests that **do not need SAP** (Utils, FilterCollection, ConnectionPropertiesManager, SAPEntityBuilder). This is the only cheap verification.
- Focused test: `mvn test -Dtest=FilterCollectionTest`
- No linter, formatter, or CI configuration exists.

### Build requires sapjco3 from a local repo
- Proprietary JCo artifact `org.hibersap:com.sap.conn.jco.sapjco3` (version range `[3.0.0,)`) is resolved from a **file-based local repo** `file:${user.home}/.localrepo/` (`jco.localrepo` property, repo id `project.local` in pom.xml). Without that directory, `mvn test` cannot build main or test sources.
- Dependency versions are open-ended ranges (`[1.1.0,)`, `[3.0.0,)`), so the build is not version-pinned.

## Running the goal

Only Mojo is `GenerateEntitiesMojo` (prefix `sapgen`, goal `generateSapEntities`, default phase `generate-sources`).

- Required params: `namePattern`, `outputDir`, `connectionProperties`. Optional: `maxResults` (default 20), `javaPackage` (default `org.hibersap.model`).
- Requires a reachable SAP system (from `sap-connection.properties`: `jco.client.ashost/user/passwd/sysnr`) and the sapjco native lib on `java.library.path` (e.g. via `MAVEN_OPTS`).

## Non-obvious behaviors & gotchas

- `connectionProperties` is a **directory path that must end with `/`**; `Utils.checkPath` throws `IllegalArgumentException` otherwise (GenerateEntitiesMojo.java:53 -> ConnectionPropertiesManager.java:76).
- If `sap-connection.properties` is missing at that path, `ConnectionPropertiesManager` silently writes the default placeholder file (`sapuser`/`password`) and continues; the failure only surfaces later, at session build/execute.
- `namePattern` is a comma-separated list of BAPI names or a pattern with `*`/`?` (converted to SAP LIKE `%`/`_`). Each comma-separated entry triggers one `RFC_READ_TABLE` search (SAPFunctionModuleSearch.java:76).
- Class names come from BAPI names via `Utils.toCamelCase(name, '_')` (e.g. `BAPI_GET_X` -> `BapiGetX`).
- Generated code uses Roaster; `roaster-jdt` is a runtime dep and must stay on the plugin's generated classpath.

## Layout

- `src/main/java/org/hibersap/plugin/GenerateEntitiesMojo.java` — Mojo / entry point.
- `src/main/java/org/hibersap/generator/` — search (`SAPFunctionModuleSearch`), model (`SAPEntityBuilder`/`SAPEntity`), properties (`ConnectionPropertiesManager`), helpers (`Utils`, `FilterCollection`).
- `src/main/resources/META-INF/sap-connection.properties` — default placeholder connection props, bundled as classpath default.
- `src/test/resources/META-INF/sap-connection.properties` — test copy; must keep exactly 12 properties (`ConnectionPropertiesManagerTest.getAllSAPProperties` asserts the count).

## Docs sync

- README.md is the canonical doc (replaced README.asciidoc). The plugin `<version>` shown in its pom example should match `pom.xml`'s `<version>`.

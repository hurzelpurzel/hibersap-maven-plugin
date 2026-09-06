# Contributing to hibersap-maven-plugin

Thanks for taking the time to contribute!

## Getting started

- Read the [README](README.md) for an overview of the project and how to build it.
- Open an [issue](https://github.com/hurzelpurzel/hibersap-maven-plugin/issues) to discuss a bug, feature request, or improvement before starting work.

## Building and verifying

- `mvn test` runs the unit tests. These are the only cheap verification, as they do not require a live SAP system.
- A focused test can be run with e.g. `mvn test -Dtest=FilterCollectionTest`.
- Building requires the proprietary `com.sap.conn.jco.sapjco3` artifact from a local repo at `file:${user.home}/.localrepo/`. See the `pom.xml` for details.

## Submitting changes

1. Fork the repository and create a feature branch.
2. Make your changes and ensure `mvn test` passes.
3. Commit with a clear, concise message describing the change.
4. Open a pull request and describe what you changed and why.

By contributing, you agree that your contributions are licensed under the [LGPL-3.0](LICENSE).

<!--
   SPDX-License-Identifier: CC-BY-4.0

   Copyright 2019-2025 The TurnKey Authors

   This work is licensed under the Creative Commons Attribution 4.0
   International License.

   You should have received a copy of the license along with this
   work. If not, see <https://creativecommons.org/licenses/by/4.0/>.
-->

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/tudo-aqua/turnkey-support/ci.yml?logo=githubactions&logoColor=white)](https://github.com/tudo-aqua/turnkey-support/actions)
[![JavaDoc](https://javadoc.io/badge2/tools.aqua/turnkey-support/javadoc.svg)](https://javadoc.io/doc/tools.aqua/turnkey-support)
[![Maven Central](https://img.shields.io/maven-central/v/tools.aqua/turnkey-support?logo=apache-maven)](https://search.maven.org/artifact/tools.aqua/turnkey-support)

# TurnKey Support Library

This is a support library for loading and writing TurnKey bundles. A TurnKey bundle is a Java
library that depends on a native library which is included in the JAR archive and unpacked at
runtime. This requires distributing the library and its dependencies for all supported platforms
inside the JAR.

However, loading libraries in the JVM is only possible if these libraries are present in the file
system. Therefore, all required libraries are unpacked to a temporary directory by the support
library. Additionally, loading of dependencies can not be done in a fully platform-agnostic way: for
some platforms, libraries can be (re-) linked to search for their dependencies in the same
directory, while for others, the dependencies must be explicitly loaded by the JVM beforehand. A
TurnKey bundle therefore includes a metadata file that a) lists all required libraries and b) lists
the required library load commands for the target platform in order.

A TurnKey bundle is identified by a _prefix_, which should correspond to the package name used by
the library's Java code (e.g., a JNI binding) to avoid namespace conflicts.

## Loading TurnKey Bundles

The support library is published as a
[Maven artifact on Maven Central](https://central.sonatype.com/artifact/tools.aqua/turnkey-support)
and can be included in every Maven-supporting build tool.

The library defines a single entry point for loading bundles, `TurnKey.load`. This method accepts
the bundle prefix and a function that can be used to load libraries, usually the loading class'
`getResourceAsStream` method. The latter is necessary to handle Java modularity, sind the libraries
can only be accessed from code in the same module, not the support library.

A sensible place for calling this method is the static initializer of, e.g., a JNI wrapper class:

```java
package com.acme.example;

import tools.aqua.turnkey.support.TurnKey;

class Example {
    static {
        TurnKey.load("com/acme/example", Example.class::getResourceAsStream);
    }
}

```

## Authoring TurnKey Bundles

A TurnKey bundle can be constructed by placing all required files in a specific structure and adding
a metadata file. Supported platforms are defined by _operating system_ and _CPU architecture_. The
JAR file should then contain all required files at `$prefix/$os/$arch`.

For each supported platform, the file `turnkey.xml` _must_ be present. It can be authored via the
`TurnKeyMetadata` class.

### Metadata File

The `turnkey.xml` file defines all metadata for the required support files. It is a Java Properties
XML file that stores collections of items as follows: for the list key `k`, the contents of `k` are
stored as `k.0`, `k.1`, etc. Each metadata file contains:

- the set `bundled-libraries`, containing all files that must be unpacked from the support file
  directory for the native code to work,
- the set `system-libraries`, containing all system libraries that are not bundled but must be
  present for the library to work (at the moment, this information is not used by the support
  library), and
- the list `load-command`, listing the required libraries in load order. If the platform support
  linking to libraries in the same directory, this will usually only contain a “root” library, if
  not, it will contain the dependency graph in inverse topological order.

### Layout Example

For the library `libexample.so` by _ACME, Inc._, a TurnKey bundle might contain:

```text
com/acme/example/Example.class            # JNI binding
com/acme/example/windows/x86/turnkey.xml  # Windows x86 metadata
com/acme/example/windows/x86/example.dll  # Windows x86 library file
com/acme/example/linux/amd64/turnkey.xml  # Linux AMD64 metadata
com/acme/example/linux/amd64/example.so   # Linux AMD64 library file
```

## Java, JPMS Support, and Nullability

The library requires Java 8. It can be used as a Java module on Java 9+ via the multi-release JAR
mechanism as the `tools.aqua.turnkey.support` module. It uses [JSpecify](https://jspecify.dev/)
annotations to declare nullability metadata.

## License

The support library's runtime code is released under the
[ISC License]("https://opensource.org/licenses/ISC"). Tests and other non-runtime code are licensed
under the [Apache Licence, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0). Standalone
documentation is licensed under the
[Creative Commons Attribution 4.0 International License](https://creativecommons.org/licenses/by/4.0/).
The library uses [JSpecify](https://jspecify.dev/) annotations, which are licensed under the
[Apache Licence, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## See Also

The [turnkey-gradle-plugin](https://github.com/tudo-aqua/turnkey-gradle-plugin) provides facilities
for rewriting non-TurnKey library files as well as modifying Java source code (to insert calls to
this library) via Gradle.

At the moment, two libraries are using this infrastructure:
[Z3-TurnKey](https://github.com/tudo-aqua/z3-turnkey) and
[cvc5-TurnKey](https://github.com/tudo-aqua/cvc5-turnkey).

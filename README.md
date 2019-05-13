# MAL

> French for "sick," something you become if you have to write too much code in it.
> - Andrew S. Tanenbaum

MAL is an assembler for the [MIC-1](https://en.wikipedia.org/wiki/MIC-1)
Micro-Assembly Language.

## Table of contents

* [Build](#build)
* [Usage](#usage)
* [Syntax](#syntax)
* [References](#references)

## Build

### Project

The project is built with [Gradle](https://gradle.org). The required version is
automatically downloaded by the wrapper, so simply clone the repository and run
the `build` task:

```sh
$ git clone https://github.com/albmoriconi/mal.git
$ ./gradlew build
```

The built program archives with executables and library dependencies can be
found in the `build/distributions` directory.

### Source code documentation

To build the Javadoc for the project, run the `javadoc` Gradle task. The
produced documentation can be found in the `build/docs/javadoc` directory.

## Usage

```
usage: mal [options] <input>
Assembler for MIC-1 Micro-Assembly language

options:
 -f <format>   output format: binary (default) | text
 -h,--help     display this help and exit
 -o <file>     write output to <file>
 ```
 
## Syntax

Work in progress.

## References

* Tanenbaum, A.S, and Austin, T. (2013). *Structured Computer Organization* (6th ed.). Pearson.

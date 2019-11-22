# MAL 🤒

[![Build Status](https://travis-ci.com/albmoriconi/mal.svg?token=YSE4xmZpZB4FSHR7YHVu&branch=master)](https://travis-ci.com/albmoriconi/mal)

> French for "sick," something you become if you have to write too much code in it.
> - Andrew S. Tanenbaum

MAL is an assembler for the [amic-0](https://github.com/albmoriconi/amic-0) Micro-Assembly Language.

## Table of contents

* [Build](#build)
* [Usage](#usage)
* [Syntax and semantics](#syntax-and-semantics)
* [References](#references)

## Build

### Project

The project is built with [Gradle](https://gradle.org). The required version is
automatically downloaded by the wrapper, so simply clone the repository and run
the `build` task:

```sh
$ git clone https://github.com/albmoriconi/mal.git
$ cd mal
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
 
## Syntax and semantics

The language is based on the [MIC-1](https://en.wikipedia.org/wiki/MIC-1) MAL,
clearly described in Tanenbaum (2013). This section describes the changes made
to its syntax and semantics.

### Case

The language is case sensitive. Statements are lower case, register names are
upper case.

### Comments

Comments go from a `#` character to the end of line.

### Statements

Two kinds of statements have been added: `empty`, that means that no bus, memory
or ALU operation is made during the cycle, and `halt`, that translates to an
empty instruction that has its address in the next address field.

### Labels

The syntax for labels is `label [= address]:` where the optional `address` is
hexadecimal, prefixed with `0x`, and specifies the instruction position in the
control store.

## References

* Tanenbaum, A.S, and Austin, T. (2013). *Structured Computer Organization* (6th
  ed.). Pearson.

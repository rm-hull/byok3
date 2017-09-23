# BYOK3
[![Build Status](https://travis-ci.org/rm-hull/byok3.svg?branch=master)](http://travis-ci.org/rm-hull/byok3)
[![Coverage Status](https://coveralls.io/repos/rm-hull/byok3/badge.svg?branch=master)](https://coveralls.io/r/rm-hull/byok3?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/5973b5240fb24f0056362d48/badge.svg)](https://www.versioneye.com/user/projects/5973b5240fb24f0056362d48)
[![Docker Pulls](https://img.shields.io/docker/pulls/richardhull/byok3.svg?maxAge=2592000)](https://hub.docker.com/r/richardhull/byok3/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/485be8ef80c140ffb76baf62b7a6b70e)](https://www.codacy.com/app/rm-hull/byok3?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rm-hull/byok3&amp;utm_campaign=Badge_Grade)
[![Maintenance](https://img.shields.io/maintenance/yes/2017.svg?maxAge=2592000)]()

BYOK (_BYE-OK_) is a [Scala](https://www.scala-lang.org/) program that implements a 
[Forth](http://lars.nocrew.org/forth2012/index.html) virtual machine.

## Building & running

Download [SBT](http://www.scala-sbt.org/), then clone the repository and build the code:

    $ git clone https://github.com/rm-hull/byok3.git
    $ cd byok3
    $ sbt repl/assembly
    
This produces a self-contained jar file `target/scala-2.12/byok3-repl.jar` 
which can be executed with:

    $ java -jar repl/target/scala-2.12/byok3-repl.jar

## Demo

[![asciicast](https://asciinema.org/a/kXEtkGGKCLPNpoiiai6g7WB55.png)](https://asciinema.org/a/kXEtkGGKCLPNpoiiai6g7WB55)

## Implementation Notes 

The forth virtual machine is based on a stateful [Context](https://github.com/rm-hull/byok3/blob/master/src/main/scala/byok3/data_structures/Context.scala) which is composed of the following parts

* Core memory address space
* A data stack
* A return stack
* A dictionaty of words
* A number of flags (compiling state, error) 

Ordinarily, a forth machine would most likely represent all these in terms of 
the core memory, but this implementation does not. That may change in the future.

The context is wrapped inside [cats](https://github.com/typelevel/cats)' State
monad,  specifically the type signature being: `StateT[Try, Context, A]`, and 
aliased as `AppState[A]`.

The `Try` part allows us to force the state to become invalidate either by using 
`inspectF`, `modifyF`, etc. or by letting the implicit `Applicatve[Try]` 
automatically take care of thrown exceptions.

This means that all the internal words can be expressed succinctly with for-comprehensions,
and further composed if necessary. For example:

```scala
@Documentation("Adds x to the single cell number at a-addr", stackEffect = "( x a-addr -- )")
val +! = for {
  addr <- dataStack(pop)
  x <- dataStack(pop)
  data <- memory(peek(addr))
  _ <- memory(poke(addr, data + x))
} yield ()
```

The interpreter and compiler both use an [executor](https://github.com/rm-hull/byok3/blob/master/src/main/scala/byok3/Executor.scala)
to recursively step through the words under consideration.

## TODO

* Move `DOES>` and `(DOES>)` to internal words (current implementation does not work).
* Implement `LOAD` and `LIST` block commands.
* Investigate if word input can be colorized with [JLine3](https://github.com/jline/jline3).
* Block editor
* Web API
* Implement full 2012 spec
* Implement forth test suite

## References

* http://lars.nocrew.org/forth2012/index.html

## License

### The MIT License (MIT)
   
Copyright (c) 2017 Richard Hull

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

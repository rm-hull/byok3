# BYOK3
[![Build Status](https://travis-ci.org/rm-hull/byok3.svg?branch=master)](http://travis-ci.org/rm-hull/byok3)
[![Coverage Status](https://coveralls.io/repos/github/rm-hull/byok3/badge.svg?branch=master)](https://coveralls.io/github/rm-hull/byok3?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/5973b5240fb24f0056362d48/badge.svg)](https://www.versioneye.com/user/projects/5973b5240fb24f0056362d48)
[![Docker Pulls](https://img.shields.io/docker/pulls/richardhull/byok3.svg?maxAge=2592000)](https://hub.docker.com/r/richardhull/byok3/)
[![Maintenance](https://img.shields.io/maintenance/yes/2018.svg?maxAge=2592000)]()

BYOK (_BYE-OK_) is a [Scala](https://www.scala-lang.org/) program that implements a
[Forth](http://lars.nocrew.org/forth2012/index.html) virtual machine. It can be run
either as a command-line REPL, or through a browser. It will almost certainly never
be useful for any purpose besides that which it has already fulfilled: forcing me to
think quite carefully about how forth works.

## Building & running

Download [SBT](http://www.scala-sbt.org/), then clone the repository and build the code:

    $ git clone https://github.com/rm-hull/byok3.git
    $ cd byok3
    $ sbt repl/assembly

This produces the REPL as a self-contained jar file `target/scala-2.12/byok3-repl.jar`
which can be executed with:

    $ java -jar repl/target/scala-2.12/byok3-repl.jar

To build the web service, execute:

    $ sbt web/assembly
    $ java -jar web/target/scala-2.12/byok3-web.jar

## Demo

A live web service is hosted on a [zeit now](https://zeit.co/now) node: https://byok3-hggauhqcjk.now.sh.
Try the following commands:

```forth
: sqrt-closer  ( square guess -- square guess adjustment ) 2dup / over - 2 / ;
   ok
: sqrt ( square -- root ) 1 begin sqrt-closer dup while + repeat drop nip ;
   ok
```

These compiled words implement the Newton-Raphson method to finding successively better
approximations of the root of a number, for example:

```forth
36 sqrt .
6   ok

53345 sqrt .
231   ok

231 dup * .
53361   ok
```

We can see the compiled code with:

```forth
: show ' dup >body swap >size disassemble ;
   ok
show sqrt-closer
000010AC:  2C 00 00 00  |,...|  : SQRT-CLOSER
000010B0:  67 00 00 00  |g...|  2DUP
000010B4:  03 00 00 00  |....|  /
000010B8:  5E 00 00 00  |^...|  OVER
000010BC:  01 00 00 00  |....|  -
000010C0:  4B 00 00 00  |K...|  (LIT)
000010C4:  02 00 00 00  |....|  2
000010C8:  03 00 00 00  |....|  /
000010CC:  2D 00 00 00  |-...|  EXIT
  ok
show sqrt
000010D0:  2C 00 00 00  |,...|  : SQRT
000010D4:  4B 00 00 00  |K...|  (LIT)
000010D8:  01 00 00 00  |....|  1
000010DC:  09 01 00 00  |....|  SQRT-CLOSER
000010E0:  66 00 00 00  |f...|  DUP
000010E4:  33 00 00 00  |3...|  0BRANCH
000010E8:  10 00 00 00  |....|  16 (==> 0x000010F8)
000010EC:  00 00 00 00  |....|  +
000010F0:  32 00 00 00  |2...|  BRANCH
000010F4:  E8 FF FF FF  |....|  -24 (==> 0x000010DC)
000010F8:  5C 00 00 00  |\...|  DROP
000010FC:  60 00 00 00  |`...|  NIP
00001100:  2D 00 00 00  |-...|  EXIT
  ok
```

Next, try loading one of the included examples (**note:** this might be better
performed from the command line, until the web service supports streamed results):

```forth
include forth/examples/mandelbrot.fs

Enter 'mandel' to draw the Mandelbrot Set.
  ok
mandel

                                                                                *
                                                                            *
                                                                          ****
                                                                       *********
                                                                       *********
                                                        *  *     *  *   *******                *
                                                          **** ***********************     *
                                                          ************************************
                                                    **** ************************************
                                                       ***************************************    *
                                                    ***********************************************
                                 *** ***** ***      ********************************************
                                 ***************   ***********************************************
                               ******************************************************************
                         **********************************************************************
         **  * * *  ***********************************************************************
                          *********************************************************************
                               ******************************************************************
                                ****************   **********************************************
                                 *** *********      ********************************************
                                                    ***********************************************
                                                       ***************************************
                                                     *** ************************************
                                                          ************************************
                                                          **** ***********************     * * *
                                                        *  *     *  *   *******   *
                                                                       *********
                                                                       *********
                                                                          ****
                                                                            *
                                                                               *
```

Alternatively, watch a screencast:

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

* ~~Move `DOES>` and `(DOES>)` to internal words (current implementation does not work).~~
* Implement `LOAD` and `LIST` block commands.
* ~~Investigate if word input can be colorized with [JLine3](https://github.com/jline/jline3).~~
* Block editor
* ~~Web API~~
* Implement full 2012 spec
* Implement forth test suite
* Performance improvements - reframe stack in terms of core memory
* Purely functional IO

## References

* http://lars.nocrew.org/forth2012/index.html
* https://rwmj.wordpress.com/2010/08/07/jonesforth-git-repository/
* http://galileo.phys.virginia.edu/classes/551.jvn.fall01/primer.htm
* https://github.com/nornagon/jonesforth/blob/master/jonesforth.S

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

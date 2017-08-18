# BYOK3
[![Build Status](https://travis-ci.org/rm-hull/byok3.svg?branch=master)](http://travis-ci.org/rm-hull/byok3)
[![Coverage Status](https://coveralls.io/repos/rm-hull/byok3/badge.svg?branch=master)](https://coveralls.io/r/rm-hull/byok3?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/5973b5240fb24f0056362d48/badge.svg)](https://www.versioneye.com/user/projects/5973b5240fb24f0056362d48)
[![Docker Pulls](https://img.shields.io/docker/pulls/richardhull/byok3.svg?maxAge=2592000)](https://hub.docker.com/r/richardhull/byok3/)
[![Maintenance](https://img.shields.io/maintenance/yes/2017.svg?maxAge=2592000)]()

BYOK (_BYE-OK_) is a [Scala](https://www.scala-lang.org/) program that implements a 
[Forth](http://lars.nocrew.org/forth2012/index.html) virtual machine.

## Building & running

Download [SBT](http://www.scala-sbt.org/), then clone the repository and build the code:

    $ git clone https://github.com/rm-hull/byok3.git
    $ cd byok3
    $ sbt assembly
    
This produces a self-contained jar file `target/scala-2.12/byok3-assembly-0.1.0.jar` 
which can be executed with:

    $ java -jar target/scala-2.12/byok3-assembly-0.1.0.jar

## Demo

[![asciicast](https://asciinema.org/a/kXEtkGGKCLPNpoiiai6g7WB55.png)](https://asciinema.org/a/kXEtkGGKCLPNpoiiai6g7WB55)

## Implementation Notes 

> TODO

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

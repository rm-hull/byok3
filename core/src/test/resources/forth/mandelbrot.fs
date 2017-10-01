\ Smallest Mandelbrot program
\
\ This mandelbrot program is even shorter than 8 lines. With its 4 lines it
\ prints a small mandelbrot in ascii characters. It is part of OpenBIOS
\ (http://www.openbios.org):

hex
: fix*  * e >>a ;
: sq    over dup fix* ;
: mandel
  4666 dup negate do
    4000 dup 2* negate do i j 1e
      begin  1- ?dup
      while  -rot sq sq 2dup + 10000 <
      while  - i + -rot fix* 2* j + rot
      repeat 2drop drop bl
      else   bl a +
      then emit 2drop
    268 +loop cr
  5de +loop ;
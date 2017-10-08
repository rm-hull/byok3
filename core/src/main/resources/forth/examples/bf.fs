\ bf interpreter
\
\ Here's a simple hello world program you can use to test it:
\
\ ++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.
\
\ [from http://sametwice.com/bf.fs]

variable cur
create buf 2000 allot

variable src-cur
variable src-size
create src 200 allot

create find-buf cell allot
2 find-buf c!
: findc find-buf 1 + 2dup 1 + c! c! find-buf find ;
: src-size++ src-size @ 1 + src-size ! ;
: src-cur-- src-cur @ 1 - src-cur ! ;
: src++@ src-cur @ 1 + dup src-cur ! c@ ;
: ?[ [char] [ = if 1 - then ;
: ?] [char] ] = if 1 + then ;

: scan-back
   begin
      src-cur-- src-cur @ c@ dup >r ?] r> ?[ dup 0= if exit then
   again
;

: ++ cur @ @ 1 + cur @ ! ;
: -- cur @ @ 1 - cur @ ! ;
: >> cur @ cell + cur ! ;
: << cur @ cell - cur ! ;
: .. cur @ @ emit ;
: [[ ;
: ]] cur @ @ if 1 scan-back drop then ;

: src-push src src-size @ + c! src-size++ ;

: readline
   cr begin
      key dup emit dup 13 = if drop cr exit then src-push
   again
;

: init src 1 - src-cur ! buf cur ! 0 src-size ! buf 2000 0 fill ;

: done? src-cur @ 1 + src src-size @ + = ;

: bf-run
   begin
      done? if exit then src++@ findc
      if
         execute
      else
         [char] ? emit drop
      then
   again
;

: main begin init readline bf-run again ;

.( Press esc to exit)
cr cr
main
\ @(#) private.fth 98/01/26 1.2
\ PRIVATIZE
\
\ Privatize words that are only needed within the file
\ and do not need to be exported.
\
\ Usage:
\    PRIVATE{
\    : FOO ;  \ Everything between PRIVATE{ and }PRIVATE will become private.
\    : MOO ;
\    }PRIVATE
\    : GOO   foo moo ;  \ can use foo and moo
\    PRIVATIZE          \ smudge foo and moo
\    ' foo              \ will fail
\
\ Copyright 1996 Phil Burk
\
\ 19970701 PLB Use unsigned compares for machines with "negative" addresses.

\ anew task-private.fth

variable private-start
variable private-stop

: PRIVATE{
    latest private-start !
    0 private-stop !
;
: }PRIVATE
    private-stop @ 0= not abort" Extra }PRIVATE"
    latest private-stop !
;
: PRIVATIZE  ( -- , smudge all words between PRIVATE{ and }PRIVATE )
    private-start @ 0= abort" Missing PRIVATE{"
    private-stop @ 0= abort" Missing }PRIVATE"
    private-stop @ 1+
    private-start @ 1+
    DO
      I NAME>
      2DUP TYPE CR
      (FORGET)
    LOOP

    0 private-start !
    0 private-stop !
;
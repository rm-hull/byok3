\ *********************************************************************
\ Some random forth code to test the stack machine

\ https://groups.google.com/forum/?hl=en#!topic/comp.lang.forth/cA76I1ryQrk

: GCD ( a b -- n )
    begin  dup while  tuck mod  repeat drop ;

\ : gcd2 ( a b d -- c )
\        >R 1 R@ +!
\        OVER IF OVER MOD SWAP R>  RECURSE
\           ELSE SWAP R> 2DROP
\           THEN ;

: SQR  ( n -- n ) dup * ;
: QUADRATIC  ( a b c d -- n )  >r swap rot r@ * + r> * + ;
: ?DOZEN  12 = IF ." dozen " ELSE ." not a dozen" THEN ;
: GREETINGS   ." Hello earthling " cr ;


\ This code prints a box made of asterisks of the specified height and width

: star 42 emit ;
: top 0 do star loop cr ; : bottom top ;
: middle star 2 - 0 do space loop star cr ;
: box ( width height -- ) cr over top 2 - 0 do dup middle loop bottom ;
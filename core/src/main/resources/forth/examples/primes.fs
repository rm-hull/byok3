\ SIEVE OF ERATOSTHENES from BYTE magazine -----------------------

DECIMAL 8190 CONSTANT TSIZE

VARIABLE FLAGS TSIZE ALLOT

: <SIEVE>  ( --- #primes )  FLAGS TSIZE 1 FILL
 0  TSIZE 0
 DO   ( n )  I FLAGS + C@
      IF    I  DUP +  3 +   DUP I +  (  I2*+3 I3*+3 )
           BEGIN  DUP TSIZE <  ( same flag )
           WHILE  0 OVER FLAGS + C! (  i' i'' )   OVER +
           REPEAT 2DROP  1+
      THEN
 LOOP       ;

: SIEVE  ." 10 iterations " CR  0   10 0
  DO     <SIEVE> swap drop
  LOOP   . ." primes " CR ;

: SIEVE50  ." 50 iterations " CR  0   50 0
  DO     <SIEVE> swap drop
  LOOP   . ." primes " CR ;

\ 10 iterations
\ 21.5 sec  Amiga Multi-Forth  Indirect Threaded
\ 8.82 sec  Amiga 1000 running JForth
\ ~5 sec  SGI Indy running pForthV9
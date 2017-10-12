\ Shapes [example][link to Forth file], using a defining word.

DECIMAL

: star  [CHAR] * EMIT ;

: .row  CR 8 0 DO
   DUP 128 AND IF  star
     ELSE  SPACE
     THEN
   1 LSHIFT
     LOOP  DROP ;

: SHAPE CREATE  8 0 DO  C,  LOOP
  DOES> 1 - DUP 8 + DO  I C@ .row  -1 +LOOP  CR ;

HEX
18 18 3C 5A 99 24 24 24  SHAPE man
81 42 24 18 18 24 24 81  SHAPE equis
AA AA FE FE 38 38 38 FE  SHAPE castle
DECIMAL
\ This is the source for the ANS test harness, it is based on the
\ harness originally developed by John Hayes

\ (C) 1995 JOHNS HOPKINS UNIVERSITY / APPLIED PHYSICS LABORATORY
\ MAY BE DISTRIBUTED FREELY AS LONG AS THIS COPYRIGHT NOTICE REMAINS.
\ VERSION 1.1

\ Revision history and possibly newer versions can be found at
\ http://www.forth200x/tests/ttester.fs

BASE @ 
HEX 

VARIABLE ACTUAL-DEPTH \ stack record 
CREATE ACTUAL-RESULTS 20 CELLS ALLOT 
VARIABLE START-DEPTH 
VARIABLE XCURSOR \ for ...}T 
VARIABLE ERROR-XT 

: ERROR ERROR-XT @ EXECUTE ; \ for vectoring of error reporting 

: "FLOATING" S" FLOATING" ; \ only compiled S" in CORE 
: "FLOATING-STACK" S" FLOATING-STACK" ; 
"FLOATING" ENVIRONMENT? [IF] 
   [IF] 
     TRUE 
   [ELSE] 
     FALSE 
   [THEN] 
[ELSE] 
   FALSE 
[THEN] CONSTANT HAS-FLOATING 

"FLOATING-STACK" ENVIRONMENT? [IF] 
   [IF] 
     TRUE 
   [ELSE] 
     FALSE 
   [THEN] 
[ELSE] \ We don't know whether the FP stack is separate. 
   HAS-FLOATING \ If we have FLOATING, we assume it is. 
[THEN] CONSTANT HAS-FLOATING-STACK 

HAS-FLOATING [IF] 
   \ Set the following to the relative and absolute tolerances you 
   \ want for approximate float equality, to be used with F in 
   \ FNEARLY=. Keep the signs, because F needs them. 
   FVARIABLE REL-NEAR DECIMAL 1E-12 HEX REL-NEAR F! 
   FVARIABLE ABS-NEAR DECIMAL 0E HEX ABS-NEAR F! 

   \ When EXACT? is TRUE, }F uses FEXACTLY=, otherwise FNEARLY=. 

   TRUE VALUE EXACT? 
   : SET-EXACT ( -- ) TRUE TO EXACT? ; 
   : SET-NEAR ( -- ) FALSE TO EXACT? ; 

   DECIMAL 
   : FEXACTLY= ( F: X Y -- S: FLAG ) 
     ( 
     Leave TRUE if the two floats are identical. 
     ) 
     0E F~ ; 
   HEX 

   : FABS= ( F: X Y -- S: FLAG ) 
     ( 
     Leave TRUE if the two floats are equal within the tolerance 
     stored in ABS-NEAR. 
     ) 
     ABS-NEAR F@ F~ ; 

   : FREL= ( F: X Y -- S: FLAG ) 
     ( 
     Leave TRUE if the two floats are relatively equal based on the 
     tolerance stored in ABS-NEAR. 
     ) 
     REL-NEAR F@ FNEGATE F~ ; 

   : F2DUP FOVER FOVER ; 
   : F2DROP FDROP FDROP ; 

   : FNEARLY= ( F: X Y -- S: FLAG ) 
     ( 
     Leave TRUE if the two floats are nearly equal. This is a 
     refinement of Dirk Zoller's FEQ to also allow X = Y, including 
     both zero, or to allow approximately equality when X and Y are too 
     small to satisfy the relative approximation mode in the F~ 
     specification. 
     ) 
     F2DUP FEXACTLY= IF F2DROP TRUE EXIT THEN 
     F2DUP FREL= IF F2DROP TRUE EXIT THEN 
     FABS= ; 

   : FCONF= ( R1 R2 -- F ) 
     EXACT? IF 
       FEXACTLY= 
     ELSE 
       FNEARLY= 
     THEN ; 
[THEN] 

HAS-FLOATING-STACK [IF] 
   VARIABLE ACTUAL-FDEPTH 
   CREATE ACTUAL-FRESULTS 20 FLOATS ALLOT 
   VARIABLE START-FDEPTH 
   VARIABLE FCURSOR 

   : EMPTY-FSTACK ( ... -- ... ) 
     FDEPTH START-FDEPTH @ < IF 
       FDEPTH START-FDEPTH @ SWAP DO 0E LOOP 
     THEN 
     FDEPTH START-FDEPTH @ > IF 
       FDEPTH START-FDEPTH @ DO FDROP LOOP 
     THEN ; 

   : F{ ( -- ) 
     FDEPTH START-FDEPTH ! 0 FCURSOR ! ; 

   : F-> ( ... -- ... ) 
     FDEPTH DUP ACTUAL-FDEPTH ! 
     START-FDEPTH @ > IF 
      FDEPTH START-FDEPTH @ - 0 DO ACTUAL-FRESULTS I FLOATS + F! LOOP 
     THEN ; 

   : F} ( ... -- ... ) 
     FDEPTH ACTUAL-FDEPTH @ = IF 
       FDEPTH START-FDEPTH @ > IF 
         FDEPTH START-FDEPTH @ - 0 DO 
           ACTUAL-FRESULTS I FLOATS + F@ FCONF= INVERT IF 
             S" INCORRECT FP RESULT: " ERROR LEAVE 
           THEN 
         LOOP 
       THEN 
     ELSE 
       S" WRONG NUMBER OF FP RESULTS: " ERROR 
     THEN ; 

   : F...}T ( -- ) 
     FCURSOR @ START-FDEPTH @ + ACTUAL-FDEPTH @ <> IF 
     S" NUMBER OF FLOAT RESULTS BEFORE '->' DOES NOT MATCH ...}T " 
     S" SPECIFICATION: " ERROR 
     ELSE FDEPTH START-FDEPTH @ = 0= IF 
     S" NUMBER OF FLOAT RESULTS BEFORE AND AFTER '->' DOES NOT MATCH: " 
     ERROR 
     THEN THEN ; 

   : FTESTER ( R -- ) 
     FDEPTH 0= ACTUAL-FDEPTH @ FCURSOR @ START-FDEPTH @ + 1+ < OR IF 
     S" NUMBER OF FLOAT RESULTS AFTER '->' BELOW ...}T SPECIFICATION: " 
     ERROR 
     ELSE ACTUAL-FRESULTS FCURSOR @ FLOATS + F@ FCONF= 0= IF 
     S" INCORRECT FP RESULT: " ERROR 
     THEN THEN 
     1 FCURSOR +! ; 

[ELSE] 
   : EMPTY-FSTACK ; 
   : F{ ; 
   : F-> ; 
   : F} ; 
   : F...}T ; 

   HAS-FLOATING [IF] 
     DECIMAL 
     : COMPUTE-CELLS-PER-FP ( -- U ) 
       DEPTH 0E DEPTH 1- >R FDROP R> SWAP - ; 
     HEX 

     COMPUTE-CELLS-PER-FP CONSTANT CELLS-PER-FP 

     : FTESTER ( R -- ) 
       DEPTH CELLS-PER-FP < 
       ACTUAL-DEPTH @ XCURSOR @ START-DEPTH @ + CELLS-PER-FP + < 
       OR IF 
         S" NUMBER OF RESULTS AFTER '->' BELOW ...}T SPECIFICATION: " 
         ERROR EXIT 
       ELSE ACTUAL-RESULTS XCURSOR @ CELLS + F@ FCONF= 0= IF 
         S" INCORRECT FP RESULT: " ERROR 
       THEN THEN 
       CELLS-PER-FP XCURSOR +! ; 
   [THEN] 
[THEN] 

: EMPTY-STACK   \ ( ... -- ) empty stack; handles underflowed stack too. 
   DEPTH START-DEPTH @ < IF 
     DEPTH START-DEPTH @ SWAP DO 0 LOOP 
   THEN 
   DEPTH START-DEPTH @ > IF 
     DEPTH START-DEPTH @ DO DROP LOOP 
   THEN 
   EMPTY-FSTACK ; 

: ERROR1    \ ( C-ADDR U -- ) display an error message 
           \ followed by the line that had the error. 
   TYPE SOURCE TYPE CR  \ display line corresponding to error 
   EMPTY-STACK \ throw away everything else 
; 

' ERROR1 ERROR-XT ! 

: T{    \ ( -- ) record the pre-test depth. 
   DEPTH START-DEPTH ! 0 XCURSOR ! F{ ; 

: ->    \ ( ... -- ) record depth and contents of stack. 
   DEPTH DUP ACTUAL-DEPTH ! \ record depth 
   START-DEPTH @ > IF   \ if there is something on the stack 
     DEPTH START-DEPTH @ - 0 DO \ save them 
       ACTUAL-RESULTS I CELLS + ! 
     LOOP 
   THEN 
   F-> ; 

: }T    \ ( ... -- ) comapre stack (expected) contents with saved 
   \ (actual) contents. 
   DEPTH ACTUAL-DEPTH @ = IF                \ if depths match 
     DEPTH START-DEPTH @ > IF               \ if something on the stack 
       DEPTH START-DEPTH @ - 0 DO       \ for each stack item 
         ACTUAL-RESULTS I CELLS + @     \ compare actual with expected 
         <> IF S" INCORRECT RESULT: " ERROR LEAVE THEN 
       LOOP 
     THEN 
   ELSE                                     \ depth mismatch 
     S" WRONG NUMBER OF RESULTS: " ERROR 
   THEN 
   F} ; 

: ...}T ( -- ) 
   XCURSOR @ START-DEPTH @ + ACTUAL-DEPTH @ <> IF 
     S" NUMBER OF CELL RESULTS BEFORE '->' DOES NOT MATCH ...}T " 
     S" SPECIFICATION: " ERROR 
   ELSE DEPTH START-DEPTH @ = 0= IF 
     S" NUMBER OF CELL RESULTS BEFORE AND AFTER '->' DOES NOT MATCH: " 
     ERROR 
   THEN THEN 
   F...}T ; 

: XTESTER ( X -- ) 
   DEPTH 0= ACTUAL-DEPTH @ XCURSOR @ START-DEPTH @ + 1+ < OR IF 
     S" NUMBER OF CELL RESULTS AFTER '->' BELOW ...}T SPECIFICATION: " 
     ERROR EXIT 
   ELSE ACTUAL-RESULTS XCURSOR @ CELLS + @ <> IF 
     S" INCORRECT CELL RESULT: " ERROR 
   THEN THEN 
   1 XCURSOR +! ; 

: X}T XTESTER ...}T ; 
: XX}T XTESTER XTESTER ...}T ; 
: XXX}T XTESTER XTESTER XTESTER ...}T ; 
: XXXX}T XTESTER XTESTER XTESTER XTESTER ...}T ; 

HAS-FLOATING [IF] 
   : R}T FTESTER ...}T ; 
   : XR}T FTESTER XTESTER ...}T ; 
   : RX}T XTESTER FTESTER ...}T ; 
   : RR}T FTESTER FTESTER ...}T ; 
   : XXR}T FTESTER XTESTER XTESTER ...}T ; 
   : XRX}T XTESTER FTESTER XTESTER ...}T ; 
   : XRR}T FTESTER FTESTER XTESTER ...}T ; 
   : RXX}T XTESTER XTESTER FTESTER ...}T ; 
   : RXR}T FTESTER XTESTER FTESTER ...}T ; 
   : RRX}T XTESTER FTESTER FTESTER ...}T ; 
   : RRR}T FTESTER FTESTER FTESTER ...}T ; 
   : XXXR}T FTESTER XTESTER XTESTER XTESTER ...}T ; 
   : XXRX}T XTESTER FTESTER XTESTER XTESTER ...}T ; 
   : XXRR}T FTESTER FTESTER XTESTER XTESTER ...}T ; 
   : XRXX}T XTESTER XTESTER FTESTER XTESTER ...}T ; 
   : XRXR}T FTESTER XTESTER FTESTER XTESTER ...}T ; 
   : XRRX}T XTESTER FTESTER FTESTER XTESTER ...}T ; 
   : XRRR}T FTESTER FTESTER FTESTER XTESTER ...}T ; 
   : RXXX}T XTESTER XTESTER XTESTER FTESTER ...}T ; 
   : RXXR}T FTESTER XTESTER XTESTER FTESTER ...}T ; 
   : RXRX}T XTESTER FTESTER XTESTER FTESTER ...}T ; 
   : RXRR}T FTESTER FTESTER XTESTER FTESTER ...}T ; 
   : RRXX}T XTESTER XTESTER FTESTER FTESTER ...}T ; 
   : RRXR}T FTESTER XTESTER FTESTER FTESTER ...}T ; 
   : RRRX}T XTESTER FTESTER FTESTER FTESTER ...}T ; 
   : RRRR}T FTESTER FTESTER FTESTER FTESTER ...}T ; 
[THEN] 

\ Set the following flag to TRUE for more verbose output; this may 
\ allow you to tell which test caused your system to hang. 
VARIABLE VERBOSE 
FALSE VERBOSE ! 

: TESTING   \ ( -- ) TALKING COMMENT. 
   SOURCE VERBOSE @ 
   IF DUP >R TYPE CR R> >IN ! 
   ELSE >IN ! DROP 
   THEN ; 

BASE !
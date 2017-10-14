\ @IGNORED

\ ------------------------------------------------------------------------
TESTING <# # #S #> HOLD SIGN BASE >NUMBER HEX DECIMAL

: S=  \ ( ADDR1 C1 ADDR2 C2 -- T/F ) COMPARE TWO STRINGS.
   >R SWAP R@ = IF            \ MAKE SURE STRINGS HAVE SAME LENGTH
      R> ?DUP IF              \ IF NON-EMPTY STRINGS
     0 DO
        OVER C@ OVER C@ - IF 2DROP <FALSE> UNLOOP EXIT THEN
        SWAP CHAR+ SWAP CHAR+
         LOOP
      THEN
      2DROP <TRUE>            \ IF WE GET HERE, STRINGS MATCH
   ELSE
      R> DROP 2DROP <FALSE>        \ LENGTHS MISMATCH
   THEN ;

: GP1  <# 41 HOLD 42 HOLD 0 0 #> S" BA" S= ;
{ GP1 -> <TRUE> }

: GP2  <# -1 SIGN 0 SIGN -1 SIGN 0 0 #> S" --" S= ;
{ GP2 -> <TRUE> }

: GP3  <# 1 0 # # #> S" 01" S= ;
{ GP3 -> <TRUE> }

: GP4  <# 1 0 #S #> S" 1" S= ;
{ GP4 -> <TRUE> }

24 CONSTANT MAX-BASE            \ BASE 2 .. 36
: COUNT-BITS
   0 0 INVERT BEGIN DUP WHILE >R 1+ R> 2* REPEAT DROP ;
COUNT-BITS 2* CONSTANT #BITS-UD        \ NUMBER OF BITS IN UD

: GP5
   BASE @ <TRUE>
   MAX-BASE 1+ 2 DO            \ FOR EACH POSSIBLE BASE
      I BASE !                \ TBD: ASSUMES BASE WORKS
      I 0 <# #S #> S" 10" S= AND
   LOOP
   SWAP BASE ! ;
{ GP5 -> <TRUE> }

: GP6
   BASE @ >R  2 BASE !
   MAX-UINT MAX-UINT <# #S #>        \ MAXIMUM UD TO BINARY
   R> BASE !                \ S: C-ADDR U
   DUP #BITS-UD = SWAP
   0 DO                    \ S: C-ADDR FLAG
      OVER C@ [CHAR] 1 = AND        \ ALL ONES
      >R CHAR+ R>
   LOOP SWAP DROP ;
{ GP6 -> <TRUE> }

: GP7
   BASE @ >R    MAX-BASE BASE !
   <TRUE>
   A 0 DO
      I 0 <# #S #>
      1 = SWAP C@ I 30 + = AND AND
   LOOP
   MAX-BASE A DO
      I 0 <# #S #>
      1 = SWAP C@ 41 I A - + = AND AND
   LOOP
   R> BASE ! ;

{ GP7 -> <TRUE> }

\ >NUMBER TESTS
CREATE GN-BUF 0 C,
: GN-STRING    GN-BUF 1 ;
: GN-CONSUMED  GN-BUF CHAR+ 0 ;
: GN'          [CHAR] ' WORD CHAR+ C@ GN-BUF C!  GN-STRING ;

{ 0 0 GN' 0' >NUMBER -> 0 0 GN-CONSUMED }
{ 0 0 GN' 1' >NUMBER -> 1 0 GN-CONSUMED }
{ 1 0 GN' 1' >NUMBER -> BASE @ 1+ 0 GN-CONSUMED }
{ 0 0 GN' -' >NUMBER -> 0 0 GN-STRING }    \ SHOULD FAIL TO CONVERT THESE
{ 0 0 GN' +' >NUMBER -> 0 0 GN-STRING }
{ 0 0 GN' .' >NUMBER -> 0 0 GN-STRING }

: >NUMBER-BASED
   BASE @ >R BASE ! >NUMBER R> BASE ! ;

{ 0 0 GN' 2' 10 >NUMBER-BASED -> 2 0 GN-CONSUMED }
{ 0 0 GN' 2'  2 >NUMBER-BASED -> 0 0 GN-STRING }
{ 0 0 GN' F' 10 >NUMBER-BASED -> F 0 GN-CONSUMED }
{ 0 0 GN' G' 10 >NUMBER-BASED -> 0 0 GN-STRING }
{ 0 0 GN' G' MAX-BASE >NUMBER-BASED -> 10 0 GN-CONSUMED }
{ 0 0 GN' Z' MAX-BASE >NUMBER-BASED -> 23 0 GN-CONSUMED }

: GN1    \ ( UD BASE -- UD' LEN ) UD SHOULD EQUAL UD' AND LEN SHOULD BE ZERO.
   BASE @ >R BASE !
   <# #S #>
   0 0 2SWAP >NUMBER SWAP DROP        \ RETURN LENGTH ONLY
   R> BASE ! ;
{ 0 0 2 GN1 -> 0 0 0 }
{ MAX-UINT 0 2 GN1 -> MAX-UINT 0 0 }
{ MAX-UINT DUP 2 GN1 -> MAX-UINT DUP 0 }
{ 0 0 MAX-BASE GN1 -> 0 0 0 }
{ MAX-UINT 0 MAX-BASE GN1 -> MAX-UINT 0 0 }
{ MAX-UINT DUP MAX-BASE GN1 -> MAX-UINT DUP 0 }

: GN2    \ ( -- 16 10 )
   BASE @ >R  HEX BASE @  DECIMAL BASE @  R> BASE ! ;
{ GN2 -> 10 A }

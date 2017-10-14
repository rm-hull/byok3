\ @IGNORED

\ ------------------------------------------------------------------------
TESTING SOURCE >IN WORD

: GS1 S" SOURCE" 2DUP EVALUATE
       >R SWAP >R = R> R> = ;
{ GS1 -> <TRUE> <TRUE> }

VARIABLE SCANS
: RESCAN?  -1 SCANS +! SCANS @ IF 0 >IN ! THEN ;

{ 2 SCANS !
345 RESCAN?
-> 345 345 }

: GS2  5 SCANS ! S" 123 RESCAN?" EVALUATE ;
{ GS2 -> 123 123 123 123 123 }

: GS3 WORD COUNT SWAP C@ ;
{ BL GS3 HELLO -> 5 CHAR H }
{ CHAR " GS3 GOODBYE" -> 7 CHAR G }
{ BL GS3
DROP -> 0 }                \ BLANK LINE RETURN ZERO-LENGTH STRING

: GS4 SOURCE >IN ! DROP ;
{ GS4 123 456
-> }

\ ------------------------------------------------------------------------
TESTING BASIC ASSUMPTIONS

{ -> }					\ START WITH CLEAN SLATE
( TEST IF ANY BITS ARE SET; ANSWER IN BASE 1 )
{ : BITSSET? IF 0 0 ELSE 0 THEN ; -> }
{  0 BITSSET? -> 0 }		( ZERO IS ALL BITS CLEAR )
{  1 BITSSET? -> 0 0 }		( OTHER NUMBER HAVE AT LEAST ONE BIT )
{ -1 BITSSET? -> 0 0 }
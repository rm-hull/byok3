\ ------------------------------------------------------------------------
TESTING EVALUATE

: GE1 S" 123" ; IMMEDIATE
: GE2 S" 123 1+" ; IMMEDIATE
: GE3 S" : GE4 345 ;" ;
: GE5 EVALUATE ; IMMEDIATE

{ GE1 EVALUATE -> 123 }            ( TEST EVALUATE IN INTERP. STATE )
{ GE2 EVALUATE -> 124 }
{ GE3 EVALUATE -> }
{ GE4 -> 345 }

{ : GE6 GE1 GE5 ; -> }            ( TEST EVALUATE IN COMPILE STATE )
{ GE6 -> 123 }
{ : GE7 GE2 GE5 ; -> }
{ GE7 -> 124 }
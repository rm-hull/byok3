\ ------------------------------------------------------------------------
TESTING CHAR [CHAR] [ ] BL S"

HEX

{ BL -> 20 }
{ CHAR X -> 58 }
{ CHAR HELLO -> 48 }
{ : GC1 [CHAR] X ; -> }
{ : GC2 [CHAR] HELLO ; -> }
{ GC1 -> 58 }
{ GC2 -> 48 }
{ : GC3 [ GC1 ] LITERAL ; -> }
{ GC3 -> 58 }
{ : GC4 S" XY" ; -> }
{ GC4 SWAP DROP -> 2 }
{ GC4 DROP DUP C@ SWAP CHAR+ C@ -> 58 59 }

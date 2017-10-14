\ ------------------------------------------------------------------------
TESTING CHAR [CHAR] [ ] BL S"

HEX

T{ BL -> 20 }T
T{ CHAR X -> 58 }T
T{ CHAR HELLO -> 48 }T
T{ : GC1 [CHAR] X ; -> }T
T{ : GC2 [CHAR] HELLO ; -> }T
T{ GC1 -> 58 }T
T{ GC2 -> 48 }T
T{ : GC3 [ GC1 ] LITERAL ; -> }T
T{ GC3 -> 58 }T
T{ : GC4 S" XY" ; -> }T
T{ GC4 SWAP DROP -> 2 }T
T{ GC4 DROP DUP C@ SWAP CHAR+ C@ -> 58 59 }T

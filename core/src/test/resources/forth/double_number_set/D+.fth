T{  0.  5. D+ ->  5. }T                         \ small integers
T{ -5.  0. D+ -> -5. }T
T{  1.  2. D+ ->  3. }T
T{  1. -2. D+ -> -1. }T
T{ -1.  2. D+ ->  1. }T
T{ -1. -2. D+ -> -3. }T
T{ -1.  1. D+ ->  0. }T
T{  0  0  0  5 D+ ->  0  5 }T                  \ mid range integers
T{ -1  5  0  0 D+ -> -1  5 }T
T{  0  0  0 -5 D+ ->  0 -5 }T
T{  0 -5 -1  0 D+ -> -1 -5 }T
T{  0  1  0  2 D+ ->  0  3 }T
T{ -1  1  0 -2 D+ -> -1 -1 }T
T{  0 -1  0  2 D+ ->  0  1 }T
T{  0 -1 -1 -2 D+ -> -1 -3 }T
T{ -1 -1  0  1 D+ -> -1  0 }T

T{ MIN-INT 0 2DUP D+ -> 0 1 }T
T{ MIN-INT S>D MIN-INT 0 D+ -> 0 0 }T

T{  HI-2INT       1. D+ -> 0 HI-INT 1+ }T    \ large double integers
T{  HI-2INT     2DUP D+ -> 1S 1- MAX-INT }T
T{ MAX-2INT MIN-2INT D+ -> -1. }T
T{ MAX-2INT  LO-2INT D+ -> HI-2INT }T
T{  LO-2INT     2DUP D+ -> MIN-2INT }T
T{  HI-2INT MIN-2INT D+ 1. D+ -> LO-2INT }T
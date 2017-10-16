T{  0.  5. D- -> -5. }T              \ small integers
T{  5.  0. D- ->  5. }T
T{  0. -5. D- ->  5. }T
T{  1.  2. D- -> -1. }T
T{  1. -2. D- ->  3. }T
T{ -1.  2. D- -> -3. }T
T{ -1. -2. D- ->  1. }T
T{ -1. -1. D- ->  0. }T
T{  0  0  0  5 D- ->  0 -5 }T       \ mid-range integers
T{ -1  5  0  0 D- -> -1  5 }T
T{  0  0 -1 -5 D- ->  1  4 }T
T{  0 -5  0  0 D- ->  0 -5 }T
T{ -1  1  0  2 D- -> -1 -1 }T
T{  0  1 -1 -2 D- ->  1  2 }T
T{  0 -1  0  2 D- ->  0 -3 }T
T{  0 -1  0 -2 D- ->  0  1 }T
T{  0  0  0  1 D- ->  0 -1 }T
T{ MIN-INT 0 2DUP D- -> 0. }T
T{ MIN-INT S>D MAX-INT 0D- -> 1 1s }T
T{ MAX-2INT max-2INT D- -> 0. }T    \ large integers
T{ MIN-2INT min-2INT D- -> 0. }T
T{ MAX-2INT  hi-2INT D- -> lo-2INT DNEGATE }T
T{  HI-2INT  lo-2INT D- -> max-2INT }T
T{  LO-2INT  hi-2INT D- -> min-2INT 1. D+ }T
T{ MIN-2INT min-2INT D- -> 0. }T
T{ MIN-2INT  lo-2INT D- -> lo-2INT }T
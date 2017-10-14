\ ------------------------------------------------------------------------
TESTING DEFINING WORDS: : ; CONSTANT VARIABLE CREATE DOES> >BODY

T{ 123 CONSTANT X123 -> }T
T{ X123 -> 123 }T
T{ : EQU CONSTANT ; -> }T
T{ X123 EQU Y123 -> }T
T{ Y123 -> 123 }T

T{ VARIABLE V1 -> }T
T{ 123 V1 ! -> }T
T{ V1 @ -> 123 }T

T{ : NOP : POSTPONE ; ; -> }T
T{ NOP NOP1 NOP NOP2 -> }T
T{ NOP1 -> }T
T{ NOP2 -> }T

T{ : DOES1 DOES> @ 1 + ; -> }T
T{ : DOES2 DOES> @ 2 + ; -> }T
T{ CREATE CR1 -> }T
T{ CR1 -> HERE }T
T{ ' CR1 >BODY 4 CELLS + -> HERE }T
T{ 1 , -> }T
T{ CR1 @ -> 1 }T
T{ DOES1 -> }T
T{ CR1 -> 2 }T
T{ DOES2 -> }T
T{ CR1 -> 3 }T

T{ : WEIRD: CREATE DOES> 1 + DOES> 2 + ; -> }T
T{ WEIRD: W1 -> }T
T{ ' W1 >BODY 4 CELLS + -> HERE }T
T{ W1 -> HERE 1 + }T
T{ W1 -> HERE 2 + }T

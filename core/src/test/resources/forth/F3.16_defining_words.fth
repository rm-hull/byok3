\ ------------------------------------------------------------------------
TESTING DEFINING WORDS: : ; CONSTANT VARIABLE CREATE DOES> >BODY

{ 123 CONSTANT X123 -> }
{ X123 -> 123 }
{ : EQU CONSTANT ; -> }
{ X123 EQU Y123 -> }
{ Y123 -> 123 }

{ VARIABLE V1 -> }
{ 123 V1 ! -> }
{ V1 @ -> 123 }

{ : NOP : POSTPONE ; ; -> }
{ NOP NOP1 NOP NOP2 -> }
{ NOP1 -> }
{ NOP2 -> }

{ : DOES1 DOES> @ 1 + ; -> }
{ : DOES2 DOES> @ 2 + ; -> }
{ CREATE CR1 -> }
{ CR1 -> HERE }
{ ' CR1 >BODY 4 CELLS + -> HERE }
{ 1 , -> }
{ CR1 @ -> 1 }
{ DOES1 -> }
{ CR1 -> 2 }
{ DOES2 -> }
{ CR1 -> 3 }

{ : WEIRD: CREATE DOES> 1 + DOES> 2 + ; -> }
{ WEIRD: W1 -> }
{ ' W1 >BODY 4 CELLS + -> HERE }
{ W1 -> HERE 1 + }
{ W1 -> HERE 2 + }

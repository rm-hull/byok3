\ ------------------------------------------------------------------------
TESTING >R R> R@

{ : GR1 >R R> ; -> }
{ : GR2 >R R@ R> DROP ; -> }
{ 123 GR1 -> 123 }
{ 123 GR2 -> 123 }
{ 1S GR1 -> 1S }   ( RETURN STACK HOLDS CELLS )
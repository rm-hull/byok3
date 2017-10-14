\ @IGNORED

\ ------------------------------------------------------------------------
TESTING 2* 2/ LSHIFT RSHIFT

{ MSB BITSSET? -> 0 0 }

{ 0S 2* -> 0S }
{ 1 2* -> 2 }
{ 4000 2* -> 8000 }
{ 1S 2* 1 XOR -> 1S }
{ MSB 2* -> 0S }

{ 0S 2/ -> 0S }
{ 1 2/ -> 0 }
{ 4000 2/ -> 2000 }
{ 1S 2/ -> 1S }                \ MSB PROPOGATED
{ 1S 1 XOR 2/ -> 1S }
{ MSB 2/ MSB AND -> MSB }

{ 1 0 LSHIFT -> 1 }
{ 1 1 LSHIFT -> 2 }
{ 1 2 LSHIFT -> 4 }
{ 1 F LSHIFT -> 8000 }            \ BIGGEST GUARANTEED SHIFT
{ 1S 1 LSHIFT 1 XOR -> 1S }
{ MSB 1 LSHIFT -> 0 }

{ 1 0 RSHIFT -> 1 }
{ 1 1 RSHIFT -> 0 }
{ 2 1 RSHIFT -> 1 }
{ 4 2 RSHIFT -> 1 }
{ 8000 F RSHIFT -> 1 }            \ BIGGEST
{ MSB 1 RSHIFT MSB AND -> 0 }        \ RSHIFT ZERO FILLS MSBS
{ MSB 1 RSHIFT 2* -> MSB }
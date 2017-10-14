\ ------------------------------------------------------------------------
TESTING BOOLEANS: INVERT AND OR XOR

{ 0 0 AND -> 0 }
{ 0 1 AND -> 0 }
{ 1 0 AND -> 0 }
{ 1 1 AND -> 1 }

{ 0 INVERT 1 AND -> 1 }
{ 1 INVERT 1 AND -> 0 }

0        CONSTANT 0S
0 INVERT CONSTANT 1S

{ 0S INVERT -> 1S }
{ 1S INVERT -> 0S }

{ 0S 0S AND -> 0S }
{ 0S 1S AND -> 0S }
{ 1S 0S AND -> 0S }
{ 1S 1S AND -> 1S }

{ 0S 0S OR -> 0S }
{ 0S 1S OR -> 1S }
{ 1S 0S OR -> 1S }
{ 1S 1S OR -> 1S }

{ 0S 0S XOR -> 0S }
{ 0S 1S XOR -> 1S }
{ 1S 0S XOR -> 1S }
{ 1S 1S XOR -> 0S }
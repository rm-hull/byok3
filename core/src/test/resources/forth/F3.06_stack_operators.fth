\ ------------------------------------------------------------------------
TESTING STACK OPS: 2DROP 2DUP 2OVER 2SWAP ?DUP DEPTH DROP DUP OVER ROT SWAP

{ 1 2 2DROP -> }
{ 1 2 2DUP -> 1 2 1 2 }
{ 1 2 3 4 2OVER -> 1 2 3 4 1 2 }
{ 1 2 3 4 2SWAP -> 3 4 1 2 }
{ 0 ?DUP -> 0 }
{ 1 ?DUP -> 1 1 }
{ -1 ?DUP -> -1 -1 }
{ DEPTH -> 0 }
{ 0 DEPTH -> 0 1 }
{ 0 1 DEPTH -> 0 1 2 }
{ 0 DROP -> }
{ 1 2 DROP -> 1 }
{ 1 DUP -> 1 1 }
{ 1 2 OVER -> 1 2 1 }
{ 1 2 3 ROT -> 2 3 1 }
{ 1 2 SWAP -> 2 1 }
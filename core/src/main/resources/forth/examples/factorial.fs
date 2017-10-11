\ Using Forth's recursion operator; in some implementations it is called MYSELF.
\ The usual example is the coding of the factorial function.

: FACTORIAL1 ( +n1 -- +n2)
   DUP 2 < IF DROP 1 EXIT THEN
   DUP 1- RECURSE *
;

\ While beloved of computer scientists, recursion makes unusually heavy use of both
\ stacks and should therefore be used with caution.

: FACTORIAL2 ( +n1 -- +n2 )
   DUP 2 < IF DROP 1 EXIT THEN
   DUP
   BEGIN DUP 2 > WHILE
   1- SWAP OVER * SWAP
   REPEAT DROP
;
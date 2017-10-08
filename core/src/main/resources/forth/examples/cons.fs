\ Cons cells a.k.a. linked lists
\ by Chris Walton
\
\ Cons cells have two items. car is the first, cdr, the second.
\ Use cxr! to store to a cell, cxr@ to read from it.
\ cons returns the address of a newly created cons cell from 2 cells given
\ cons0 as above, but uses 0 instead of given arguments.
\
\ Linked lists can be made by having first item be data, second link to next.
\ Double linked lists will have a link to a cell in cdr describing
\ both previous and next.
\ This thing is infinitely extendable - use it to your advantage.


: car! ( v a - ) ! ;
: cdr! ( v a - ) cell+ ! ;
: car@ ( a - v ) @ ;
: cdr@ ( a - v ) cell+ @ ;
: cons ( k v - a ) swap 2 cells allocate throw tuck ! tuck ! ;
: cons0 ( - a ) 0 0 cons ;

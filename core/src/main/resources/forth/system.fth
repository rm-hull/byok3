: (   41 parse 2drop ; immediate
( That was the definition for the comment word. )
( Now we can add comments to what we are doing! )
( Note: default decimal numeric input mode. )

: \ ( <line> -- , comment out the rest of line)
   13 parse 2drop ; immediate

\ 1 echo !   \ Uncomment this line to echo Forth code while compiling.

\ *********************************************************************
\ This is another style of comment that is common in Forth.
\ BYOK (portmanteau of BYE & OK) is a BYO kernel which happens to
\ implement a forth machine on i686 bare metal.
\
\ Substantial portions of this file were lifted straight from pForth
\ (http://pforth.googlecode.com/svn/trunk/fth/system.fth)
\ *********************************************************************
: BL 32 ;

: SPACE  ( -- )  bl emit ;
: CR  ( -- , cause output to appear at the beginning of the next line )  10 emit ;

: $MOVE  ( $src $dst ) over c@ 1+ cmove ;

: COUNT  dup 1+ swap c@ ;

\ Miscellaneous support words
: ON   ( addr -- , set true ) -1 swap ! ;
: OFF  ( addr -- , set false ) 0 swap ! ;

: <= ( a b -- f , true if A <= b ) > 0= ;
: >= ( a b -- f , true if A >= b ) < 0= ;
: NOT ( n -- !n , logical negation ) 0= ;
: DNEGATE ( d -- -d , negate by doing 0-d )
        0 swap -
;
: CELL+  ( n -- n+cell )  cell + ;
: CELL-  ( n -- n-cell )  cell - ;
: CELL*  ( n -- n*cell )  cells ;

: CHAR+  ( n -- n+size_of_char ) 1+ ;
: CHARS  ( n -- n*size_of_char, don't do anything ) ; immediate

\ useful stack manipulation words
: -ROT ( a b c -- c a b ) rot rot ;
: 3DUP ( a b c -- a b c a b c ) 2 pick 2 pick 2 pick ;

\ ------------------------------------------------------------------
: ID.  ( nfa -- ) count 31 and type ;

: BINARY    2 base ! ;
: OCTAL     8 base ! ;
: DECIMAL  10 base ! ;
: HEX      16 base ! ;

: MOVE$  ( a1 n a2 -- ) swap cmove ;

: BETWEEN ( n lo hi -- flag , true if between lo & hi )
        >r over r> > >r
        < r> or 0=
;

: [  ( -- , enter interpreter mode )  0 state ! ; immediate
: ]  ( -- , enter compile mode )      1 state ! ;

: ?  ( <name> -- )  @ . ;

: EVEN-UP  ( n -- n | n+1, make even )  dup 1 and + ;
: ALIGNED  ( addr -- a-addr )
    [ ' (lit) , cell 1- , ]  +
    [ ' (lit) , cell 1- invert , ]  and ;

: ALIGN  ( -- , align DP ) dp @ aligned dp ! ;
: ALLOT  ( nbytes -- , allot space in dictionary ) dp +! align ;

: C,  ( c -- ) here c! 1 dp +! ;
\ : W,  ( w -- ) dp @ even-up dup dp ! w! 2 dp +! ;
\ : , ( n -- , lay into dictionary )  align here !  cell allot ;

: SEE ( <name> -- )
    ' dup
    >body swap >size
    disassemble ;

\ Compiler support -------------------------------------------------
: COMPILE,   ( xt -- , compile call to xt ) , ;
: [COMPILE]  ( <name> -- , compile now even if immediate ) ' compile, ; immediate
: (COMPILE)  ( xt -- , postpone compilation of token )
    [compile] literal ( compile a call to literal )
    ( store xt of word to be compiled )

    [ ' compile, ] literal \ compile call to compile,
    compile, ;

: COMPILE  ( <name> --, save xt and compile later ) ' (compile) ; immediate

\ Error codes defined in ANSI Exception word set -------------------
: ERR_ABORT       -1 ;
: ERR_ABORTQ      -2 ;
: ERR_EXECUTING  -14 ;
: ERR_PAIRS      -22 ;

: ABORT ( i*x -- ) err_abort throw ;

\ Conditionals in '83 form -----------------------------------------
: CONDITIONAL_KEY ( -- , lazy constant ) 29521 ;
: ?CONDITION  ( f -- )  conditional_key - err_pairs ?error ;
: >MARK       ( -- addr )   here 0 , ;
: >RESOLVE    ( addr -- )   here over - swap ! ;
: <MARK       ( -- addr )   here ;
: <RESOLVE    ( addr -- )   here - , ;

: ?COMP  ( -- , error if not compiling ) state @ 0= err_executing ?error ;
: ?PAIRS ( n m -- ) - err_pairs ?error ;

\ Conditional primitives -------------------------------------------
: IF     ( -- f orig )  ?comp compile 0branch conditional_key >mark ; immediate
: THEN   ( f orig -- )  swap ?condition >resolve ; immediate
: BEGIN  ( -- f dest )  ?comp conditional_key <mark ; immediate
: AGAIN  ( f dest -- )  compile branch swap ?condition <resolve ; immediate
: UNTIL  ( f dest -- )  compile 0branch swap ?condition <resolve ; immediate
: AHEAD  ( -- f orig )  compile branch conditional_key >mark ; immediate

\ Conditionals built from primitives -------------------------------
: ELSE   ( f orig1 -- f orig2 )  [compile] ahead 2swap [compile] then ; immediate
: WHILE  ( f dest -- f origi f dest ) [compile] if 2swap ; immediate
: REPEAT  ( -- f orig f dest ) [compile] again [compile] then ; immediate

: [']  ( <name> -- xt , define compile time tick )
    ?comp ' [compile] literal
; immediate

: (DOES>)  ( xt -- , modify previous definition to execute code at xt )
        latest >body   \ get address of code for new word
        3 cell* +      \ offset to EXIT cell in create word
        !              \ store execution token of DOES> code in new word
;

: DOES>   ( -- , define execution code for CREATE word )
        0 [compile] literal \ dummy literal to hold xt
        here cell-          \ address of zero in literal
        compile (does>)     \ call (DOES>) from new creation word
        >r                  \ move addrz to return stack so ; doesn't see stack garbage
        [compile] ;         \ terminate part of code before does>
        r>
        :noname       ( addrz xt )
        compile rdrop       \ drop a stack frame (call becomes goto)
        swap !              \ save execution token in literal
; immediate


: 2! ( x1 x2 addr -- , store x2 followed by x1 )
        swap over ! cell+ ! ;
: 2@ ( addr -- x1 x2 )
        dup cell+ @ swap @ ;

: 2* ( n -- n*2 )
        2 *
;
: 2/ ( n -- n/2 )
        2 /
;


\ define some useful constants ------------------------------
1 0= constant FALSE
0 0= constant TRUE
32 constant BL

\ Stack data structure ----------------------------------------
\ This is a general purpose stack utility used to implement necessary
\ stacks for the compiler or the user.  Not real fast.
\ These stacks grow up which is different then normal.
\   cell 0 - stack pointer, offset from pfa of word
\   cell 1 - limit for range checking
\   cell 2 - first data location

: :STACK   ( #cells -- )
        CREATE  2 cells ,          ( offset of first data location )
                dup ,              ( limit for range checking, not currently used )
                cells cell+ allot  ( allot an extra cell for safety )
;

: >STACK  ( n stack -- , push onto stack, postincrement )
        dup @ 2dup cell+ swap ! ( -- n stack offset )
        + !
;

: STACK>  ( stack -- n , pop , predecrement )
        dup @ cell- 2dup swap !
        + @
;

: STACK@ ( stack -- n , copy )
        dup @ cell- + @
;

: STACK.PICK ( index stack -- n , grab Nth from top of stack )
        dup @ cell- +
        swap cells -   \ offset for index
        @
;
: STACKP ( stack -- ptr , to next empty location on stack )
    dup @ +
;

: 0STACKP  ( stack -- , clear stack)
    8 swap !
;

32 :stack ustack
ustack 0stackp

\ Define JForth like words.
: >US ustack >stack ;
: US> ustack stack> ;
: US@ ustack stack@ ;
: 0USP ustack 0stackp ;



\ DO LOOP ------------------------------------------------

3 constant do_flag
4 constant leave_flag
5 constant ?do_flag

: DO    ( -- , loop-back do_flag jump-from ?do_flag )
        ?comp
        compile  (do)
        here >us do_flag  >us  ( for backward branch )
; immediate

: ?DO    ( -- , loop-back do_flag jump-from ?do_flag  , on user stack )
        ?comp
        ( leave address to set for forward branch )
        compile  (?do)
        here 0 ,
        here >us do_flag  >us  ( for backward branch )
        >us ( for forward branch ) ?do_flag >us
; immediate

: LEAVE  ( -- addr leave_flag )
        compile (leave)
        here 0 , >us
        leave_flag >us
; immediate

: LOOP-FORWARD  ( -us- jump-from ?do_flag -- )
        BEGIN
                us@ leave_flag =
                us@ ?do_flag =
                OR
        WHILE
                us> leave_flag =
                IF
                        us> here over - cell+ swap !
                ELSE
                        us> dup
                        here swap -
                        cell+ swap !
                THEN
        REPEAT
;

: LOOP-BACK  (  loop-addr do_flag -us- )
        us> do_flag ?pairs
        us> here -  here
        !
        cell allot
;

: LOOP    ( -- , loop-back do_flag jump-from ?do_flag )
   compile  (loop)
   loop-forward loop-back
; immediate

\ : DOTEST 5 0 do 333 . loop 888 . ;
\ : ?DOTEST0 0 0 ?do 333 . loop 888 . ;
\ : ?DOTEST1 5 0 ?do 333 . loop 888 . ;

: +LOOP    ( -- , loop-back do_flag jump-from ?do_flag )
   compile  (+loop)
   loop-forward loop-back
; immediate

: UNLOOP ( loop-sys -r- )
        r> \ save return pointer
        rdrop rdrop
        >r
;


: RECURSE  ( ? -- ? , call the word currently being defined )
    latest compile,
; immediate

: SPACE  bl emit ;
: 0SP depth 0 ?do drop loop ;

\ : >NEWLINE ( -- , CR if needed )
\         out @ 0>
\         IF cr
\         THEN
\ ;


: 2! ( x1 x2 addr -- , store x2 followed by x1 ) swap over ! cell+ ! ;
: 2@ ( addr -- x1 x2 ) dup cell+ @ swap @ ;

: DABS ( d -- |d| )
        dup 0<
        IF dnegate
        THEN
;

: S>D  ( s -- d , extend signed single precision to double )
        dup 0<
        IF -1
        ELSE 0
        THEN
;

: D>S ( d -- s ) drop ;

: PARSE-WORD ( "<spaces>name<space>" -- c-addr u ) bl parse ;
: /STRING  ( addr len n -- addr' len' ) over min rot over  + -rot - ;
: PLACE    ( addr len to -- , move string ) 3dup 1+ swap cmove c! drop ;

: ASCII ( <char> -- char, state smart )
    bl parse drop c@
    state @ 1 =
    IF [compile] literal
    ELSE
    THEN
; immediate

: CHAR    ( <char> -- char , interpret mode ) bl parse drop c@ ;
: [CHAR]  ( <char> -- char , for compile mode )
    char [compile] literal
; immediate

: $TYPE  ( $string -- ) count type ;
: 'word  ( -- addr )  here ;

: EVEN    ( addr -- addr' )   dup 1 and +  ;

: (C")  ( -- $addr ) r> dup count + aligned >r ;
: (S")  ( -- c-addr cnt ) r> count 2dup + aligned >r ;
: (.")  ( -- , type following string ) r> count 2dup + aligned >r type ;
: ",    ( addr len -- , place string into dict ) tuck 'word place 1+ allot align ;
: ,"    ( -- ) [char] " parse ", ;

: .(  ( <string> --, type string delimited by parens )
    [char] ) parse type
; immediate

: ."  ( <string> -- , type string )
    state @ 1 =
    IF compile (.")  ,"
    ELSE [char] " parse type
    THEN
; immediate

: .'   ( <string> -- , type string delimited by single quote )
        state @
        IF    compile (.")  [char] ' parse ",
        ELSE [char] ' parse type
        THEN
; immediate

: C"    ( <string> -- addr , return string address, ANSI )
        state @
        IF compile (c")   ,"
        ELSE [char] " parse pad place pad
        THEN
; immediate

: S"    ( <string> -- , -- addr , return string address, ANSI )
        state @
        IF compile (s")   ,"
        ELSE [char] " parse pad place pad count
        THEN
; immediate

: "    ( <string> -- , -- addr , return string address )
        [compile] C"
; immediate
: P"    ( <string> -- , -- addr , return string address )
        [compile] C"
; immediate

: ""  ( <string> -- addr )
       state @
       IF
               compile (C")
               bl parse-word  ",
       ELSE
               bl parse-word pad place pad
       THEN
; immediate

: SLITERAL ( addr cnt -- , compile string )
    compile (S")
    ",
; IMMEDIATE

: $APPEND ( addr count $1 -- , append text to $1 )
    over >r
        dup >r
    count +  ( -- a2 c2 end1 )
    swap cmove
    r> dup c@  ( a1 c1 )
    r> + ( -- a1 totalcount )
    swap c!
;

\ ANSI word to replace [COMPILE] and COMPILE ----------------
: POSTPONE  ( <name> -- )
	bl word find
	dup
    0= -13 ?ERROR
    0>
    IF compile,  \ immediate
    ELSE (compile)  \ normal
    THEN

; immediate

\ -----------------------------------------------------------------
\ Auto Initialization
: AUTO.INIT  ( -- )
\ Kernel finds AUTO.INIT and executes it after loading dictionary.
\    ." Begin AUTO.INIT ------" cr
;
: AUTO.TERM  ( -- )
\ Kernel finds AUTO.TERM and executes it on bye.
\    ." End AUTO.TERM ------" cr
;

: INCLUDE? ( <word> <file> -- , load file if word not defined )
        bl word find
        IF drop bl word drop  ( eat word from source )
        ELSE drop include
        THEN
; immediate

: CLEARSTACK ( i*x -- )
    BEGIN depth
    WHILE drop
    REPEAT ;

variable pictured_output     \ hidden?
variable pictured_output_len \ hidden?

: <# ( -- ) pad pictured_output !  0 pictured_output_len ! ;
: #> ( -- addr n ) drop pictured_output @ pictured_output_len @ ;

: HOLD ( c -- )
    pictured_output @ dup dup 1+ pictured_output_len @ cmove c!
    1 pictured_output_len +!
;

\ ------------------------ INPUT -------------------------------

: SIGN ( n -- ) < 0 IF 45 hold THEN
;

: DIGIT ( n -- ascii )
   dup 10 < IF 48 ELSE 87 THEN + ;

: # ( n -- n )
    base @ /mod swap digit hold ;

: #S ( n -- )
    BEGIN base @ /mod dup
    WHILE swap digit hold
    REPEAT
    digit hold ;


: LWORD  ( char -- addr )
        parse-word here place here \ 00002 , use PARSE-WORD
;

: (WARNING")  ( flag $message -- )
    swap
    IF count type
    ELSE drop
    THEN
;

: WARNING" ( flag <message> -- , print warning if true. )
    [compile] "  ( compile message )
    state @
    IF  compile (warning")
    ELSE (warning")
    THEN
; IMMEDIATE

: ABORT" ( flag <message> -- , print warning if true. )
    [compile] "  ( compile message )
    state @
    IF  compile (abort")
    ELSE (abort")
    THEN
; IMMEDIATE

: DEFER ( "name" -- )
   CREATE ['] ABORT ,
   DOES> ( ... -- ... ) @ EXECUTE ;

: DEFER! ( xt2 xt1 -- )
   >BODY 4 CELL* + ! ;

: DEFER@ ( xt1 -- xt2 )
   >BODY 4 CELL* + @ ;

: IS ( xt "<spaces>name" -- , Skip leading spaces and parse name delimited by a space. Set name to execute xt. )
   STATE @ IF
     POSTPONE ['] POSTPONE DEFER!
   ELSE
     ' DEFER!
   THEN ; IMMEDIATE


\ : $ ( <number> -- N , convert next number as hex )
\     base @ hex
\     32 lword number? num_type_single = not
\     abort" Not a single number!"
\     swap base !
\     state @
\     IF [compile] literal
\     THEN
\ ; immediate
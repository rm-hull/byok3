: N>R \ xn .. x1 N -- ; R: -- x1 .. xn n 
\ Transfer N items and count to the return stack. 
   DUP                        \ xn .. x1 N N -- 
   BEGIN 
      DUP 
   WHILE 
      ROT R> SWAP >R >R      \ xn .. N N -- ; R: .. x1 -- 
      1-                      \ xn .. N 'N -- ; R: .. x1 -- 
   REPEAT 
   DROP                       \ N -- ; R: x1 .. xn -- 
   R> SWAP >R >R 
; 


: NR> \ -- xn .. x1 N ; R: x1 .. xn N -- 
\ Pull N items and count off the return stack. 
   R> R> SWAP >R DUP 
   BEGIN 
      DUP 
   WHILE 
      R> R> SWAP >R -ROT 
      1- 
   REPEAT 
   DROP 
; 


\ : SYNONYM \ "newname" "oldname" --
\ \ Create a new definition which redirects to an existing one.
\    CREATE IMMEDIATE
\     HIDE ' , REVEAL
\    DOES>
\      @ STATE @ 0= OVER IMMEDIATE? OR
\      IF EXECUTE ELSE COMPILE, THEN
\ ;


: [DEFINED] BL WORD FIND NIP 0<> ; IMMEDIATE

: [UNDEFINED] BL WORD FIND NIP 0= ; IMMEDIATE

: [ELSE] ( -- ) 
    1 BEGIN                                            \ level 
       BEGIN BL WORD COUNT DUP WHILE                    \ level adr len 
         2DUP S" [IF]" COMPARE 0= IF                    \ level adr len 
             2DROP 1+                                   \ level' 
          ELSE                                          \ level adr len 
            2DUP S" [ELSE]" COMPARE 0= IF               \ level adr len 
                2DROP 1- DUP IF 1+ THEN                 \ level' 
            ELSE                                        \ level adr len 
                S" [THEN]" COMPARE 0= IF                \ level 
                   1-                                   \ level' 
               THEN 
             THEN 
          THEN ?DUP 0= IF EXIT THEN                     \ level' 
       REPEAT 2DROP                                     \ level 
   REFILL 0= UNTIL                                     \ level 
    DROP 
; IMMEDIATE 


: [IF] ( flag -- ) 
   0= IF POSTPONE [ELSE] THEN 
; IMMEDIATE

: [THEN] ( -- ) ; IMMEDIATE



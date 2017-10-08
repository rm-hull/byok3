\ Adapted from: https://github.com/tgvaughan/forth.jl/blob/master/examples/mandelbrot.4th

( --- Complex arithmetic --- )

( Location of floating point. )
10000 constant precision

: sign dup abs / ;

: >scaled
    precision 10 / * over
    ?dup 0<> if 
        sign *
    then
    swap precision * +
;

( Redefine multiplication.  Yay forth! )
: * precision */ ;

: c* ( x1 y1 x2 y2 -- x3 y3 )
    swap -rot               ( x1 x2 y1 y2 )
    2dup * negate           ( x1 x2 y1 y2 -y1y2 )
    4 pick 4 pick * +       ( x1 x2 y1 y2 x1x2-y1y2)
    4 roll 2 roll *         ( x2 y1 x1x2-y1y2 x1y2 )
    3 roll 3 roll * +       ( x1x2-y1y2 x1y2+x2y1 )
;

: c+ ( x1 y1 x2 y2 -- x3 y3 )
    rot +
    -rot +
    swap
;

: csq 2dup c* ;


: conj ( x y -- x -y )
    negate
;

: cmagsq ( x1 y1 -- mag )
    2dup conj c* +
;

( --- Mandelbrot set calculations  --- )

: iterate ( cr ci zr zi -- cr ci z'r z'i )
    2over 2swap csq c+
;

20 constant maxiter

: inSet? ( cr ci -- res )
    0 0     ( z_0 = 0 )
    
    true    ( flag indicating set membership )
    maxiter 0 do
        drop

        iterate
        2dup cmagsq
        4 0 >scaled > if
            false ( not in set )
            leave
        then

        true ( maybe in set )
    loop

    ( Clear z and c, leaving set membership flag ) 
    -rot 2drop -rot 2drop
;

100 constant xsteps
30 constant ysteps

( Draw the Mandelbrot Set!)
: mandeldraw ( x1 y1 x2 y2 -- )

    cr

    0 pick 3 pick - ysteps /
    1 pick 4 pick do

        2 pick 5 pick - xsteps /
        3 pick 6 pick do

            i j inSet? if
                42 emit
            else
                space
            then

        dup +loop
        drop

        cr

    dup +loop
    drop
;

( Clean up - hide non-standard multiplication def. )
( hide * )

( Default picture )
: mandel
        -2 0 >scaled -1 0 >scaled 0 5 >scaled 1 0 >scaled
        mandeldraw
;

CR .( Enter 'mandel' to draw the Mandelbrot Set.) CR
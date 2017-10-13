\ ------------------------------------------------------------------------
TESTING DO LOOP +LOOP I J UNLOOP LEAVE EXIT

{ : GD1 DO I LOOP ; -> }
{ 4 1 GD1 -> 1 2 3 }
{ 2 -1 GD1 -> -1 0 1 }
{ MID-UINT+1 MID-UINT GD1 -> MID-UINT }

{ : GD2 DO I -1 +LOOP ; -> }
{ 1 4 GD2 -> 4 3 2 1 }
{ -1 2 GD2 -> 2 1 0 -1 }
{ MID-UINT MID-UINT+1 GD2 -> MID-UINT+1 MID-UINT }

{ : GD3 DO 1 0 DO J LOOP LOOP ; -> }
{ 4 1 GD3 -> 1 2 3 }
{ 2 -1 GD3 -> -1 0 1 }
{ MID-UINT+1 MID-UINT GD3 -> MID-UINT }

{ : GD4 DO 1 0 DO J LOOP -1 +LOOP ; -> }
{ 1 4 GD4 -> 4 3 2 1 }
{ -1 2 GD4 -> 2 1 0 -1 }
{ MID-UINT MID-UINT+1 GD4 -> MID-UINT+1 MID-UINT }

{ : GD5 123 SWAP 0 DO I 4 > IF DROP 234 LEAVE THEN LOOP ; -> }
{ 1 GD5 -> 123 }
{ 5 GD5 -> 123 }
{ 6 GD5 -> 234 }

{ : GD6  ( PAT: {0 0},{0 0}{1 0}{1 1},{0 0}{1 0}{1 1}{2 0}{2 1}{2 2} )
   0 SWAP 0 DO
      I 1+ 0 DO I J + 3 = IF I UNLOOP I UNLOOP EXIT THEN 1+ LOOP
    LOOP ; -> }
{ 1 GD6 -> 1 }
{ 2 GD6 -> 3 }
{ 3 GD6 -> 4 1 2 }

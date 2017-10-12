: ARRAY ( #rows #cols -- )
   CREATE DUP , * ALLOT
   DOES> ( member: row col -- addr )
        ROT OVER @ * + +  CELL+ ;

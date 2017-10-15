: s13 S" aaaaa a" ;	          \ Six spaces
T{ PAD 25 CHAR a FILL -> }T	       \ Fill PAD with 25 'a's
T{ PAD 5 CHARS + 6 BLANK -> }T	   \ Put 6 spaced from character 5
T{ PAD 12 s13 COMPARE -> 0 }T	      \ PAD Should now be same as s13
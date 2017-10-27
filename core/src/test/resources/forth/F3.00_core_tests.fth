\ The test cases in John Hayes' original test suite were designed to test
\ features before they were used in later tests. Due to the structure of
\ this annex the progressive testing has been lost. This section attempts
\ to retain the integrity of the original test suite by laying out the
\ test progression for the core word set.
\
\ While this suite does test many aspects of the core word set, it is not
\ comprehensive. A standard system should pass all of the tests within this
\ suite. A system cannot claim to be standard simply because it passes this
\ test suite.
\
\ The test starts by verifying basic assumptions about number representation.
\ It then builds on this with tests of boolean logic, shifting, and comparisons.
\ It then tests the basic stack manipulations and arithmetic. Ultimately, it
\ tests the Forth interpreter and compiler.
\
\ Note that all of the tests in this suite assume the current base is hexadecimal.
\ ------------------------------------------------------------------------

require forth/tester.fth

VERBOSE OFF
HEX

TESTING CORE WORDS

0 INVERT                 CONSTANT MAX-UINT
0 INVERT 1 RSHIFT        CONSTANT MAX-INT
0 INVERT 1 RSHIFT INVERT CONSTANT MIN-INT
0 INVERT 1 RSHIFT        CONSTANT MID-UINT
0 INVERT 1 RSHIFT INVERT CONSTANT MID-UINT+1

0        CONSTANT 0S
0 INVERT CONSTANT 1S

0S CONSTANT <FALSE>
1S CONSTANT <TRUE>

( WE TRUST 1S, INVERT, AND BITSSET?; WE WILL CONFIRM RSHIFT LATER )
1S 1 RSHIFT INVERT CONSTANT MSB
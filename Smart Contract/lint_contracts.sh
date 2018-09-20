#!/bin/sh
# prints results from 2 linters on the command line.

solium -d contracts

#solhint "contracts/**/*.sol"  <-- solhint tool has currently some strange suggestions, that complains on truffle contracts as well.

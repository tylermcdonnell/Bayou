README - P3

Mike Feilbach (mfeilbach@utexas.edu), mjf2628, feilbach
Tyler McDonnell (tyler@cs.utexas.edu), tsm563, tyler
University of Texas at Austin
CS 380D -- Distributed Computing I
Professor Lorenzo Alvisi
Fall 2015

MIKE:
Slip days used (this project): 4
Slip days used (total)       : Homework: 6, Projects: 6

TYLER:
Slip days used (this project): 4
Slip days used (total):        Homework: 2, Projects: 6

--------------------------------------------------------------------------------
- To Run:
--------------------------------------------------------------------------------

To run, execute:

javac Master.java
java Master

Alternatively, run Master.java in a Java IDE such as Eclipse.

Once running Master.java, you can make API calls by using the defined format 
and pressing enter (new line) between each command. Make special note of the
script API command, which will read in entire files of pre-defined commands.

You can see examples of test scripts (including the ones provided to us) in
the tests/ directory.

NOTE: the script command (s):
      (1) assumes all tests are in the tests/ directory
      (2) automaticallyl appends ".test" on the name of the test you give it
      (3) is "s."
      
      Example: once the Master class is running, simply enter:
      "s 1_1" to run test 1_1.test within the tests/ directory.

--------------------------------------------------------------------------------
- To Clean:
--------------------------------------------------------------------------------

To clean-up, simply run a "clean" command.  In all tests we have provided,
a clean command is included at the very end of each test.  Thus, running all
tests would look like:

s 1_1
s 1_2
s 2_2
...
s RYW
S WFR

--------------------------------------------------------------------------------
- Notes:
--------------------------------------------------------------------------------

This implementation of multi-decree Paxos is strongly based on Robbert van 
Renesse's paper, "Paxos Made Moderately Complex." 

Project assumptions:

1. TODO


Here is a list of implementation-specific design decisions:

1. Each client has its own read-set and write-set, which is used to provide
   all four session guarantees: "Read Your Writes," "Writes Follow Reads,"
   "Monotonic Reads," and "Monotonic Writes."
   
2. The server a client x will issue its requets to is the server 
   specified in the most recent joinClient x [serverId] command.
  
--------------------------------------------------------------------------------
- Interface Provided: 
--------------------------------------------------------------------------------

The exact interface in the Bayou specification doc, with the following
additional commands:

s [name of test in tests/ directory, without ".test" in file name]
  See the "To Run" section for precise instructions.
  
printAll
  Prints the Logs for each server.
  
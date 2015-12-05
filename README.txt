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
      (2) automatically appends ".test" on the name of the test you give it
      (3) is "s"
      
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

Alternatively, you can simply call clean between tests. For example:

s test1
clean
s test2
clean 
...

On occasion, the OS does not seem to want to re-open ports in a timely manner, 
and it is possible that clean will not allow a port to be re-opened for some
additional amount of time. An additional wait or call to clean should fix things
in this rare case.

--------------------------------------------------------------------------------
- Notes:
--------------------------------------------------------------------------------

This implementation of multi-decree Paxos is strongly based on Robbert van 
Renesse's paper, "Paxos Made Moderately Complex." 

Project assumptions:

1. Processes will not fail (crash). Communication links between processes may
   crash, but the processes themselves will not be faulty. 

Here is a list of implementation-specific design decisions:

1. Session Guarantees - This design implements the "Read Your Writes", "Writes
   Follow Reads", "Monotonic Reads", and "Monotonic Writes" session guarantees.
   To enforce these, each client maintains its own read-set and write-set, 
   which it passes with each read or write request. The server then compares
   these provided vectors with its own to determine whether it can meet the 
   read guarantees, if the client is requesting a read, or the write guarantees,
   if the client is requesting a write.
   
2. A client uses requests to the server specified in the most recent joinClient
   command. See API below for more details.

3. In implementing the total ordering of Write Log messages, we chose to use
   a simple lexicographical ordering for the ordering of Server IDs, where the
   Server IDs are recursively named as in "Flexible Update Propagation for 
   Weakly Consistent Replication." The name of a Server is "<joinedThrough, stamp>"
   where joinedThrough is the ID of the server through which a server joined and
   stamp is the accept stamp of the Join write on that server. Interestingly, 
   this means that newer servers will actually be considered "greater than" old
   servers, lexicographically.
   
4. Our stabilize command is entirely time dependent. Though we've chosen what
   we believe is a very reasonable and effective stabilize command, it's possible
   that, depending upon network topology, the probabilistic guarantees of stabilize
   might vary.
   
--------------------------------------------------------------------------------
- Interface Provided: 
--------------------------------------------------------------------------------

The exact interface in the Bayou specification doc, with the following 
modifications:

joinClient [clientId] [serverId] This command will start a client and connect it
								 to the specified server. To be "connected" to a
								 server means that all client requests will be
								 processed by the server. If the client ID already
								 exists, then this command simply connects the client
								 to the specified server. We believe this is how the
								 command was intended to work.

s [test]						 Runs a test. Test is the name of a test that is in
								 the bayou_handout/tests/ directory WITHOUT the ".test"
								 extension. For example, to run sample test "1_1.test",
								 you would enter "s 1_1".
  See the "To Run" section for precise instructions.
  
clean
  See the "To Clean" section.
  
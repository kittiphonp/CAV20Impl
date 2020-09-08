   +--------------------------------------------------------------+
   |               ===  Evaluation Quickstart  ===                |
   |                                                              |
   | To reproduce the results of the paper, do the following:     |
   |  * Open a terminal and "cd ~/Desktop/scripts".               |
   |  * Run "./generate_mytable1.sh"                              |
   |                                                              |
   | mytable1.txt then is generated, which takes about 40 minutes.|
   |                                                              |
   | mytable1.txt is a reproduction of table 1 in our paper       |
   | (~/Desktop/paper.pdf).                                       |
   |                                                              |
   | Username and password are "user" (e.g., to "sudo")           |
   +--------------------------------------------------------------+

Scroll down for further relevant notes.














= Contents =
This VM contains all tools and their sources relevant in the CAV'20 paper

  "Widest Paths and Global Propagation in Bounded Value Iteration for Stochastic Games",

namely:

1) The original implementation of the BVI algorithm in [Kelmendi+, CAV'18] (mycode/DFL)

2) A modification of 1) (mycode/DFL_m)

3) An implementation of our BVI algorithms as an extension of 1) (mycode/WP)


Implementation 1) is provided by the authors of [Kelmendi+, CAV'18], which is made by 
modifying the source code of PRISM-GAMES (https://www.prismmodelchecker.org/games/).

All of these tools are installed and ready to use for any further experiments you want to conduct.



= Files =
In ~/Desktop the following are provided:

README.txt   ... this file
mymodels     ... the benchmark set called by the main code
mycode       ... main code that implements the BVI algorithms (DFL, DFL_m, DFL-BRTDP, and WP)
scripts      ... scripts that conduct experiments
paper.pdf    ... our accepted paper


When you run the script (generate_mytable1.sh), the following files will be created:

mytable1.txt ... a reproduction of table1 in paper.pdf
mydata       ... log file



= The code =
The source code of each implementation can be found in ~/Desktop/mycode/$ALGO_NAME/src
Our BVI technique via Widest Path Problem appears in ~/Desktop/mycode/WP/src/explicit/STPGModelChecker.java
See the method 'computeReachProbsValIterBounded' (Line 2107-2419).



= The scripts =
In the folder ~/Desktop/scripts there are three shell-scripts.


1) run_benchmarks.sh     ... it runs the tests on the models and generates log files. 
                             The following should be noted:

-    For the sake of fast computation, the time limit for each BVI computation is set to 1 minute 
     (Line 168). This is much smaller than the number in our paper (6 hours). For more accurate 
     reproduction of table 1, change this number and take a longer time limit.
-    In Line 36, the number of repetitions for BRTDP is defined (it is a randomized algorithm). 
     We set the number to 2 for the sake of fast computation, while we did 5 repetitions 
     in our paper.

2) read_logs.sh         ... it reads the generated log files and reproduces table1 in our paper. 

3) generate_mytable1.sh ... it calls the other 2 scripts.



= The log files =
The log directory (~/Desktop/mydata) is created when you execute the script (generate_mytable1.sh).
For each model and algorithm, the following log files are created:

1) .log file  ... the log produced by the main code

2) .stat file ... the log taken by the OS (the "/usr/bin/time" function)



= Usage =
One can conduct tests for specific models and algorithms as follows.
Also you can find some options by executing ~/Desktop/mycode/WP/bin/prism -help

== DFL ==

To use the implementation of BVI by [Kelmendi+, CAV'18], first
cd ~/Desktop/mycode/DFL
Then execute bin/prism ../../mymodels/$CHOOSEMODELFILE ../../mymodels/$CHOOSEPROPERTYFILE -BVI_A
e.g. bin/prism ../../mymodels/mdsm/mdsm.prism ../../mymodels/mdsm/mdsm.props -BVI_A

== DFL_m ==

To use the modification of DFL, first
cd ~/Desktop/mycode/DFL_m
Then execute bin/prism ../../mymodels/$CHOOSEMODELFILE ../../mymodels/$CHOOSEPROPERTYFILE -BVI_A
e.g. bin/prism ../../mymodels/mdsm/mdsm.prism ../../mymodels/mdsm/mdsm.props -BVI_A

== DFL_BRTDP == 

To use the implementation of BRTDP by [Kelmendi+, CAV'18], first
cd ~/Desktop/mycode/DFL
Then execute bin/prism ../../mymodels/$CHOOSEMODELFILE ../../mymodels/$CHOOSEPROPERTYFILE -heuristics RTDP_ADJ
e.g. bin/prism ../../mymodels/mdsm/mdsm.prism ../../mymodels/mdsm/mdsm.props -heuristics RTDP_ADJ

== WP == 

To use the implementation of our WP algorithm, first 
cd ~/Desktop/mycode/WP/
Then execute bin/prism ../../mymodels/$CHOOSEMODELFILE ../../mymodels/$CHOOSEPROPERTYFILE -BVI_A
e.g. bin/prism ../../mymodels/mdsm/mdsm.prism ../../mymodels/mdsm/mdsm.props -BVI_A



== Comments on a reproduced data ==
Apart from the difference due to a short timeout (cf. note on "run_benchmarks.sh"), 
we are aware of the following differences between table1 in our paper (table1)
and the table reproduced by this VM (mytable1.txt). 


1) In mytable1.txt, we will not see the number of ECs for the following models:

mdsm,N=4; cloud,N=7; teamform,N=5; investor,N=100.

   For all of these cases, the EC computation fails because of an Out Of Memory error.
   The corresponding numbers in table1 were computed by a different algorithm we made, 
   which turned out to be incorrect when we prepared for the artifact evaluation. 
   We are still yet to fix this issue, but the number of ECs is supplementary information 
   of models and we believe the data still demonstrates the advantage of our new algorithm. 
   In particular, notice that our algorithm does not use EC computations at all.


2) For the following model, the number of ECs in table1 and mytable1.txt will be different:

investor, N=50.

   This is due to incorrectness of the aforementioned algorithm we made; the number in mytable1.txt 
   (29,690) is the correct one. On this point, our paper will be fixed in the final version.


3) DFL beats DFL_m in mytable1.txt when the model is manyECs, while their performances are 
   identical in table1. We are still not aware of the reason for this difference, but in 
   either case our claim does not change, namely our algorithm WP outperforms both DFL and DFL_m.








== References ==
[Kelmendi+, CAV'18] Kelmendi, E., Kramer, J., Kretinsky, J., Weininger, M.: Value Iteration for Simple Stochastic Games: Stopping Criterion and Learning Algorithm, Proc. CAV 2018, pp. 623-642. Springer (2018)


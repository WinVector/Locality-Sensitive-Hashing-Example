
Simple exaple of Locality Senstive Hashing
see: 
     http://www.win-vector.com/blog/2011/11/an-appreciation-of-locality-sensitive-hashing/
     http://www.win-vector.com/dfiles/LocalitySensitiveHashing.pdf

Copyright 2011 Win-Vector LLC
Distributed under GPLv3 license (see http://www.gnu.org/copyleft/gpl.html).

To run example:
   java com.winvector.lsh.BuildNbhds

example output:


working with 10000 0/1 vectors of dimension 100
building LSH solution	Fri Dec 23 12:12:33 PST 2011
	working on pop 1	Fri Dec 23 12:12:33 PST 2011
		inspected fraction: 0.0
	working on pop 2	Fri Dec 23 12:12:33 PST 2011
		inspected fraction: 0.0
	working on pop 3	Fri Dec 23 12:12:33 PST 2011
		inspected fraction: 0.0083
	working on pop 4	Fri Dec 23 12:12:33 PST 2011
		inspected fraction: 0.0284
	working on pop 5	Fri Dec 23 12:12:33 PST 2011
		inspected fraction: 0.0606
	working on pop 6	Fri Dec 23 12:12:34 PST 2011
		inspected fraction: 0.1039
	working on pop 7	Fri Dec 23 12:12:34 PST 2011
		inspected fraction: 0.1562
	working on pop 8	Fri Dec 23 12:12:34 PST 2011
		inspected fraction: 0.3135
	working on pop 9	Fri Dec 23 12:12:34 PST 2011
		inspected fraction: 0.5195
	working on pop 10	Fri Dec 23 12:12:34 PST 2011
		inspected fraction: 0.6508
	working on pop 11	Fri Dec 23 12:12:35 PST 2011
		inspected fraction: 0.7068
	working on pop 12	Fri Dec 23 12:12:35 PST 2011
		inspected fraction: 0.7451
	working on pop 13	Fri Dec 23 12:12:35 PST 2011
		inspected fraction: 0.7939
	working on pop 14	Fri Dec 23 12:12:35 PST 2011
		inspected fraction: 0.8514
	working on pop 15	Fri Dec 23 12:12:35 PST 2011
		inspected fraction: 0.8731
	working on pop 16	Fri Dec 23 12:12:35 PST 2011
		inspected fraction: 0.8405
	working on pop 17	Fri Dec 23 12:12:36 PST 2011
		inspected fraction: 0.8028
	working on pop 18	Fri Dec 23 12:12:36 PST 2011
		inspected fraction: 0.7594
	working on pop 19	Fri Dec 23 12:12:36 PST 2011
		inspected fraction: 0.7289
	working on pop 20	Fri Dec 23 12:12:36 PST 2011
		inspected fraction: 0.6848
done LHS	3246.0MS	Fri Dec 23 12:12:36 PST 2011
building brue force solution	Fri Dec 23 12:12:36 PST 2011
done brute force	84172.0MS	Fri Dec 23 12:14:00 PST 2011
speedup: 25.930991990141713
mean difference in cosine similarity to nearest neighbor: 0.005541643648993844
   


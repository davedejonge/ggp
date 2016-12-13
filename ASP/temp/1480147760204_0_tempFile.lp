node(1..6).
edge(1, (2;3;4)). edge(2, (4;5;6)). edge(3, (1;4;5)). edge(4, (1;2)). edge(5, (3;4;6)). edge(6, (2;3;5)). 
cost(1,2,2). cost(1,3,3). cost(1,4,1). cost(2,4,2). cost(2,5,2). cost(2,6,4). cost(3,1,3). cost(3,4,2). cost(3,5,2). cost(4,1,1). cost(4,2,2). cost(5,3,2). cost(5,4,2). cost(5,6,1). cost(6,2,4). cost(6,3,3). cost(6,5,1). 
{ cycle(X,Y) : edge(X,Y) } = 1 :- node(X). 
{ cycle(X,Y) : edge(X,Y) } = 1 :- node(Y). 
reached(Y) :- cycle(1,Y). 
reached(Y) :- cycle(X,Y), reached(X). 
:- node(Y), not reached(Y). 
#minimize { C,X,Y : cycle(X,Y), cost(X,Y,C) }. #show cycle/2. 

role(xplayer).
role(oplayer).
index(1).
index(2).
index(3).
base(cell(X,Y,b)) :- index(X), index(Y).
base(cell(X,Y,x)) :- index(X), index(Y).
base(cell(X,Y,o)) :- index(X), index(Y).
base(control(P)) :- role(P).
input(P,mark(X,Y)) :- index(X), index(Y), role(P).
input(P,noop) :- role(P).
init(cell(1,1,b)).
init(cell(1,2,b)).
init(cell(1,3,b)).
init(cell(2,1,b)).
init(cell(2,2,b)).
init(cell(2,3,b)).
init(cell(3,1,b)).
init(cell(3,2,b)).
init(cell(3,3,b)).
init(control(xplayer)).
next(cell(M,N,x)) :- does(xplayer,mark(M,N)), true(cell(M,N,b)).
next(cell(M,N,o)) :- does(oplayer,mark(M,N)), true(cell(M,N,b)).
next(cell(M,N,W)) :- true(cell(M,N,W)), W != b.
next(cell(M,N,b)) :- does(W,mark(J,K)), true(cell(M,N,b)), {M != J; N != K}.
next(control(xplayer)) :- true(control(oplayer)).
next(control(oplayer)) :- true(control(xplayer)).
row(M,X) :- true(cell(M,1,X)), true(cell(M,2,X)), true(cell(M,3,X)).
column(N,X) :- true(cell(1,N,X)), true(cell(2,N,X)), true(cell(3,N,X)).
diagonal(X) :- true(cell(1,1,X)), true(cell(2,2,X)), true(cell(3,3,X)).
diagonal(X) :- true(cell(1,3,X)), true(cell(2,2,X)), true(cell(3,1,X)).
line(X) :- row(M,X).
line(X) :- column(M,X).
line(X) :- diagonal(X).
open :- true(cell(M,N,b)).
legal(W,mark(X,Y)) :- true(cell(X,Y,b)), true(control(W)).
legal(xplayer,noop) :- true(control(oplayer)).
legal(oplayer,noop) :- true(control(xplayer)).
goal(xplayer,100) :- line(x).
goal(xplayer,50) :- not line(x), not line(o), not open.
goal(xplayer,0) :- line(o).
goal(oplayer,100) :- line(o).
goal(oplayer,50) :- not line(x), not line(o), not open.
goal(oplayer,0) :- line(x).
terminal :- line(x).
terminal :- line(o).
terminal :- not open.
true(X) :- next(X).

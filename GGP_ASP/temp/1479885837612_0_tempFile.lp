role(red).
role(black).
legal(red,noop) :- true(control(black)).
legal(red,drop(X)) :- true(control(red)), columnOpen(X).
legal(black,noop) :- true(control(red)).
legal(black,drop(X)) :- true(control(black)), columnOpen(X).
next(cell(X,1,Player)) :- does(Player,drop(X)), columnEmpty(X).
next(cell(X,Y2,Player)) :- does(Player,drop(X)), cellOpen(X,Y2), succ(Y1,Y2), not cellOpen(X,Y1).
next(cell(X,Y,Player)) :- true(cell(X,Y,Player)).
next(control(red)) :- true(control(black)).
next(control(black)) :- true(control(red)).
cellOpen(X,Y) :- x(X), y(Y), not true(cell(X,Y,red)), not true(cell(X,Y,black)).
columnOpen(X) :- cellOpen(X,6).
columnEmpty(X) :- cellOpen(X,1).
boardOpen :- x(X), columnOpen(X).
line(Player) :- true(cell(X1,Y,Player)), succ(X1,X2), succ(X2,X3), succ(X3,X4), true(cell(X2,Y,Player)), true(cell(X3,Y,Player)), true(cell(X4,Y,Player)).
line(Player) :- true(cell(X,Y1,Player)), succ(Y1,Y2), succ(Y2,Y3), succ(Y3,Y4), true(cell(X,Y2,Player)), true(cell(X,Y3,Player)), true(cell(X,Y4,Player)).
line(Player) :- true(cell(X1,Y1,Player)), succ(X1,X2), succ(X2,X3), succ(X3,X4), succ(Y1,Y2), succ(Y2,Y3), succ(Y3,Y4), true(cell(X2,Y2,Player)), true(cell(X3,Y3,Player)), true(cell(X4,Y4,Player)).
line(Player) :- true(cell(X1,Y4,Player)), succ(X1,X2), succ(X2,X3), succ(X3,X4), succ(Y3,Y4), succ(Y2,Y3), succ(Y1,Y2), true(cell(X2,Y3,Player)), true(cell(X3,Y2,Player)), true(cell(X4,Y1,Player)).
succ(1,2).
succ(2,3).
succ(3,4).
succ(4,5).
succ(5,6).
succ(6,7).
succ(7,8).
x(1).
x(2).
x(3).
x(4).
x(5).
x(6).
x(7).
x(8).
y(1).
y(2).
y(3).
y(4).
y(5).
y(6).
isAction(drop(3)).
isAction(drop(2)).
isAction(noop).
isAction(drop(8)).
isAction(drop(4)).
isAction(drop(6)).
isAction(drop(7)).
isAction(drop(1)).
isAction(drop(5)).
1 {does(R,A) : isAction(A)} 1 :- role(R).
:- does(R,A), not legal(R,A).
{ true(P) : tdom(P)}.
tdom(cell(6,1,red)).
tdom(cell(6,4,red)).
tdom(cell(2,4,black)).
tdom(cell(3,5,red)).
tdom(cell(6,1,black)).
tdom(cell(1,2,black)).
tdom(cell(3,4,black)).
tdom(cell(2,4,red)).
tdom(cell(6,2,red)).
tdom(cell(3,2,red)).
tdom(cell(3,1,red)).
tdom(cell(2,3,red)).
tdom(cell(4,1,red)).
tdom(cell(1,6,red)).
tdom(cell(7,4,black)).
tdom(cell(2,2,red)).
tdom(cell(5,6,black)).
tdom(cell(5,3,black)).
tdom(cell(7,6,black)).
tdom(control(red)).
tdom(cell(8,4,black)).
tdom(cell(2,3,black)).
tdom(cell(8,5,red)).
tdom(cell(6,2,black)).
tdom(cell(7,5,red)).
tdom(cell(4,5,red)).
tdom(cell(8,2,black)).
tdom(cell(1,5,black)).
tdom(cell(1,4,black)).
tdom(cell(8,2,red)).
tdom(cell(3,1,black)).
tdom(cell(5,4,black)).
tdom(cell(5,2,black)).
tdom(cell(7,2,red)).
tdom(cell(7,3,black)).
tdom(cell(4,4,black)).
tdom(cell(7,5,black)).
tdom(cell(2,2,black)).
tdom(cell(1,3,black)).
tdom(cell(3,5,black)).
tdom(cell(1,4,red)).
tdom(control(black)).
tdom(cell(5,5,black)).
tdom(cell(8,3,black)).
tdom(cell(1,3,red)).
tdom(cell(5,3,red)).
tdom(cell(5,2,red)).
tdom(cell(1,6,black)).
tdom(cell(7,2,black)).
tdom(cell(6,6,black)).
tdom(cell(7,4,red)).
tdom(cell(2,6,red)).
tdom(cell(4,1,black)).
tdom(cell(6,4,black)).
tdom(cell(2,5,red)).
tdom(cell(8,4,red)).
tdom(cell(1,1,red)).
tdom(cell(7,6,red)).
tdom(cell(4,2,red)).
tdom(cell(4,3,black)).
tdom(cell(2,5,black)).
tdom(cell(4,5,black)).
tdom(cell(6,5,red)).
tdom(cell(4,6,black)).
tdom(cell(8,6,red)).
tdom(cell(1,1,black)).
tdom(cell(4,4,red)).
tdom(cell(4,3,red)).
tdom(cell(4,6,red)).
tdom(cell(5,1,black)).
tdom(cell(3,6,red)).
tdom(cell(3,4,red)).
tdom(cell(1,5,red)).
tdom(cell(3,3,black)).
tdom(cell(8,6,black)).
tdom(cell(6,3,black)).
tdom(cell(3,6,black)).
tdom(cell(8,1,black)).
tdom(cell(2,6,black)).
tdom(cell(4,2,black)).
tdom(cell(2,1,red)).
tdom(cell(8,3,red)).
tdom(cell(5,5,red)).
tdom(cell(7,1,black)).
tdom(cell(5,4,red)).
tdom(cell(1,2,red)).
tdom(cell(7,3,red)).
tdom(cell(5,1,red)).
tdom(cell(7,1,red)).
tdom(cell(8,1,red)).
tdom(cell(6,5,black)).
tdom(cell(5,6,red)).
tdom(cell(6,3,red)).
tdom(cell(3,2,black)).
tdom(cell(8,5,black)).
tdom(cell(2,1,black)).
tdom(cell(6,6,red)).
tdom(cell(3,3,red)).
domY0(black).
domY0(red).
hyp :- 0 { true(control(Y0)) : domY0(Y0) } 1.
:- not hyp.
t :- 0 { next(control(Y0)) : domY0(Y0) } 1.
:- t.
#show true/1.
#show does/2.

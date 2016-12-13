role(robot).
board(1).
board(2).
board(3).
button(p).
button(q).
button(r).
base(step(1)).
base(step(2)).
base(step(3)).
base(step(4)).
base(step(5)).
base(step(6)).
base(step(7)).
base(state(Bu,Bo)) :- button(Bu), board(Bo).
action(a).
action(b).
action(c).
input(robot,set(A1,A2,A3)) :- action(A1), action(A2), action(A3).
init(step(1)).
action(a).
action(b).
action(c).
legal(robot,set(A1,A2,A3)) :- action(A1), action(A2), action(A3).
doesOne(robot,Action,1) :- does(robot,set(Action,A2,A3)).
doesOne(robot,Action,2) :- does(robot,set(A1,Action,A3)).
doesOne(robot,Action,3) :- does(robot,set(A1,A2,Action)).
next(state(p,Board)) :- doesOne(robot,a,Board), not true(state(p,Board)).
next(state(p,Board)) :- doesOne(robot,b,Board), true(state(q,Board)).
next(state(p,Board)) :- doesOne(robot,c,Board), true(state(p,Board)).
next(state(q,Board)) :- doesOne(robot,a,Board), true(state(q,Board)).
next(state(q,Board)) :- doesOne(robot,b,Board), true(state(p,Board)).
next(state(q,Board)) :- doesOne(robot,c,Board), true(state(r,Board)).
next(state(r,Board)) :- doesOne(robot,a,Board), true(state(r,Board)).
next(state(r,Board)) :- doesOne(robot,b,Board), true(state(r,Board)).
next(state(r,Board)) :- doesOne(robot,c,Board), true(state(q,Board)).
next(step(Y)) :- true(step(X)), successor(X,Y).
goal(robot,100) :- success(1), success(2).
goal(robot,0) :- board(Bo), button(Bu), not true(state(Bu,Bo)).
success(Board) :- true(state(p,Board)), true(state(q,Board)), true(state(r,Board)).
terminal :- success(1), success(2).
terminal :- true(step(7)).
successor(1,2).
successor(2,3).
successor(3,4).
successor(4,5).
successor(5,6).
successor(6,7).
true(X) :- next(X).
true(X) :- init(X).
does(X,Y) :- legal(X,Y).

role(yellow).
role(green).
role(blue).
role(magenta).
base(cell(C,R)) :- index(C), role(R), not reserved(C).
base(cell(h1,teal)).
base(cell(h2,teal)).
base(cell(i1,teal)).
base(cell(a1,red)).
base(cell(b1,red)).
base(cell(b2,red)).
base(control(R)) :- role(R).
base(first(R)) :- role(R).
base(step(N)) :- succ(X,N).
base(step(1)).
input(R,move(C1,C2)) :- role(R), car(R,C1,C2).
input(R,move(C1,C2)) :- role(R), cdr(R,C1,C2).
input(R,move(C1,C3)) :- role(R), car(R,C1,C2), car(R,C2,C3).
input(R,move(C1,C3)) :- role(R), cdr(R,C1,C2), cdr(R,C2,C3).
input(R,move(C1,C5)) :- role(R), car(R,C1,C2), car(R,C2,C3), car(R,C3,C4), car(R,C4,C5).
input(R,move(C1,C5)) :- role(R), car(R,C1,C2), car(R,C2,C3), cdr(R,C3,C4), cdr(R,C4,C5).
input(R,move(C1,C5)) :- role(R), cdr(R,C1,C2), cdr(R,C2,C3), car(R,C3,C4), car(R,C4,C5).
input(R,move(C1,C5)) :- role(R), cdr(R,C1,C2), cdr(R,C2,C3), cdr(R,C3,C4), cdr(R,C4,C5).
input(R,noop) :- role(R).
index(a1).
index(b1).
index(b2).
index(c1).
index(c2).
index(c3).
index(c4).
index(c5).
index(c6).
index(c7).
index(d1).
index(d2).
index(d3).
index(d4).
index(d5).
index(d6).
index(e1).
index(e2).
index(e3).
index(e4).
index(e5).
index(f1).
index(f2).
index(f3).
index(f4).
index(f5).
index(f6).
index(g1).
index(g2).
index(g3).
index(g4).
index(g5).
index(g6).
index(g7).
index(h1).
index(h2).
index(i1).
reserved(a1).
reserved(b1).
reserved(b2).
reserved(h1).
reserved(h2).
reserved(i1).
init(cell(a1,red)).
init(cell(b1,red)).
init(cell(b2,red)).
init(cell(c1,magenta)).
init(cell(c2,magenta)).
init(cell(c6,yellow)).
init(cell(c7,yellow)).
init(cell(d1,magenta)).
init(cell(d6,yellow)).
init(cell(f1,blue)).
init(cell(f6,green)).
init(cell(g1,blue)).
init(cell(g2,blue)).
init(cell(g6,green)).
init(cell(g7,green)).
init(cell(h1,teal)).
init(cell(h2,teal)).
init(cell(i1,teal)).
init(control(yellow)).
init(step(1)).
next(cell(E,Player)) :- does(Player,move(B,E)).
next(cell(X,Z)) :- true(cell(X,Z)), not affected(X).
affected(X) :- does(Player,move(X,Z)).
next(control(green)) :- true(control(yellow)).
next(control(blue)) :- true(control(green)).
next(control(magenta)) :- true(control(blue)).
next(control(yellow)) :- true(control(magenta)).
next(first(yellow)) :- stones(yellow,all), not stones(blue,all), not stones(green,all), not stones(magenta,all).
next(first(green)) :- stones(green,all), not stones(blue,all), not stones(yellow,all), not stones(magenta,all).
next(first(blue)) :- stones(blue,all), not stones(yellow,all), not stones(green,all), not stones(magenta,all).
next(first(magenta)) :- stones(magenta,all), not stones(blue,all), not stones(green,all), not stones(yellow,all).
next(first(Player)) :- role(Player), true(first(Player)).
next(step(Y)) :- true(step(X)), succ(X,Y).
succ(1,2).
succ(2,3).
succ(3,4).
succ(4,5).
succ(5,6).
succ(6,7).
succ(7,8).
succ(8,9).
succ(9,10).
succ(10,11).
succ(11,12).
succ(12,13).
succ(13,14).
succ(14,15).
succ(15,16).
succ(16,17).
succ(17,18).
succ(18,19).
succ(19,20).
succ(20,21).
succ(21,22).
succ(22,23).
succ(23,24).
succ(24,25).
succ(25,26).
succ(26,27).
succ(27,28).
succ(28,29).
succ(29,30).
succ(30,31).
succ(31,32).
succ(32,33).
succ(33,34).
succ(34,35).
succ(35,36).
succ(36,37).
succ(37,38).
succ(38,39).
succ(39,40).
succ(40,41).
succ(41,42).
succ(42,43).
succ(43,44).
succ(44,45).
succ(45,46).
succ(46,47).
succ(47,48).
succ(48,49).
succ(49,50).
succ(50,51).
succ(51,52).
succ(52,53).
succ(53,54).
succ(54,55).
succ(55,56).
succ(56,57).
succ(57,58).
succ(58,59).
succ(59,60).
legal(Player,move(B,E)) :- true(control(Player)), movable(Player,B,E).
legal(Player,noop) :- true(control(Player2)), Player != Player2, role(Player).
legal(Player,noop) :- true(control(Player)), not hasAnyLegalMove(Player).
hasAnyLegalMove(Player) :- movable(Player,B,E).
occupied(C) :- true(cell(C,P)).
empty(C) :- index(C), not occupied(C).
movable(Player,B,E) :- true(cell(B,Player)), car(Player,B,C), occupied(C), car(Player,C,X), empty(X), car(Player,X,D), occupied(D), car(Player,D,E), empty(E).
movable(Player,B,E) :- true(cell(B,Player)), car(Player,B,C), occupied(C), car(Player,C,X), empty(X), cdr(Player,X,D), occupied(D), cdr(Player,D,E), empty(E).
movable(Player,B,E) :- true(cell(B,Player)), cdr(Player,B,C), occupied(C), cdr(Player,C,X), empty(X), car(Player,X,D), occupied(D), car(Player,D,E), empty(E).
movable(Player,B,E) :- true(cell(B,Player)), cdr(Player,B,C), occupied(C), cdr(Player,C,X), empty(X), cdr(Player,X,D), occupied(D), cdr(Player,D,E), empty(E).
movable(Player,B,E) :- true(cell(B,Player)), car(Player,B,C), occupied(C), car(Player,C,E), empty(E).
movable(Player,B,E) :- true(cell(B,Player)), cdr(Player,B,C), occupied(C), cdr(Player,C,E), empty(E).
movable(Player,B,E) :- true(cell(B,Player)), car(Player,B,E), empty(E).
movable(Player,B,E) :- true(cell(B,Player)), cdr(Player,B,E), empty(E).
car(yellow,B,E) :- caryellow(B,E).
car(green,B,E) :- carmagenta(E,B).
car(blue,B,E) :- caryellow(E,B).
car(magenta,B,E) :- carmagenta(B,E).
cdr(yellow,B,E) :- cdryellow(B,E).
cdr(green,B,E) :- cdrmagenta(E,B).
cdr(blue,B,E) :- cdryellow(E,B).
cdr(magenta,B,E) :- cdrmagenta(B,E).
carmagenta(c1,d1).
carmagenta(d1,e1).
carmagenta(c2,d2).
carmagenta(e1,f2).
carmagenta(d2,e2).
carmagenta(c3,d3).
carmagenta(f2,g3).
carmagenta(e2,f3).
carmagenta(d3,e3).
carmagenta(c4,d4).
carmagenta(f3,g4).
carmagenta(e3,f4).
carmagenta(d4,e4).
carmagenta(c5,d5).
carmagenta(f4,g5).
carmagenta(e4,f5).
carmagenta(d5,e5).
carmagenta(f5,g6).
carmagenta(e5,f6).
carmagenta(f6,g7).
cdrmagenta(c1,c2).
cdrmagenta(d1,d2).
cdrmagenta(c2,c3).
cdrmagenta(e1,e2).
cdrmagenta(d2,d3).
cdrmagenta(c3,c4).
cdrmagenta(f2,f3).
cdrmagenta(e2,e3).
cdrmagenta(d3,d4).
cdrmagenta(c4,c5).
cdrmagenta(g3,g4).
cdrmagenta(f3,f4).
cdrmagenta(e3,e4).
cdrmagenta(d4,d5).
cdrmagenta(g4,g5).
cdrmagenta(f4,f5).
cdrmagenta(e4,e5).
cdrmagenta(g5,g6).
cdrmagenta(f5,f6).
cdrmagenta(g6,g7).
caryellow(c7,c6).
caryellow(c6,c5).
caryellow(d6,d5).
caryellow(c5,c4).
caryellow(d5,d4).
caryellow(e5,e4).
caryellow(c4,c3).
caryellow(d4,d3).
caryellow(e4,e3).
caryellow(f5,f4).
caryellow(g5,g4).
caryellow(d3,d2).
caryellow(e3,e2).
caryellow(f4,f3).
caryellow(e2,e1).
caryellow(f3,f2).
caryellow(g4,g3).
caryellow(f2,f1).
caryellow(g3,g2).
caryellow(g2,g1).
cdryellow(c7,d6).
cdryellow(c6,d5).
cdryellow(d6,e5).
cdryellow(c5,d4).
cdryellow(d5,e4).
cdryellow(e5,f5).
cdryellow(c4,d3).
cdryellow(d4,e3).
cdryellow(e4,f4).
cdryellow(f5,g5).
cdryellow(c3,d2).
cdryellow(d3,e2).
cdryellow(e3,f3).
cdryellow(f4,g4).
cdryellow(d2,e1).
cdryellow(e2,f2).
cdryellow(f3,g3).
cdryellow(e1,f1).
cdryellow(f2,g2).
cdryellow(f1,g1).
goal(Player,100) :- true(first(Player)).
goal(Player,75) :- stones(Player,all), true(first(Player2)), Player != Player2.
goal(Player,50) :- stones(Player,some), true(first(Player2)), Player != Player2.
goal(Player,25) :- stones(Player,one), true(first(Player2)), Player != Player2.
goal(Player,0) :- stones(Player,none), true(first(Player2)), Player != Player2.
goal(Player,100) :- true(step(60)), stones(Player,all), not finished.
goal(Player,50) :- true(step(60)), stones(Player,some), not finished.
goal(Player,25) :- true(step(60)), stones(Player,one), not finished.
goal(Player,0) :- true(step(60)), stones(Player,none), not finished.
finished :- true(first(Player)).
toCount(yellow,f1,g1,g2).
toCount(green,c1,c2,d1).
toCount(blue,c6,c7,d6).
toCount(magenta,f6,g6,g7).
stones(Player,all) :- toCount(Player,X,Y,Z), true(cell(X,Player)), true(cell(Y,Player)), true(cell(Z,Player)).
stones(Player,some) :- toCount(Player,X,Y,Z), true(cell(X,Player)), true(cell(Y,Player)), not true(cell(Z,Player)).
stones(Player,some) :- toCount(Player,X,Y,Z), true(cell(X,Player)), not true(cell(Y,Player)), true(cell(Z,Player)).
stones(Player,some) :- toCount(Player,X,Y,Z), not true(cell(X,Player)), true(cell(Y,Player)), true(cell(Z,Player)).
stones(Player,one) :- toCount(Player,X,Y,Z), true(cell(X,Player)), not true(cell(Y,Player)), not true(cell(Z,Player)).
stones(Player,one) :- toCount(Player,X,Y,Z), not true(cell(X,Player)), true(cell(Y,Player)), not true(cell(Z,Player)).
stones(Player,one) :- toCount(Player,X,Y,Z), not true(cell(X,Player)), not true(cell(Y,Player)), true(cell(Z,Player)).
stones(Player,none) :- toCount(Player,X,Y,Z), not true(cell(X,Player)), not true(cell(Y,Player)), not true(cell(Z,Player)).
terminal :- true(step(60)).
terminal :- stones(Player1,all), stones(Player2,all), Player1 != Player2.
true(X) :- next(X).
true(X) :- init(X).
does(X,Y) :- legal(X,Y).

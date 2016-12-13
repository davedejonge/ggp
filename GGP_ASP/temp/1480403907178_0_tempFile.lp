role(red).
role(black).
base(cell(M,N,Role)) :- row(M), col(N), role(Role).
base(connected(K,M,N)) :- succ(K,O), row(M), col(N).
base(owner(K,Role)) :- succ(K,O), role(Role).
base(step(K)) :- succ(K,O).
base(control(Role)) :- role(Role).
input(Role,place(M,N)) :- role(Role), row(M), col(N).
input(Role,noop) :- role(Role).
init(control(red)).
init(step(1)).
legal(Role,place(M,N)) :- true(control(Role)), row(M), col(N), not true(cell(M,N,red)), not true(cell(M,N,black)).
legal(red,noop) :- true(control(black)).
legal(black,noop) :- true(control(red)).
next(cell(M,N,Role)) :- does(Role,place(M,N)).
next(cell(M,N,Role)) :- true(cell(M,N,Role)).
next(step(N)) :- true(step(M)), succ(M,N).
next(owner(K,Role)) :- true(step(K)), true(control(Role)).
next(owner(K,Role)) :- true(owner(K,Role)).
next(connected(K,M,N)) :- does(Role,place(M,N)), true(step(K)).
next(connected(K,M,N)) :- transfer(E), true(connected(E,M,N)), true(step(K)).
next(connected(K,M,N)) :- true(connected(K,M,N)), not transfer(K).
next(control(red)) :- true(control(black)).
next(control(black)) :- true(control(red)).
transfer(K) :- does(Role,place(X,Y)), true(owner(K,Role)), true(connected(K,M,N)), adjacent(X,Y,M,N).
goal(red,100) :- redpath.
goal(red,0) :- not redpath.
goal(black,100) :- blackpath.
goal(black,0) :- not blackpath.
terminal :- redpath.
terminal :- blackpath.
adjacent(J,K,J,N) :- nextcol(K,N), row(J).
adjacent(J,K,J,N) :- nextcol(N,K), row(J).
adjacent(J,K,M,K) :- nextrow(J,M), col(K).
adjacent(J,K,M,K) :- nextrow(M,J), col(K).
adjacent(J,K,M,N) :- nextrow(M,J), nextcol(K,N).
adjacent(J,K,M,N) :- nextrow(J,M), nextcol(N,K).
redpath :- true(connected(K,a,I)), true(connected(K,i,J)), true(owner(K,red)).
blackpath :- true(connected(K,I,1)), true(connected(K,J,9)), true(owner(K,black)).
row(a).
row(b).
row(c).
row(d).
row(e).
row(f).
row(g).
row(h).
row(i).
col(1).
col(2).
col(3).
col(4).
col(5).
col(6).
col(7).
col(8).
col(9).
nextrow(a,b).
nextrow(b,c).
nextrow(c,d).
nextrow(d,e).
nextrow(e,f).
nextrow(f,g).
nextrow(g,h).
nextrow(h,i).
nextcol(1,2).
nextcol(2,3).
nextcol(3,4).
nextcol(4,5).
nextcol(5,6).
nextcol(6,7).
nextcol(7,8).
nextcol(8,9).
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
succ(60,61).
succ(61,62).
succ(62,63).
succ(63,64).
succ(64,65).
succ(65,66).
succ(66,67).
succ(67,68).
succ(68,69).
succ(69,70).
succ(70,71).
succ(71,72).
succ(72,73).
succ(73,74).
succ(74,75).
succ(75,76).
succ(76,77).
succ(77,78).
succ(78,79).
succ(79,80).
succ(80,81).
succ(81,82).
true(X) :- next(X).
true(X) :- init(X).
does(X,Y) :- legal(X,Y).

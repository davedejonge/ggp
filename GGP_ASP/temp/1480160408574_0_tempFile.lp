role(white).
role(black).
piece(p0000).
piece(p0001).
piece(p0010).
piece(p0011).
piece(p0100).
piece(p0101).
piece(p0110).
piece(p0111).
piece(p1000).
piece(p1001).
piece(p1010).
piece(p1011).
piece(p1100).
piece(p1101).
piece(p1110).
piece(p1111).
base(pool(Piece)) :- piece(Piece).
base(selected(Piece)) :- piece(Piece).
base(cell(X,Y,Piece)) :- index(X), index(Y), piece(Piece).
base(cell(X,Y,empty)) :- index(X), index(Y), piece(Piece).
base(selectControl(P)) :- role(P).
base(placeControl(P)) :- role(P).
input(P,noop) :- role(P).
input(P,select(Piece)) :- role(P), piece(Piece).
input(P,place(Piece,X,Y)) :- role(P), piece(Piece), index(X), index(Y).
init(cell(1,1,empty)).
init(cell(1,2,empty)).
init(cell(1,3,empty)).
init(cell(1,4,empty)).
init(cell(2,1,empty)).
init(cell(2,2,empty)).
init(cell(2,3,empty)).
init(cell(2,4,empty)).
init(cell(3,1,empty)).
init(cell(3,2,empty)).
init(cell(3,3,empty)).
init(cell(3,4,empty)).
init(cell(4,1,empty)).
init(cell(4,2,empty)).
init(cell(4,3,empty)).
init(cell(4,4,empty)).
init(pool(Piece)) :- piece(Piece).
init(selectControl(white)).
legal(Player,select(Piece)) :- role(Player), true(selectControl(Player)), true(pool(Piece)).
legal(Player,place(Piece,X,Y)) :- role(Player), true(placeControl(Player)), true(selected(Piece)), true(cell(X,Y,empty)).
legal(Player,noop) :- role(Player), not true(selectControl(Player)), not true(placeControl(Player)).
next(pool(Piece)) :- true(pool(Piece)), not does(white,select(Piece)), not does(black,select(Piece)).
next(selected(Piece)) :- role(Player), does(Player,select(Piece)).
next(cell(X,Y,State)) :- true(cell(X,Y,State)), role(Player), does(Player,select(Piece)).
next(cell(X1,Y1,State)) :- true(cell(X1,Y1,State)), role(Player), does(Player,place(Piece,X2,Y2)), distinctCell(X1,Y1,X2,Y2).
next(cell(X,Y,Piece)) :- does(Player,place(Piece,X,Y)).
next(selectControl(white)) :- true(placeControl(white)).
next(placeControl(white)) :- true(selectControl(black)).
next(selectControl(black)) :- true(placeControl(black)).
next(placeControl(black)) :- true(selectControl(white)).
terminal :- line.
terminal :- not boardOpen.
goal(white,100) :- line, placedLast(white).
goal(white,50) :- not line, not boardOpen.
goal(white,0) :- not line, boardOpen.
goal(white,0) :- line, placedLast(black).
goal(black,100) :- line, placedLast(black).
goal(black,50) :- not line, not boardOpen.
goal(black,0) :- not line, boardOpen.
goal(black,0) :- line, placedLast(white).
placedLast(Player) :- true(selectControl(Player)).
boardOpen :- true(cell(X,Y,empty)).
line :- row.
line :- column.
line :- diagonal.
row :- index(Y), true(cell(1,Y,P1)), true(cell(2,Y,P2)), true(cell(3,Y,P3)), true(cell(4,Y,P4)), set(P1,P2,P3,P4).
column :- index(X), true(cell(X,1,P1)), true(cell(X,2,P2)), true(cell(X,3,P3)), true(cell(X,4,P4)), set(P1,P2,P3,P4).
diagonal :- true(cell(1,1,P1)), true(cell(2,2,P2)), true(cell(3,3,P3)), true(cell(4,4,P4)), set(P1,P2,P3,P4).
diagonal :- true(cell(1,4,P1)), true(cell(2,3,P2)), true(cell(3,2,P3)), true(cell(4,1,P4)), set(P1,P2,P3,P4).
set(P1,P2,P3,P4) :- bit(Bit), index(N), bitNIs(N,P1,Bit), bitNIs(N,P2,Bit), bitNIs(N,P3,Bit), bitNIs(N,P4,Bit).
distinctCell(X1,Y1,X2,Y2) :- index(X1), index(Y1), index(X2), index(Y2), X1 != X2.
distinctCell(X1,Y1,X2,Y2) :- index(X1), index(Y1), index(X2), index(Y2), Y1 != Y2.
bit(0).
bit(1).
index(1).
index(2).
index(3).
index(4).
bitNIs(4,p0000,0).
bitNIs(3,p0000,0).
bitNIs(2,p0000,0).
bitNIs(1,p0000,0).
bitNIs(4,p0001,0).
bitNIs(3,p0001,0).
bitNIs(2,p0001,0).
bitNIs(1,p0001,1).
bitNIs(4,p0010,0).
bitNIs(3,p0010,0).
bitNIs(2,p0010,1).
bitNIs(1,p0010,0).
bitNIs(4,p0011,0).
bitNIs(3,p0011,0).
bitNIs(2,p0011,1).
bitNIs(1,p0011,1).
bitNIs(4,p0100,0).
bitNIs(3,p0100,1).
bitNIs(2,p0100,0).
bitNIs(1,p0100,0).
bitNIs(4,p0101,0).
bitNIs(3,p0101,1).
bitNIs(2,p0101,0).
bitNIs(1,p0101,1).
bitNIs(4,p0110,0).
bitNIs(3,p0110,1).
bitNIs(2,p0110,1).
bitNIs(1,p0110,0).
bitNIs(4,p0111,0).
bitNIs(3,p0111,1).
bitNIs(2,p0111,1).
bitNIs(1,p0111,1).
bitNIs(4,p1000,1).
bitNIs(3,p1000,0).
bitNIs(2,p1000,0).
bitNIs(1,p1000,0).
bitNIs(4,p1001,1).
bitNIs(3,p1001,0).
bitNIs(2,p1001,0).
bitNIs(1,p1001,1).
bitNIs(4,p1010,1).
bitNIs(3,p1010,0).
bitNIs(2,p1010,1).
bitNIs(1,p1010,0).
bitNIs(4,p1011,1).
bitNIs(3,p1011,0).
bitNIs(2,p1011,1).
bitNIs(1,p1011,1).
bitNIs(4,p1100,1).
bitNIs(3,p1100,1).
bitNIs(2,p1100,0).
bitNIs(1,p1100,0).
bitNIs(4,p1101,1).
bitNIs(3,p1101,1).
bitNIs(2,p1101,0).
bitNIs(1,p1101,1).
bitNIs(4,p1110,1).
bitNIs(3,p1110,1).
bitNIs(2,p1110,1).
bitNIs(1,p1110,0).
bitNIs(4,p1111,1).
bitNIs(3,p1111,1).
bitNIs(2,p1111,1).
bitNIs(1,p1111,1).
true(X) :- next(X).
true(X) :- init(X).
does(X,Y) :- legal(X,Y).

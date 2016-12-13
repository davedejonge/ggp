package ddejonge.ggp.tools.zobrist;

class ContainerNode<T> extends TranspoNode<T>{

	T containedObject;
	
	ContainerNode(T containedObject) {
		this.containedObject = containedObject;
	}

	@Override
	void clear() {
		this.containedObject = null;		
	}
	
}

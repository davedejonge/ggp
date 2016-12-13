package ddejonge.ggp.tools.zobrist;


class PointerNode<T> extends TranspoNode<T>{
	
	final static int HASH_LENGTH = 64; // a long has 64 bits.
	final static int NUM_CHILDREN_PER_LAYER = 16;
	
	final static int INDEX_LENGTH = 4; // because 2^4 == NUM_CHILDREN_PER_LAYER
	final static int MASK = NUM_CHILDREN_PER_LAYER - 1; // if index length is 4, then this is a binary number consisting of 4 1's:  MASK = 1111
	int MAX_DEPTH = HASH_LENGTH / INDEX_LENGTH;
	
	TranspoNode<T>[] children = new TranspoNode[NUM_CHILDREN_PER_LAYER];
	
	
	PointerNode(){
	}
	
	
	T retrieve(T treeNode, long hash){
		
		if(this.children == null){
			 children = new TranspoNode[NUM_CHILDREN_PER_LAYER];
		}
		//note that children is set to null whenever we call clear(). Therefore we need to check if this array is null.
		// also note that we still also need to initialize children when the PointerNode is constructed,
		// because the initialization here only works for the root node.
		
		return retrieve(treeNode, hash, 1);
	}
	
	private T retrieve(T treeNode, long hash, int depth){
		
		// if depth == 1 then we need the 4 left most bits, so we need to shift 60 bits to the right.
		// if depth == 2 then we need the 4 bits next to the first 4.
		// if depth == 3 then we need the 4 bits next to the first 8.
		
		int shift = HASH_LENGTH - INDEX_LENGTH * depth;
		int index =  (int)((hash >> shift) & MASK);
		
		if(children[index] == null){
			if(depth == MAX_DEPTH){
				children[index] = new ContainerNode<T>(treeNode);
				
			}else{
				children[index] = new PointerNode<T>();
			}
		}
		
		TranspoNode<T> child = children[index];
		
		if(child instanceof ContainerNode){
			return ((ContainerNode<T>)child).containedObject;
		}else{		
			return ((PointerNode<T>)child).retrieve(treeNode, hash, depth+1);
		}
	}
	
	@Override
	void clear(){
		
		if(this.children == null){
			return;
		}
		
		for(int i=0; i<children.length; i++){
			TranspoNode<T> transpoNode = children[i];
			if(transpoNode != null){
				transpoNode.clear();
				children[i] = null;
			}
		}
		this.children = null;
	}
}

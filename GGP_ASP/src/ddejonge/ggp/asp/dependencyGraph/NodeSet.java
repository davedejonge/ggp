package ddejonge.ggp.asp.dependencyGraph;

class NodeSet {
	
	long[] longs;

	NodeSet(int maxNumNodes){
		longs = new long[(maxNumNodes/64) + 1];
	}
	
	void add(Node node){
		
		int index = node.id/64;
		int elementId = node.id % 64;
		
		longs[index] = addElement(longs[index], elementId); 
	}
	
	boolean contains(Node node){
		
		int index = node.id/64;
		int elementId = node.id % 64;
		
		return containsElement(longs[index], elementId);
	}
	
	
	void remove(Node node){
		
		int index = node.id/64;
		int elementId = node.id % 64;
		
		longs[index] = removeElement(longs[index], elementId);
	}
	
	
	static long addElement(long set, int elementId){
		return set | (1L<<elementId);
	}
	
	static boolean containsElement(long set, int elementId){
		return (1L<<elementId & set) == (1L<<elementId);
	}
		
	static long removeElement(long set, int elementId){
		return set & ~(1L<<elementId);
	}
	
}

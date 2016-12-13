package ddejonge.ggp.tools.graph;


public class Edge {
	
	public static int edgesGenerated = 0;
	
	private Vertex from;
	private Vertex to;

	//Constructor is not public, because it should only be called from Graph.
	protected Edge(Vertex from, Vertex to){
		this.from = from;
		this.to = to;
		
		edgesGenerated++;
	}
	
	public Vertex getFrom(){
		return from;
	}
	
	public Vertex getTo(){
		return to;
	}
	
	
	void cleanUp_private(){
		//should only be called from graph.
		this.from = null;
		this.to = null;
	}
	
	protected void cleanUp(){
		//can be overridden
	}
	
	public String toString(){
		return from.toString() + " --> " + to.toString();
	}
	
	@Override
	public boolean equals(Object object){
		
		if( ! (object instanceof Edge)){
			return false;
		}
		
		Edge otherEdge = (Edge)object;
		
		return this.from.equals(otherEdge.from) &&  this.to.equals(otherEdge.to);
	}
	
	@Override
	public int hashCode(){
		return 31*from.hashCode() + to.hashCode();
	}
}

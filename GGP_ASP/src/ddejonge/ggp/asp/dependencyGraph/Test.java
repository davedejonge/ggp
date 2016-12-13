package ddejonge.ggp.asp.dependencyGraph;

import java.util.List;

import org.ggp.base.util.gdl.grammar.*;

import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.graph.Edge;
import ddejonge.ggp.tools.graph.Vertex;

public class Test {
	
	public static void main(String[] args) {
		
		//load some game...
		List<Gdl> rules = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\GGP_Players\\games\\ticTacToe\\ticTacToe.kif");
		
		DependencyGraph graph = DependencyGraphFactory.constructGraph(rules);
		
		System.out.println("NODES:");
		for(Vertex node : graph.getVertices()){
			System.out.println(node);
		}
		
		System.out.println();
		System.out.println("EDGES:");
		for(Edge edge : graph.getEdges()){
			System.out.println(edge);
		}
		
		GdlConstant xplayer = GdlPool.getConstant("xplayer");
		GdlConstant noop = GdlPool.getConstant("noop");
		GdlRelation legal_xplayer_noop = GdlPool.getRelation(GdlPool.LEGAL, new GdlTerm[]{xplayer, noop} );
		
		boolean depends = graph.dependsOn(legal_xplayer_noop, GdlPool.DOES);
		System.out.println("depends: " + depends);
		
		System.out.println("finished!");
	}
	
}

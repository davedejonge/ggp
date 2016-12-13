package ddejonge.ggp.mcts.tools.visual;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.JFileChooser;

import ddejonge.ggp.mcts.MCTSGraph;
import ddejonge.ggp.tools.visual.TreeViewer;

public class ViewGraph {

	public static void main(String[] args) {


		File currentDirectory = new File("C:\\Users\\30044279\\Dropbox\\java projects\\GGP_Players\\log\\");
		
		final JFileChooser fc = new JFileChooser(currentDirectory);
		
		int returnVal = fc.showOpenDialog(null);
		
		if(returnVal != JFileChooser.APPROVE_OPTION){
			return;
		}
		
		File selectedFile = fc.getSelectedFile();
		
		try(
				FileInputStream fin = new FileInputStream(selectedFile);
				ObjectInputStream ois = new ObjectInputStream(fin);
		){
			
			MCTSGraph graph = (MCTSGraph) ois.readObject();
			TreeViewer mtv = new TreeViewer(graph.getRoot(), selectedFile.getName());
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
	}

}

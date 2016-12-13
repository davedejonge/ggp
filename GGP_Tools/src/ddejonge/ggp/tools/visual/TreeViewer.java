package ddejonge.ggp.tools.visual;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;



public class TreeViewer extends JFrame {

	//static MyTreeViewer myTreeViewer;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//myTreeViewer = new MyTreeViewer();
	}
	
	

	public TreeViewer(ViewableTreeNode root, String title) {
		
		this.setTitle(title);  
			
		int x = (int) Math.round(100.0 * Math.random());
		int y = (int) Math.round(100.0 * Math.random());
				
		this.setBounds(x, y, 500, 500);

		DefaultMutableTreeNode dmt_root = new DefaultMutableTreeNode(root);
		addChildren(dmt_root); 

		JTree tree = new JTree(dmt_root);
		this.add(new JScrollPane(tree));
		
		this.setVisible(true);
		
		
	}
	

	void addChildren(DefaultMutableTreeNode dmt_node){
		
		
		List<? extends ViewableTreeNode> objectChildren = ((ViewableTreeNode)dmt_node.getUserObject()).getChildren();
		
		if(objectChildren == null || objectChildren.size() == 0){
			return;
		}
		
		DefaultMutableTreeNode newChild;
		for(ViewableTreeNode child : objectChildren){
			newChild = new DefaultMutableTreeNode(child);
			dmt_node.add(newChild);
			addChildren(newChild);
			
		}
		
	}

	

}

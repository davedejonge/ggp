package ddejonge.ggp.tools.visual;

import java.util.List;

public interface ViewableTreeNode {
	
	//public ITreeNode getParent();	
	public List<? extends ViewableTreeNode> getChildren();
	
	//public String getLabelString(); //the label that should be displayed in the tree viewer.
	
}

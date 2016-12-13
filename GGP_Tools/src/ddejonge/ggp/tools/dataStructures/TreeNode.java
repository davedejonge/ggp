package ddejonge.ggp.tools.dataStructures;

import java.util.ArrayList;
import java.util.List;



public class TreeNode<T> /*implements ITreeNode */{
	
	//STATIC FIELDS
	static int numGenerated = 0;
	
	//FIELDS
	int id;
	int depth = 0;
	TreeNode<T> parent;
	List<TreeNode<T>> children;
	T label;

	//CONSTRUCTORS
	public TreeNode(T label){
		id = numGenerated++;
		parent = null;
		this.label = label;
	}
	
	public TreeNode(TreeNode<T> parent, T label){
		id = numGenerated++;
		if(parent != null){
			parent.addChild(this);
		}
		this.label = label;
	}

	//METHODS

	
	
	//GETTERS AND SETTERS
	public T getLabel(){
		return label;
	}
	
	public void addChild(TreeNode<T> childNode){
		
		if(children == null){
			children = new ArrayList<>();
		}
		children.add(childNode);
		
		childNode.parent = this;
		childNode.depth = this.depth+1;
	}
	
	public void addChildren(List<TreeNode<T>> childNodes){
		
		if(children == null){
			children = new ArrayList<>();
		}
		
		children.addAll(childNodes);
		
		for(TreeNode<T> childNode : childNodes){
			childNode.parent = this;
			childNode.depth = this.depth+1;
		}
		
	}
	
	public void removeChild(TreeNode<T> childNode){
		children.remove(childNode);
		childNode.parent = null;
		childNode.depth = 0;
	}
	
	
	public TreeNode<T> getParent(){
		return this.parent;
	}
	
	public List<TreeNode<T>> getChildren(){
		return this.children;
	}
	
	public boolean hasChildren(){
		return children != null && children.size() > 0;
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	/**
	 * Returns a list containing all labels along the branch from the root to this node (including the label of this node).<br/>
	 * The list will be filled in reverse order: the label of the root will be the last element in this list.
	 * 
	 * @return
	 */
	public void getBranchLabels(List<T> listToFill){
		
		TreeNode<T> loopNode = this;
		while(loopNode != null){
			listToFill.add(loopNode.label);
			loopNode = loopNode.parent;
		}
		
		return;
	}
	
	public String toString(){
		return this.label.toString();
	}


}

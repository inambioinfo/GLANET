/**
 * @author Burcak Otlu Saritas
 *
 * 
 */

package intervaltree;

import common.Commons;



public class IntervalTreeNode{
	
	//Basic attributes
	int max;
	int low;
	int high;
	String chromName;

	int numberofBases;
	
	
	char color;
	IntervalTreeNode left;
	IntervalTreeNode right;
	IntervalTreeNode parent;

	//Sentinel Node
	String name;

	//Node type whether it is original or merged node
	String nodeType;
	
	
	public String getChromName() {
		return chromName;
	}

	public void setChromName(String chromName) {
		this.chromName = chromName;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public int getNumberofBases() {
		if (this.isNotSentinel()){
			return (this.getHigh()-this.getLow()+1);
		}else{
			return 0;
		}
			
		
	}

	public void setNumberofBases(int numberofBases) {
		this.numberofBases = numberofBases;
	}

	
	
	

	public boolean isSentinel(){
		if (Commons.SENTINEL.equals(this.getName())){
			return true;
		}else 
			return false;
		
	}
	
	public boolean isNotSentinel(){
		if (Commons.NOT_SENTINEL.equals(this.getName())){
			return true;
		}else 
			return false;
		
	}
	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}



	
	
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	
	
	
	
	


	public int getLow() {
		return low;
	}
	public void setLow(int low) {
		this.low = low;
	}
	public int getHigh() {
		return high;
	}
	public void setHigh(int high) {
		this.high = high;
	}
	public char getColor() {
		return color;
	}
	public void setColor(char color) {
		this.color = color;
	}
	public IntervalTreeNode getLeft() {
		return left;
	}
	public void setLeft(IntervalTreeNode left) {
		this.left = left;
	}
	public IntervalTreeNode getRight() {
		return right;
	}
	public void setRight(IntervalTreeNode right) {
		this.right = right;
	}
	public IntervalTreeNode getParent() {
		return parent;
	}
	public void setParent(IntervalTreeNode parent) {
		this.parent = parent;
	}
	
	
	
	
	

	
	
	//SENTINEL node
	public IntervalTreeNode(){
		this.color = Commons.BLACK;			
		this.name = Commons.SENTINEL;
		this.numberofBases = 0;
	}
	
	//testing interval tree operations
	public IntervalTreeNode(int low, int high) {
		super();
		this.low = low;
		this.high = high;
		
		this.left = new IntervalTreeNode();
		this.right = new IntervalTreeNode();
		this.parent = new IntervalTreeNode();
		this.name = Commons.NOT_SENTINEL;
		this.numberofBases = high-low+1;
					
	}
	
	
	
	//process input data
	//remove overlaps
	public IntervalTreeNode(String chromName, int low, int high) {
		super();
		this.chromName = chromName;
		this.low = low;
		this.high = high;
		
		this.left = new IntervalTreeNode();
		this.right = new IntervalTreeNode();
		this.parent = new IntervalTreeNode();
		this.name = Commons.NOT_SENTINEL;
		this.numberofBases = high-low+1;
					

	}
	
	//process input data
	//remove overlaps
	//merged interval tree node
	public IntervalTreeNode(String chromName, int low, int high,String nodeType) {
		super();
		this.chromName = chromName;
		this.low = low;
		this.high = high;
		this.nodeType = nodeType;
		
		this.left = new IntervalTreeNode();
		this.right = new IntervalTreeNode();
		this.parent = new IntervalTreeNode();
		this.name = Commons.NOT_SENTINEL;
		this.numberofBases = high-low+1;
					

	}
		
	
	

	

}

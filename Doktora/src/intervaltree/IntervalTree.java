/**
 * @author Burcak Otlu Saritas
 *
 * 
 */


/*
 * Interval Tree is a red black tree which is augmented with max attribute.
 * Red black tree is a variation of binary search tree + 1 bit per node: an attribute color, which is either red or black.
 * Red black tree is balanced, its height is O(log n), where is n is the number of nodes.
 * Operations will take O(log n) time in the worst case. 
 * 
 * Red black tree properties:
 * 1. Every node is either red or black.
 * 2. The root is black.
 * 3. Every leaf(nil[T]) is black.
 * 4. If a node is red, then both its children are black. (Hence no two reds in a row on a simple path from the root to a leaf.)
 * 5. For each node, all paths from the node to descendant leaves contain the same number of black nodes.
 * 
 * This Interval Tree implementation is based on Introduction to Algorithms book of Cormen et al. 2nd Edition
 * 
 * Please note that IntervalTreeDelete function does not handle erroneous inputs:
 * Like deleting a node which does not exists in the interval tree 
 * and deleting the root of tree when there is no NOT_SENTINEL node has left.
 * 
 * 
 */
package intervaltree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import annotate.intervals.parametric.PermutationNumberTfNameCellLineNameOverlap;
import annotate.intervals.parametric.PermutationNumberUcscRefSeqGeneOverlap;
import annotate.intervals.parametric.TfNameandCellLineNameOverlap;
import annotate.intervals.parametric.UcscRefSeqGeneOverlap;

import common.Commons;


public class IntervalTree {

	IntervalTreeNode root;
	int numberofNodes;
	long numberofNonOverlappingBases;
	

	public long getNumberofNonOverlappingBases() {
		return numberofNonOverlappingBases;
	}

	public void setNumberofNonOverlappingBases(long numberofNonOverlappingBases) {
		this.numberofNonOverlappingBases = numberofNonOverlappingBases;
	}

	public int getNumberofNodes() {
		return numberofNodes;
	}

	public void setNumberofNodes(int numberofNodes) {
		this.numberofNodes = numberofNodes;
	}

	public IntervalTreeNode getRoot() {
		return root;
	}

	public void setRoot(IntervalTreeNode root) {
		this.root = root;
	}
	
	

	public IntervalTree() {
		super();
		root = new IntervalTreeNode();
		this.numberofNodes = 0;
		this.numberofNonOverlappingBases =0;
	}
	
	

	public static int maximum(int x, int y, int z){
		
		int max = x;
	
		if (y>max) 
			max = y;
		if (z>max ) 
			max = z;
		
		return max;
	}
	
	
	public static int minimum(int x, int y, int z){
		
		int min = x;
	
		if (y<min) 
			min = y;
		if (z<min) 
			min = z;
		
		return min;
	}
	

	
	public static int max(IntervalTreeNode node){
		
		int a = Integer.MIN_VALUE;
		int b = Integer.MIN_VALUE;
		
		if (node.getLeft().isNotSentinel())
			a= node.getLeft().getMax();
		
		if(node.getRight().isNotSentinel())
			b= node.getRight().getMax();
		
		return maximum(node.getHigh(), a, b);
					
	}
	
	public static int min(IntervalTreeNode node){
		
		int a = Integer.MAX_VALUE;
		int b = Integer.MAX_VALUE;
		
		if (node.getLeft().isNotSentinel())
			a= node.getLeft().getMin();
		
		if(node.getRight().isNotSentinel())
			b= node.getRight().getMin();
		
		return minimum(node.getLow(), a, b);
					
	}
	
	
	public void incrementHeightByOne(IntervalTreeNode node){
		
		node.setHeight(node.getHeight()+1);
		
		if (node.getLeft().isNotSentinel()){
			incrementHeightByOne(node.getLeft());
		}
		if (node.getRight().isNotSentinel()){
			incrementHeightByOne(node.getRight());
		}
		
	}
	
	public void decrementHeightByOne(IntervalTreeNode node){
		node.setHeight(node.getHeight()-1);
		
		if (node.getLeft().isNotSentinel()){
			decrementHeightByOne(node.getLeft());
		}
		if (node.getRight().isNotSentinel()){
			decrementHeightByOne(node.getRight());
		}
		
	}
	
//	We change the pointer structure with Rotation  
//	When we do a left rotation on a node x,
//	we assume that its right child y is not null.
//	Left rotation pivots around the link from x to y.
//	It makes the y the new root of the subtree, with x as the new left child of y 
//	and y's left child becomes x's right child
//	burcak: left rotate edilen node x, right child'inin y diyelim, left child'i oluyor
	public void intervalTreeLeftRotate(IntervalTree tree, IntervalTreeNode x){
		
		IntervalTreeNode y = x.getRight();		
		
		x.setRight(y.getLeft());
		if(!Commons.SENTINEL.equals(y.getLeft().getName())){
			y.getLeft().setParent(x);
		}
		y.setParent(x.getParent());
		
		if(Commons.SENTINEL.equals(x.getParent().getName())){
			tree.setRoot(y);
		} else{
			if(x==(x.getParent().getLeft())){
				x.getParent().setLeft(y);
			}else{
				x.getParent().setRight(y);
			}	
		}
		
		y.setLeft(x);
		x.setParent(y);
		
//		update max attributes
		x.setMax(max(x));
		y.setMax(max(y));
		
//		update min attributes
		x.setMin(min(x));
		y.setMin(min(y));
		
		//update height attributes
		x.setHeight(x.getHeight()+1);
		if(x.getLeft().isNotSentinel()){
			incrementHeightByOne(x.getLeft());
		}
		
		y.setHeight(y.getHeight()-1);
		if(y.getRight().isNotSentinel()){
			decrementHeightByOne(y.getRight());
		}
		
					
	}
	
//	burcak: right rotate edilen node x left child'inin y diyelim, right child'i oluyor
	public void intervalTreeRightRotate(IntervalTree tree, IntervalTreeNode x){
		IntervalTreeNode y = x.getLeft();
		
		x.setLeft(y.getRight());
		if(!Commons.SENTINEL.equals(y.getRight().getName())){
			y.getRight().setParent(x);
		}
		y.setParent(x.getParent());
		
		if(Commons.SENTINEL.equals(x.getParent().getName())){
			tree.setRoot(y);
		} else{
			if(x==(x.getParent().getRight())){
				x.getParent().setRight(y);
			}else{
				x.getParent().setLeft(y);
			}	
		}
		
		y.setRight(x);
		x.setParent(y);
		
//		update max attributes
		x.setMax(max(x));
		y.setMax(max(y));		
		
//		update min attributes
		x.setMin(min(x));
		y.setMin(min(y));
		
		//update height attributes
		x.setHeight(x.getHeight()+1);
		if(x.getRight().isNotSentinel()){
			incrementHeightByOne(x.getRight());
		}
		
		y.setHeight(y.getHeight()-1);
		if(y.getLeft().isNotSentinel()){
			decrementHeightByOne(y.getLeft());
		}
						
	}

	
	public void intervalTreeDeleteFixUp(IntervalTree tree, IntervalTreeNode x){
		IntervalTreeNode w;
		
		
		while(x!=tree.getRoot() && x.getColor()==Commons.BLACK){
			if (x== (x.getParent()).getLeft()){
				w = x.getParent().getRight();
				
				//case1: w is red.
				if(w.getColor()==Commons.RED){
					w.setColor(Commons.BLACK);								//Case1
					x.getParent().setColor(Commons.RED);					//Case1
					intervalTreeLeftRotate(tree, x.getParent());	//Case1
					w = x.getParent().getRight(); 					//Case1
				}
				
				//case2: w is black and both of w's children are black
				if(w.getLeft().getColor()==Commons.BLACK && w.getRight().getColor()==Commons.BLACK ){
					w.setColor(Commons.RED);								//Case2
					x= x.getParent();								//Case2
				}else{
					//case3: w is black, w's left child is red and w's right child is black
					if((w.getRight()).getColor()==Commons.BLACK){
						w.getLeft().setColor(Commons.BLACK);						//Case3
						w.setColor(Commons.RED);								//Case3
						intervalTreeRightRotate(tree, w);				//Case3
						w = x.getParent().getRight();					//Case3
					}
					//case4: w is black, w's left child is black and w's right child is red.
					w.setColor(x.getParent().getColor());				//Case4
					x.getParent().setColor(Commons.BLACK);						//Case4
					w.getRight().setColor(Commons.BLACK);							//Case4
					intervalTreeLeftRotate(tree, x.getParent());		//Case4
					x = tree.getRoot();									//Case4						
					
				}				
				
			}else{
				
				w = x.getParent().getLeft();
				
				//case1: w is red.
				if(w.getColor()==Commons.RED){
					w.setColor(Commons.BLACK);								//Case1
					x.getParent().setColor(Commons.RED);					//Case1
					intervalTreeRightRotate(tree, x.getParent());	//Case1
					w = x.getParent().getLeft(); 					//Case1
				}
				
				//case2: w is black and both of w's children are black
				if(w.getRight().getColor()==Commons.BLACK && w.getLeft().getColor()==Commons.BLACK ){
					w.setColor(Commons.RED);								//Case2
					x= x.getParent();								//Case2
				}else{
					//case3: w is black, w's right child is red and w's left child is black
					if((w.getLeft()).getColor()==Commons.BLACK){
						w.getRight().setColor(Commons.BLACK);						//Case3
						w.setColor(Commons.RED);								//Case3
						intervalTreeLeftRotate(tree, w);				//Case3
						w = x.getParent().getLeft();					//Case3
					}
					//case4: w is black, w's right child is black and w's left child is red.
					w.setColor(x.getParent().getColor());				//Case4
					x.getParent().setColor(Commons.BLACK);						//Case4
					w.getLeft().setColor(Commons.BLACK);							//Case4
					intervalTreeRightRotate(tree, x.getParent());		//Case4
					x = tree.getRoot();									//Case4						
					
				}
			
			}
			
		}//end of while
		
		x.setColor(Commons.BLACK);
	}
	
	public void intervalTreeInsertFixUp(IntervalTree tree, IntervalTreeNode z){
		IntervalTreeNode y;
		
			while(z.getParent().getColor()==Commons.RED){
				if ((z.getParent().getParent().getLeft()) == z.getParent()){
					y= z.getParent().getParent().getRight();
					if (y.getColor()==Commons.RED){
						//Case1 y is red
						z.getParent().setColor(Commons.BLACK); 				//Case1
						y.setColor(Commons.BLACK);             				//Case1
						z.getParent().getParent().setColor(Commons.RED); 	//Case1
						z = z.getParent().getParent(); 				//Case1
					}else {
						if (z==z.getParent().getRight()){
							//Case2 y is black, z is a right child 
							z = z.getParent();							//Case2
							intervalTreeLeftRotate(tree, z);			//Case2
						}
						//Case3 y is black, z is a left child
						z.getParent().setColor(Commons.BLACK);								//Case3
						z.getParent().getParent().setColor(Commons.RED);					//Case3 
						intervalTreeRightRotate(tree,z.getParent().getParent()); 	//Case3
						
					}
				}else{
					y= z.getParent().getParent().getLeft();
					if (y.getColor()==Commons.RED){
						z.getParent().setColor(Commons.BLACK); 				//Case1
						y.setColor(Commons.BLACK);             				//Case1
						z.getParent().getParent().setColor(Commons.RED); 	//Case1
						z = z.getParent().getParent(); 				//Case1
					}else{ 
						if (z==z.getParent().getLeft()){
							z = z.getParent();							//Case2
							intervalTreeRightRotate(tree, z);			//Case2
						}
						z.getParent().setColor(Commons.BLACK);								//Case3
						z.getParent().getParent().setColor(Commons.RED);					//Case3 
						intervalTreeLeftRotate(tree,z.getParent().getParent()); 	//Case3
						
					}
				}
								
			}
		tree.getRoot().setColor(Commons.BLACK);
	}
	
	public void updateMinAttribute(IntervalTreeNode x){
		int savedMin;
		
		if(x.isNotSentinel()){
			savedMin= x.getMin(); 
			x.setMin(min(x));	
			
			if(savedMin!=x.getMin()){
				updateMinAttribute(x.getParent());
			}	
		}		
		
	}

	public void updateMaxAttribute(IntervalTreeNode x){
		
		
		boolean hasChanged = false;
		int savedMax;
		
		if(x.isNotSentinel()){
			savedMax= x.getMax(); 
			x.setMax(max(x));	
			
			if(savedMax!=x.getMax()){
				hasChanged = true;
			}
			
		}else{
			return;
		}
		
		if (hasChanged)
			updateMaxAttribute(x.getParent());
		
		
	}
	
	/*
	 * intervalTreeMinimum returns the minimum elementin the subtree rooted at a given node x
	 */
	public IntervalTreeNode intervalTreeMinimum(IntervalTreeNode x){
		while(!Commons.SENTINEL.equals(x.getLeft().getName())){
			x = x.getLeft();
		}
		
		return x;
	}
	
	/*
	 * intervalTreeSuccessor is broken into two cases.
	 * If the right subtree of node x is nonempty, then the successor of x is just the left-most node in the right subtree.
	 * 
	 * On the other hand, if the right subtree of node x is empty and x has a successor y, then is the lowest ancestor of x 
	 * whose left child is an ancestor of x. 
	 * To find y, we go up the tree from x until we encounter a node that is the left child of its parent.
	 */
	public IntervalTreeNode intervalTreeSuccessor(IntervalTreeNode x){
		IntervalTreeNode y;
		
		if (!Commons.SENTINEL.equals(x.getRight().getName())){
			return intervalTreeMinimum(x.getRight());			
		} 
		
		y = x.getParent();
		
		while(!Commons.SENTINEL.equals(y.getName()) && (x==y.getRight())){
			x = y;
			y = y.getParent();
		}		
		
		return y;
	}
	
	/*
	 *  If we delete, thus removing a node, what color was the node that was removed?
	 *  
	 *  RED? Ok, since we won't have changed any black-heights, nor will we have created two red nodes in a row.
	 *  Also, cannot cause a violation of property 2, since if the removed node was red, it could not have been the root.
	 *  
	 *  BLACK? Could cause there to be two reds in a row(violating property 4), and can also cause a violation of property 5.
	 *  Could also cause a violation of property 2, if the removed node was the root and its child-which becomes the new root -was red. 
	 */
	public IntervalTreeNode intervalTreeDelete(IntervalTree tree, IntervalTreeNode z){
		
        //since z might be changed, do the decrements before
		//Decrement the number of nodes by one
		tree.setNumberofNodes(tree.getNumberofNodes()-1);
		
		//Decrease the number of non overlapping bases by size of the deleted node z
		tree.setNumberofNonOverlappingBases(tree.getNumberofNonOverlappingBases()-z.getNumberofBases());
		
		
		//Start by doing regular binary search tree 
		IntervalTreeNode y;
		IntervalTreeNode x;
		
		//Determine a node y to splice out.
		//The node y is either the input node z (if z has at most 1 child) or
		//the successor of z if z has two children.
		if (Commons.SENTINEL.equals(z.getLeft().getName()) || Commons.SENTINEL.equals(z.getRight().getName())){
			y = z;			
		}else{
			y = intervalTreeSuccessor(z);
		}
		
		//x is set to the not null child of the y or
		//x is set to nil[T] if y has no children.
		if(!Commons.SENTINEL.equals(y.getLeft().getName())){
			x = y.getLeft();
		}else{
			x = y.getRight();
		}
		
		//The node y is spliced out here by modifying pointers in p[y] and x.
		//Splicing out y is somewhat complicated by the need for proper handling of the boundary conditions,
		//which occur when x = null or when y is the root.
		//

		x.setParent(y.getParent());
		x.setHeight(y.getParent().getHeight()+1);
		
		if (Commons.SENTINEL.equals(y.getParent().getName())){
			tree.setRoot(x);
		}else{
			if(y==(y.getParent()).getLeft()){
				(y.getParent()).setLeft(x);
			}else{
				(y.getParent()).setRight(x);
			}
		}
		
		
		//set max value of parent of x		
		updateMaxAttribute(x.getParent());
		//set min value of parent of x		
		updateMinAttribute(x.getParent());
			
		//If the successor of z was the node spliced out, 
		//the contents of z are moved from y to z, overwriting the previous contents.
		//data fields of node y is copied into node z.
		//node y takes place of node z.
		if(y!=z){
			//copy y's satellite data into z
			z.setChromName(y.getChromName());
			z.setTfbsorHistoneName(y.getTfbsorHistoneName());			
			z.setCellLineName(y.getCellLineName());			
			z.setFileName(y.getFileName());
			z.setRefSeqGeneName(y.getRefSeqGeneName());
			z.setGeneEntrezId(y.getGeneEntrezId());
			z.setGeneHugoSymbol(y.getGeneHugoSymbol());
			z.setIntervalName(y.getIntervalName());
			z.setStrand(y.getStrand());
			
			z.setLow(y.getLow());
			z.setHigh(y.getHigh());
			z.setNumberofBases(y.getNumberofBases());
			
			//Burcak commented only the data has been changed
			//Left, Right and Parent does not change.
			//height does not change
//			z.setLeft(y.getLeft());
//			z.setRight(y.getRight());
//			z.setParent(y.getParent());	
//			z.setColor(y.getColor());
			
			
			//set the max of node z
			updateMaxAttribute(z);
			
			//set the min of node z
			updateMinAttribute(z);
			
			
		}
		
		//If y is black, we could have violations of red-black properties:
		//1. Every node is either black or red. Ok.
		//2. The root is black. If y is the root and x is red, then the  root has become red. 
		//3. Every leaf is black. Ok.
		//4. If a node is red, then both of its children are black. (Hence no two reds in a row on a simple path from the root to a leaf.) Violation if p[y] and x are both red.
		//5. For each node, all paths from the node to descendant leaves contain the same number of black nodes. Any path containing y now has 1 fewer black node.
		//5. 5.1 correct by giving x an "extra black".
		//5. 5.2 Add 1 to count of black nodes on paths containing x.
		//5. 5.3 Now property 5 is Ok, but property 1 is not.
		//5. 5.4 x is either doubly black (if color[x] = BLACK) or red&black (if color[x] = RED) .
		//5. 5.4 The attribute color [x] is still either RED or BLACK. No new values for color attribute.
		//5. 5.4 In other words, the extra blackness on a node is by virtue of x pointing to the node.		
		if (y.getColor()==Commons.BLACK){
			intervalTreeDeleteFixUp(tree,x);
		}
		
		
		
		//The node y is returned so that calling procedure can recyle it via the free list.
		return y;
		
	}
	
	public void  intervalTreeInsert(IntervalTree tree, IntervalTreeNode z){
		//Increment the number of nodes by one
		tree.setNumberofNodes(tree.getNumberofNodes()+1);
				
		//Increase the number of non overlapping bases by size of the inserted node z
		tree.setNumberofNonOverlappingBases(tree.getNumberofNonOverlappingBases()+z.getNumberofBases());
		
		
		//Set y to SENTINEL node
		IntervalTreeNode y = new IntervalTreeNode();
		//Set x to the root
		IntervalTreeNode x = tree.getRoot();
		
//		This while sets the parent for the new inserted node z
		while(!(Commons.SENTINEL.equals(x.getName()))){
			y = x;
			if (z.getLow()< x.getLow())
				x = x.getLeft();
			else 
				x = x.getRight();
		}
		
		z.setParent(y);
		
//		This part sets whether the new inserted node is the left or right child of parent
		if (Commons.SENTINEL.equals(y.getName())){//enters for the first insert
			tree.setRoot(z);
		} else{
			if (z.getLow()<y.getLow()){
				y.setLeft(z);
			}else{
				y.setRight(z);
			}			
		}
		
//		sets the left right color attributes of the new inserted node
		z.setLeft(new IntervalTreeNode());
		z.setRight(new IntervalTreeNode());
		z.setColor(Commons.RED);
		z.setMax(z.getHigh());
		z.setMin(z.getLow());
		z.setHeight(z.getParent().getHeight()+1);
		
		updateMaxAttribute(z.getParent());
		updateMinAttribute(z.getParent());
		
		intervalTreeInsertFixUp(tree,z);
		
		
		
	}
	
	
	public void intervalTreeInfixTraversal(IntervalTreeNode node){
		
		if (node.getLeft().isNotSentinel())
			intervalTreeInfixTraversal(node.getLeft());
		
		if (node.isNotSentinel()){
			System.out.println(node.getLow() + "\t"+ node.getHigh() + "\t" + node.getMax() + "\t" + node.getColor());
		}
		
		if (node.getRight().isNotSentinel())
			intervalTreeInfixTraversal(node.getRight());
				
	}
	
	
	public void intervalTreeInfixTraversal(IntervalTreeNode node,BufferedWriter bufferedWriter){
		
		if (node.getLeft().isNotSentinel())
			intervalTreeInfixTraversal(node.getLeft(),bufferedWriter);
		
		try {
			
			if (node.isNotSentinel()){				
				bufferedWriter.write(node.getLow() + "\t"+ node.getHigh() + "\t" + node.getMax()+ "\n");
				bufferedWriter.flush();
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (node.getRight().isNotSentinel())
			intervalTreeInfixTraversal(node.getRight(),bufferedWriter);
		
				
	}
	
	public void intervalTreeInfixTraversal(IntervalTreeNode node,BufferedWriter bufferedWriter, String type){
		
		if (node.getLeft().isNotSentinel())
			intervalTreeInfixTraversal(node.getLeft(),bufferedWriter,type);
		
		try {
			if (node.isNotSentinel()){
				
				if (Commons.DNASE.equals(type)){
					bufferedWriter.write(node.getChromName()+ "\t" + node.getLow()+"\t"+ node.getHigh() +"\t" + node.getCellLineName()+"\t" + node.getFileName()+"\n");																
				}else if (Commons.TFBS.equals(type)){
					bufferedWriter.write(node.getChromName()+ "\t" + node.getLow()+"\t"+ node.getHigh()+"\t" + node.getTfbsorHistoneName()+"\t" + node.getCellLineName()+"\t" + node.getFileName()+"\n");												
				}else if (Commons.HISTONE.equals(type)){
					bufferedWriter.write(node.getChromName()+ "\t" + node.getLow()+"\t"+ node.getHigh()+"\t" + node.getTfbsorHistoneName()+"\t" + node.getCellLineName()+"\t" + node.getFileName()+"\n");												
				}else if(Commons.UCSC_GENE.equals(type)){
					bufferedWriter.write(node.getChromName()+ "\t" + node.getLow()+"\t"+ node.getHigh()+"\t" + node.getRefSeqGeneName()+ "\t" + node.getGeneEntrezId()+ "\t" + node.getIntervalName()+ "\t" + node.getStrand() + "\t" + node.getGeneHugoSymbol()+ "\n");
//					bufferedWriter.write(refSeqGeneIntervalList.get(j).getChromName()+ "\t" +refSeqGeneIntervalList.get(j).getIntervalStart()+"\t"+ refSeqGeneIntervalList.get(j).getIntervalEnd()+"\t" +refSeqGeneIntervalList.get(j).getRefSeqGeneName()+ "\t" + refSeqGeneIntervalList.get(j).getGeneId()+ "\t" +refSeqGeneIntervalList.get(j).getIntervalName()+ "\t" + refSeqGeneIntervalList.get(j).getStrand() + "\t" + refSeqGeneIntervalList.get(j).getAlternateGeneName()+ "\n");					
				}else if (Commons.PROCESS_INPUT_DATA_REMOVE_OVERLAPS.equals(type)){
					bufferedWriter.write(node.getChromName()+ "\t" + node.getLow()+"\t"+ node.getHigh() +  "\n");

				}
				bufferedWriter.flush();
			}
				
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (node.getRight().isNotSentinel())
			intervalTreeInfixTraversal(node.getRight(),bufferedWriter,type);
		
				
	}
	
	public static boolean overlaps(int low_x, int high_x, int low_y, int high_y ){
		if (( low_x <= high_y) && (low_y <= high_x))
			return true;
		else 
			return false;
	}
	

	//For debug purposes
	public static void findGivenNode(IntervalTreeNode node, int low, int high, List<IntervalTreeNode> foundNodes){
		//exact node
		if (node.getLow() == low && node.getHigh() == high){
			//Found
			foundNodes.add(node);
		}
		
		//partial overlap possibility
		if(node.getLeft().isNotSentinel() && node.getLeft().getMin()<= high && low <= node.getLeft().getMax()) {
			findGivenNode(node.getLeft(), low, high,foundNodes);
		}
		
		//partial overlap possibility
		if(node.getRight().isNotSentinel() && node.getRight().getMin()<= high && low <= node.getRight().getMax()) {
			findGivenNode(node.getRight(), low, high,foundNodes);
		}
	 
		
	}
	
	
	//finds the left most node in the previous non empty node list
	public static IntervalTreeNode findLeftMostNodefromPreviousQuery(List<IntervalTreeNode> previousNonEmptyOverlappingNodeList){
			 IntervalTreeNode leftMostNode = previousNonEmptyOverlappingNodeList.get(0);
			  
			 for(IntervalTreeNode node: previousNonEmptyOverlappingNodeList){
				
				 if (node.getLow()< leftMostNode.getLow()){
					 leftMostNode = node;
				 }
			 }	 
			 
			return leftMostNode;
		 
	}
	
	//Go up in the interval tree for the new query 
	public static IntervalTreeNode findMostGeneralSearchStaringNodeforNewQuery(Interval interval, IntervalTreeNode leftMostNode){
			//For OCD GWAS taking right most node did not work
			//Test for POSITIVE CONTROL DATA
			
			IntervalTreeNode parent = leftMostNode;
			IntervalTreeNode previousParent;
			
			previousParent = parent;
			parent = parent.getParent();
			
			//Exit the loop when parent.getLow()>interval.getHigh()
			while( 	(parent.isNotSentinel()) &&
					(parent.getLow()<=interval.getHigh()) )
			    {
				previousParent = parent;
				parent = parent.getParent();
				
			}
			
			return previousParent;
		}
	
	
	//Go up in the interval tree for the new query 
	public static IntervalTreeNode findMostGeneralSearchStaringNodeforNewQuery(Interval interval, IntervalTreeNode leftMostNode, IntervalTreeNode rightMostNode){
		//For OCD GWAS taking right most node did not work
		//Test for POSITIVE CONTROL DATA
		
		IntervalTreeNode parent = leftMostNode;
		IntervalTreeNode previousParent;
		
		previousParent = parent;
		parent = parent.getParent();
		
		//Exit the loop when parent.getLow()>interval.getHigh()
		while( 	(parent.isNotSentinel()) &&
				(parent.getLow()<=interval.getHigh()) )
		    {
			previousParent = parent;
			parent = parent.getParent();
			
		}
		
		return previousParent;
	}
	
	//Go down in the interval tree for the new query
	public static IntervalTreeNode findMostSpecificSearchStaringNodeforNewQuery(Interval interval,IntervalTreeNode node){
		//if there is possibility of overlap between interval and node's children
		//or overlaps with the node
		if		(((node.getLeft().isNotSentinel()) && (node.getRight().isNotSentinel()) &&
				((node.getLeft().getMin() <= interval.getHigh())  && (interval.getLow()<= node.getLeft().getMax())) &&
				((node.getRight().getMin() <= interval.getHigh())  && (interval.getLow()<= node.getRight().getMax())))
				 || 
				 (node.getLow()<= interval.getHigh() && interval.getLow() <= node.getHigh()))
		{
				return node;
		}else if(node.getLeft().isNotSentinel() &&
				((node.getLeft().getMin() <= interval.getHigh())  && (interval.getLow()<= node.getLeft().getMax()))){
				return findMostSpecificSearchStaringNodeforNewQuery(interval,node.getLeft());
		}else if(node.getRight().isNotSentinel() &&
				((node.getRight().getMin() <= interval.getHigh())  && (interval.getLow()<= node.getRight().getMax()))){
				return findMostSpecificSearchStaringNodeforNewQuery(interval,node.getRight());
		}
		//no way out
		//does not overlap with the node and its children
		else 
			return new IntervalTreeNode();
		
	}
	
	public static void writeRouteFromRoottoThisNode(IntervalTreeNode node){
		IntervalTreeNode parent;
		
		if (node.isNotSentinel()){
			parent = node.getParent();
			
			if (parent.isNotSentinel()){
				if (parent.getLeft()==node){
					writeRouteFromRoottoThisNode(node.getParent());
					System.out.println("Left");
				}else if (parent.getRight()==node){
					writeRouteFromRoottoThisNode(node.getParent());
					System.out.println("Right");
				}
			}
		}
		
	}
	
	public static void printRouteFromRoottoThisNode(IntervalTreeNode root, int low, int high, List<IntervalTreeNode> foundNodes){
		if (root.isNotSentinel()){
			findGivenNode(root,low, high,foundNodes);	
		}
		
		for(IntervalTreeNode foundNode: foundNodes){
			System.out.println("Height: "+ foundNode.height);
			writeRouteFromRoottoThisNode(foundNode);
		}
		
	}
	
	//Normal
	public void findAllOverlappingIntervals(IntervalTreeNode node, Interval interval){
		if (node.isNotSentinel()){
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
					System.out.println("overlap" + node.getLow() + "\t" + node.getHigh());
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax())){
				findAllOverlappingIntervals(node.getLeft(),interval);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getLow()<=interval.getHigh())){
				findAllOverlappingIntervals(node.getRight(),interval);	
				
			}
		}		
	}
	
	
	//Normal: for calculation of non overlapping base pairs in whole genome using interval tree
	public void findAllOverlappingIntervals(List<IntervalTreeNode> overlappedNodeList, IntervalTreeNode root, IntervalTreeNode newNode){
		
		if (root.isNotSentinel() && newNode.isNotSentinel()){
			if (overlaps(root.getLow(), root.getHigh(), newNode.getLow(), newNode.getHigh())){
				overlappedNodeList.add(root);
//				System.out.println("overlap " + root.getLow() + "\t" + root.getHigh());
			}
			
						
			if((root.getLeft().isNotSentinel()) && (newNode.getLow()<=root.getLeft().getMax())){
				findAllOverlappingIntervals(overlappedNodeList,root.getLeft(),newNode);	
			}
			
			if((root.getRight().isNotSentinel()) && (newNode.getLow()<=root.getRight().getMax()) && (root.getLow()<=newNode.getHigh())){
				findAllOverlappingIntervals(overlappedNodeList,root.getRight(),newNode);	
				
			}
		}
		
		
	}
	
		//Normal
		//First argument is the root of the interval tree
	    //Second argument is the node that is looked for whether it overlaps with any node in the interval tree
		public IntervalTreeNode findFirstOverlappingIntervals(IntervalTreeNode root, IntervalTreeNode newNode){
			
			IntervalTreeNode overlappedNode=null;
			
			if (root.isNotSentinel()){
				if (overlaps(root.getLow(), root.getHigh(), newNode.getLow(), newNode.getHigh())){
//						System.out.println(root.getLow() + "\t" + root.getHigh());						
						return root;
						
				}else{
					if((root.getLeft().isNotSentinel()) && (newNode.getLow()<=root.getLeft().getMax())){
						overlappedNode = findFirstOverlappingIntervals(root.getLeft(),newNode);
						if (overlappedNode!=null)
							return overlappedNode;
					}
					
					if((root.getRight().isNotSentinel()) && (newNode.getLow()<=root.getRight().getMax()) && (root.getLow()<=newNode.getHigh())){
						overlappedNode = findFirstOverlappingIntervals(root.getRight(),newNode);	
						if (overlappedNode!=null)
							return overlappedNode;
					}
				}
								
			}
				
			return null;
			
			
			
	}
			
			
	
	//Search1
	public void findAllOverlappingHistoneIntervals(IntervalTreeNode node, Interval interval, BufferedWriter bufferedWriter, List<IntervalTreeNode> overlappingNodeList){
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				
				overlappingNodeList.add(node);
				
				try {
					bufferedWriter.write("histone" + "\t" + node.getChromName().toString()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" +node.getTfbsorHistoneName().toString()+ "\t" + node.getCellLineName().toString() + "\t" + node.getFileName().toString() +"\n");
					bufferedWriter.flush();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<=interval.getHigh())){
				findAllOverlappingHistoneIntervals(node.getLeft(),interval,bufferedWriter,overlappingNodeList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingHistoneIntervals(node.getRight(),interval,bufferedWriter,overlappingNodeList);	
				
			}
				
	}
	
	
	//Empirical P Value Calculation
	//with IO
	public void findAllOverlappingHistoneIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, Map<String,Integer> permutationNumberHistoneNameCellLineName2ZeroorOneMap){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		File directory = null;
		
		int repeatNumberReflectedPermutationNumber = ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber;

		String permutationNumberHistoneNameaCellLineName;
		

			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				try {			
					permutationNumberHistoneNameaCellLineName =  Commons.PERMUTATION + repeatNumberReflectedPermutationNumber + "_" + node.getTfbsorHistoneName() + "_" + node.getCellLineName();
					
					bufferedWriter = bufferedWriterHashMap.get(permutationNumberHistoneNameaCellLineName);
					
					if (bufferedWriter==null){

						directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_HISTONE + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
						if (!directory.exists()) {
							if (!directory.mkdir()){
								System.out.println("Failed to create directory!");
							}
						}
						
					
						fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_HISTONE + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\"+ permutationNumberHistoneNameaCellLineName + ".txt",true);
						bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriterHashMap.put(permutationNumberHistoneNameaCellLineName,bufferedWriter);
					}
					
					if(permutationNumberHistoneNameCellLineName2ZeroorOneMap.get(permutationNumberHistoneNameaCellLineName)==null){
						permutationNumberHistoneNameCellLineName2ZeroorOneMap.put(permutationNumberHistoneNameaCellLineName, 1);
					}
										
					bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "histone" + "\t" + node.getChromName()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getTfbsorHistoneName()+ "\t" + String.valueOf(node.getCellLineName()) + "\t" + node.getFileName() +"\n");
					bufferedWriter.flush();
									

				} catch (IOException e) {
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingHistoneIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName,bufferedWriterHashMap,permutationNumberHistoneNameCellLineName2ZeroorOneMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingHistoneIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,bufferedWriterHashMap,permutationNumberHistoneNameCellLineName2ZeroorOneMap);	
				
			}
				
	}


	//Empirical P Value Calculation
	//without IO
	//without overlappedNodeList
	public void findAllOverlappingHistoneIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,Integer> permutationNumberHistoneNameCellLineName2ZeroorOneMap){
		String permutationNumberHistoneNameaCellLineName;
		

			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				
					
					permutationNumberHistoneNameaCellLineName =  Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber) + "_" + node.getTfbsorHistoneName() + "_" + node.getCellLineName();
												
					if(permutationNumberHistoneNameCellLineName2ZeroorOneMap.get(permutationNumberHistoneNameaCellLineName)==null){
						permutationNumberHistoneNameCellLineName2ZeroorOneMap.put(permutationNumberHistoneNameaCellLineName, 1);
					}																				
			}
						
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingHistoneIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName,permutationNumberHistoneNameCellLineName2ZeroorOneMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingHistoneIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,permutationNumberHistoneNameCellLineName2ZeroorOneMap);	
				
			}
			
	}
	//Empirical P Value Calculation
	//without IO
	//with overlappedNodeList
	public void findAllOverlappingHistoneIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,Integer> permutationNumberHistoneNameCellLineName2ZeroorOneMap,List<IntervalTreeNode> overlappingNodeList){
		String permutationNumberHistoneNameaCellLineName;
		

			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				
					overlappingNodeList.add(node);
					
					permutationNumberHistoneNameaCellLineName =  Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber) + "_" + node.getTfbsorHistoneName() + "_" + node.getCellLineName();
												
					if(permutationNumberHistoneNameCellLineName2ZeroorOneMap.get(permutationNumberHistoneNameaCellLineName)==null){
						permutationNumberHistoneNameCellLineName2ZeroorOneMap.put(permutationNumberHistoneNameaCellLineName, 1);
					}																				
			}
						
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingHistoneIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName,permutationNumberHistoneNameCellLineName2ZeroorOneMap,overlappingNodeList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingHistoneIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,permutationNumberHistoneNameCellLineName2ZeroorOneMap,overlappingNodeList);	
				
			}
			
	}
	
	//Search2 For finding the number of each histoneNameandCellLineName: k for the given search input size: n
	//For each search input line, each histoneNameandCellLineName will have value 1 or 0
	//These 1 or 0's will be accumulated in histoneNameandCellLineName2KMap		
	public void findAllOverlappingHistoneIntervals(IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, List<String> histoneNameList, Map<String,Integer> histoneNameandCellLineName2ZeroorOneMap){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		String histoneNameandCellLine = node.getTfbsorHistoneName() + "_" + node.getCellLineName();
		

			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh()) && histoneNameList.contains(node.getTfbsorHistoneName())){
				try {			
					
					bufferedWriter = bufferedWriterHashMap.get(histoneNameandCellLine);
					
					if (bufferedWriter==null){						
						fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_HISTONE +"_" + histoneNameandCellLine + ".txt");
						bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriterHashMap.put(histoneNameandCellLine,bufferedWriter);
						bufferedWriter.write("Searched for chr" + "\t"  + "interval low" + "\t" + "interval high" + "\t" + "histone node chrom name" + "\t"  + "node Low" + "\t" + "node high" + "\t" + "node Histone Name" + "\t" + "node CellLineName" + "\t" + "node FileName" +"\n");
						
					}
					
					if(histoneNameandCellLineName2ZeroorOneMap.get(histoneNameandCellLine)==null){
						histoneNameandCellLineName2ZeroorOneMap.put(histoneNameandCellLine, 1);
					}
										
					bufferedWriter.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + node.getChromName()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getTfbsorHistoneName()+ "\t" + String.valueOf(node.getCellLineName()) + "\t" + node.getFileName() +"\n");
					bufferedWriter.flush();
									

				} catch (IOException e) {
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax())&& (node.getLeft().getMin() <= interval.getHigh())){
				findAllOverlappingHistoneIntervals(node.getLeft(),interval,chromName,bufferedWriterHashMap, histoneNameList,histoneNameandCellLineName2ZeroorOneMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingHistoneIntervals(node.getRight(),interval,chromName,bufferedWriterHashMap,histoneNameList,histoneNameandCellLineName2ZeroorOneMap);	
				
			}
				
	}
	
	
	//Empirical P Value Calculation
	//with IO
	//with OverlapNodeList
	public void findAllOverlappingTfbsIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, Map<String,Integer> permutationNumberTfbsNameCellLineName2ZeroorOneMap,List<PermutationNumberTfNameCellLineNameOverlap> 	permutationNumberTfNameCellLineNameOverlapList){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		File directory;
		
		int repeatNumberReflectedPermutationNumber = ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber;

		String permutationNumberTfNameCellLineName;
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				try {
						
					
					permutationNumberTfNameCellLineName = Commons.PERMUTATION + repeatNumberReflectedPermutationNumber+ "_" + node.getTfbsorHistoneName() + "_" + node.getCellLineName();
					
					permutationNumberTfNameCellLineNameOverlapList.add(new PermutationNumberTfNameCellLineNameOverlap(permutationNumberTfNameCellLineName,node.getLow(),node.getHigh()));
					
					bufferedWriter = bufferedWriterHashMap.get(permutationNumberTfNameCellLineName);
					
					if (bufferedWriter==null){	
						directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_TFBS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
						if (!directory.exists()) {
							if (!directory.mkdir()){
								System.out.println("Failed to create directory!");
							}
						}
						
					fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_TFBS + Commons.PERMUTATION + repeatNumberReflectedPermutationNumber+ "\\" + permutationNumberTfNameCellLineName + ".txt",true);
					bufferedWriter = new BufferedWriter(fileWriter);
					bufferedWriterHashMap.put(permutationNumberTfNameCellLineName,bufferedWriter);
					}
					
					if(permutationNumberTfbsNameCellLineName2ZeroorOneMap.get(permutationNumberTfNameCellLineName)==null){
						permutationNumberTfbsNameCellLineName2ZeroorOneMap.put(permutationNumberTfNameCellLineName, 1);
					}
					
					bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() +"\t" + "tfbs" + "\t" + node.getChromName()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getTfbsorHistoneName()+ "\t" + node.getCellLineName() + "\t" + node.getFileName() +"\n");
					bufferedWriter.flush();
					
					

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName, bufferedWriterHashMap,permutationNumberTfbsNameCellLineName2ZeroorOneMap,permutationNumberTfNameCellLineNameOverlapList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName, bufferedWriterHashMap,permutationNumberTfbsNameCellLineName2ZeroorOneMap,permutationNumberTfNameCellLineNameOverlapList);	
				
			}		
	}
		
	
	//Empirical P Value Calculation
	//with IO
	public void findAllOverlappingTfbsIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, Map<String,Integer> permutationNumberTfbsNameCellLineName2ZeroorOneMap){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		File directory;
		
		int repeatNumberReflectedPermutationNumber = ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber;

		String permutationNumberTfbsNameCellLineName;
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				try {
						
					
					permutationNumberTfbsNameCellLineName = Commons.PERMUTATION + repeatNumberReflectedPermutationNumber+ "_" + node.getTfbsorHistoneName() + "_" + node.getCellLineName();
					
					bufferedWriter = bufferedWriterHashMap.get(permutationNumberTfbsNameCellLineName);
					
					if (bufferedWriter==null){	
						directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_TFBS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
						if (!directory.exists()) {
							if (!directory.mkdir()){
								System.out.println("Failed to create directory!");
							}
						}
						
					fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_TFBS + Commons.PERMUTATION + repeatNumberReflectedPermutationNumber+ "\\" + permutationNumberTfbsNameCellLineName + ".txt",true);
					bufferedWriter = new BufferedWriter(fileWriter);
					bufferedWriterHashMap.put(permutationNumberTfbsNameCellLineName,bufferedWriter);
					}
					
					if(permutationNumberTfbsNameCellLineName2ZeroorOneMap.get(permutationNumberTfbsNameCellLineName)==null){
						permutationNumberTfbsNameCellLineName2ZeroorOneMap.put(permutationNumberTfbsNameCellLineName, 1);
					}
					
					bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() +"\t" + "tfbs" + "\t" + node.getChromName()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getTfbsorHistoneName()+ "\t" + node.getCellLineName() + "\t" + node.getFileName() +"\n");
					bufferedWriter.flush();
					
					

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName, bufferedWriterHashMap,permutationNumberTfbsNameCellLineName2ZeroorOneMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName, bufferedWriterHashMap,permutationNumberTfbsNameCellLineName2ZeroorOneMap);	
				
			}
		
	}

    	//Empirical P Value Calculation
		//without IO
		//without overlappedNodeList
		public void findAllOverlappingTfbsIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,Integer> permutationNumberTfbsNameCellLineName2ZeroorOneMap){
			String permutationNumberTfbsNameCellLineName;
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				
					
				permutationNumberTfbsNameCellLineName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + node.getTfbsorHistoneName() + "_" + node.getCellLineName();			
					
				if(permutationNumberTfbsNameCellLineName2ZeroorOneMap.get(permutationNumberTfbsNameCellLineName)==null){
					permutationNumberTfbsNameCellLineName2ZeroorOneMap.put(permutationNumberTfbsNameCellLineName, 1);
				}
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName,permutationNumberTfbsNameCellLineName2ZeroorOneMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,permutationNumberTfbsNameCellLineName2ZeroorOneMap);	
				
			}
			
		}

//		//Empirical P Value Calculation
//		//without IO
//		//with overlappedNodeList
//		public void findAllOverlappingTfbsIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,Integer> permutationNumberTfbsNameCellLineName2ZeroorOneMap,List<IntervalTreeNode> overlappingNodeList){
//			String permutationNumberTfbsNameCellLineName;
//			
//				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
//					
//					overlappingNodeList.add(node);
//						
//					permutationNumberTfbsNameCellLineName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + node.getTfbsorHistoneName() + "_" + node.getCellLineName();			
//						
//					if(permutationNumberTfbsNameCellLineName2ZeroorOneMap.get(permutationNumberTfbsNameCellLineName)==null){
//						permutationNumberTfbsNameCellLineName2ZeroorOneMap.put(permutationNumberTfbsNameCellLineName, 1);
//					}
//				}
//				
//				
//				if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
//					findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName,permutationNumberTfbsNameCellLineName2ZeroorOneMap,overlappingNodeList);	
//				}
//				
//				if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
//					findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,permutationNumberTfbsNameCellLineName2ZeroorOneMap,overlappingNodeList);	
//					
//				}
//				
//		}

		
		//Empirical P Value Calculation
		//without IO
		//with permutationNumberTfNameCellLineNameOverlapList
		public void findAllOverlappingTfbsIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,Integer> permutationNumberTfbsNameCellLineName2ZeroorOneMap,List<PermutationNumberTfNameCellLineNameOverlap> permutationNumberTfNameCellLineNameOverlapList){
			
			String permutationNumberTfNameCellLineName;
			
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
					
						
					permutationNumberTfNameCellLineName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + node.getTfbsorHistoneName() + "_" + node.getCellLineName();			
					
					permutationNumberTfNameCellLineNameOverlapList.add(new PermutationNumberTfNameCellLineNameOverlap(permutationNumberTfNameCellLineName,node.getLow(),node.getHigh()));
					
					if(permutationNumberTfbsNameCellLineName2ZeroorOneMap.get(permutationNumberTfNameCellLineName)==null){
						permutationNumberTfbsNameCellLineName2ZeroorOneMap.put(permutationNumberTfNameCellLineName, 1);
					}
				}
				
				
				if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
					findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName,permutationNumberTfbsNameCellLineName2ZeroorOneMap,permutationNumberTfNameCellLineNameOverlapList);	
				}
				
				if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
					findAllOverlappingTfbsIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,permutationNumberTfbsNameCellLineName2ZeroorOneMap,permutationNumberTfNameCellLineNameOverlapList);	
					
				}
				
		}
	
	//Search2 For finding the number of each tfbs:k for the given search input size: n
	//For each search input line, each tfbs will have value 1 or 0
	//These 1 or 0's will be accumulated in tfbs2KMap
	public void findAllOverlappingTfbsIntervals(IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, List<String> tfbsNameList, Map<String,Integer> tfbsNameandCellLineName2ZeroorOneMap){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		String tfbsNameandCellLineName = node.getTfbsorHistoneName() + "_" + node.getCellLineName();
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh()) && tfbsNameList.contains(node.getTfbsorHistoneName())){
				try {
										
					bufferedWriter = bufferedWriterHashMap.get(tfbsNameandCellLineName);
					
					if (bufferedWriter==null){						
						fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_TFBS +"_" + tfbsNameandCellLineName + ".txt");
						bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriterHashMap.put(tfbsNameandCellLineName,bufferedWriter);
					}
					
					if(tfbsNameandCellLineName2ZeroorOneMap.get(tfbsNameandCellLineName)==null){
						tfbsNameandCellLineName2ZeroorOneMap.put(tfbsNameandCellLineName, 1);
					}
					
					bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() +"\t" + "tfbs" + "\t" + node.getChromName()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getTfbsorHistoneName()+ "\t" + node.getCellLineName() + "\t" + node.getFileName() +"\n");
					bufferedWriter.flush();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin() <= interval.getHigh())){
				findAllOverlappingTfbsIntervals(node.getLeft(),interval,chromName, bufferedWriterHashMap,tfbsNameList,tfbsNameandCellLineName2ZeroorOneMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingTfbsIntervals(node.getRight(),interval,chromName, bufferedWriterHashMap,tfbsNameList,tfbsNameandCellLineName2ZeroorOneMap);	
				
			}		
	}

	
	//New Functionality added
	//Search2 For finding the number of each tfbs:k for the given search input size: n
	//For each search input line, each tfbs will have value 1 or 0
	//These 1 or 0's will be accumulated in tfbs2KMap
	public void findAllOverlappingTfbsIntervals(IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, List<String> tfbsNameList, Map<String,Integer> tfbsNameandCellLineName2ZeroorOneMap, List<TfNameandCellLineNameOverlap> tfandCellLineOverlapList){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		String tfbsNameandCellLineName = node.getTfbsorHistoneName() + "_" + node.getCellLineName();
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh()) && tfbsNameList.contains(node.getTfbsorHistoneName())){
				try {
					
					bufferedWriter = bufferedWriterHashMap.get(tfbsNameandCellLineName);
					
					if (bufferedWriter==null){						
						fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_TFBS +"_" + tfbsNameandCellLineName + ".txt");
						bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriterHashMap.put(tfbsNameandCellLineName,bufferedWriter);
						bufferedWriter.write("Searched for chr" + "\t" + "interval Low" + "\t" + "interval High" +"\t" + "tfbs node Chrom Name"+ "\t"  + "node Low" + "\t" + "node High" + "\t" + "node Tfbs Name" + "\t" + "node CellLineName" + "\t" + "node FileName" +"\n");
						
					}
					
					if(tfbsNameandCellLineName2ZeroorOneMap.get(tfbsNameandCellLineName)==null){
						tfbsNameandCellLineName2ZeroorOneMap.put(tfbsNameandCellLineName, 1);
					}
					
					bufferedWriter.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + node.getChromName()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getTfbsorHistoneName()+ "\t" + node.getCellLineName() + "\t" + node.getFileName() +"\n");
					bufferedWriter.flush();
					
					tfandCellLineOverlapList.add(new TfNameandCellLineNameOverlap(tfbsNameandCellLineName,node.getLow(),node.getHigh()));

					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin() <= interval.getHigh())){
				findAllOverlappingTfbsIntervals(node.getLeft(),interval,chromName, bufferedWriterHashMap,tfbsNameList,tfbsNameandCellLineName2ZeroorOneMap,tfandCellLineOverlapList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingTfbsIntervals(node.getRight(),interval,chromName, bufferedWriterHashMap,tfbsNameList,tfbsNameandCellLineName2ZeroorOneMap,tfandCellLineOverlapList);	
			}		
	}
	//New Functionality added
	

	
	
	//Search1
	public void findAllOverlappingTfbsIntervals(IntervalTreeNode node, Interval interval, BufferedWriter bufferedWriter,List<IntervalTreeNode> overlappedNodeList){
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				
				overlappedNodeList.add(node);
				
				try {
					bufferedWriter.write("tfbs" + "\t" + node.getChromName().toString()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" +node.getTfbsorHistoneName().toString()+ "\t" + node.getCellLineName().toString() + "\t" + node.getFileName().toString() +"\n");
					bufferedWriter.flush();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<=interval.getHigh())){
				findAllOverlappingTfbsIntervals(node.getLeft(),interval,bufferedWriter,overlappedNodeList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingTfbsIntervals(node.getRight(),interval,bufferedWriter,overlappedNodeList);	
				
			}
				
	}
	
	//Search1
	public void findAllOverlappingDnaseIntervals(IntervalTreeNode node, Interval interval, BufferedWriter bufferedWriter, List<IntervalTreeNode> overlappedNodeList){
			
		if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				
			overlappedNodeList.add(node);
				
				try {
					bufferedWriter.write("dnase" + "\t" + node.getChromName().toString()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getCellLineName().toString() + "\t" + node.getFileName().toString() +"\n");
					bufferedWriter.flush();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<=interval.getHigh())){
				findAllOverlappingDnaseIntervals(node.getLeft(),interval,bufferedWriter,overlappedNodeList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingDnaseIntervals(node.getRight(),interval,bufferedWriter,overlappedNodeList);		
			}
							
	}

	
	//Empirical P Value Calculation
	//with IO
	public void findAllOverlappingDnaseIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, Map<String,Integer> permutationNumberDnaseCellLineName2ZeroorOneMap){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		File directory = null;
		String permutationNumberDnaseCellLineName;
		
		int repeatNumberReflectedPermutationNumber = ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber;
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				try {
					
					permutationNumberDnaseCellLineName = Commons.PERMUTATION + repeatNumberReflectedPermutationNumber + "_" + node.getCellLineName();
					
					bufferedWriter = bufferedWriterHashMap.get(permutationNumberDnaseCellLineName);
					
					if (bufferedWriter==null){
						directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_DNASE + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber + "\\");
						if (!directory.exists()) {
							if (!directory.mkdir()){
								System.out.println("Failed to create directory!");
							}
						}
													
						fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_DNASE + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber + "\\"+ permutationNumberDnaseCellLineName + ".txt",true);
						bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriterHashMap.put(permutationNumberDnaseCellLineName,bufferedWriter);
					}										
					
					if(permutationNumberDnaseCellLineName2ZeroorOneMap.get(permutationNumberDnaseCellLineName)==null){
						permutationNumberDnaseCellLineName2ZeroorOneMap.put(permutationNumberDnaseCellLineName, 1);
					}
					
					bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh()+ "\t" + "dnase" + "\t" + node.getChromName()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getCellLineName() + "\t" + node.getFileName() +"\n");
					bufferedWriter.flush();	
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingDnaseIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName,bufferedWriterHashMap, permutationNumberDnaseCellLineName2ZeroorOneMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingDnaseIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,bufferedWriterHashMap,permutationNumberDnaseCellLineName2ZeroorOneMap);	
				
			}
						
	}
	

		//Empirical P Value Calculation
		//without IO
		//without overlappedNodeList
		public void findAllOverlappingDnaseIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,Integer> permutationNumberDnaseCellLineName2ZeroorOneMap){
		String permutationNumberDnaseCellLineName;
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
						
					permutationNumberDnaseCellLineName = Commons.PERMUTATION + ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS+permutationNumber) + "_" + node.getCellLineName();
									
					if(permutationNumberDnaseCellLineName2ZeroorOneMap.get(permutationNumberDnaseCellLineName)==null){
						permutationNumberDnaseCellLineName2ZeroorOneMap.put(permutationNumberDnaseCellLineName, 1);
					}
			}//End of IF OVERLAPS
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingDnaseIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName, permutationNumberDnaseCellLineName2ZeroorOneMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingDnaseIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,permutationNumberDnaseCellLineName2ZeroorOneMap);	
				
			}
							
		}
		//Empirical P Value Calculation
		//without IO
		//with overlappedNodeList
		public void findAllOverlappingDnaseIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,Integer> permutationNumberDnaseCellLineName2ZeroorOneMap,List<IntervalTreeNode> overlappingNodeList){
			String permutationNumberDnaseCellLineName;
	
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				
					overlappingNodeList.add(node);
					
					permutationNumberDnaseCellLineName = Commons.PERMUTATION + ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS+permutationNumber) + "_" + node.getCellLineName();
					
					
					if(permutationNumberDnaseCellLineName2ZeroorOneMap.get(permutationNumberDnaseCellLineName)==null){
						permutationNumberDnaseCellLineName2ZeroorOneMap.put(permutationNumberDnaseCellLineName, 1);
					}
			}//End of IF OVERLAPS
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingDnaseIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName, permutationNumberDnaseCellLineName2ZeroorOneMap,overlappingNodeList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingDnaseIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,permutationNumberDnaseCellLineName2ZeroorOneMap,overlappingNodeList);	
				
			}
							
		}

	
	//Search2 For finding the number of each dnase cell line:k for the given search input size: n
	//For each search input line, each dnase cell line will have value 1 or 0
	//These 1 or 0's will be accumulated in dnaseCellLine2KMap		
	public void findAllOverlappingDnaseIntervals(IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, List<String> dnaseCellLineNameList,Map<String,Integer> dnaseCellLine2OneorZeroMap){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh()) && dnaseCellLineNameList.contains(node.getCellLineName())){
				try {
					
										
					bufferedWriter = bufferedWriterHashMap.get(node.getCellLineName());
					
					if (bufferedWriter==null){
						fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_DNASE +"_" + node.getCellLineName() + ".txt");
						bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriterHashMap.put(node.getCellLineName(),bufferedWriter);
						bufferedWriter.write("Searched for chr" + "\t" + "given interval low" + "\t" + 	"given interval high"+ "\t" + "dnase overlap chrom name" + "\t"  + "node low" + "\t" + "node high" + "\t" + "node CellLineName" + "\t" + "node FileName" +"\n");
						
						
					}										
					
					if(dnaseCellLine2OneorZeroMap.get(node.getCellLineName())==null){
						dnaseCellLine2OneorZeroMap.put(node.getCellLineName(), 1);
					}
					
					bufferedWriter.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh()+ "\t" + node.getChromName()+ "\t"  + node.getLow() + "\t" + node.getHigh() + "\t" + node.getCellLineName() + "\t" + node.getFileName() +"\n");
					bufferedWriter.flush();	
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin() <= interval.getHigh())){
				findAllOverlappingDnaseIntervals(node.getLeft(),interval,chromName,bufferedWriterHashMap,dnaseCellLineNameList, dnaseCellLine2OneorZeroMap);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingDnaseIntervals(node.getRight(),interval,chromName,bufferedWriterHashMap,dnaseCellLineNameList,dnaseCellLine2OneorZeroMap);	
				
			}					
	}

	
	//Empirical P Value Calculation
	//Search2 KeggPathway
	//For finding the number of each keggpathway:k for the given search input size: n
	//For each search input line, each kegg pathway will have a value of 1 or 0
	//These 1 or 0's will be accumulated in keggPathway2KMap	
	//with IO
	public void findAllOverlappingUcscRefSeqGenesIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, Map<String,List<String>> geneId2KeggPathwayMap, Map<String,Integer> permutationNumberKeggPathway2OneorZeroMap, String type, String keggPathwayAnalysisType){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		File directory;
		
		int repeatNumberReflectedPermutationNumber = ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber;

		String permutationNumberKeggPathwayName = null;
		String keggPathwayName = null;
		List<String> keggPathWayListContainingThisGeneId = null;
		
			
			if (Commons.NCBI_GENE_ID.equals(type)){
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
					try {
												
						//write exon based results
						if (Commons.EXON_BASED_KEGG_PATHWAY_ANALYSIS.equals(keggPathwayAnalysisType)){
							
							//exon based kegg pathway analysis
							if (node.getIntervalName().startsWith(Commons.EXON)){
								
								keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
								
								if(keggPathWayListContainingThisGeneId!=null){
									for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
										keggPathwayName = keggPathWayListContainingThisGeneId.get(i);
										permutationNumberKeggPathwayName = Commons.PERMUTATION + repeatNumberReflectedPermutationNumber+ "_" + keggPathwayName;	
											
										bufferedWriter = bufferedWriterHashMap.get(permutationNumberKeggPathwayName);
										
										if (bufferedWriter == null){
											directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
											if (!directory.exists()) {
												if (!directory.mkdir()){
													System.out.println("Failed to create directory!");
												}
											}
											
											fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\" + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber +"_exonBased_" + keggPathwayName + ".txt",true);
											bufferedWriter = new BufferedWriter(fileWriter);
											bufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
										
											
										}
										
										if(permutationNumberKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
											permutationNumberKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
										}
										
										bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
										bufferedWriter.flush();	
									
										
									}// End of For: for all keggpathways having this gene in their gene list
								} //End of If: keggPathWayListContainingThisGeneId is not null								
							}// End of If: Exon Based Kegg Pathway Analysis, Overlapped node is an exon

						}
						//write regulation based results
						else if (Commons.REGULATION_BASED_KEGG_PATHWAY_ANALYSIS.equals(keggPathwayAnalysisType)){
							//Regulation Based kegg pathway analysis
							if (node.getIntervalName().startsWith(Commons.INTRON) ||
								node.getIntervalName().startsWith(Commons.FIVE_P_ONE) ||
								node.getIntervalName().startsWith(Commons.FIVE_P_TWO) ||
								node.getIntervalName().startsWith(Commons.THREE_P_ONE)||
								node.getIntervalName().startsWith(Commons.THREE_P_TWO)){
								
								keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
								
								if(keggPathWayListContainingThisGeneId!=null){
									for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
										keggPathwayName = keggPathWayListContainingThisGeneId.get(i);
										permutationNumberKeggPathwayName = Commons.PERMUTATION + repeatNumberReflectedPermutationNumber+ "_" + keggPathwayName;	
											
										bufferedWriter = bufferedWriterHashMap.get(permutationNumberKeggPathwayName);
										
										if (bufferedWriter == null){
											directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
											
											if (!directory.exists()) {
												if (!directory.mkdir()){
													System.out.println("Failed to create directory!");
												}
											}
											
											
											fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS  + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\"+ Commons.PERMUTATION + repeatNumberReflectedPermutationNumber + "_regulationBased_" + keggPathwayName + ".txt",true);
											bufferedWriter = new BufferedWriter(fileWriter);
											bufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
										
											
										}
										
										if(permutationNumberKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
											permutationNumberKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
										}
										
										bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
										bufferedWriter.flush();	
											
									}// End of For: for all kegg pathways having this gene in their gene list
								} // End of If:		keggPathWayListContainingThisGeneId is not null					
							}//End of If: Regulation Based kegg pathway Analysis, Overlapped node is an intron, 5P1, 5P2, 3P1, 3P2

						}
						//write all results
						else{
							keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
							
							if(keggPathWayListContainingThisGeneId!=null){
								for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
									keggPathwayName = keggPathWayListContainingThisGeneId.get(i);
									permutationNumberKeggPathwayName = Commons.PERMUTATION + repeatNumberReflectedPermutationNumber+ "_" + keggPathwayName;	
											
									bufferedWriter = bufferedWriterHashMap.get(permutationNumberKeggPathwayName);
									
									if (bufferedWriter == null){
										directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
										if (!directory.exists()) {
											if (!directory.mkdir()){
												System.out.println("Failed to create directory!");
											}
										}
										
										fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\" + Commons.PERMUTATION + repeatNumberReflectedPermutationNumber +"_all_" + keggPathwayName + ".txt",true);
										bufferedWriter = new BufferedWriter(fileWriter);
										bufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
									
										
									}
									
									if(permutationNumberKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
										permutationNumberKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
									}
									
									bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
									bufferedWriter.flush();	
									
									
								}// End of For: for all kegg pathways having this gene in their gene list
							} // End of If:		keggPathWayListContainingThisGeneId is not null	
							
						}
												
						
																
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}	
			} //End of If: type is NCBI_GENE_ID
				
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName,bufferedWriterHashMap, geneId2KeggPathwayMap, permutationNumberKeggPathway2OneorZeroMap,type,keggPathwayAnalysisType);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName,bufferedWriterHashMap, geneId2KeggPathwayMap, permutationNumberKeggPathway2OneorZeroMap,type,keggPathwayAnalysisType);	
				
			}
						
	}

	
	//Empirical P Value Calculation
	//Search2 KeggPathway
	//For finding the number of each keggpathway:k for the given search input size: n
	//For each search input line, each kegg pathway will have a value of 1 or 0
	//These 1 or 0's will be accumulated in keggPathway2KMap	
	//without IO
	public void findAllOverlappingUcscRefSeqGenesIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,List<String>> geneId2KeggPathwayMap, Map<String,Integer> permutationNumberKeggPathway2OneorZeroMap, String type, String keggPathwayAnalysisType){
		String permutationNumberKeggPathwayName = null;
		String keggPathwayName = null;
		List<String> keggPathWayListContainingThisGeneId = null;
		
			
			if (Commons.NCBI_GENE_ID.equals(type)){
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
					
						//write exon based results
						if (Commons.EXON_BASED_KEGG_PATHWAY_ANALYSIS.equals(keggPathwayAnalysisType)){
							
							//exon based kegg pathway analysis
							if (node.getIntervalName().startsWith(Commons.EXON)){
								
								keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
								
								if(keggPathWayListContainingThisGeneId!=null){
									for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
										
										permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathWayListContainingThisGeneId.get(i);	
										keggPathwayName = keggPathWayListContainingThisGeneId.get(i);
											
										
										if(permutationNumberKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
											permutationNumberKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
										}
										
									}// End of For: for all keggpathways having this gene in their gene list
								} //End of If: keggPathWayListContainingThisGeneId is not null								
							}// End of If: Exon Based Kegg Pathway Analysis, Overlapped node is an exon

						}
						//write regulation based results
						else if (Commons.REGULATION_BASED_KEGG_PATHWAY_ANALYSIS.equals(keggPathwayAnalysisType)){
							//Regulation Based kegg pathway analysis
							if (node.getIntervalName().startsWith(Commons.INTRON) ||
								node.getIntervalName().startsWith(Commons.FIVE_P_ONE) ||
								node.getIntervalName().startsWith(Commons.FIVE_P_TWO) ||
								node.getIntervalName().startsWith(Commons.THREE_P_ONE)||
								node.getIntervalName().startsWith(Commons.THREE_P_TWO)){
								
								keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
								
								if(keggPathWayListContainingThisGeneId!=null){
									for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
										permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathWayListContainingThisGeneId.get(i);	
										keggPathwayName = keggPathWayListContainingThisGeneId.get(i);
											
										
										
										if(permutationNumberKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
											permutationNumberKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
										}
										
								
											
									}// End of For: for all kegg pathways having this gene in their gene list
								} // End of If:		keggPathWayListContainingThisGeneId is not null					
							}//End of If: Regulation Based kegg pathway Analysis, Overlapped node is an intron, 5P1, 5P2, 3P1, 3P2

						}//regulation based kegg pathway analysis
						//write all results
						else{
							keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
							
							if(keggPathWayListContainingThisGeneId!=null){
								for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
									permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathWayListContainingThisGeneId.get(i);	
									keggPathwayName = keggPathWayListContainingThisGeneId.get(i);
											
									
									
									if(permutationNumberKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
										permutationNumberKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
									}
									
								
									
								}// End of For: for all kegg pathways having this gene in their gene list
							} // End of If:		keggPathWayListContainingThisGeneId is not null	
							
						}//all kegg pathway analysis
												
						
																
									
				}//End of if: there is overlap	
			} //End of If: type is NCBI_GENE_ID
				
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName, geneId2KeggPathwayMap, permutationNumberKeggPathway2OneorZeroMap,type,keggPathwayAnalysisType);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName, geneId2KeggPathwayMap, permutationNumberKeggPathway2OneorZeroMap,type,keggPathwayAnalysisType);		
			}
						
	}

	//NEW FUNCIONALITY
	//with IO
	//with Overlap Node List
	public void findAllOverlappingUcscRefSeqGenesIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,List<String>> geneId2KeggPathwayMap, Map<String,BufferedWriter> exonBasedKeggPathwayBufferedWriterHashMap, Map<String,BufferedWriter> regulationBasedKeggPathwayBufferedWriterHashMap, Map<String,BufferedWriter> allBasedKeggPathwayBufferedWriterHashMap, Map<String,Integer> permutationNumberExonBasedKeggPathway2OneorZeroMap, Map<String,Integer> permutationNumberRegulationBasedKeggPathway2OneorZeroMap,Map<String,Integer> permutationNumberAllBasedKeggPathway2OneorZeroMap,String type,List<PermutationNumberUcscRefSeqGeneOverlap> permutationNumberExonBasedKeggPathwayOverlapList,List<PermutationNumberUcscRefSeqGeneOverlap> permutationNumberRegulationBasedKeggPathwayOverlapList,List<PermutationNumberUcscRefSeqGeneOverlap> permutationNumberAllBasedKeggPathwayOverlapList){
		String permutationNumberKeggPathwayName = null;
		String keggPathwayName;
		List<String> keggPathwayListContainingThisGeneId = null;
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		File directory;
		
		int repeatNumberReflectedPermutationNumber = ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber;

		
		if (Commons.NCBI_GENE_ID.equals(type)){
			//There is overlap
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				try {
					
					keggPathwayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
				
					//write exon based results
					if (node.getIntervalName().startsWith(Commons.EXON)){							
						if(keggPathwayListContainingThisGeneId!=null){
							
							permutationNumberExonBasedKeggPathwayOverlapList.add(new PermutationNumberUcscRefSeqGeneOverlap(Commons.PERMUTATION + repeatNumberReflectedPermutationNumber,node.getRefSeqGeneName(), node.getIntervalName(), node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathwayListContainingThisGeneId));

							for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
								
								keggPathwayName = keggPathwayListContainingThisGeneId.get(i);											
								permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;	
								
								bufferedWriter = exonBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberKeggPathwayName);
								
								if (bufferedWriter == null){
									directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
									if (!directory.exists()) {
										if (!directory.mkdir()){
											System.out.println("Failed to create directory!");
										}
									}
									
									fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\" + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber +"_exonBased_" + keggPathwayName + ".txt",true);
									bufferedWriter = new BufferedWriter(fileWriter);
									exonBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
								}
								
								
								if(permutationNumberExonBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
									permutationNumberExonBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
								}
								
								bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
								bufferedWriter.flush();	
								
							}// End of For: for all keggpathways having this gene in their gene list
						} //End of If: keggPathWayListContainingThisGeneId is not null								
					}// End of If: Exon Based Kegg Pathway Analysis, Overlapped node is an exon

					
					//write regulation based results
					//Regulation Based kegg pathway analysis
					if (node.getIntervalName().startsWith(Commons.INTRON) ||
						node.getIntervalName().startsWith(Commons.FIVE_P_ONE) ||
						node.getIntervalName().startsWith(Commons.FIVE_P_TWO) ||
						node.getIntervalName().startsWith(Commons.THREE_P_ONE)||
						node.getIntervalName().startsWith(Commons.THREE_P_TWO)){
													
						if(keggPathwayListContainingThisGeneId!=null){
							
							permutationNumberRegulationBasedKeggPathwayOverlapList.add(new PermutationNumberUcscRefSeqGeneOverlap(Commons.PERMUTATION + repeatNumberReflectedPermutationNumber,node.getRefSeqGeneName(), node.getIntervalName(), node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathwayListContainingThisGeneId));
							
							for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
								
								keggPathwayName = keggPathwayListContainingThisGeneId.get(i);
								permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;
								
								bufferedWriter = regulationBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberKeggPathwayName);
								
								if (bufferedWriter == null){
									directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
									if (!directory.exists()) {
										if (!directory.mkdir()){
											System.out.println("Failed to create directory!");
										}
									}
									
									fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\" + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber +"_regulationBased_" + keggPathwayName + ".txt",true);
									bufferedWriter = new BufferedWriter(fileWriter);
									regulationBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
								}
									
								if(permutationNumberRegulationBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
									permutationNumberRegulationBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
								}
						
								bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
								bufferedWriter.flush();	
						
																		
							}// End of For: for all kegg pathways having this gene in their gene list
						} // End of If:		keggPathWayListContainingThisGeneId is not null					
					}//End of If: Regulation Based kegg pathway Analysis, Overlapped node is an intron, 5P1, 5P2, 3P1, 3P2

					
					
					//write all results							
					if(keggPathwayListContainingThisGeneId!=null){
						
						permutationNumberAllBasedKeggPathwayOverlapList.add(new PermutationNumberUcscRefSeqGeneOverlap(Commons.PERMUTATION + repeatNumberReflectedPermutationNumber,node.getRefSeqGeneName(), node.getIntervalName(), node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathwayListContainingThisGeneId));
						
						for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
							
							keggPathwayName = keggPathwayListContainingThisGeneId.get(i);
							permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;
							
							bufferedWriter = allBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberKeggPathwayName);
							
							if (bufferedWriter == null){
								directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
								if (!directory.exists()) {
									if (!directory.mkdir()){
										System.out.println("Failed to create directory!");
									}
								}
								
								fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\" + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber +"_allBased_" + keggPathwayName + ".txt",true);
								bufferedWriter = new BufferedWriter(fileWriter);
								allBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
							}
																		
							if(permutationNumberAllBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
								permutationNumberAllBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
							}
															
						}// End of For: for all kegg pathways having this gene in their gene list
					} // End of If:		keggPathWayListContainingThisGeneId is not null	
					
											
				} catch (IOException e) {
					e.printStackTrace();
				}
															
								
			}//End of if: there is overlap	
		} //End of If: type is NCBI_GENE_ID
			
		if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
			findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName, geneId2KeggPathwayMap, exonBasedKeggPathwayBufferedWriterHashMap,regulationBasedKeggPathwayBufferedWriterHashMap,allBasedKeggPathwayBufferedWriterHashMap,permutationNumberExonBasedKeggPathway2OneorZeroMap,permutationNumberRegulationBasedKeggPathway2OneorZeroMap,permutationNumberAllBasedKeggPathway2OneorZeroMap,type, permutationNumberExonBasedKeggPathwayOverlapList, permutationNumberRegulationBasedKeggPathwayOverlapList, permutationNumberAllBasedKeggPathwayOverlapList);	
		}
		
		if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
			findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName, geneId2KeggPathwayMap,exonBasedKeggPathwayBufferedWriterHashMap,regulationBasedKeggPathwayBufferedWriterHashMap,allBasedKeggPathwayBufferedWriterHashMap,permutationNumberExonBasedKeggPathway2OneorZeroMap,permutationNumberRegulationBasedKeggPathway2OneorZeroMap,permutationNumberAllBasedKeggPathway2OneorZeroMap,type, permutationNumberExonBasedKeggPathwayOverlapList, permutationNumberRegulationBasedKeggPathwayOverlapList, permutationNumberAllBasedKeggPathwayOverlapList);		
		}					
	}	
	
	
	
	//NEW FUNCIONALITY
	//with IO
	public void findAllOverlappingUcscRefSeqGenesIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,List<String>> geneId2KeggPathwayMap, Map<String,BufferedWriter> exonBasedKeggPathwayBufferedWriterHashMap, Map<String,BufferedWriter> regulationBasedKeggPathwayBufferedWriterHashMap, Map<String,BufferedWriter> allBasedKeggPathwayBufferedWriterHashMap, Map<String,Integer> permutationNumberExonBasedKeggPathway2OneorZeroMap, Map<String,Integer> permutationNumberRegulationBasedKeggPathway2OneorZeroMap,Map<String,Integer> permutationNumberAllBasedKeggPathway2OneorZeroMap,String type){
		String permutationNumberKeggPathwayName = null;
		String keggPathwayName;
		List<String> keggPathwayListContainingThisGeneId = null;
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		File directory;
		
		int repeatNumberReflectedPermutationNumber = ((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber;

			
			if (Commons.NCBI_GENE_ID.equals(type)){
				//There is overlap
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
					try {
						
						keggPathwayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
					
						//write exon based results
						if (node.getIntervalName().startsWith(Commons.EXON)){							
							if(keggPathwayListContainingThisGeneId!=null){
								for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
									
									keggPathwayName = keggPathwayListContainingThisGeneId.get(i);											
									permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;	
									
									bufferedWriter = exonBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberKeggPathwayName);
									
									if (bufferedWriter == null){
										directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
										if (!directory.exists()) {
											if (!directory.mkdir()){
												System.out.println("Failed to create directory!");
											}
										}
										
										fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\" + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber +"_exonBased_" + keggPathwayName + ".txt",true);
										bufferedWriter = new BufferedWriter(fileWriter);
										exonBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
									}
									
									
									if(permutationNumberExonBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
										permutationNumberExonBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
									}
									
									bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
									bufferedWriter.flush();	
									
								}// End of For: for all keggpathways having this gene in their gene list
							} //End of If: keggPathWayListContainingThisGeneId is not null								
						}// End of If: Exon Based Kegg Pathway Analysis, Overlapped node is an exon

						
						//write regulation based results
						//Regulation Based kegg pathway analysis
						if (node.getIntervalName().startsWith(Commons.INTRON) ||
							node.getIntervalName().startsWith(Commons.FIVE_P_ONE) ||
							node.getIntervalName().startsWith(Commons.FIVE_P_TWO) ||
							node.getIntervalName().startsWith(Commons.THREE_P_ONE)||
							node.getIntervalName().startsWith(Commons.THREE_P_TWO)){
														
							if(keggPathwayListContainingThisGeneId!=null){
								for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
									
									keggPathwayName = keggPathwayListContainingThisGeneId.get(i);
									permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;
									
									bufferedWriter = regulationBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberKeggPathwayName);
									
									if (bufferedWriter == null){
										directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
										if (!directory.exists()) {
											if (!directory.mkdir()){
												System.out.println("Failed to create directory!");
											}
										}
										
										fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\" + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber +"_regulationBased_" + keggPathwayName + ".txt",true);
										bufferedWriter = new BufferedWriter(fileWriter);
										regulationBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
									}
										
									if(permutationNumberRegulationBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
										permutationNumberRegulationBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
									}
							
									bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
									bufferedWriter.flush();	
							
																			
								}// End of For: for all kegg pathways having this gene in their gene list
							} // End of If:		keggPathWayListContainingThisGeneId is not null					
						}//End of If: Regulation Based kegg pathway Analysis, Overlapped node is an intron, 5P1, 5P2, 3P1, 3P2

						
						
						//write all results							
						if(keggPathwayListContainingThisGeneId!=null){
							for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
								
								keggPathwayName = keggPathwayListContainingThisGeneId.get(i);
								permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;
								
								bufferedWriter = allBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberKeggPathwayName);
								
								if (bufferedWriter == null){
									directory = new File(Commons.ANNOTATE_PERMUTATIONS_FOR_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\");
									if (!directory.exists()) {
										if (!directory.mkdir()){
											System.out.println("Failed to create directory!");
										}
									}
									
									fileWriter = new FileWriter(Commons.ANNOTATE_PERMUTATIONS_FOR_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber+ "\\" + Commons.PERMUTATION +repeatNumberReflectedPermutationNumber +"_allBased_" + keggPathwayName + ".txt",true);
									bufferedWriter = new BufferedWriter(fileWriter);
									allBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberKeggPathwayName, bufferedWriter);
								}
																			
								if(permutationNumberAllBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
									permutationNumberAllBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
								}
																
							}// End of For: for all kegg pathways having this gene in their gene list
						} // End of If:		keggPathWayListContainingThisGeneId is not null	
						
												
					} catch (IOException e) {
						e.printStackTrace();
					}
																
									
				}//End of if: there is overlap	
			} //End of If: type is NCBI_GENE_ID
				
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName, geneId2KeggPathwayMap, exonBasedKeggPathwayBufferedWriterHashMap,regulationBasedKeggPathwayBufferedWriterHashMap,allBasedKeggPathwayBufferedWriterHashMap,permutationNumberExonBasedKeggPathway2OneorZeroMap,permutationNumberRegulationBasedKeggPathway2OneorZeroMap,permutationNumberAllBasedKeggPathway2OneorZeroMap,type);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName, geneId2KeggPathwayMap,exonBasedKeggPathwayBufferedWriterHashMap,regulationBasedKeggPathwayBufferedWriterHashMap,allBasedKeggPathwayBufferedWriterHashMap,permutationNumberExonBasedKeggPathway2OneorZeroMap,permutationNumberRegulationBasedKeggPathway2OneorZeroMap,permutationNumberAllBasedKeggPathway2OneorZeroMap,type);		
			}					
		}	
	
		//NEW FUNCIONALITY
		//EXON BASED KEGG PATHWAY
		//REGULATION BASED KEGG PATHWAY
		//ALL BASED KEGG PATHWAY
		//Empirical P Value Calculation
		//Search2 KeggPathway
		//For finding the number of each keggpathway:k for the given search input size: n
		//For each search input line, each kegg pathway will have a value of 1 or 0
		//These 1 or 0's will be accumulated in keggPathway2KMap	
		//without IO
		public void findAllOverlappingUcscRefSeqGenesIntervals(int repeatNumber,int permutationNumber,int NUMBER_OF_PERMUTATIONS,IntervalTreeNode node, Interval interval, String chromName, Map<String,List<String>> geneId2KeggPathwayMap, Map<String,Integer> permutationNumberExonBasedKeggPathway2OneorZeroMap, Map<String,Integer> permutationNumberRegulationBasedKeggPathway2OneorZeroMap,Map<String,Integer> permutationNumberAllBasedKeggPathway2OneorZeroMap,String type,List<PermutationNumberUcscRefSeqGeneOverlap> permutationNumberExonBasedKeggPathwayOverlapList,List<PermutationNumberUcscRefSeqGeneOverlap> permutationNumberRegulationBasedKeggPathwayOverlapList,List<PermutationNumberUcscRefSeqGeneOverlap> permutationNumberAllBasedKeggPathwayOverlapList){
			String permutationNumberKeggPathwayName = null;
			String keggPathwayName;
			List<String> keggPathwayListContainingThisGeneId = null;
			
			int repeatNumberConsideredPermutationNumber = (repeatNumber-1)*NUMBER_OF_PERMUTATIONS + permutationNumber;
				
				if (Commons.NCBI_GENE_ID.equals(type)){
					//There is overlap
					if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
						
						keggPathwayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
						
						
							//write exon based results
							if (node.getIntervalName().startsWith(Commons.EXON)){							
								if(keggPathwayListContainingThisGeneId!=null){
									
									permutationNumberExonBasedKeggPathwayOverlapList.add(new PermutationNumberUcscRefSeqGeneOverlap(Commons.PERMUTATION + repeatNumberConsideredPermutationNumber,node.getRefSeqGeneName(), node.getIntervalName(), node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathwayListContainingThisGeneId));

									
									for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
										
										keggPathwayName = keggPathwayListContainingThisGeneId.get(i);											
										permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;
										
										
										if(permutationNumberExonBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
											permutationNumberExonBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
										}
										
									}// End of For: for all keggpathways having this gene in their gene list
								} //End of If: keggPathWayListContainingThisGeneId is not null								
							}// End of If: Exon Based Kegg Pathway Analysis, Overlapped node is an exon

							
							//write regulation based results
							//Regulation Based kegg pathway analysis
							if (node.getIntervalName().startsWith(Commons.INTRON) ||
								node.getIntervalName().startsWith(Commons.FIVE_P_ONE) ||
								node.getIntervalName().startsWith(Commons.FIVE_P_TWO) ||
								node.getIntervalName().startsWith(Commons.THREE_P_ONE)||
								node.getIntervalName().startsWith(Commons.THREE_P_TWO)){
															
								if(keggPathwayListContainingThisGeneId!=null){
									
									permutationNumberRegulationBasedKeggPathwayOverlapList.add(new PermutationNumberUcscRefSeqGeneOverlap(Commons.PERMUTATION + repeatNumberConsideredPermutationNumber ,node.getRefSeqGeneName(), node.getIntervalName(), node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathwayListContainingThisGeneId));

									for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
										
										keggPathwayName = keggPathwayListContainingThisGeneId.get(i);
										permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;	
											
										if(permutationNumberRegulationBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
											permutationNumberRegulationBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
										}
																				
									}// End of For: for all kegg pathways having this gene in their gene list
								} // End of If:		keggPathWayListContainingThisGeneId is not null					
							}//End of If: Regulation Based kegg pathway Analysis, Overlapped node is an intron, 5P1, 5P2, 3P1, 3P2

							
							
							//write all results							
							if(keggPathwayListContainingThisGeneId!=null){
								
								permutationNumberAllBasedKeggPathwayOverlapList.add(new PermutationNumberUcscRefSeqGeneOverlap(Commons.PERMUTATION + repeatNumberConsideredPermutationNumber, node.getRefSeqGeneName(), node.getIntervalName(), node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathwayListContainingThisGeneId));

								for(int i= 0; i<keggPathwayListContainingThisGeneId.size(); i++){
									
									keggPathwayName = keggPathwayListContainingThisGeneId.get(i);
									permutationNumberKeggPathwayName = Commons.PERMUTATION + (((repeatNumber-1)*NUMBER_OF_PERMUTATIONS) + permutationNumber)+ "_" + keggPathwayName;	
																				
									if(permutationNumberAllBasedKeggPathway2OneorZeroMap.get(permutationNumberKeggPathwayName)==null){
										permutationNumberAllBasedKeggPathway2OneorZeroMap.put(permutationNumberKeggPathwayName, 1);
									}
																	
								}// End of For: for all kegg pathways having this gene in their gene list
							} // End of If:		keggPathWayListContainingThisGeneId is not null	
							
													
							
																	
										
					}//End of if: there is overlap	
				} //End of If: type is NCBI_GENE_ID
					
				if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<= interval.getHigh())){
					findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getLeft(),interval,chromName, geneId2KeggPathwayMap, permutationNumberExonBasedKeggPathway2OneorZeroMap,permutationNumberRegulationBasedKeggPathway2OneorZeroMap,permutationNumberAllBasedKeggPathway2OneorZeroMap,type,permutationNumberExonBasedKeggPathwayOverlapList,permutationNumberRegulationBasedKeggPathwayOverlapList,permutationNumberAllBasedKeggPathwayOverlapList);	
				}
				
				if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
					findAllOverlappingUcscRefSeqGenesIntervals(repeatNumber,permutationNumber,NUMBER_OF_PERMUTATIONS,node.getRight(),interval,chromName, geneId2KeggPathwayMap, permutationNumberExonBasedKeggPathway2OneorZeroMap,permutationNumberRegulationBasedKeggPathway2OneorZeroMap,permutationNumberAllBasedKeggPathway2OneorZeroMap,type,permutationNumberExonBasedKeggPathwayOverlapList,permutationNumberRegulationBasedKeggPathwayOverlapList,permutationNumberAllBasedKeggPathwayOverlapList);		
				}
							
		}

	//NEW FUNCIONALITY
		
		
	//New Functionality starts
	//Search2 Kegg Pathway
	//Search for TF
	//Search for KEGG Pathways (exon based, regulation based, all based)
	//Search for TF and KEGG Pathways (tf and exonBased, tf and regulationBased,tf and allBased)	
	//will be modified	
	public void findAllOverlappingUcscRefSeqGenesIntervals(IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> exonBasedKeggPathwayBufferedWriterHashMap, Map<String,BufferedWriter> regulationBasedKeggPathwayBufferedWriterHashMap, Map<String,BufferedWriter> allBasedKeggPathwayBufferedWriterHashMap, Map<String,List<String>> geneId2KeggPathwayMap, List<String> keggPathwayNameList, Map<String,Integer> exonBasedKeggPathway2OneorZeroMap,Map<String,Integer> regulationBasedKeggPathway2OneorZeroMap, Map<String,Integer> allBasedKeggPathway2OneorZeroMap, String type, List<UcscRefSeqGeneOverlap> exonBasedKeggPathwayOverlapList, List<UcscRefSeqGeneOverlap> regulationBasedKeggPathwayOverlapList, List<UcscRefSeqGeneOverlap> allBasedKeggPathwayOverlapList){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		String keggPathwayName = null;
		List<String> keggPathWayListContainingThisGeneId = null;
		
			if (Commons.NCBI_GENE_ID.equals(type)){
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
					
					keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
					
					try {
												
						//write exon based kegg pathway results
						if (node.getIntervalName().startsWith(Commons.EXON)){
							
							if(keggPathWayListContainingThisGeneId!=null){
								
								exonBasedKeggPathwayOverlapList.add(new UcscRefSeqGeneOverlap(node.getRefSeqGeneName(), node.getIntervalName(), node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathWayListContainingThisGeneId));

								for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
									keggPathwayName = keggPathWayListContainingThisGeneId.get(i);	
									
									if (keggPathwayNameList.contains(keggPathwayName)){
										
										bufferedWriter = exonBasedKeggPathwayBufferedWriterHashMap.get(keggPathwayName);
										
										if (bufferedWriter == null){
											fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_EXON_BASED_KEGG_PATHWAY_ANALYSIS +"_exonBased_" + keggPathwayName + ".txt");
											bufferedWriter = new BufferedWriter(fileWriter);
											exonBasedKeggPathwayBufferedWriterHashMap.put(keggPathwayName, bufferedWriter);
											bufferedWriter.write("Searched for chr" + "\t" + "interval Low" + "\t" + "interval High" + "\t" + "ucscRefSeqGene node ChromName" + "\t" +  "node Low" + "\t" + "node High" + "\t" + "node RefSeqGeneName"+ "\t" + "node IntervalName" + "\t" + "node GeneHugoSymbol"+ "\t"+ "node GeneEntrezId" +"\n");
											
											
										}
										
										if(exonBasedKeggPathway2OneorZeroMap.get(keggPathwayName)==null){
											exonBasedKeggPathway2OneorZeroMap.put(keggPathwayName, 1);
										}
										
										bufferedWriter.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh()  + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
										bufferedWriter.flush();	
										
									
									}//If this keggPathwayName is in  keggPathwayNameList
									
									
								}// End of For: for all keggpathways having this gene in their gene list
							} //End of If: keggPathWayListContainingThisGeneId is not null								
						}// End of If: Exon Based Kegg Pathway Analysis, Overlapped node is an exon

						
						//write regulation based kegg pathway results
						if (node.getIntervalName().startsWith(Commons.INTRON) ||
							node.getIntervalName().startsWith(Commons.FIVE_P_ONE) ||
							node.getIntervalName().startsWith(Commons.FIVE_P_TWO) ||
							node.getIntervalName().startsWith(Commons.THREE_P_ONE)||
							node.getIntervalName().startsWith(Commons.THREE_P_TWO)){
							
							
							if(keggPathWayListContainingThisGeneId!=null){
								
								regulationBasedKeggPathwayOverlapList.add(new UcscRefSeqGeneOverlap(node.getRefSeqGeneName(), node.getIntervalName(), node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathWayListContainingThisGeneId));

								for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
									keggPathwayName = keggPathWayListContainingThisGeneId.get(i);	
									
									if (keggPathwayNameList.contains(keggPathwayName)){
										
										bufferedWriter = regulationBasedKeggPathwayBufferedWriterHashMap.get(keggPathwayName);
										
										if (bufferedWriter == null){
											fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS +"_regulationBased_" + keggPathwayName + ".txt");
											bufferedWriter = new BufferedWriter(fileWriter);
											regulationBasedKeggPathwayBufferedWriterHashMap.put(keggPathwayName, bufferedWriter);
											bufferedWriter.write("Searched for chr" + "\t" + "interval Low" + "\t" + "interval High" + "\t" + "ucscRefSeqGene node ChromName" + "\t" +  "node Low" + "\t" + "node High" + "\t" + "node RefSeqGeneName"+ "\t" + "node IntervalName" + "\t" + "node GeneHugoSymbol"+ "\t"+ "node GeneEntrezId" +"\n");

										}
										
										if(regulationBasedKeggPathway2OneorZeroMap.get(keggPathwayName)==null){
											regulationBasedKeggPathway2OneorZeroMap.put(keggPathwayName, 1);
										}
										
										bufferedWriter.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh()  + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
										bufferedWriter.flush();	
										

									}//If this keggPathwayName is in  keggPathwayNameList
									
									
								}// End of For: for all kegg pathways having this gene in their gene list
							} // End of If:		keggPathWayListContainingThisGeneId is not null					
						}//End of If: Regulation Based kegg pathway Analysis, Overlapped node is an intron, 5P1, 5P2, 3P1, 3P2

						
						//write all results
						if(keggPathWayListContainingThisGeneId!=null){
							
							allBasedKeggPathwayOverlapList.add(new UcscRefSeqGeneOverlap(node.getRefSeqGeneName(), node.getIntervalName(),node.getGeneHugoSymbol(), node.getGeneEntrezId(), node.getLow(), node.getHigh(),keggPathWayListContainingThisGeneId));
							
							for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
								keggPathwayName = keggPathWayListContainingThisGeneId.get(i);	
								
								if (keggPathwayNameList.contains(keggPathwayName)){
									
									bufferedWriter = allBasedKeggPathwayBufferedWriterHashMap.get(keggPathwayName);
									
									if (bufferedWriter == null){
										fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_ALL_BASED_KEGG_PATHWAY_ANALYSIS +"_allBased_" + keggPathwayName + ".txt");
										bufferedWriter = new BufferedWriter(fileWriter);
										allBasedKeggPathwayBufferedWriterHashMap.put(keggPathwayName, bufferedWriter);
										bufferedWriter.write("Searched for chr" + "\t" + "interval Low" + "\t" + "interval High" + "\t" + "ucscRefSeqGene node ChromName" + "\t" +  "node Low" + "\t" + "node High" + "\t" + "node RefSeqGeneName"+ "\t" + "node IntervalName" + "\t" + "node GeneHugoSymbol"+ "\t"+ "node GeneEntrezId" +"\n");

										
									}
									
									if(allBasedKeggPathway2OneorZeroMap.get(keggPathwayName)==null){
										allBasedKeggPathway2OneorZeroMap.put(keggPathwayName, 1);
									}
									
									bufferedWriter.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh()  + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
									bufferedWriter.flush();	
									

								}//If this keggPathwayName is in  keggPathwayNameList
								
								
							}// End of For: for all kegg pathways having this gene in their gene list
						} // End of If:		keggPathWayListContainingThisGeneId is not null	
							
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}	
			} //End of If: type is NCBI_GENE_ID
				
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin() <= interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(node.getLeft(),interval,chromName,exonBasedKeggPathwayBufferedWriterHashMap, regulationBasedKeggPathwayBufferedWriterHashMap,allBasedKeggPathwayBufferedWriterHashMap,geneId2KeggPathwayMap, keggPathwayNameList, exonBasedKeggPathway2OneorZeroMap,regulationBasedKeggPathway2OneorZeroMap,allBasedKeggPathway2OneorZeroMap,type,exonBasedKeggPathwayOverlapList,regulationBasedKeggPathwayOverlapList,allBasedKeggPathwayOverlapList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(node.getRight(),interval,chromName,exonBasedKeggPathwayBufferedWriterHashMap, regulationBasedKeggPathwayBufferedWriterHashMap,allBasedKeggPathwayBufferedWriterHashMap, geneId2KeggPathwayMap, keggPathwayNameList, exonBasedKeggPathway2OneorZeroMap,regulationBasedKeggPathway2OneorZeroMap,allBasedKeggPathway2OneorZeroMap,type,exonBasedKeggPathwayOverlapList,regulationBasedKeggPathwayOverlapList,allBasedKeggPathwayOverlapList);	
				
			}
	}
	//New Functionality ends	
	//Search2 Kegg Pathway	
	
	
	//Search2 KeggPathway
	//For finding the number of each keggpathway:k for the given search input size: n
	//For each search input line, each kegg pathway will have a value of 1 or 0
	//These 1 or 0's will be accumulated in keggPathway2KMap		
	public void findAllOverlappingUcscRefSeqGenesIntervals(IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, Map<String,List<String>> geneId2KeggPathwayMap, List<String> keggPathwayNameList, Map<String,Integer> keggPathway2OneorZeroMap, String type, String keggPathwayAnalysisType){
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		String keggPathwayName = null;
		List<String> keggPathWayListContainingThisGeneId = null;
		
			
			if (Commons.NCBI_GENE_ID.equals(type)){
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
					try {
												
						//write exon based results
						if (Commons.EXON_BASED_KEGG_PATHWAY_ANALYSIS.equals(keggPathwayAnalysisType)){
							
							//exon based kegg pathway analysis
							if (node.getIntervalName().startsWith(Commons.EXON)){
								
								keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
								
								if(keggPathWayListContainingThisGeneId!=null){
									for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
										keggPathwayName = keggPathWayListContainingThisGeneId.get(i);	
										
										if (keggPathwayNameList.contains(keggPathwayName)){
											
											bufferedWriter = bufferedWriterHashMap.get(keggPathwayName);
											
											if (bufferedWriter == null){
												fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_EXON_BASED_KEGG_PATHWAY_ANALYSIS +"_exonBased_" + keggPathwayName + ".txt");
												bufferedWriter = new BufferedWriter(fileWriter);
												bufferedWriterHashMap.put(keggPathwayName, bufferedWriter);
												
											}
											
											if(keggPathway2OneorZeroMap.get(keggPathwayName)==null){
												keggPathway2OneorZeroMap.put(keggPathwayName, 1);
											}
											
											bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
											bufferedWriter.flush();	
										}//If this keggPathwayName is in  keggPathwayNameList
										
										
									}// End of For: for all keggpathways having this gene in their gene list
								} //End of If: keggPathWayListContainingThisGeneId is not null								
							}// End of If: Exon Based Kegg Pathway Analysis, Overlapped node is an exon

						}
						//write regulation based results
						else if (Commons.REGULATION_BASED_KEGG_PATHWAY_ANALYSIS.equals(keggPathwayAnalysisType)){
							//Regulation Based kegg pathway analysis
							if (node.getIntervalName().startsWith(Commons.INTRON) ||
								node.getIntervalName().startsWith(Commons.FIVE_P_ONE) ||
								node.getIntervalName().startsWith(Commons.FIVE_P_TWO) ||
								node.getIntervalName().startsWith(Commons.THREE_P_ONE)||
								node.getIntervalName().startsWith(Commons.THREE_P_TWO)){
								
								keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
								
								if(keggPathWayListContainingThisGeneId!=null){
									for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
										keggPathwayName = keggPathWayListContainingThisGeneId.get(i);	
										
										if (keggPathwayNameList.contains(keggPathwayName)){
											
											bufferedWriter = bufferedWriterHashMap.get(keggPathwayName);
											
											if (bufferedWriter == null){
												fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS +"_regulationBased_" + keggPathwayName + ".txt");
												bufferedWriter = new BufferedWriter(fileWriter);
												bufferedWriterHashMap.put(keggPathwayName, bufferedWriter);
												
											}
											
											if(keggPathway2OneorZeroMap.get(keggPathwayName)==null){
												keggPathway2OneorZeroMap.put(keggPathwayName, 1);
											}
											
											bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
											bufferedWriter.flush();	
										}//If this keggPathwayName is in  keggPathwayNameList
										
										
									}// End of For: for all kegg pathways having this gene in their gene list
								} // End of If:		keggPathWayListContainingThisGeneId is not null					
							}//End of If: Regulation Based kegg pathway Analysis, Overlapped node is an intron, 5P1, 5P2, 3P1, 3P2

						}
						//write all results
						else{
							keggPathWayListContainingThisGeneId =  geneId2KeggPathwayMap.get(node.getGeneEntrezId().toString());
							
							if(keggPathWayListContainingThisGeneId!=null){
								for(int i= 0; i<keggPathWayListContainingThisGeneId.size(); i++){
									keggPathwayName = keggPathWayListContainingThisGeneId.get(i);	
									
									if (keggPathwayNameList.contains(keggPathwayName)){
										
										bufferedWriter = bufferedWriterHashMap.get(keggPathwayName);
										
										if (bufferedWriter == null){
											fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_ALL_BASED_KEGG_PATHWAY_ANALYSIS +"_all_" + keggPathwayName + ".txt");
											bufferedWriter = new BufferedWriter(fileWriter);
											bufferedWriterHashMap.put(keggPathwayName, bufferedWriter);
											
										}
										
										if(keggPathway2OneorZeroMap.get(keggPathwayName)==null){
											keggPathway2OneorZeroMap.put(keggPathwayName, 1);
										}
										
										bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
										bufferedWriter.flush();	
									}//If this keggPathwayName is in  keggPathwayNameList
									
									
								}// End of For: for all kegg pathways having this gene in their gene list
							} // End of If:		keggPathWayListContainingThisGeneId is not null	
							
						}
												
						
																
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}	
			} //End of If: type is NCBI_GENE_ID
				
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin() <= interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(node.getLeft(),interval,chromName,bufferedWriterHashMap, geneId2KeggPathwayMap, keggPathwayNameList, keggPathway2OneorZeroMap,type,keggPathwayAnalysisType);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(node.getRight(),interval,chromName,bufferedWriterHashMap, geneId2KeggPathwayMap, keggPathwayNameList, keggPathway2OneorZeroMap,type,keggPathwayAnalysisType);	
				
			}
	}
		

//Search2
public void findAllOverlappingUcscRefSeqGenesIntervals(IntervalTreeNode node, Interval interval, String chromName, Map<String,BufferedWriter> bufferedWriterHashMap, Map<String,Integer> nameorIdHashMap, String type){
	FileWriter fileWriter = null;
	BufferedWriter bufferedWriter = null;
	Integer count ;
	
	try {
		
		if (node.isNotSentinel()){
			
			if (Commons.NCBI_GENE_ID.equals(type)){
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh()) && nameorIdHashMap.containsKey(node.getGeneEntrezId().toString())){
						
					bufferedWriter = bufferedWriterHashMap.get(node.getGeneEntrezId().toString());
					count =  nameorIdHashMap.get(node.getGeneEntrezId().toString());
					
					if (bufferedWriter == null){
						fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_NCBI_GENE_ID +"_" +String.valueOf(node.getGeneEntrezId()) + ".txt");
						bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriterHashMap.put(node.getGeneEntrezId().toString(), bufferedWriter);
					
					}							
					
					
					
					bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName() + "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
					bufferedWriter.flush();					
										
					count++;
					nameorIdHashMap.put(node.getGeneEntrezId().toString(), count);
				}	
			}else if (Commons.NCBI_RNA_NUCLEOTIDE_ACCESSION_VERSION.equals(type)){
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh()) &&nameorIdHashMap.containsKey(node.getRefSeqGeneName())){

						bufferedWriter = bufferedWriterHashMap.get(node.getRefSeqGeneName());
						count =  nameorIdHashMap.get(node.getRefSeqGeneName());
						
						if (bufferedWriter == null){
							fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_NCBI_RNA +"_" + node.getRefSeqGeneName() + ".txt");
							bufferedWriter = new BufferedWriter(fileWriter);
							bufferedWriterHashMap.put(node.getRefSeqGeneName(), bufferedWriter);							
						}
													
						bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName() + "\t" + node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
						bufferedWriter.flush();		
						
						count++;
						nameorIdHashMap.put(node.getRefSeqGeneName(), count);
										
				}	
			}else if(Commons.UCSC_GENE_ALTERNATE_NAME.equals(type)){
				if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh()) && nameorIdHashMap.containsKey(node.getGeneHugoSymbol())){
				
						bufferedWriter = bufferedWriterHashMap.get(node.getGeneHugoSymbol());
						count =  nameorIdHashMap.get(node.getGeneHugoSymbol());
						
						if (bufferedWriter == null){
							fileWriter = new FileWriter(Commons.ANNOTATE_INTERVALS_USING_INTERVAL_TREE_OUTPUT_FILE_PATH_FOR_UCSC_GENE_ALTERNATE_NAME +"_" + node.getGeneHugoSymbol() + ".txt");
							bufferedWriter = new BufferedWriter(fileWriter);
							bufferedWriterHashMap.put(node.getGeneHugoSymbol(), bufferedWriter);							
						}
												
						bufferedWriter.write("Searched for" + "\t" + chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + "ucscRefSeqGene" + "\t" + node.getChromName()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName()+ "\t" +  node.getIntervalName() + "\t" + node.getGeneHugoSymbol()+ "\t"+ node.getGeneEntrezId() +"\n");
						bufferedWriter.flush();					
						
						count++;
						nameorIdHashMap.put(node.getGeneHugoSymbol(), count);
				}	
			}

			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax())){
				findAllOverlappingUcscRefSeqGenesIntervals(node.getLeft(),interval,chromName, bufferedWriterHashMap, nameorIdHashMap,type);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getLow()<=interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(node.getRight(),interval,chromName, bufferedWriterHashMap, nameorIdHashMap,type);	
				
			}
		} // If node is not null
					
		
	} catch (IOException e) {
		e.printStackTrace();
	}		
}	






	//Search1
	public void findAllOverlappingUcscRefSeqGenesIntervals(IntervalTreeNode node, Interval interval, BufferedWriter bufferedWriter,List<IntervalTreeNode> overlappingNodeList){
		
			if (overlaps(node.getLow(), node.getHigh(), interval.getLow(), interval.getHigh())){
				
				overlappingNodeList.add(node);
				
				try {
					bufferedWriter.write("ucscRefSeqGene" + "\t" + node.getChromName().toString()+ "\t" +  node.getLow() + "\t" + node.getHigh() + "\t" + node.getRefSeqGeneName().toString()+ "\t" + node.getIntervalName().toString() + "\t" + node.getGeneHugoSymbol().toString()+ "\t"+ node.getGeneEntrezId() +"\n");
					bufferedWriter.flush();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
			
			
			if((node.getLeft().isNotSentinel()) && (interval.getLow()<=node.getLeft().getMax()) && (node.getLeft().getMin()<=interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(node.getLeft(),interval,bufferedWriter,overlappingNodeList);	
			}
			
			if((node.getRight().isNotSentinel()) && (interval.getLow()<=node.getRight().getMax()) && (node.getRight().getMin()<=interval.getHigh())){
				findAllOverlappingUcscRefSeqGenesIntervals(node.getRight(),interval,bufferedWriter,overlappingNodeList);	
				
			}
						
	}
	
	public void printOverlappingIntervalsList(List<IntervalTreeNode> list){
		Iterator<IntervalTreeNode> itr = list.iterator();
		
		while(itr.hasNext()){
			IntervalTreeNode  node =(IntervalTreeNode) itr.next();			
			System.out.println(node.getLow() + "\t" + node.getHigh());
			
		}
	}
	
	
	public static void printPath(Path path){
		IntervalTreeNode node;
		int numberofBlackNodes = 0;
		
		System.out.println("One Path Starts");
		
		for(int i = 0; i<path.getNodeList().size(); i++){
			node = path.getNodeList().get(i);
			
			if (node.getColor() == Commons.BLACK)
				numberofBlackNodes++;
			
			System.out.println("Node(" + node.getLow() + "," + node.getHigh() + ",Max: " + node.getMax() + ") Color: "+node.getColor() +" Name: "+ node.getName());
			
		}
		
		
		System.out.println("--- Number of black nodes: " + numberofBlackNodes);
		
	}
	
	public static void findAllPathsFromRootToLeavesUsingDepthFirstSearch(IntervalTreeNode node,IntervalTreeProperties properties,Path path , List<Path> allPathsFromRootToLeaves){
		
		//Null node
		if (node==null){
				return;
		}	
		// SENTINEL or NOT_SENTINEL_NODE
		else {
			//SENTINEL NODE
			if (node.isSentinel()){
		
				 if (node.getColor()!=Commons.BLACK){
					 properties.setEverySentinelLeafIsBlack(false);
						 return;
				 }
				 
				 path.getNodeList().add(node);
				  
			 	 if (node.getColor()==Commons.BLACK){
			 		 path.setNumberofBlackNodes(path.getNumberofBlackNodes()+1);
			 	 }
			 	 
			 	 //A leaf has been reached
			 	 //Create a new Path object
			 	 //Since path instance changes rapidly during recursion
			 	 
			 	 Path onePathFromRootToLeaf = new Path(path.getNumberofBlackNodes(),path.getNodeList());			 	 
			 	 //printPath(onePathFromRootToLeaf);				 		
		 		 allPathsFromRootToLeaves.add(onePathFromRootToLeaf);		 		 		
			}
			 
		 	//NOT SENTINEL NODE
			 else if (node.isNotSentinel()){
				 	if (node.getColor()!= Commons.RED && node.getColor()!= Commons.BLACK ){
				 			properties.setEveryNodeIsEitherRedorBlack(false);
				 			return;
				 	}
				 	
				 	if (node.getColor()==Commons.RED){
				 		if (node.getLeft().getColor()!=Commons.BLACK && node.getRight().getColor()!=Commons.BLACK){
				 			properties.setEveryRedNodeHasBlackChildren(false);
				 			return;
				 		}
				 	}
				 	
				 	if (node.getMax()!=max(node)){
				 		properties.setEveryNotSentinelNodeHasRightMaxValue(false);
				 		return;
				 		
				 	}
					
				 	 path.getNodeList().add(node);
				 	 
				 	 if (node.getColor()==Commons.BLACK){
				 		 path.setNumberofBlackNodes(path.getNumberofBlackNodes()+1);
				 	 }
				 	 
				 	
				 	findAllPathsFromRootToLeavesUsingDepthFirstSearch(node.getLeft(), properties,path,allPathsFromRootToLeaves);
				 	findAllPathsFromRootToLeavesUsingDepthFirstSearch(node.getRight(), properties,path,allPathsFromRootToLeaves);
				 	
			 }		 
		} //SENTINEL or NOT_SENTINEL node
		
		 path.getNodeList().remove(node);
	 	 
	 	if (node.getColor()==Commons.BLACK){
	 		 path.setNumberofBlackNodes(path.getNumberofBlackNodes()-1);
	 	 }
	}
	
	
	public static void checkNumberofBlackNodesinAllPathsFromRootToLeaves(IntervalTreeProperties properties, List<Path> allPaths){
		int numberofBlackNodes =0;
	
		if (allPaths.size()>0){
			numberofBlackNodes = allPaths.get(0).getNumberofBlackNodes();
		}
			
			
		for(int i=1; i<allPaths.size();i++){
			if (numberofBlackNodes!=allPaths.get(i).getNumberofBlackNodes()){
				properties.setAllPathsfromRoottoLeavesHasSameNumberofBlackNodes(false);
				break;
			}				
		}
	}
	/*
	 * Traverse the tree using depth first search 
	 * And check whether the properties hold or not
	 */
	
	public static void checkProperties(IntervalTree tree,IntervalTreeProperties properties){
		Path path = new Path();
		List<Path> allPathsFromRoottoLeaves = new ArrayList<Path>();
		
		findAllPathsFromRootToLeavesUsingDepthFirstSearch(tree.getRoot(),properties,path,allPathsFromRoottoLeaves);
		
		checkNumberofBlackNodesinAllPathsFromRootToLeaves(properties,allPathsFromRoottoLeaves);
	
		return;
	}
	
	/*
	 * Check for whether the given tree holds the red black tree properties
	 * 
	 *  Red black tree properties:
	 * 1. Every node is either red or black.
	 * 2. The root is black.
	 * 3. Every leaf(nil[T]) is black.
	 * 4. If a node is red, then both its children are black. (Hence no two reds in a row on a simple path from the root to a leaf.)
	 * 5. For each node, all paths from the node to descendant leaves contain the same number of black nodes.
	 * 
	 * 
	 */
	public static boolean checkIntervalTreePropertiesHolds(IntervalTree tree){
		
		boolean intevalTreePropertiesHold = true;
		
		IntervalTreeProperties properties = new IntervalTreeProperties();
		
		if (tree.getRoot().getColor()!=Commons.BLACK)
			properties.setRootIsBlack(false);
			
		checkProperties(tree,properties);
		
		intevalTreePropertiesHold = properties.isEveryNodeIsEitherRedorBlack() && 
									properties.isRootIsBlack() && 
									properties.isEverySentinelLeafIsBlack()  && 
									properties.isEveryRedNodeHasBlackChildren() && 
									properties.isAllPathsfromRoottoLeavesHasSameNumberofBlackNodes() && 
									properties.isEveryNotSentinelNodeHasRightMaxValue();
		
		return intevalTreePropertiesHold;
		
	}
	
	public static void intervalTreeInsertDelete(IntervalTree tree,IntervalTreeNode node, String operation){
		
		boolean isIntervalTreePropertiesHold;
		IntervalTreeNode splicedOutNode;
		
		if (Commons.INSERT.equals(operation)){
			System.out.println("After insert node (" + node.getLow() +"," + node.getHigh() +")");
			tree.intervalTreeInsert(tree, node);
			System.out.println("Tree Root color: " + tree.getRoot().getColor()+ " Tree Root Low: " + tree.getRoot().getLow() + " Tree Root High: " +tree.getRoot().getHigh() + " Tree Root Max: " + tree.getRoot().getMax() + " Tree Root's Parent's Name: " + tree.getRoot().getParent().getName() );
			tree.intervalTreeInfixTraversal(tree.getRoot());
			isIntervalTreePropertiesHold = checkIntervalTreePropertiesHolds(tree);
			System.out.println("Does the interval tree properties hold? " + isIntervalTreePropertiesHold);
			
			
		}else if(Commons.DELETE.equals(operation)){
			System.out.println("After delete node (" + node.getLow() +"," + node.getHigh() +")");
			splicedOutNode = tree.intervalTreeDelete(tree, node);
			splicedOutNode = null;
			System.out.println("Tree Root color: " + tree.getRoot().getColor()+ " Tree Root Low: " + tree.getRoot().getLow() + " Tree Root High: " +tree.getRoot().getHigh() + " Tree Root Max: " + tree.getRoot().getMax() + " Tree Root's Parent's Name: " + tree.getRoot().getParent().getName() );
			tree.intervalTreeInfixTraversal(tree.getRoot());
			isIntervalTreePropertiesHold = checkIntervalTreePropertiesHolds(tree);
			System.out.println("Does the interval tree properties hold? " + isIntervalTreePropertiesHold);
			
		}
		
		System.out.println("-------------------------------------------------------------------------------");
		
		
	}
	
	
	/*
	 * IntervalTree is composed of sentinel and not sentinel nodes.
	 * Sentinel nodes are the leaf nodes.
	 * Sentinel nodes have left and right nodes null.
	 * 
	 * Not sentinel nodes are the not leaf nodes.
	 * Not sentinel nodes have not null left and  right nodes.
	 *
	 */
	public static void deleteNodesofIntervalTree(IntervalTreeNode node){
		if(node.isNotSentinel()){
			deleteNodesofIntervalTree(node.getLeft());
			deleteNodesofIntervalTree(node.getRight());
			node = null;
			
		}else if(node.isSentinel()){
			node =null;
		}
		
	}
	
	
	public static void main(String[] args) {
		
		IntervalTree tree = new IntervalTree();
		boolean isIntervalTreePropertiesHold;

		//Check whether interval tree properties hold when the tree is null?
		isIntervalTreePropertiesHold = checkIntervalTreePropertiesHolds(tree);
		
		
		List<IntervalTreeNode> resultList ;
		
		IntervalTreeNode node1 = new IntervalTreeNode(10,15);
		IntervalTreeNode node_1 = new IntervalTreeNode(10,15);
		IntervalTreeNode node_2 = new IntervalTreeNode(10,15);
		IntervalTreeNode node2 = new IntervalTreeNode(5,40);
		IntervalTreeNode node3 = new IntervalTreeNode(15,60);
		IntervalTreeNode node4 = new IntervalTreeNode(2,80);
		IntervalTreeNode node5 = new IntervalTreeNode(1,100);
		IntervalTreeNode node6 = new IntervalTreeNode(3,150);
		IntervalTreeNode node7 = new IntervalTreeNode(6,36);
		IntervalTreeNode node8 = new IntervalTreeNode(7,77);
		IntervalTreeNode node9 = new IntervalTreeNode(8,200);
		IntervalTreeNode node10 = new IntervalTreeNode(4,20);
		IntervalTreeNode node11 = new IntervalTreeNode(40,140);
		IntervalTreeNode node12 = new IntervalTreeNode(60,120);
		IntervalTreeNode node13 = new IntervalTreeNode(50,150);
		IntervalTreeNode node14 = new IntervalTreeNode(55,90);		
		IntervalTreeNode node15 = new IntervalTreeNode(30,40);
		
		intervalTreeInsertDelete(tree,node1, Commons.INSERT);
		intervalTreeInsertDelete(tree,node_1, Commons.INSERT);
		intervalTreeInsertDelete(tree,node_2, Commons.INSERT);
		intervalTreeInsertDelete(tree,node2, Commons.INSERT);
		intervalTreeInsertDelete(tree,node3, Commons.INSERT);
		intervalTreeInsertDelete(tree,node4, Commons.INSERT);
		intervalTreeInsertDelete(tree,node5, Commons.INSERT);
		intervalTreeInsertDelete(tree,node6, Commons.INSERT);
		intervalTreeInsertDelete(tree,node7, Commons.INSERT);
		intervalTreeInsertDelete(tree,node8, Commons.INSERT);
		intervalTreeInsertDelete(tree,node9, Commons.INSERT);
		intervalTreeInsertDelete(tree,node10, Commons.INSERT);
		intervalTreeInsertDelete(tree,node11, Commons.INSERT);
		intervalTreeInsertDelete(tree,node12, Commons.INSERT);
		intervalTreeInsertDelete(tree,node13, Commons.INSERT);
		intervalTreeInsertDelete(tree,node14, Commons.INSERT);
			
		
		intervalTreeInsertDelete(tree,node2, Commons.DELETE);
		intervalTreeInsertDelete(tree,node8, Commons.DELETE);
		intervalTreeInsertDelete(tree,node12, Commons.DELETE);
		intervalTreeInsertDelete(tree,node4, Commons.DELETE);
		intervalTreeInsertDelete(tree,node14, Commons.DELETE);
		intervalTreeInsertDelete(tree,node_1, Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		intervalTreeInsertDelete(tree,node2, Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		

		//Deletion of erroneous data: no NOT_SENTINEL node has been left
//		intervalTreeInsertDelete(tree,tree.getRoot(), Commons.DELETE);
		
		
		//Deletion of erroneous data: non existing node
//		System.out.println("After delete node (" + node15.getLow() +"," + node15.getHigh() +")");
//		node = tree.intervalTreeDelete(tree, node15);
//		node = null;
//		System.out.println("Tree root name: "+ tree.getRoot().getName() + " Tree Root color: " + tree.getRoot().getColor()+ " Tree Root Low: " + tree.getRoot().getLow() + " Tree Root High: " +tree.getRoot().getHigh() + " Tree Root Max: " + tree.getRoot().getMax() + " Tree Root's Parent's Name: " + tree.getRoot().getParent().getName() );
//		tree.intervalTreeInfixTraversal(tree.getRoot());
		
		
		
	
//		System.out.println("Overlapping Intervals");
//		tree.findAllOverlappingIntervals(tree.getRoot(), new Interval(5,20));
		
//		tree.printOverlappingIntervalsList(resultList);
		
	}

	
}
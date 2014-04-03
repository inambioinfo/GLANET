/*
 * Prepared Input data will be processed.
 * Input data might be composed of snps or intervals in a mixed mode, does not matter.
 * Input will not contain any overlaps.
 * Overlaps will be merged.
 * 
 * 
 */
package processinputdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import intervaltree.IntervalTree;
import intervaltree.IntervalTreeNode;
import auxiliary.FileOperations;

import common.Commons;

 

public class RemoveOverlaps {
	
	
	public static IntervalTreeNode mergeIntervals(IntervalTreeNode node1, IntervalTreeNode node2){
		
		
			
		if (node2.getLow()<node1.getLow()){
			node1.setLow(node2.getLow());			
		}
			
		
		if (node2.getHigh()>node1.getHigh()){
			node1.setHigh(node2.getHigh());
		}
		
		return node1;
		
	}
	
	public static void updateMergedNode(IntervalTreeNode mergedNode, IntervalTreeNode overlappedNode){
		if (overlappedNode.getLow()<mergedNode.getLow()){
			mergedNode.setLow(overlappedNode.getLow());			
		}
			
		
		if (overlappedNode.getHigh()>mergedNode.getHigh()){
			mergedNode.setHigh(overlappedNode.getHigh());
		}
		
		mergedNode.setNumberofBases(mergedNode.getHigh()-mergedNode.getLow()+1);
	}
	
	
	public static IntervalTreeNode compute(Map<IntervalTreeNode,IntervalTreeNode> splicedNode2CopiedNodeMap,IntervalTreeNode overlappedNode){
		IntervalTreeNode node = splicedNode2CopiedNodeMap.get(overlappedNode);
		IntervalTreeNode savedPreviousNode = null;
		
		while(node!=null){
			savedPreviousNode = node;
			node = splicedNode2CopiedNodeMap.get(node);
		}
	
		return savedPreviousNode;
	}
	
	/*
	 * Partition the input intervals in a chromosomed based manner
	 * Then for each of the 24 chromosome based partitioned input intervals
	 * Construct an interval tree
	 * If the new interval overlaps with any interval in the interval tree
	 * Merge the intervals, update the existing interval
	 * Otherwise insert the new interval.
	 * 
	 * Then write each interval tree to an output file.
	 * In this manner input file will get rid of overlaps.
	 * 
	 */
	public static void removeOverlaps(String[] args){
		
		
		String glanetFolder 	= args[1];	
		String outputFolder 	= glanetFolder + System.getProperty("file.separator") + Commons.OUTPUT + System.getProperty("file.separator");
		String inputFileName  	= outputFolder + Commons.PROCESSED_INPUT_FILE;
		
		Map<String,IntervalTree> chromosome2IntervalTree = new HashMap<String,IntervalTree>();
				
		String strLine;
		String chromosomeName;
		int low;
		int high;
			
		int indexofFirstTab;
		int indexofSecondTab;
		
		FileReader fileReader;
		BufferedReader bufferedReader;
		
		IntervalTree intervalTree = null;
		IntervalTreeNode intervalTreeNode = null;
		
		try {
			fileReader = new FileReader(inputFileName);
			bufferedReader = new BufferedReader(fileReader);
			
			while ((strLine = bufferedReader.readLine())!=null){
				//example strLine
				//chr12	90902	90902
				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t',indexofFirstTab+1);
				
				chromosomeName = strLine.substring(0, indexofFirstTab);
				
				if (indexofSecondTab>indexofFirstTab){
					low = Integer.parseInt(strLine.substring(indexofFirstTab+1, indexofSecondTab));					
					high = Integer.parseInt(strLine.substring(indexofSecondTab+1));
				}else{
					low = Integer.parseInt(strLine.substring(indexofFirstTab+1));					
					high = low;
				}
				
				
				intervalTree = chromosome2IntervalTree.get(chromosomeName);
				intervalTreeNode = new IntervalTreeNode(chromosomeName,low, high);
				
				//create chromosome based interval tree
				if(intervalTree == null){
					intervalTree = new IntervalTree();
					intervalTree.intervalTreeInsert(intervalTree, intervalTreeNode);
					chromosome2IntervalTree.put(chromosomeName, intervalTree);
				}
				//chromosome based interval tree already exists
				else{
					List<IntervalTreeNode> overlappedNodeList= new ArrayList<IntervalTreeNode>();
					
					intervalTree.findAllOverlappingIntervals(overlappedNodeList,intervalTree.getRoot(), intervalTreeNode);
					
					//there is overlap
					if (overlappedNodeList!= null && overlappedNodeList.size()>0){
							
						IntervalTreeNode mergedNode = new IntervalTreeNode(intervalTreeNode.getChromName(),intervalTreeNode.getLow(), intervalTreeNode.getHigh(),intervalTreeNode.getCellLineName(), intervalTreeNode.getFileName(),Commons.MERGED_NODE);
						IntervalTreeNode splicedoutNode = null;
						IntervalTreeNode nodetoBeDeleted =null;	
						//you may try to delete a node which is already spliced out by former deletions
						//therefore you must keep track of the real node to be deleted in case of trial of deletion of an already spliced out node.
						Map<IntervalTreeNode,IntervalTreeNode> splicedoutNode2CopiedNodeMap = new HashMap<IntervalTreeNode, IntervalTreeNode>();
						
						for(int i= 0; i<overlappedNodeList.size(); i++){
								
							IntervalTreeNode overlappedNode = overlappedNodeList.get(i);
								
							updateMergedNode(mergedNode, overlappedNode);
							
							//if the to be deleted, intended interval tree node is an already spliced out node
							//in other words if it is copied into another interval tree node
							//then you have to delete that node
							//not the already spliced out node
							
							nodetoBeDeleted =compute(splicedoutNode2CopiedNodeMap,overlappedNode) ;
							
							if 	(nodetoBeDeleted!=null){
									//they are the same
									//current overlapped node has been copied to the previously deleted overlapped node
									//current overlapped node is spliced out by the previous delete operation
									//so delete that previously deleted overlapped node in order to delete the current overlapped node
									//since current overlapped node is copied to the previously deleted overlapped node
									//Now we can delete this overlappedNode
									splicedoutNode = intervalTree.intervalTreeDelete(intervalTree, nodetoBeDeleted);
									
									if(splicedoutNode!=nodetoBeDeleted)
										splicedoutNode2CopiedNodeMap.put(splicedoutNode,nodetoBeDeleted);	
							}else{							
								//Now we can delete this overlappedNode
								splicedoutNode = intervalTree.intervalTreeDelete(intervalTree, overlappedNode);
								
								if (splicedoutNode!=overlappedNode)
									splicedoutNode2CopiedNodeMap.put(splicedoutNode,overlappedNode);
						
							}
							
						}//end of for: each overlapped node in the list
						intervalTree.intervalTreeInsert(intervalTree, mergedNode);
						
					}
					//there is no overlap
					else{
							//insert interval
						intervalTree.intervalTreeInsert(intervalTree, intervalTreeNode);
					}
				}
				
			}//End of While: read each input line
			
			bufferedReader.close();
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//write to output file
		FileWriter fileWriter;
		BufferedWriter bufferedWriter;
		
		IntervalTree tree = null;
		
		String type = Commons.PROCESS_INPUT_DATA_REMOVE_OVERLAPS;
		
		try {
			fileWriter = FileOperations.createFileWriter(outputFolder + Commons.REMOVED_OVERLAPS_INPUT_FILE);
			bufferedWriter = new BufferedWriter(fileWriter);
			
			for(Map.Entry<String,IntervalTree> chr2IntervalTree : chromosome2IntervalTree.entrySet()){
				
				chromosomeName = chr2IntervalTree.getKey();
				tree = chr2IntervalTree.getValue();
				
				//write the nodes of the interval tree in a sorted way
				tree.intervalTreeInfixTraversal(tree.getRoot(), bufferedWriter, type);
				
							
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		 
	 }
	
	
	public static void writeInputFileWithoutOverlaps(String outputFileName, Map<String,IntervalTree>chromosome2IntervalTree){
		FileWriter fileWriter;
		BufferedWriter bufferedWriter;
		
		String chromosomeName = null;
		IntervalTree tree = null;
		
		String type = Commons.PROCESS_INPUT_DATA_REMOVE_OVERLAPS;
		
		try {
			fileWriter = FileOperations.createFileWriter(outputFileName);
			bufferedWriter = new BufferedWriter(fileWriter);
			
			for(Map.Entry<String,IntervalTree> chr2IntervalTree : chromosome2IntervalTree.entrySet()){
				
				chromosomeName = chr2IntervalTree.getKey();
				tree = chr2IntervalTree.getValue();
				
				//write the nodes of the interval tree in a sorted way
				tree.intervalTreeInfixTraversal(tree.getRoot(), bufferedWriter, type);
				
							
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}
	
	
	//args[0] must have input file name with folder
	//args[1] must have GLANET installation folder with "\\" at the end. This folder will be used for outputFolder and dataFolder.
	//args[2] must have Input File Format		
	//args[3] must have Number of Permutations	
	//args[4] must have False Discovery Rate (ex: 0.05)
	//args[5] must have Generate Random Data Mode (with GC and Mapability/without GC and Mapability)
	//args[6] must have writeGeneratedRandomDataMode checkBox
	//args[7] must have writePermutationBasedandParametricBasedAnnotationResultMode checkBox
	//args[8] must have writePermutationBasedAnnotationResultMode checkBox
	public static void run(String[] args){
	
		removeOverlaps(args);
		
	}
	
	//args[0] must have input file name with folder
	//args[1] must have GLANET installation folder with "\\" at the end. This folder will be used for outputFolder and dataFolder.
	//args[2] must have Input File Format		
	//args[3] must have Number of Permutations	
	//args[4] must have False Discovery Rate (ex: 0.05)
	//args[5] must have Generate Random Data Mode (with GC and Mapability/without GC and Mapability)
	//args[6] must have writeGeneratedRandomDataMode checkBox
	//args[7] must have writePermutationBasedandParametricBasedAnnotationResultMode checkBox
	//args[8] must have writePermutationBasedAnnotationResultMode checkBox
	public static void main(String[] args){
		
		removeOverlaps(args);
		
			
	}

}
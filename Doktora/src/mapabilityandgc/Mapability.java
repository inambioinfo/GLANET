/**
 * @author Burcak Otlu
 * Aug 19, 2013
 * 11:37:56 PM
 * 2013
 *
 * 
 */
package mapabilityandgc;

import intervaltree.IntervalTree;
import intervaltree.IntervalTreeNode;
import empiricalpvalues.GCCharArray;
import empiricalpvalues.InputLine;
import empiricalpvalues.MapabilityFloatArray;

public class Mapability {
	
	static IntervalTree mapabilityIntervalTree;
	
	
	
	

	public static IntervalTree getMapabilityIntervalTree() {
		return mapabilityIntervalTree;
	}


	public static void setMapabilityIntervalTree(IntervalTree mapabilityIntervalTree) {
		Mapability.mapabilityIntervalTree = mapabilityIntervalTree;
	}


	
	public static void fillChromBasedMapabilityArrayfromFile(int chromSize,String inputFileName, MapabilityFloatArray mapabilityFloatArray){
		FileReader fileReader;
		BufferedReader bufferedReader;
		String strLine;
		//chrY	10000	10153	0.5

		String chromName = null;
		int low;
		int high;
		float mapability;
		
		int indexofFirstTab;
		int indexofSecondTab;
		int indexofThirdTab;
		
		try {
			fileReader = new FileReader(inputFileName);
			bufferedReader = new BufferedReader(fileReader);
			
			while((strLine=bufferedReader.readLine())!=null){
				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab+1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab+1);
				
				chromName = strLine.substring(0, indexofFirstTab);
				low = Integer.parseInt(strLine.substring(indexofFirstTab+1, indexofSecondTab));
				high = Integer.parseInt(strLine.substring(indexofSecondTab+1,indexofThirdTab));
				mapability = Float.parseFloat(strLine.substring(indexofThirdTab+1));
				
				//high is 1-based therefore it can be equal to chromSize
				if (low>=chromSize || high > chromSize ){
					System.out.println("Unexpected situation: There exists a line in mapability file of " + chromName + " which exceeds chromsize " + chromSize + " low: " + low + " high: "+ high);
				}
				
				//High-1 is done here
				for(int i= low; i<high; i++){
					mapabilityFloatArray.getMapabilityArray()[i] = mapability;
				}
							
			}
			
			System.out.println("This file must be read only once " + inputFileName + " chromName: " + chromName + " Mapability Double Array construction has ended.");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
		
	//todo
	public static  void fillChromBasedMapabilityArray(int chromSize, String chromName, MapabilityFloatArray mapabilityFloatArray){		
		switch(chromName){
			case Commons.CHROMOSOME1:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR1_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME2:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR2_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME3:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR3_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME4:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR4_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME5:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR5_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME6:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR6_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME7:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR7_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME8:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR8_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME9:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR9_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME10:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR10_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME11:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR11_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME12:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR12_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME13:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR13_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME14:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR14_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME15:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR15_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME16:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR16_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME17:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR17_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME18:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR18_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME19:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR19_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME20:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR20_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME21:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR21_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOME22:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHR22_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOMEX:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHRX_FILE,mapabilityFloatArray);
										break;
			case Commons.CHROMOSOMEY:   fillChromBasedMapabilityArrayfromFile(chromSize,Commons.MAPABILITY_HG19_CHRY_FILE,mapabilityFloatArray);
										break;
							
					
		}
	}

	
	
	//todo
	//for variance calculation among functional elements' mapability values
	public static float calculateMapability(int low, int high, IntervalTree mapabilityIntervalTree){
		
		
		float accumulatedMapability=0;
		
		int numberofOverlappingBases;
		List<IntervalTreeNode> overlappedNodeList= new ArrayList<IntervalTreeNode>();
		
		IntervalTreeNode node = new IntervalTreeNode(low, high);
		
		
		mapabilityIntervalTree.findAllOverlappingIntervals(overlappedNodeList,mapabilityIntervalTree.getRoot(), node);
		
		
		//there is overlap
		//calculate mapability
		if (overlappedNodeList!= null && overlappedNodeList.size()>0){
			
			//Base assumption is that nodes in the overlappingNodeList do not overlap with each other
			for(int i=0; i<overlappedNodeList.size(); i++){
				numberofOverlappingBases = calculateTheNumberofOverlappingBases(node,overlappedNodeList.get(i));
				accumulatedMapability = accumulatedMapability + (overlappedNodeList.get(i).getMapability()* numberofOverlappingBases);
			}//end of FOR
			
		} // End of IF
		
		accumulatedMapability = accumulatedMapability/node.getNumberofBases();
				
		return accumulatedMapability;
		
	}
	
	
	public static float differenceofMapabilities(InputLine inputLine1, InputLine inputLine2){
		return Math.abs(inputLine1.getMapability() - inputLine2.getMapability());
	}
	
	
	public static int calculateTheNumberofOverlappingBases(IntervalTreeNode  node1, IntervalTreeNode node2){

		// Base Assumption is that these nodes overlaps
		//We know that node1 and node2 overlaps
		int start = Math.max(node1.getLow(), node2.getLow());
		int end = Math.min(node1.getHigh(), node2.getHigh()); 
		
		int numberofOverlappingBases = end-start+1;
		
		return numberofOverlappingBases;
	}
	
	//check it, test it
	public static void calculateMapabilityofInterval(InputLine givenInputLine,IntervalTree mapabilityIntervalTree){
		int numberofOverlappingBases;
		
		float accumulatedMapability=0;
		
		int low = givenInputLine.getLow();
		int high = givenInputLine.getHigh();
		
		List<IntervalTreeNode> overlappedNodeList= new ArrayList<IntervalTreeNode>();
		
		IntervalTreeNode node = new IntervalTreeNode(low, high);
		
		mapabilityIntervalTree.findAllOverlappingIntervals(overlappedNodeList,mapabilityIntervalTree.getRoot(), node);
		
		//there is overlap
		//calculate mapability
		if (overlappedNodeList!= null && overlappedNodeList.size()>0){
			
			//Base assumption is that nodes in the overlappingNodeList do not overlap with each other
			for(int i=0; i<overlappedNodeList.size(); i++){
				numberofOverlappingBases = calculateTheNumberofOverlappingBases(node,overlappedNodeList.get(i));
				accumulatedMapability = accumulatedMapability + (overlappedNodeList.get(i).getMapability()* numberofOverlappingBases);
			}//end of FOR
			
		} // End of IF
		
		accumulatedMapability = accumulatedMapability/node.getNumberofBases();
		
		givenInputLine.setMapability(accumulatedMapability);
	}
	

	public static void calculateMapabilityofIntervalUsingArray(InputLine givenInputLine,MapabilityFloatArray mapabilityDoubleArray){
		
		float accumulatedMapability=0;
		
		int low = givenInputLine.getLow();
		int high = givenInputLine.getHigh();
		
		int length=high-low+1;
		
	
		for(int i = low; i<=high; i++){
			accumulatedMapability = accumulatedMapability + mapabilityDoubleArray.getMapabilityArray()[i];
		}
		
		accumulatedMapability = accumulatedMapability/length;
			
		givenInputLine.setMapability(accumulatedMapability);
	}
	
	
	public static void fillChromosomeBasedMapabilityIntervalTreefromFile(int chromSize,String inputFileName, IntervalTree chromBasedMapabilityIntervalTree){
		
		FileReader fileReader;
		BufferedReader bufferedReader;
		String strLine;
		//chrY	10000	10153	0.5

		String chromName = null;
		int low;
		int high;
		float mapability;
		
		int indexofFirstTab;
		int indexofSecondTab;
		int indexofThirdTab;
		
		try {
			fileReader = new FileReader(inputFileName);
			bufferedReader = new BufferedReader(fileReader);
			
			while((strLine=bufferedReader.readLine())!=null){
				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab+1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab+1);
				
				chromName = strLine.substring(0, indexofFirstTab);
				low = Integer.parseInt(strLine.substring(indexofFirstTab+1, indexofSecondTab));
				high = Integer.parseInt(strLine.substring(indexofSecondTab+1,indexofThirdTab));
				mapability = Float.parseFloat(strLine.substring(indexofThirdTab+1));
				
				//high is 1-based therefore it can be equal to chromSize
				if (low>=chromSize|| high > chromSize ){
					System.out.println("Unexpected situation: There exists a line in mapability file of " + chromName + " which exceeds chromsize " + chromSize + " low: " + low + " high: "+ high);
				}
				
				
				//High-1 is done here
				IntervalTreeNode node = new IntervalTreeNode(chromName, low, high-1, mapability);
				
				//Assumption there will be no overlaps
				chromBasedMapabilityIntervalTree.intervalTreeInsert(chromBasedMapabilityIntervalTree, node);
				
				
//				//Check for overlaps each time before you insert a node
//				List<IntervalTreeNode> overlappedNodeList= new ArrayList<IntervalTreeNode>();
//				
//				chromBasedMapabilityIntervalTree.findAllOverlappingIntervals(overlappedNodeList,chromBasedMapabilityIntervalTree.getRoot(), node);
//				
//				//there is overlap
//				if (overlappedNodeList!= null && overlappedNodeList.size()>0){
//					System.out.println("Unexpected situation: there is overlap in interval tree for mapability");
//				}else{
//					chromBasedMapabilityIntervalTree.intervalTreeInsert(chromBasedMapabilityIntervalTree, node);
//					
//				}
							
			}
			
			System.out.println("This file must be read only once " + inputFileName + " chromName: " + chromName + " Mapability Interval Tree construction has ended.");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	
	public static void fillChromosomeBasedMapabilityIntervalTree(int chromSize,String chromName,IntervalTree chromBasedMapabilityIntervalTree){	
		
		switch(chromName){
			case Commons.CHROMOSOME1:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR1_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME2:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR2_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME3:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR3_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME4:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR4_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME5:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR5_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME6:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR6_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME7:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR7_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME8:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR8_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME9:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR9_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME10:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR10_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME11:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR11_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME12: 	fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR12_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME13:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR13_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME14:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR14_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME15:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR15_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME16:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR16_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME17:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR17_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME18:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR18_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME19:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR19_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME20:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR20_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME21:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR21_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOME22:  fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHR22_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOMEX:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHRX_FILE,chromBasedMapabilityIntervalTree);
										break;
			case Commons.CHROMOSOMEY:   fillChromosomeBasedMapabilityIntervalTreefromFile(chromSize,Commons.MAPABILITY_HG19_CHRY_FILE,chromBasedMapabilityIntervalTree);
										break;
	
		}//End of Switch
		
	}
	
	public static void main(String[] args) {
		
		//for testing purposes
		List<Integer> hg19ChromosomeSizes	= new ArrayList<Integer>();
    			
    	GRCh37Hg19Chromosome.initializeChromosomeSizes(hg19ChromosomeSizes);
    	//get the hg19 chromosome sizes
    	GRCh37Hg19Chromosome.getHg19ChromosomeSizes(hg19ChromosomeSizes, Commons.HG19_CHROMOSOME_SIZES_INPUT_FILE);
		
		String chromName = Commons.CHROMOSOME17;
    	int chromSize = hg19ChromosomeSizes.get(16);
    	
    	GCCharArray gcCharArray=null;
    	MapabilityFloatArray mapabilityFloatArray = null;
    	IntervalTree mapabilityIntervalTree=null;
    	
		gcCharArray = ChromosomeBasedGCArray.getChromosomeGCArray(chromName,chromSize);
		mapabilityFloatArray = ChromosomeBasedMapabilityArray.getChromosomeMapabilityArray(chromName,chromSize);
		mapabilityIntervalTree = ChromosomeBasedMapabilityIntervalTree.getChromosomeBasedMapabilityIntervalTree(chromName, chromSize);
		
		int low= 35100000;
		int high = 35100999;
		
		InputLine inputLine = new InputLine(chromName, low, high);
		
		Mapability.calculateMapabilityofInterval(inputLine, mapabilityIntervalTree);
		System.out.println("Using Interval Tree "+ inputLine.getMapability());
		Mapability.calculateMapabilityofIntervalUsingArray(inputLine, mapabilityFloatArray);
		System.out.println("Using Double Array: "+ inputLine.getMapability());
		
    	
    }

}

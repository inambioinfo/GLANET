/**
 * @author burcakotlu
 * @date May 9, 2014 
 * @time 10:45:02 AM
 */
package empiricalpvalues;

import generate.randomdata.RandomDataGenerator;
import hg19.GRCh37Hg19Chromosome;
import intervaltree.ChromosomeName;
import intervaltree.IntervalTree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;

import keggpathway.ncbigenes.KeggPathwayUtility;
import mapabilityandgc.ChromosomeBasedGCArray;
import mapabilityandgc.ChromosomeBasedMapabilityArray;
import annotate.intervals.parametric.AnnotateGivenIntervalsWithGivenParameters;
import auxiliary.FileOperations;
import auxiliary.FunctionalElement;
import auxiliary.NumberofComparisons;
import auxiliary.NumberofComparisonsforBonferroniCorrectionCalculation;

import common.Commons;

/**
 * 
 */
public class AnnotatePermutationsWithEnrichmentChoicesWithNumbers {


	
	static class GenerateRandomData extends RecursiveTask<Map<Integer,List<InputLine>>>{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5508399455444935122L;
		private final int chromSize;
		private final ChromosomeName chromName;
		private final List<InputLine> chromosomeBasedOriginalInputLines;
			
		private final String generateRandomDataMode;
		private final String writeGeneratedRandomDataMode;
		
		private final int lowIndex;
		private final int highIndex;
		
		private final List<AnnotationTask> listofAnnotationTasks;

		private final GCCharArray gcCharArray;
		private final MapabilityFloatArray mapabilityFloatArray;
		private final String outputFolder;
				
		public GenerateRandomData(String outputFolder,int chromSize, ChromosomeName chromName, List<InputLine> chromosomeBasedOriginalInputLines, String generateRandomDataMode, String writeGeneratedRandomDataMode,int lowIndex, int highIndex, List<AnnotationTask> listofAnnotationTasks,GCCharArray gcCharArray, MapabilityFloatArray mapabilityFloatArray) {
			
			this.outputFolder = outputFolder;
			
			this.chromSize = chromSize;
			this.chromName = chromName;
			this.chromosomeBasedOriginalInputLines = chromosomeBasedOriginalInputLines;

			this.generateRandomDataMode = generateRandomDataMode;
			this.writeGeneratedRandomDataMode = writeGeneratedRandomDataMode;
			
			this.lowIndex = lowIndex;
			this.highIndex = highIndex;
			
			this.listofAnnotationTasks = listofAnnotationTasks;
					
			this.gcCharArray = gcCharArray;
			this.mapabilityFloatArray = mapabilityFloatArray;
		}

		
		protected Map<Integer,List<InputLine>> compute() {
			
			int middleIndex;
			Map<Integer,List<InputLine>> rightRandomlyGeneratedData;
			Map<Integer,List<InputLine>> leftRandomlyGeneratedData;
	        
	    	Integer permutationNumber;
			AnnotationTask annotationTask;
					
			//DIVIDE
			if (highIndex-lowIndex>8){
			 	middleIndex = lowIndex + (highIndex - lowIndex) / 2;
			 	GenerateRandomData left  = new GenerateRandomData(outputFolder,chromSize, chromName, chromosomeBasedOriginalInputLines, generateRandomDataMode,writeGeneratedRandomDataMode,lowIndex,middleIndex,listofAnnotationTasks,gcCharArray,mapabilityFloatArray);
			 	GenerateRandomData right = new GenerateRandomData(outputFolder,chromSize, chromName, chromosomeBasedOriginalInputLines, generateRandomDataMode,writeGeneratedRandomDataMode,middleIndex,highIndex,listofAnnotationTasks,gcCharArray,mapabilityFloatArray);
	            left.fork();
	            rightRandomlyGeneratedData = right.compute();
	            leftRandomlyGeneratedData  = left.join();
	            
	            //Add the contents of leftRandomlyGeneratedData into rightRandomlyGeneratedData
	            mergeMaps(rightRandomlyGeneratedData,leftRandomlyGeneratedData);
	            
	            leftRandomlyGeneratedData = null;
	            return rightRandomlyGeneratedData;
	 		}
			//CONQUER
			else {
				
				Map<Integer,List<InputLine>> randomlyGeneratedDataMap = new HashMap<Integer,List<InputLine>>();
				 	
				for(int i=lowIndex; i<highIndex; i++){
					 annotationTask = listofAnnotationTasks.get(i);					 					
					 
					 permutationNumber = annotationTask.getPermutationNumber();
					 					      
				     System.out.println("Generate Random Data For Permutation: " + permutationNumber + "\t" +chromName);	
				     
				     randomlyGeneratedDataMap.put(permutationNumber, RandomDataGenerator.generateRandomData(gcCharArray,mapabilityFloatArray,chromSize, chromName,chromosomeBasedOriginalInputLines, ThreadLocalRandom.current(), generateRandomDataMode));
				      
				     //Write Generated Random Data
				     if(Commons.WRITE_GENERATED_RANDOM_DATA.equals(writeGeneratedRandomDataMode)){
				    	writeGeneratedRandomData(outputFolder,randomlyGeneratedDataMap.get(permutationNumber),permutationNumber);
				     }
						
				}//End of FOR
					
				return randomlyGeneratedDataMap;
			}

		}//End of compute method
		
		//Add the content of leftMap to rightMap
		//Clear and null leftMap
		protected void mergeMaps(Map<Integer,List<InputLine>> rightRandomlyGeneratedDataMap,Map<Integer,List<InputLine>> leftRandomlyGeneratedDataMap){
			
			for(Map.Entry<Integer, List<InputLine>> entry: leftRandomlyGeneratedDataMap.entrySet()){
				Integer permutationNumber = entry.getKey();
				
				if (!rightRandomlyGeneratedDataMap.containsKey(permutationNumber)){
					rightRandomlyGeneratedDataMap.put(permutationNumber, entry.getValue());
				}
			}//End of for
			
			leftRandomlyGeneratedDataMap.clear();
			leftRandomlyGeneratedDataMap = null;
				
		}
	       
		
		protected void writeGeneratedRandomData(String outputFolder,List<InputLine> randomlyGeneratedData,int permutationNumber){
			 FileWriter fileWriter;
		     BufferedWriter bufferedWriter;
		     InputLine randomlyGeneratedInputLine;
		        
		 	try {
		 		
				fileWriter = FileOperations.createFileWriter(outputFolder + Commons.RANDOMLY_GENERATED_DATA_FOLDER  + Commons.PERMUTATION + permutationNumber  + "_" + Commons.RANDOMLY_GENERATED_DATA  +".txt",true);
				bufferedWriter = new BufferedWriter(fileWriter);
				
				for(int i=0;i<randomlyGeneratedData.size(); i++){
					randomlyGeneratedInputLine = randomlyGeneratedData.get(i);
					bufferedWriter.write(ChromosomeName.convertEnumtoString(randomlyGeneratedInputLine.getChrName()) +"\t" + randomlyGeneratedInputLine.getLow() + "\t" + randomlyGeneratedInputLine.getHigh() +System.getProperty("line.separator"));
					bufferedWriter.flush();
				}//End of FOR
				
				bufferedWriter.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}//End of GenerateRandomData Class
	
		

	static class Annotate extends RecursiveTask<AllMaps>{
		
		
	private static final long serialVersionUID = 2919115895116169524L;
	private final int chromSize;
	private final ChromosomeName chromName;
	private final Map<Integer,List<InputLine>> randomlyGeneratedDataMap;
	private final int runNumber;
	private final int numberofPermutations;
	
	private final String writePermutationBasedandParametricBasedAnnotationResultMode;
	
	private final List<AnnotationTask> listofAnnotationTasks;
	private final IntervalTree intervalTree;
	private final IntervalTree ucscRefSeqGenesIntervalTree;
	
	private final String annotationType;
	private final String tfandKeggPathwayEnrichmentType;
	
	private final int lowIndex;
	private final int highIndex;
	
	private final Map<String,List<String>> geneId2KeggPathwayMap;
	
	private final String outputFolder;
	
	private final int overlapDefinition;
	
	
	
	
	public Annotate(String outputFolder,int chromSize, ChromosomeName chromName, Map<Integer,List<InputLine>> randomlyGeneratedDataMap, int runNumber,int numberofPermutations, String writePermutationBasedandParametricBasedAnnotationResultMode,int lowIndex, int highIndex, List<AnnotationTask> listofAnnotationTasks, IntervalTree intervalTree, IntervalTree ucscRefSeqGenesIntervalTree,String annotationType, String tfandKeggPathwayEnrichmentType,Map<String, List<String>> geneId2KeggPathwayMap,int overlapDefinition) {
		this.outputFolder  = outputFolder;
		
		this.chromSize = chromSize;
		this.chromName = chromName;
		this.randomlyGeneratedDataMap = randomlyGeneratedDataMap;
		this.runNumber = runNumber;
		this.numberofPermutations = numberofPermutations;
		
		this.writePermutationBasedandParametricBasedAnnotationResultMode = writePermutationBasedandParametricBasedAnnotationResultMode;
		
		this.lowIndex = lowIndex;
		this.highIndex = highIndex;
		
		this.listofAnnotationTasks = listofAnnotationTasks;
		this.intervalTree = intervalTree;
	
		//sent full when annotation for tf and ucsc refseq genes will be done
		//otherwise sent null
		this.ucscRefSeqGenesIntervalTree = ucscRefSeqGenesIntervalTree;
		
		this.annotationType = annotationType;	
		this.tfandKeggPathwayEnrichmentType = tfandKeggPathwayEnrichmentType;
		
		//geneId2KeggPathwayMap
		//sent full when annotation for tf and ucsc refseq genes will be done
		//otherwise sent null
		this.geneId2KeggPathwayMap = geneId2KeggPathwayMap;
		
		this.overlapDefinition = overlapDefinition;
		
	}
	
	
		
		protected AllMaps compute() {
			
			int middleIndex;
			AllMaps rightAllMaps;
	        AllMaps leftAllMaps;
	        
	    	AnnotationTask annotationTask;
			Integer permutationNumber;
			List<AllMaps> listofAllMaps;
			AllMaps allMaps;
				
			//DIVIDE
			if (highIndex-lowIndex>9){
			 	middleIndex = lowIndex + (highIndex - lowIndex) / 2;
	            Annotate left  = new Annotate(outputFolder,chromSize, chromName, randomlyGeneratedDataMap, runNumber,numberofPermutations,writePermutationBasedandParametricBasedAnnotationResultMode,lowIndex,middleIndex,listofAnnotationTasks,intervalTree,ucscRefSeqGenesIntervalTree,annotationType,tfandKeggPathwayEnrichmentType,geneId2KeggPathwayMap,overlapDefinition);
	            Annotate right = new Annotate(outputFolder,chromSize, chromName, randomlyGeneratedDataMap, runNumber,numberofPermutations,writePermutationBasedandParametricBasedAnnotationResultMode,middleIndex,highIndex,listofAnnotationTasks,intervalTree,ucscRefSeqGenesIntervalTree,annotationType,tfandKeggPathwayEnrichmentType,geneId2KeggPathwayMap,overlapDefinition);
	            left.fork();
	            rightAllMaps = right.compute();
	            leftAllMaps  = left.join();
	            combineAllMaps(leftAllMaps,rightAllMaps);
	            leftAllMaps= null;
	            return rightAllMaps;
			}
			//CONQUER
			else {
				
				listofAllMaps = new ArrayList<AllMaps>();
				allMaps = new AllMaps();
				 	
				for(int i=lowIndex; i<highIndex; i++){
					 annotationTask = listofAnnotationTasks.get(i);
					 permutationNumber = annotationTask.getPermutationNumber();
					      
				     System.out.println("Annotate Random Data For Permutation: " + permutationNumber + "\t" +chromName + "\t" + annotationType);	
				     
				     //NEW FUNCTIONALITY HAS BEEN ADDED
				     if(Commons.DO_NOT_WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT.equals(writePermutationBasedandParametricBasedAnnotationResultMode)){
				    	 listofAllMaps.add(AnnotateGivenIntervalsWithGivenParameters.annotatePermutationwithoutIO(permutationNumber,chromName,randomlyGeneratedDataMap.get(permutationNumber), intervalTree,ucscRefSeqGenesIntervalTree,annotationType,tfandKeggPathwayEnrichmentType,geneId2KeggPathwayMap,overlapDefinition));
				     }
				     
				     //NEW FUNCTIONALITY HAS BEEN ADDED
				     else if (Commons.WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT.equals(writePermutationBasedandParametricBasedAnnotationResultMode)){
				     	 listofAllMaps.add(AnnotateGivenIntervalsWithGivenParameters.annotatePermutationwithIO(outputFolder,permutationNumber,chromName,randomlyGeneratedDataMap.get(permutationNumber), intervalTree,ucscRefSeqGenesIntervalTree,annotationType,tfandKeggPathwayEnrichmentType,geneId2KeggPathwayMap,overlapDefinition));
				     }						
				}//End of FOR
					
				combineListofAllMaps(listofAllMaps,allMaps);
				listofAllMaps = null;
				return allMaps;
				
	
			}
		}		
		
		//Why are we doing this?
		//Accumulate the allMaps in the left into allMaps in the right
		protected void combineListofAllMaps(List<AllMaps> listofAllMaps,AllMaps allMaps){
			for(int i =0; i<listofAllMaps.size(); i++){
				combineAllMaps(listofAllMaps.get(i), allMaps);
			}
		}
		
		
		
	
		//Accumulate leftMap in the rightMap
		//Accumulate number of overlaps 
		//based on permutationNumber and ElementName
		protected void  combineMaps(Map<String,Integer> leftMap, Map<String,Integer> rightMap){
			
			for(Map.Entry<String, Integer> entry: leftMap.entrySet()){
				String permutationNumberElementName = entry.getKey();
				Integer numberofOverlaps = entry.getValue();
				
				if (rightMap.get(permutationNumberElementName)==null){
					rightMap.put(permutationNumberElementName, numberofOverlaps);
				}else{
					rightMap.put(permutationNumberElementName, rightMap.get(permutationNumberElementName)+numberofOverlaps);
				}
			}
			
			leftMap.clear();
			leftMap = null;
			//deleteMap(leftMap);
	
		}
		
		
		protected void combineAllMaps(AllMaps leftAllMaps, AllMaps rightAllMaps) {
				
				//LEFT ALL MAPS
				Map<String,Integer> leftPermutationNumberDnaseCellLineName2KMap = leftAllMaps.getPermutationNumberDnaseCellLineName2KMap();
				Map<String,Integer> leftPermutationNumberTfbsNameCellLineName2KMap = leftAllMaps.getPermutationNumberTfNameCellLineName2KMap();
				Map<String,Integer> leftPermutationNumberHistoneNameCellLineName2KMap = leftAllMaps.getPermutationNumberHistoneNameCellLineName2KMap();
				
				Map<String,Integer> leftPermutationNumberExonBasedKeggPathway2KMap = leftAllMaps.getPermutationNumberExonBasedKeggPathway2KMap();
				Map<String,Integer> leftPermutationNumberRegulationBasedKeggPathway2KMap = leftAllMaps.getPermutationNumberRegulationBasedKeggPathway2KMap();
				Map<String,Integer> leftPermutationNumberAllBasedKeggPathway2KMap = leftAllMaps.getPermutationNumberAllBasedKeggPathway2KMap();
				
				Map<String,Integer> leftPermutationNumberTfExonBasedKeggPathway2KMap 		= leftAllMaps.getPermutationNumberTfExonBasedKeggPathway2KMap();
				Map<String,Integer> leftPermutationNumberTfRegulationBasedKeggPathway2KMap 	= leftAllMaps.getPermutationNumberTfRegulationBasedKeggPathway2KMap();
				Map<String,Integer> leftPermutationNumberTfAllBasedKeggPathway2KMap 		= leftAllMaps.getPermutationNumberTfAllBasedKeggPathway2KMap();

				Map<String,Integer> leftPermutationNumberTfCellLineExonBasedKeggPathway2KMap 		= leftAllMaps.getPermutationNumberTfCellLineExonBasedKeggPathway2KMap();
				Map<String,Integer> leftPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap 	= leftAllMaps.getPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap();
				Map<String,Integer> leftPermutationNumberTfCellLineAllBasedKeggPathway2KMap 		= leftAllMaps.getPermutationNumberTfCellLineAllBasedKeggPathway2KMap();
				
			
				//RIGHT ALL MAPS
				Map<String,Integer> rightPermutationNumberDnaseCellLineName2KMap = rightAllMaps.getPermutationNumberDnaseCellLineName2KMap();
				Map<String,Integer> rightPermutationNumberTfbsNameCellLineName2KMap = rightAllMaps.getPermutationNumberTfNameCellLineName2KMap();
				Map<String,Integer> rightPermutationNumberHistoneNameCellLineName2KMap = rightAllMaps.getPermutationNumberHistoneNameCellLineName2KMap();
				
				Map<String,Integer> rightPermutationNumberExonBasedKeggPathway2KMap = rightAllMaps.getPermutationNumberExonBasedKeggPathway2KMap();
				Map<String,Integer> rightPermutationNumberRegulationBasedKeggPathway2KMap = rightAllMaps.getPermutationNumberRegulationBasedKeggPathway2KMap();
				Map<String,Integer> rightPermutationNumberAllBasedKeggPathway2KMap = rightAllMaps.getPermutationNumberAllBasedKeggPathway2KMap();
				
				Map<String,Integer> rightPermutationNumberTfExonBasedKeggPathway2KMap 		= rightAllMaps.getPermutationNumberTfExonBasedKeggPathway2KMap();
				Map<String,Integer> rightPermutationNumberTfRegulationBasedKeggPathway2KMap = rightAllMaps.getPermutationNumberTfRegulationBasedKeggPathway2KMap();
				Map<String,Integer> rightPermutationNumberTfAllBasedKeggPathway2KMap 		= rightAllMaps.getPermutationNumberTfAllBasedKeggPathway2KMap();

				Map<String,Integer> rightPermutationNumberTfCellLineExonBasedKeggPathway2KMap 		= rightAllMaps.getPermutationNumberTfCellLineExonBasedKeggPathway2KMap();
				Map<String,Integer> rightPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap = rightAllMaps.getPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap();
				Map<String,Integer> rightPermutationNumberTfCellLineAllBasedKeggPathway2KMap 		= rightAllMaps.getPermutationNumberTfCellLineAllBasedKeggPathway2KMap();
								
				
				if (leftPermutationNumberDnaseCellLineName2KMap!=null){
					combineMaps(leftPermutationNumberDnaseCellLineName2KMap,rightPermutationNumberDnaseCellLineName2KMap);
					leftPermutationNumberDnaseCellLineName2KMap = null;
				}
				
				if(leftPermutationNumberTfbsNameCellLineName2KMap!=null){
					combineMaps(leftPermutationNumberTfbsNameCellLineName2KMap,rightPermutationNumberTfbsNameCellLineName2KMap);
					leftPermutationNumberTfbsNameCellLineName2KMap = null;
				}
				
				if(leftPermutationNumberHistoneNameCellLineName2KMap!=null){
					combineMaps(leftPermutationNumberHistoneNameCellLineName2KMap,rightPermutationNumberHistoneNameCellLineName2KMap);
					leftPermutationNumberHistoneNameCellLineName2KMap = null;					
				}
				
				if(leftPermutationNumberExonBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberExonBasedKeggPathway2KMap,rightPermutationNumberExonBasedKeggPathway2KMap);
					leftPermutationNumberExonBasedKeggPathway2KMap = null;					
				}
				
				if (leftPermutationNumberRegulationBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberRegulationBasedKeggPathway2KMap,rightPermutationNumberRegulationBasedKeggPathway2KMap);
					leftPermutationNumberRegulationBasedKeggPathway2KMap = null;
				}
				
				if (leftPermutationNumberAllBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberAllBasedKeggPathway2KMap,rightPermutationNumberAllBasedKeggPathway2KMap);
					leftPermutationNumberAllBasedKeggPathway2KMap = null;
				}
				
				if (leftPermutationNumberTfCellLineExonBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberTfCellLineExonBasedKeggPathway2KMap,rightPermutationNumberTfCellLineExonBasedKeggPathway2KMap);
					leftPermutationNumberTfCellLineExonBasedKeggPathway2KMap = null;
				}
				
				if (leftPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap,rightPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap);
					leftPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap = null;
				}
				
				if (leftPermutationNumberTfCellLineAllBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberTfCellLineAllBasedKeggPathway2KMap,rightPermutationNumberTfCellLineAllBasedKeggPathway2KMap);
					leftPermutationNumberTfCellLineAllBasedKeggPathway2KMap = null;
				}
			
				if (leftPermutationNumberTfExonBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberTfExonBasedKeggPathway2KMap,rightPermutationNumberTfExonBasedKeggPathway2KMap);
					leftPermutationNumberTfExonBasedKeggPathway2KMap = null;
				}
				
				if (leftPermutationNumberTfRegulationBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberTfRegulationBasedKeggPathway2KMap,rightPermutationNumberTfRegulationBasedKeggPathway2KMap);
					leftPermutationNumberTfRegulationBasedKeggPathway2KMap = null;
				}
				
				if (leftPermutationNumberTfAllBasedKeggPathway2KMap!=null){
					combineMaps(leftPermutationNumberTfAllBasedKeggPathway2KMap,rightPermutationNumberTfAllBasedKeggPathway2KMap);
					leftPermutationNumberTfAllBasedKeggPathway2KMap = null;
				}
				
				
				//delete AllMaps
				//deleteAllMaps(leftAllMaps);
				leftAllMaps = null;
						
		}//End of combineAllMaps
		
		
		  
		
		protected void  deleteRandomlyGeneratedData(List<InputLine>randomlyGeneratedData){
			for(InputLine inputLine: randomlyGeneratedData){
				inputLine.setChrName(null);
				inputLine= null;
			}
			
			randomlyGeneratedData.clear();
		}
			
		
		protected void deleteMap(Map<String,Integer> map){
			if (map!=null){
				for(Map.Entry<String, Integer> entry: map.entrySet()){
					entry.setValue(null);
					entry= null;			
				}
				map= null;
			}
			
		}
		
		protected void deleteAllMaps(AllMaps allMaps){
			Map<String,Integer> map = allMaps.getPermutationNumberDnaseCellLineName2KMap();
			deleteMap(map);
			map = allMaps.getPermutationNumberTfNameCellLineName2KMap();
			deleteMap(map);
			map = allMaps.getPermutationNumberHistoneNameCellLineName2KMap();
			deleteMap(map);
			map = allMaps.getPermutationNumberExonBasedKeggPathway2KMap();
			deleteMap(map);
			map = allMaps.getPermutationNumberRegulationBasedKeggPathway2KMap();
			deleteMap(map);
			allMaps = null;
		}
		
		
	}

	
	
	static class AnnotateWithNumbers extends RecursiveTask<AllMapsWithNumbers>{
		
		
	private static final long serialVersionUID = 2919115895116169524L;
	private final int chromSize;
	private final ChromosomeName chromName;
	private final Map<Integer,List<InputLine>> randomlyGeneratedDataMap;
	private final int runNumber;
	private final int numberofPermutations;
	
	private final String writePermutationBasedandParametricBasedAnnotationResultMode;
	
	private final List<AnnotationTask> listofAnnotationTasks;
	private final IntervalTree intervalTree;
	private final IntervalTree ucscRefSeqGenesIntervalTree;
	
	private final String annotationType;
	private final String tfandKeggPathwayEnrichmentType;
	
	private final int lowIndex;
	private final int highIndex;
	
	private final Map<String,List<String>> geneId2KeggPathwayMap;
	
	private final String outputFolder;
	
	private final int overlapDefinition;
	
	
		
	public AnnotateWithNumbers(String outputFolder,int chromSize, ChromosomeName chromName, Map<Integer,List<InputLine>> randomlyGeneratedDataMap, int runNumber,int numberofPermutations, String writePermutationBasedandParametricBasedAnnotationResultMode,int lowIndex, int highIndex, List<AnnotationTask> listofAnnotationTasks, IntervalTree intervalTree, IntervalTree ucscRefSeqGenesIntervalTree,String annotationType, String tfandKeggPathwayEnrichmentType,Map<String, List<String>> geneId2KeggPathwayMap,int overlapDefinition) {
		this.outputFolder  = outputFolder;
		
		this.chromSize = chromSize;
		this.chromName = chromName;
		this.randomlyGeneratedDataMap = randomlyGeneratedDataMap;
		this.runNumber = runNumber;
		this.numberofPermutations = numberofPermutations;
		
		this.writePermutationBasedandParametricBasedAnnotationResultMode = writePermutationBasedandParametricBasedAnnotationResultMode;
		
		this.lowIndex = lowIndex;
		this.highIndex = highIndex;
		
		this.listofAnnotationTasks = listofAnnotationTasks;
		this.intervalTree = intervalTree;
	
		//sent full when annotation for tf and ucsc refseq genes will be done
		//otherwise sent null
		this.ucscRefSeqGenesIntervalTree = ucscRefSeqGenesIntervalTree;
		
		this.annotationType = annotationType;	
		this.tfandKeggPathwayEnrichmentType = tfandKeggPathwayEnrichmentType;
		
		//geneId2KeggPathwayMap
		//sent full when annotation for tf and ucsc refseq genes will be done
		//otherwise sent null
		this.geneId2KeggPathwayMap = geneId2KeggPathwayMap;
		
		this.overlapDefinition = overlapDefinition;		
	}
	
	
		
		protected AllMapsWithNumbers compute() {
			
			int middleIndex;
			AllMapsWithNumbers rightAllMapsWithNumbers;
	        AllMapsWithNumbers leftAllMapsWithNumbers;
	        
	    	AnnotationTask annotationTask;
			Integer permutationNumber;
			List<AllMapsWithNumbers> listofAllMapsWithNumbers;
			AllMapsWithNumbers allMapsWithNumbers;
				
			//DIVIDE
			if (highIndex-lowIndex>9){
			 	middleIndex = lowIndex + (highIndex - lowIndex) / 2;
			 	AnnotateWithNumbers left  = new AnnotateWithNumbers(outputFolder,chromSize, chromName, randomlyGeneratedDataMap, runNumber,numberofPermutations,writePermutationBasedandParametricBasedAnnotationResultMode,lowIndex,middleIndex,listofAnnotationTasks,intervalTree,ucscRefSeqGenesIntervalTree,annotationType,tfandKeggPathwayEnrichmentType,geneId2KeggPathwayMap,overlapDefinition);
			 	AnnotateWithNumbers right = new AnnotateWithNumbers(outputFolder,chromSize, chromName, randomlyGeneratedDataMap, runNumber,numberofPermutations,writePermutationBasedandParametricBasedAnnotationResultMode,middleIndex,highIndex,listofAnnotationTasks,intervalTree,ucscRefSeqGenesIntervalTree,annotationType,tfandKeggPathwayEnrichmentType,geneId2KeggPathwayMap,overlapDefinition);
	            left.fork();
	            rightAllMapsWithNumbers = right.compute();
	            leftAllMapsWithNumbers  = left.join();
	            combineLeftAllMapsandRightAllMaps(leftAllMapsWithNumbers, rightAllMapsWithNumbers);
	            leftAllMapsWithNumbers= null;
	            return rightAllMapsWithNumbers;
			}
			//CONQUER
			else {
				
				listofAllMapsWithNumbers = new ArrayList<AllMapsWithNumbers>();
				allMapsWithNumbers = new AllMapsWithNumbers();
				 	
				for(int i=lowIndex; i<highIndex; i++){
					 annotationTask = listofAnnotationTasks.get(i);
					 permutationNumber = annotationTask.getPermutationNumber();
					      
				     System.out.println("Annotate Random Data For Permutation: " + permutationNumber + "\t" +chromName + "\t" + annotationType);	
				     
				     //NEW FUNCTIONALITY HAS BEEN ADDED
				     if(Commons.DO_NOT_WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT.equals(writePermutationBasedandParametricBasedAnnotationResultMode)){
				    	 listofAllMapsWithNumbers.add(AnnotateGivenIntervalsWithGivenParameters.annotatePermutationwithoutIOwithNumbers(permutationNumber,chromName,randomlyGeneratedDataMap.get(permutationNumber), intervalTree,ucscRefSeqGenesIntervalTree,annotationType,tfandKeggPathwayEnrichmentType,geneId2KeggPathwayMap,overlapDefinition));
				     }
				     
				     //NEW FUNCTIONALITY HAS BEEN ADDED
				     else if (Commons.WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT.equals(writePermutationBasedandParametricBasedAnnotationResultMode)){
				     	 listofAllMapsWithNumbers.add(AnnotateGivenIntervalsWithGivenParameters.annotatePermutationwithIOwithNumbers(outputFolder,permutationNumber,chromName,randomlyGeneratedDataMap.get(permutationNumber), intervalTree,ucscRefSeqGenesIntervalTree,annotationType,tfandKeggPathwayEnrichmentType,geneId2KeggPathwayMap,overlapDefinition));
				     }						
				}//End of FOR
					
				combineListofAllMapsWithNumbers(listofAllMapsWithNumbers,allMapsWithNumbers);
				listofAllMapsWithNumbers = null;
				return allMapsWithNumbers;
				
	
			}
		}		
		
		//Accumulate the allMaps in the left into allMaps in the right
		protected void combineListofAllMapsWithNumbers(List<AllMapsWithNumbers> listofAllMaps,AllMapsWithNumbers allMaps){
			for(int i =0; i<listofAllMaps.size(); i++){
				combineLeftAllMapsandRightAllMaps(listofAllMaps.get(i), allMaps);
			}
		}	
		
		
		
		//Combine leftAllMapsWithNumbers and rightAllMapsWithNumbers in rightAllMapsWithNumbers
		protected void combineLeftAllMapsandRightAllMaps(AllMapsWithNumbers leftAllMapsWithNumbers, AllMapsWithNumbers rightAllMapsWithNumbers) {
			
			//LEFT ALL MAPS WITH NUMBERS
			Map<Long,Integer> leftPermutationNumberDnaseCellLineName2KMap = leftAllMapsWithNumbers.getPermutationNumberDnaseCellLineName2KMap();
			Map<Long,Integer> leftPermutationNumberTfbsNameCellLineName2KMap = leftAllMapsWithNumbers.getPermutationNumberTfNameCellLineName2KMap();
			Map<Long,Integer> leftPermutationNumberHistoneNameCellLineName2KMap = leftAllMapsWithNumbers.getPermutationNumberHistoneNameCellLineName2KMap();
			
			Map<Long,Integer> leftPermutationNumberExonBasedKeggPathway2KMap = leftAllMapsWithNumbers.getPermutationNumberExonBasedKeggPathway2KMap();
			Map<Long,Integer> leftPermutationNumberRegulationBasedKeggPathway2KMap = leftAllMapsWithNumbers.getPermutationNumberRegulationBasedKeggPathway2KMap();
			Map<Long,Integer> leftPermutationNumberAllBasedKeggPathway2KMap = leftAllMapsWithNumbers.getPermutationNumberAllBasedKeggPathway2KMap();
			
			Map<Long,Integer> leftPermutationNumberTfExonBasedKeggPathway2KMap 		= leftAllMapsWithNumbers.getPermutationNumberTfExonBasedKeggPathway2KMap();
			Map<Long,Integer> leftPermutationNumberTfRegulationBasedKeggPathway2KMap 	= leftAllMapsWithNumbers.getPermutationNumberTfRegulationBasedKeggPathway2KMap();
			Map<Long,Integer> leftPermutationNumberTfAllBasedKeggPathway2KMap 		= leftAllMapsWithNumbers.getPermutationNumberTfAllBasedKeggPathway2KMap();

			Map<Long,Integer> leftPermutationNumberTfCellLineExonBasedKeggPathway2KMap 		= leftAllMapsWithNumbers.getPermutationNumberTfCellLineExonBasedKeggPathway2KMap();
			Map<Long,Integer> leftPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap 	= leftAllMapsWithNumbers.getPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap();
			Map<Long,Integer> leftPermutationNumberTfCellLineAllBasedKeggPathway2KMap 		= leftAllMapsWithNumbers.getPermutationNumberTfCellLineAllBasedKeggPathway2KMap();
			
		
			//RIGHT ALL MAPS WITH NUMBERS
			Map<Long,Integer> rightPermutationNumberDnaseCellLineName2KMap = rightAllMapsWithNumbers.getPermutationNumberDnaseCellLineName2KMap();
			Map<Long,Integer> rightPermutationNumberTfbsNameCellLineName2KMap = rightAllMapsWithNumbers.getPermutationNumberTfNameCellLineName2KMap();
			Map<Long,Integer> rightPermutationNumberHistoneNameCellLineName2KMap = rightAllMapsWithNumbers.getPermutationNumberHistoneNameCellLineName2KMap();
			
			Map<Long,Integer> rightPermutationNumberExonBasedKeggPathway2KMap = rightAllMapsWithNumbers.getPermutationNumberExonBasedKeggPathway2KMap();
			Map<Long,Integer> rightPermutationNumberRegulationBasedKeggPathway2KMap = rightAllMapsWithNumbers.getPermutationNumberRegulationBasedKeggPathway2KMap();
			Map<Long,Integer> rightPermutationNumberAllBasedKeggPathway2KMap = rightAllMapsWithNumbers.getPermutationNumberAllBasedKeggPathway2KMap();
			
			Map<Long,Integer> rightPermutationNumberTfExonBasedKeggPathway2KMap 		= rightAllMapsWithNumbers.getPermutationNumberTfExonBasedKeggPathway2KMap();
			Map<Long,Integer> rightPermutationNumberTfRegulationBasedKeggPathway2KMap = rightAllMapsWithNumbers.getPermutationNumberTfRegulationBasedKeggPathway2KMap();
			Map<Long,Integer> rightPermutationNumberTfAllBasedKeggPathway2KMap 		= rightAllMapsWithNumbers.getPermutationNumberTfAllBasedKeggPathway2KMap();

			Map<Long,Integer> rightPermutationNumberTfCellLineExonBasedKeggPathway2KMap 		= rightAllMapsWithNumbers.getPermutationNumberTfCellLineExonBasedKeggPathway2KMap();
			Map<Long,Integer> rightPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap = rightAllMapsWithNumbers.getPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap();
			Map<Long,Integer> rightPermutationNumberTfCellLineAllBasedKeggPathway2KMap 		= rightAllMapsWithNumbers.getPermutationNumberTfCellLineAllBasedKeggPathway2KMap();
							
			//DNASE
			if (leftPermutationNumberDnaseCellLineName2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberDnaseCellLineName2KMap,rightPermutationNumberDnaseCellLineName2KMap);
				leftPermutationNumberDnaseCellLineName2KMap = null;
			}
			
			//TF
			if(leftPermutationNumberTfbsNameCellLineName2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberTfbsNameCellLineName2KMap,rightPermutationNumberTfbsNameCellLineName2KMap);
				leftPermutationNumberTfbsNameCellLineName2KMap = null;
			}
			
			//HISTONE
			if(leftPermutationNumberHistoneNameCellLineName2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberHistoneNameCellLineName2KMap,rightPermutationNumberHistoneNameCellLineName2KMap);
				leftPermutationNumberHistoneNameCellLineName2KMap = null;					
			}
			
			//EXON BASED KEGG PATHWAY
			if(leftPermutationNumberExonBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberExonBasedKeggPathway2KMap,rightPermutationNumberExonBasedKeggPathway2KMap);
				leftPermutationNumberExonBasedKeggPathway2KMap = null;					
			}
			
			//REGULATION BASED KEGG PATHWAY
			if (leftPermutationNumberRegulationBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberRegulationBasedKeggPathway2KMap,rightPermutationNumberRegulationBasedKeggPathway2KMap);
				leftPermutationNumberRegulationBasedKeggPathway2KMap = null;
			}
			
			//ALL BASED KEGG PATHWAY
			if (leftPermutationNumberAllBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberAllBasedKeggPathway2KMap,rightPermutationNumberAllBasedKeggPathway2KMap);
				leftPermutationNumberAllBasedKeggPathway2KMap = null;
			}
			
			//TF and EXON BASED KEGG PATHWAY
			if (leftPermutationNumberTfExonBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberTfExonBasedKeggPathway2KMap,rightPermutationNumberTfExonBasedKeggPathway2KMap);
				leftPermutationNumberTfExonBasedKeggPathway2KMap = null;
			}
			
			//TF and REGULATION BASED KEGG PATHWAY
			if (leftPermutationNumberTfRegulationBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberTfRegulationBasedKeggPathway2KMap,rightPermutationNumberTfRegulationBasedKeggPathway2KMap);
				leftPermutationNumberTfRegulationBasedKeggPathway2KMap = null;
			}
			
			//TF and ALL BASED KEGG PATHWAY
			if (leftPermutationNumberTfAllBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberTfAllBasedKeggPathway2KMap,rightPermutationNumberTfAllBasedKeggPathway2KMap);
				leftPermutationNumberTfAllBasedKeggPathway2KMap = null;
			}
			
			//TF and CellLine and EXON BASED KEGG PATHWAY
			if (leftPermutationNumberTfCellLineExonBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberTfCellLineExonBasedKeggPathway2KMap,rightPermutationNumberTfCellLineExonBasedKeggPathway2KMap);
				leftPermutationNumberTfCellLineExonBasedKeggPathway2KMap = null;
			}
			
			//TF and CellLine and REGULATION BASED KEGG PATHWAY
			if (leftPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap,rightPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap);
				leftPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap = null;
			}
			
			//TF and CellLine and ALL BASED KEGG PATHWAY
			if (leftPermutationNumberTfCellLineAllBasedKeggPathway2KMap!=null){
				combineLeftMapandRightMap(leftPermutationNumberTfCellLineAllBasedKeggPathway2KMap,rightPermutationNumberTfCellLineAllBasedKeggPathway2KMap);
				leftPermutationNumberTfCellLineAllBasedKeggPathway2KMap = null;
			}
		
					
			
			//delete AllMaps
			//deleteAllMaps(leftAllMaps);
			leftAllMapsWithNumbers = null;
					
		}//End of combineAllMaps
		
		  
		

		
		
		
		//Accumulate leftMapWithNumbers in the rightMapWithNumbers
		//Accumulate number of overlaps 
		//based on permutationNumber and ElementName
		protected void  combineLeftMapandRightMap(Map<Integer,Integer> leftMapWithNumbers, Map<Integer,Integer> rightMapWithNumbers){
			
			for(Map.Entry<Integer, Integer> entry: leftMapWithNumbers.entrySet()){
				Integer permutationNumberElementName = entry.getKey();
				Integer numberofOverlaps = entry.getValue();
				
				if (rightMapWithNumbers.get(permutationNumberElementName)==null){
					rightMapWithNumbers.put(permutationNumberElementName, numberofOverlaps);
				}else{
					rightMapWithNumbers.put(permutationNumberElementName, rightMapWithNumbers.get(permutationNumberElementName)+numberofOverlaps);
				}
			}
			
			leftMapWithNumbers.clear();
			leftMapWithNumbers = null;
		
		}
		
		protected void  deleteRandomlyGeneratedData(List<InputLine>randomlyGeneratedData){
			for(InputLine inputLine: randomlyGeneratedData){
				inputLine.setChrName(null);
				inputLine= null;
			}
			
			randomlyGeneratedData.clear();
		}
			
		
		protected void deleteMap(Map<String,Integer> map){
			if (map!=null){
				for(Map.Entry<String, Integer> entry: map.entrySet()){
					entry.setValue(null);
					entry= null;			
				}
				map= null;
			}
			
		}
		
		protected void deleteAllMaps(AllMaps allMaps){
			Map<String,Integer> map = allMaps.getPermutationNumberDnaseCellLineName2KMap();
			deleteMap(map);
			map = allMaps.getPermutationNumberTfNameCellLineName2KMap();
			deleteMap(map);
			map = allMaps.getPermutationNumberHistoneNameCellLineName2KMap();
			deleteMap(map);
			map = allMaps.getPermutationNumberExonBasedKeggPathway2KMap();
			deleteMap(map);
			map = allMaps.getPermutationNumberRegulationBasedKeggPathway2KMap();
			deleteMap(map);
			allMaps = null;
		}
		
		
	}
	
	
	public static void readOriginalInputDataLines(List<InputLine> originalInputLines, String inputFileName){
		FileReader fileReader;
		BufferedReader bufferedReader;
		String strLine;
		
		int indexofFirstTab;
		int indexofSecondTab;
		
		ChromosomeName chrName;
		int low;
		int high;
	
		System.out.println("Input data file name is: " + inputFileName);
		
		try {
			fileReader = new FileReader(inputFileName);
			bufferedReader = new BufferedReader(fileReader);
			
			while( (strLine= bufferedReader.readLine())!=null){
				
				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab+1);
				
				chrName = ChromosomeName.convertStringtoEnum(strLine.substring(0, indexofFirstTab));
				
				if (indexofSecondTab>0){
					low = Integer.parseInt(strLine.substring(indexofFirstTab+1, indexofSecondTab));
					high = Integer.parseInt(strLine.substring(indexofSecondTab+1));
				}else{
					low = Integer.parseInt(strLine.substring(indexofFirstTab+1));
					high = low;
				}
				
				InputLine originalInputLine = new InputLine(chrName, low, high);
				originalInputLines.add(originalInputLine);
			
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public  static void partitionDataChromosomeBased(List<InputLine> originalInputLines, Map<ChromosomeName,List<InputLine>> chromosomeBasedOriginalInputLines){
		InputLine line;
		ChromosomeName chrName;
		List<InputLine> list;
		
		
		for(int i = 0; i<originalInputLines.size(); i++){
			
			line = originalInputLines.get(i);
			chrName = line.getChrName();
			list = chromosomeBasedOriginalInputLines.get(chrName);
			
			if (list == null){
				list = new ArrayList<InputLine>();
				list.add(line);
				chromosomeBasedOriginalInputLines.put(chrName, list);
			}else{
				list.add(line);
				chromosomeBasedOriginalInputLines.put(chrName,list);
			}
		}
	}
	
	//@todo
	public static Integer getPermutationNumber(Integer permutationNumberElementNumber){
		return 0;
	}
	
	//@todo
	public static Integer getElementNumber(Integer permutationNumberElementNumber){
		return 0;
	}

	//@todo  I must get permutationNumber and elementNumber from combinedNumber
	//convert permutation augmented name to only name
	//Fill elementName2ALLMap and originalElementName2KMap in convert methods	
	public static void convert(Map<Integer,Integer> permutationElement2KMap,Map<Integer,List<Integer>> elementNumber2AllKMap, Map<Integer,Integer> elementNumber2OriginalKMap){
		
		Integer permutationNumberElementNumber;
		Integer permutationNumber;		
		Integer elementNumber;
		
		Integer numberofOverlaps;
				
		List<Integer> list;
		
		for(Map.Entry<Integer, Integer> entry: permutationElement2KMap.entrySet()){
			
			//example permutationAugmentedName PERMUTATION0_K562
			//@todo get permutationNumber from permutationAugmentedName
			permutationNumberElementNumber = entry.getKey();
			numberofOverlaps = entry.getValue();
			
			permutationNumber = getPermutationNumber(permutationNumberElementNumber);
			elementNumber = getElementNumber(permutationNumberElementNumber);
			
			//example permutationNumber PERMUTATION0
				
			//@todo check this zero value for permutation of original data
			if (Commons.ZERO.equals(permutationNumber)){
				elementNumber2OriginalKMap.put(elementNumber, numberofOverlaps);
			}else{
				list =elementNumber2AllKMap.get(elementNumber);
				
				if(list ==null){
					list = new ArrayList<Integer>();
					list.add(numberofOverlaps);
					elementNumber2AllKMap.put(elementNumber, list);
				}else{
					list.add(numberofOverlaps);
					elementNumber2AllKMap.put(elementNumber, list);
					
				}
			}
			
			
			
		}
	}
	
	public void fillMapfromMap(Map<String,Integer> toMap, Map<String,Integer> fromMap){
		String name;
		Integer numberofOverlaps;
		
		for(Map.Entry<String, Integer> entry: fromMap.entrySet()){
			name = entry.getKey();
			numberofOverlaps = entry.getValue();
			
			toMap.put(name, numberofOverlaps);
			
			
		}
	}
	
//	public void annotateOriginalInputData(String inputDataFileName,Map<String,Integer> originalDnase2KMap, Map<String,Integer> originalTfbs2KMap, Map<String,Integer> originalHistone2KMap, Map<String,Integer> originalExonBasedKeggPathway2KMap, Map<String,Integer> originalRegulationBasedKeggPathway2KMap){
//		AnnotateGivenIntervalsWithGivenParameters annotateIntervals = new AnnotateGivenIntervalsWithGivenParameters();
//		
//		AllName2KMaps name2KMap = annotateIntervals.annotateOriginalData(inputDataFileName);
//		
//		fillMapfromMap(originalDnase2KMap, name2KMap.getDnaseCellLineName2NumberofOverlapsMap());
//		fillMapfromMap(originalTfbs2KMap, name2KMap.getTfbsNameandCellLineName2NumberofOverlapsMap());
//		fillMapfromMap(originalHistone2KMap, name2KMap.getHistoneNameandCellLineName2NumberofOverlapsMap());
//		fillMapfromMap(originalExonBasedKeggPathway2KMap, name2KMap.getExonBasedKeggPathway2NumberofOverlapsMap());
//		fillMapfromMap(originalRegulationBasedKeggPathway2KMap, name2KMap.getRegulationBasedKeggPathway2NumberofOverlapsMap());
//		
//		
//	}
	


	//Empirical P Value and Bonferroni Corrected Empirical P Value
	//List<FunctionalElement> list is filled in this method
	//Using name2AccumulatedKMap and originalName2KMap
	public void calculateEmpricalPValues(Integer numberofComparisons, int numberofRepeats, int numberofPermutations,Map<String, List<Integer>> name2AccumulatedKMap,Map<String, Integer> originalName2KMap, List<FunctionalElement> list){
		
			
		String  originalName;
		Integer originalNumberofOverlaps;
		List<Integer> listofNumberofOverlaps;
		Integer numberofOverlaps;
		int  numberofPermutationsHavingOverlapsGreaterThanorEqualto = 0;
		Float empiricalPValue;
		Float bonferroniCorrectedEmpiricalPValue;
		
		FunctionalElement functionalElement;
					
		
		//only consider the names in the original name 2 k map
		for(Map.Entry<String, Integer> entry: originalName2KMap.entrySet()){
			originalName = entry.getKey();
			originalNumberofOverlaps = entry.getValue();
			
			listofNumberofOverlaps = name2AccumulatedKMap.get(originalName);

			//Initialise numberofPermutationsHavingOverlapsGreaterThanorEqualto to zero for each original name 
			numberofPermutationsHavingOverlapsGreaterThanorEqualto = 0;
			
			if (listofNumberofOverlaps!=null){
				for(int i =0; i<listofNumberofOverlaps.size(); i++){
					
					numberofOverlaps =listofNumberofOverlaps.get(i);
					
					if(numberofOverlaps >= originalNumberofOverlaps){
						numberofPermutationsHavingOverlapsGreaterThanorEqualto++;
					}
				}//end of for
			}//end of if
			
			empiricalPValue = (numberofPermutationsHavingOverlapsGreaterThanorEqualto * 1.0f)/(numberofRepeats * numberofPermutations);
			bonferroniCorrectedEmpiricalPValue = ((numberofPermutationsHavingOverlapsGreaterThanorEqualto* 1.0f)/(numberofRepeats * numberofPermutations)) * numberofComparisons;
			
			if(bonferroniCorrectedEmpiricalPValue>=1){
				bonferroniCorrectedEmpiricalPValue = 1.0f;
			}
			
			functionalElement = new FunctionalElement();
			functionalElement.setName(originalName);
			functionalElement.setEmpiricalPValue(empiricalPValue);
			functionalElement.setBonferroniCorrectedEmpiricalPValue(bonferroniCorrectedEmpiricalPValue);
			
			//18 FEB 2014
			functionalElement.setOriginalNumberofOverlaps(originalNumberofOverlaps);
			functionalElement.setNumberofPermutationsHavingOverlapsGreaterThanorEqualto(numberofPermutationsHavingOverlapsGreaterThanorEqualto);
			functionalElement.setNumberofPermutations(numberofRepeats * numberofPermutations);
			functionalElement.setNumberofComparisons(numberofComparisons);
			
			list.add(functionalElement);
				
		}//end of for
			
		
	}
		
		
		
	
	public static void generateAnnotationTasks(ChromosomeName chromName, List<AnnotationTask> listofAnnotationTasks,int runNumber,int numberofPermutationsinThisRun){
		
		
			for(int permutationNumber = 1; permutationNumber<= numberofPermutationsinThisRun; permutationNumber++){
				listofAnnotationTasks.add(new AnnotationTask(chromName, (runNumber-1)* Commons.NUMBER_OF_PERMUTATIONS_IN_EACH_RUN + permutationNumber));
			}
	}
	
	
	public static void generateAnnotationTaskforOriginalData(ChromosomeName chromName, List<AnnotationTask> listofAnnotationTasks,Integer originalDataPermutationNumber){
		listofAnnotationTasks.add(new AnnotationTask(chromName, 0));
	}
	
	public static IntervalTree generateDnaseIntervalTreeWithNumbers(String dataFolder,ChromosomeName chromName){		
		return AnnotateGivenIntervalsWithGivenParameters.createDnaseIntervalTreeWithNumbers(dataFolder,chromName);	
	}
	
	public static IntervalTree generateTfbsIntervalTree(String dataFolder,ChromosomeName chromName){		
		return AnnotateGivenIntervalsWithGivenParameters.createTfbsIntervalTree(dataFolder,chromName);	
	}
	
	public static IntervalTree generateHistoneIntervalTree(String dataFolder,ChromosomeName chromName){		
		return AnnotateGivenIntervalsWithGivenParameters.createHistoneIntervalTree(dataFolder,chromName);	
	}
	
	public static IntervalTree generateUcscRefSeqGeneIntervalTree(String dataFolder,ChromosomeName chromName){		
		return AnnotateGivenIntervalsWithGivenParameters.createUcscRefSeqGenesIntervalTree(dataFolder,chromName);	
	}
	
	public void generateIntervalTrees(String outputFolder,ChromosomeName chromName, List<IntervalTree> listofIntervalTrees){
		IntervalTree dnaseIntervalTree;
		IntervalTree tfbsIntervalTree ;
		IntervalTree histoneIntervalTree;
		IntervalTree ucscRefSeqGeneIntervalTree;
		
				
		dnaseIntervalTree			= AnnotateGivenIntervalsWithGivenParameters.createDnaseIntervalTree(outputFolder,chromName);
		tfbsIntervalTree 			= AnnotateGivenIntervalsWithGivenParameters.createTfbsIntervalTree(outputFolder,chromName);
		histoneIntervalTree  		= AnnotateGivenIntervalsWithGivenParameters.createHistoneIntervalTree(outputFolder,chromName);
		ucscRefSeqGeneIntervalTree 	= AnnotateGivenIntervalsWithGivenParameters.createUcscRefSeqGenesIntervalTree(outputFolder,chromName);
		
		//order is important
		listofIntervalTrees.add(dnaseIntervalTree);
		listofIntervalTrees.add(tfbsIntervalTree);
		listofIntervalTrees.add(histoneIntervalTree);
		listofIntervalTrees.add(ucscRefSeqGeneIntervalTree);
		
	}

	public static void closeBufferedWriters(Map<String,BufferedWriter> permutationNumber2BufferedWriterHashMap){
		
		BufferedWriter bufferedWriter = null;
		try {
			
			for(Map.Entry<String,BufferedWriter> entry: permutationNumber2BufferedWriterHashMap.entrySet() ){
				bufferedWriter = entry.getValue();
				bufferedWriter.close();				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void closeBuferedWriterswithIntegerKey(Map<Integer,BufferedWriter> permutationNumber2BufferedWriterHashMap){
		
		BufferedWriter bufferedWriter = null;
		try {
			
			for(Map.Entry<Integer,BufferedWriter> entry: permutationNumber2BufferedWriterHashMap.entrySet() ){
				bufferedWriter = entry.getValue();
				bufferedWriter.close();				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void writeAnnotationstoFiles(String outputFolder,Map<Integer,Integer> permutationNumberElementNumber2KMap, Map<String,BufferedWriter> permutationNumber2BufferedWriterHashMap, String folderName, String extraFileName){
		
		Integer permutationNumberElementNumber;
		Integer permutationNumber;
		Integer elementNumber;
		
		Integer numberofOverlaps;
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		for(Map.Entry<Integer, Integer> entry: permutationNumberElementNumber2KMap.entrySet()){
			permutationNumberElementNumber = entry.getKey();
			numberofOverlaps = entry.getValue();
			
			permutationNumber = getPermutationNumber(permutationNumberElementNumber);
			elementNumber =  getElementNumber(permutationNumberElementNumber);
			
			bufferedWriter = permutationNumber2BufferedWriterHashMap.get(permutationNumber) ;
			
			try {
				
				if (bufferedWriter==null){
						fileWriter = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATION + folderName + permutationNumber +  "_" + extraFileName + ".txt");
						bufferedWriter = new BufferedWriter(fileWriter);
						//@todo
						permutationNumber2BufferedWriterHashMap.put(permutationNumber.toString(), bufferedWriter);							
				}
				bufferedWriter.write(elementNumber +"\t" + numberofOverlaps +System.getProperty("line.separator"));
				
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}//End of for
		
	}
	
	//Accumulate chromosomeBasedName2KMap results in accumulatedName2KMap
	//Accumulate number of overlaps coming from each chromosome
	//based on permutationNumber and ElementName
	public static void accumulate(Map<Integer,Integer> chromosomeBasedName2KMap, Map<Integer,Integer> accumulatedName2KMap){
		Integer permutationNumberElementName;
		Integer numberofOverlaps;
		
		for(Map.Entry<Integer, Integer> entry: chromosomeBasedName2KMap.entrySet()){
			permutationNumberElementName = entry.getKey();
			numberofOverlaps = entry.getValue();
			
			
			if (accumulatedName2KMap.get(permutationNumberElementName)==null){
				accumulatedName2KMap.put(permutationNumberElementName, numberofOverlaps);
			}else{
				accumulatedName2KMap.put(permutationNumberElementName, accumulatedName2KMap.get(permutationNumberElementName) + numberofOverlaps);
				
			}
		}
		
	}
	
	
	
	public static void accumulate(AllMapsWithNumbers chromosomeBasedAllMaps, AllMapsWithNumbers accumulatedAllMaps){
		
		//Dnase
		accumulate(chromosomeBasedAllMaps.getPermutationNumberDnaseCellLineName2KMap(), accumulatedAllMaps.getPermutationNumberDnaseCellLineName2KMap());
		
		//Tfbs
		accumulate(chromosomeBasedAllMaps.getPermutationNumberTfNameCellLineName2KMap(), accumulatedAllMaps.getPermutationNumberTfNameCellLineName2KMap());
		
		//Histone
		accumulate(chromosomeBasedAllMaps.getPermutationNumberHistoneNameCellLineName2KMap(), accumulatedAllMaps.getPermutationNumberHistoneNameCellLineName2KMap());
		
		//Exon Based Kegg Pathway
		accumulate(chromosomeBasedAllMaps.getPermutationNumberExonBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberExonBasedKeggPathway2KMap());
		
		//Regulation Based Kegg Pathway
		accumulate(chromosomeBasedAllMaps.getPermutationNumberRegulationBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberRegulationBasedKeggPathway2KMap());
				
	}
	
	//Accumulate chromosomeBasedAllMaps in accumulatedAllMaps
	//Coming from each chromosome
	public static void accumulate(AllMapsWithNumbers chromosomeBasedAllMaps, AllMapsWithNumbers accumulatedAllMaps, String annotationType){
		
		if (Commons.DNASE_ANNOTATION.equals(annotationType)){
			//Dnase
			accumulate(chromosomeBasedAllMaps.getPermutationNumberDnaseCellLineName2KMap(), accumulatedAllMaps.getPermutationNumberDnaseCellLineName2KMap());
		}else if (Commons.TFBS_ANNOTATION.equals(annotationType)){
			//Tfbs
			accumulate(chromosomeBasedAllMaps.getPermutationNumberTfNameCellLineName2KMap(), accumulatedAllMaps.getPermutationNumberTfNameCellLineName2KMap());
		}else if (Commons.HISTONE_ANNOTATION.equals(annotationType)){
			//Histone
			accumulate(chromosomeBasedAllMaps.getPermutationNumberHistoneNameCellLineName2KMap(), accumulatedAllMaps.getPermutationNumberHistoneNameCellLineName2KMap());
		}else if (Commons.UCSC_REFSEQ_GENE_ANNOTATION.equals(annotationType)){
			//Exon Based Kegg Pathway
			accumulate(chromosomeBasedAllMaps.getPermutationNumberExonBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberExonBasedKeggPathway2KMap());
			
			//Regulation Based Kegg Pathway
			accumulate(chromosomeBasedAllMaps.getPermutationNumberRegulationBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberRegulationBasedKeggPathway2KMap());
			
			//All Based Kegg Pathway
			accumulate(chromosomeBasedAllMaps.getPermutationNumberAllBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberAllBasedKeggPathway2KMap());
	
		}else if (Commons.TF_CELLLINE_KEGG_PATHWAY_ANNOTATION.equals(annotationType)){
			//TF and CellLine and Kegg Pathway Annotation
			accumulate(chromosomeBasedAllMaps.getPermutationNumberTfCellLineExonBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberTfCellLineExonBasedKeggPathway2KMap());
			accumulate(chromosomeBasedAllMaps.getPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap());
			accumulate(chromosomeBasedAllMaps.getPermutationNumberTfCellLineAllBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberTfCellLineAllBasedKeggPathway2KMap());
		}else if (Commons.TF_KEGG_PATHWAY_ANNOTATION.equals(annotationType)){
			//TF and Kegg Pathway Annotation
			accumulate(chromosomeBasedAllMaps.getPermutationNumberTfExonBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberTfExonBasedKeggPathway2KMap());
			accumulate(chromosomeBasedAllMaps.getPermutationNumberTfRegulationBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberTfRegulationBasedKeggPathway2KMap());
			accumulate(chromosomeBasedAllMaps.getPermutationNumberTfAllBasedKeggPathway2KMap(), accumulatedAllMaps.getPermutationNumberTfAllBasedKeggPathway2KMap());
			//NEW FUNCIONALITY
		}
		
						
	}
	
	
	
	public void deleteIntervalTrees(List<IntervalTree> listofIntervalTrees){
		IntervalTree dnaseIntervalTree = listofIntervalTrees.get(0);
		IntervalTree tfbsIntervalTree = listofIntervalTrees.get(1);
		IntervalTree histoneIntervalTree = listofIntervalTrees.get(2);
		IntervalTree ucscRefSeqGenesIntervalTree = listofIntervalTrees.get(3);
		
		IntervalTree.deleteNodesofIntervalTree(dnaseIntervalTree.getRoot());
		IntervalTree.deleteNodesofIntervalTree(tfbsIntervalTree.getRoot());
		IntervalTree.deleteNodesofIntervalTree(histoneIntervalTree.getRoot());
		IntervalTree.deleteNodesofIntervalTree(ucscRefSeqGenesIntervalTree.getRoot());
		
//		dnaseIntervalTree = new IntervalTree();
		dnaseIntervalTree 	= null;
		tfbsIntervalTree 	= null;
		histoneIntervalTree = null;
		ucscRefSeqGenesIntervalTree	= null;
	}
	
	
	public static void deleteIntervalTree(IntervalTree intervalTree){
		
		IntervalTree.deleteNodesofIntervalTree(intervalTree.getRoot());
		intervalTree 	= null;
	}
	
	
	public static void deleteAnnotationTasks(List<AnnotationTask> listofAnnotationTasks){
		for(AnnotationTask annotationTask : listofAnnotationTasks){
			annotationTask.setChromName(null);
			annotationTask = null;
		}
	}
	
	public void deleteGCCharArray(char[] gcCharArray){
		gcCharArray = null;
	}
	
	public void deleteMapabilityFloatArray(float[] mapabilityFloatArray){
		mapabilityFloatArray = null;
	}
	
	
	
	//NEW FUNCIONALITY ADDED
	//First Generate random data concurrently
	//then annotate permutations concurrently
	//the tasks are executed
	//after all the parallel work is done
	//results are written to files
	public static void annotateAllPermutationsInThreads(String outputFolder,String dataFolder,int NUMBER_OF_AVAILABLE_PROCESSORS,int runNumber, int numberofPermutationsinThisRun,List<InputLine> allOriginalInputLines, Map<Integer,List<Integer>> dnase2AllKMap,Map<Integer,List<Integer>> tfbs2AllKMap,Map<Integer,List<Integer>> histone2AllKMap,Map<Integer,List<Integer>> exonBasedKeggPathway2AllKMap,Map<Integer,List<Integer>> regulationBasedKeggPathway2AllKMap,Map<Integer,List<Integer>> allBasedKeggPathway2AllKMap,Map<Integer,List<Integer>> tfExonBasedKeggPathway2AllKMap, Map<Integer,List<Integer>> tfRegulationBasedKeggPathway2AllKMap,Map<Integer,List<Integer>> tfAllBasedKeggPathway2AllKMap,Map<Integer,List<Integer>> tfCellLineExonBasedKeggPathway2AllKMap, Map<Integer,List<Integer>> tfCellLineRegulationBasedKeggPathway2AllKMap,Map<Integer,List<Integer>> tfCellLineAllBasedKeggPathway2AllKMap, String generateRandomDataMode, String writeGeneratedRandomDataMode,String writePermutationBasedandParametricBasedAnnotationResultMode,String writePermutationBasedAnnotationResultMode,Map<Integer,Integer> originalDnase2KMap,Map<Integer,Integer> originalTfbs2KMap,Map<Integer,Integer> originalHistone2KMap,Map<Integer,Integer> originalExonBasedKeggPathway2KMap,Map<Integer,Integer> originalRegulationBasedKeggPathway2KMap,Map<Integer,Integer> originalAllBasedKeggPathway2KMap, Map<Integer,Integer> originalTfExonBasedKeggPathway2KMap,Map<Integer,Integer> originalTfRegulationBasedKeggPathway2KMap,Map<Integer,Integer> originalTfAllBasedKeggPathway2KMap,Map<Integer,Integer> originalTfCellLineExonBasedKeggPathway2KMap,Map<Integer,Integer> originalTfCellLineRegulationBasedKeggPathway2KMap,Map<Integer,Integer> originalTfCellLineAllBasedKeggPathway2KMap, String dnaseEnrichment, String histoneEnrichment, String tfEnrichment, String keggPathwayEnrichment, String tfKeggPathwayEnrichment, String tfCellLineKeggPathwayEnrichment,int overlapDefinition){
		
		//allMaps stores chromosome based results
		AllMapsWithNumbers allMapsWithNumbers = new AllMapsWithNumbers();
		
		//accumulatedAllMaps stores accumulated results coming from each chromosome
		AllMapsWithNumbers accumulatedAllMapsWithNumbers = new AllMapsWithNumbers();

		
		
		/******************************************************************************************************/
		/****************************ACCUMULATED ALL MAPS******************************************************/
		accumulatedAllMapsWithNumbers.setPermutationNumberDnaseCellLineNumber2KMap(new HashMap<Long,Integer>());
		accumulatedAllMapsWithNumbers.setPermutationNumberTfNameCellLineName2KMap(new HashMap<Integer,Integer>());
		accumulatedAllMapsWithNumbers.setPermutationNumberHistoneNameCellLineName2KMap(new HashMap<Integer,Integer>());
		
		accumulatedAllMapsWithNumbers.setPermutationNumberExonBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
		accumulatedAllMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
		accumulatedAllMapsWithNumbers.setPermutationNumberAllBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
		
		//Will be used 	for Tf and KEGG Pathway Enrichment
		accumulatedAllMapsWithNumbers.setPermutationNumberTfExonBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
		accumulatedAllMapsWithNumbers.setPermutationNumberTfRegulationBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
		accumulatedAllMapsWithNumbers.setPermutationNumberTfAllBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
			
		//Will be used 	for Tf and CellLine and KEGG Pathway Enrichment
		accumulatedAllMapsWithNumbers.setPermutationNumberTfCellLineExonBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
		accumulatedAllMapsWithNumbers.setPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
		accumulatedAllMapsWithNumbers.setPermutationNumberTfCellLineAllBasedKeggPathway2KMap(new HashMap<Integer,Integer>());
		/****************************ACCUMULATED ALL MAPS******************************************************/
		/******************************************************************************************************/
		
		
		
		/******************************************************************************************************/		
		/**************************USED FOR WRITING PERMUTATION BASED RESULTS**********************************/		
		//STRING to BufferedWriter
		Map<String,BufferedWriter> permutationNumber2DnaseBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		Map<String,BufferedWriter> permutationNumber2TfbsBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		Map<String,BufferedWriter> permutationNumber2HistoneBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		
		Map<String,BufferedWriter> permutationNumber2ExonBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		Map<String,BufferedWriter> permutationNumber2RegulationBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		Map<String,BufferedWriter> permutationNumber2AllBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		
		//Will be used 	for Tf and KEGG Pathway Enrichment
		Map<String,BufferedWriter> permutationNumber2TfExonBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		Map<String,BufferedWriter> permutationNumber2TfRegulationBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		Map<String,BufferedWriter> permutationNumber2TfAllBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		
		//Will be used 	for Tf and CellLine and KEGG Pathway Enrichment
		Map<String,BufferedWriter> permutationNumber2TfCellLineExonBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		Map<String,BufferedWriter> permutationNumber2TfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		Map<String,BufferedWriter> permutationNumber2TfCellLineAllBasedKeggPathwayBufferedWriterHashMap = new HashMap<String,BufferedWriter>();
		/**************************USED FOR WRITING PERMUTATION BASED RESULTS**********************************/		
		/******************************************************************************************************/
		
		
		
		
		/******************************************************************************************************/		
		/*******************************ORIGINAL INPUT LINES***************************************************/		
		Map<ChromosomeName,List<InputLine>> originalInputLinesMap = new HashMap<ChromosomeName,List<InputLine>>();
		/*******************************ORIGINAL INPUT LINES***************************************************/		
		/******************************************************************************************************/		
		
		//todo test it 
		//SecureRandom myrandom = new SecureRandom();

		List<AnnotationTask> listofAnnotationTasks 	= null;
		IntervalTree intervalTree 					= null;
		
		//For NEW FUNCTIONALITY
		IntervalTree tfIntervalTree 				= null;
		IntervalTree ucscRefSeqGenesIntervalTree 	= null;
		
		//For efficiency
		//Fill this map only once.
		//@todo This map can be <Integer,List<Integer>>
		//Kegg Pathway 2 Integer is already ready
		//NCBI Gene Id is Integer
		Map<String,List<String>> geneId2KeggPathwayMap = new HashMap<String, List<String>>();
		KeggPathwayUtility.createNcbiGeneId2KeggPathwayMap(dataFolder,Commons.KEGG_PATHWAY_2_NCBI_GENE_IDS_INPUT_FILE, geneId2KeggPathwayMap);
	
		GCCharArray gcCharArray						= null;
    	MapabilityFloatArray mapabilityFloatArray 	= null;
    	List<Integer> hg19ChromosomeSizes 			= new ArrayList<Integer>();
    	
		//Partition the original input data lines in a chromosome based manner
		partitionDataChromosomeBased(allOriginalInputLines,originalInputLinesMap);
				
    	hg19.GRCh37Hg19Chromosome.initializeChromosomeSizes(hg19ChromosomeSizes);
    	//get the hg19 chromosome sizes
    	hg19.GRCh37Hg19Chromosome.getHg19ChromosomeSizes(hg19ChromosomeSizes, dataFolder,Commons.HG19_CHROMOSOME_SIZES_INPUT_FILE);
		
		ChromosomeName chromName;
    	int chromSize;
    	List<InputLine> chromosomeBaseOriginalInputLines;
    	Map<Integer,List<InputLine>> permutationNumber2RandomlyGeneratedDataHashMap = new HashMap<Integer,List<InputLine>>();
    	
    	AnnotateWithNumbers annotateWithNumbers;
    	GenerateRandomData generateRandomData;
    	ForkJoinPool pool = new ForkJoinPool(NUMBER_OF_AVAILABLE_PROCESSORS);
    	
    	long startTimeAllPermutations = System.currentTimeMillis();
    		       		
		System.out.println("Run Number: " + runNumber);

		//for each chromosome
		for(int i= 1 ; i<=Commons.NUMBER_OF_CHROMOSOMES_HG19; i++){
			
    		chromName = GRCh37Hg19Chromosome.getChromosomeName(i);
			chromSize = hg19ChromosomeSizes.get(i-1);
			
			System.out.println("chromosome name:" + chromName + " chromosome size: " + chromSize);
			chromosomeBaseOriginalInputLines 	= originalInputLinesMap.get(chromName);
							
			if (chromosomeBaseOriginalInputLines!=null){
										
				//initialize list of annotation tasks
				listofAnnotationTasks = new ArrayList<AnnotationTask>();
				
				gcCharArray = new GCCharArray();
				mapabilityFloatArray = new MapabilityFloatArray();
			
				//generate tasks
				System.out.println("Generate annotation tasks has started.");
				generateAnnotationTasks(chromName,listofAnnotationTasks,runNumber,numberofPermutationsinThisRun);
				System.out.println("Generate annotation tasks has ended.");
				
				//Fill gcCharArray and 	mapabilityFloatArray		
				if (Commons.GENERATE_RANDOM_DATA_WITH_MAPPABILITY_AND_GC_CONTENT.equals(generateRandomDataMode)){
					gcCharArray = ChromosomeBasedGCArray.getChromosomeGCArray(dataFolder,chromName,chromSize);
					mapabilityFloatArray = ChromosomeBasedMapabilityArray.getChromosomeMapabilityArray(dataFolder,chromName,chromSize);
				}
				
				/******************************************************************************************************/		
				/**********************************GENERATE RANDOM DATA STARTS*****************************************/						
				System.out.println("Generate Random Data and Annotate has started.");	
			    long startTime = System.currentTimeMillis();
			    
			    System.out.println("First Generate Random Data");
			    System.out.println("Generate Random Data has started.");
 			    //First generate Random Data
			    generateRandomData = new GenerateRandomData(outputFolder,chromSize,chromName,chromosomeBaseOriginalInputLines,generateRandomDataMode,writeGeneratedRandomDataMode,Commons.ZERO, listofAnnotationTasks.size(),listofAnnotationTasks,gcCharArray,mapabilityFloatArray);
			    permutationNumber2RandomlyGeneratedDataHashMap = pool.invoke(generateRandomData);
			    System.out.println("Generate Random Data has ended.");
			    /**********************************GENERATE RANDOM DATA ENDS*****************************************/						
			    /******************************************************************************************************/		
			    
			    
			    
			    /******************************************************************************************************/		
				/**********************************GENERATE ANNOTATION TASK FOR GIVEN ORIGINAL DATA STARTS*************/						
			    //In each run
			    //generate task for original data
		     	//After Random Data Generation has been ended
				//generate task for User Given Original Data(Genomic Variants)
			    //Since we do not need random data, there is  original data is given
				generateAnnotationTaskforOriginalData(chromName,listofAnnotationTasks,Commons.ORIGINAL_DATA_PERMUTATION_NUMBER);
				
				//Add the original data to permutationNumber2RandomlyGeneratedDataHashMap
				permutationNumber2RandomlyGeneratedDataHashMap.put(Commons.ORIGINAL_DATA_PERMUTATION_NUMBER, chromosomeBaseOriginalInputLines);
			 	    
				gcCharArray = null;				
				mapabilityFloatArray = null;
				/**********************************GENERATE ANNOTATION TASK FOR GIVEN ORIGINAL DATA ENDS*************/						
			    /******************************************************************************************************/		
			   
				
				
				/******************************************************************************************************/		
				/*****************************ANNOTATE PERMUTATIONS STARTS*********************************************/					
				System.out.println("Annotate has started.");
				
				if (dnaseEnrichment.equals(Commons.DO_DNASE_ENRICHMENT)){
					
					//dnase
    			    //generate dnase interval tree
    			    intervalTree = generateDnaseIntervalTreeWithNumbers(dataFolder,chromName);
    			    annotateWithNumbers = new AnnotateWithNumbers(outputFolder,chromSize,chromName,permutationNumber2RandomlyGeneratedDataHashMap,runNumber,numberofPermutationsinThisRun,writePermutationBasedandParametricBasedAnnotationResultMode,Commons.ZERO, listofAnnotationTasks.size(),listofAnnotationTasks,intervalTree,null,Commons.DNASE_ANNOTATION,null,null,overlapDefinition);
    				allMapsWithNumbers = pool.invoke(annotateWithNumbers);    			    
    			    accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers, Commons.DNASE_ANNOTATION);
    			    allMapsWithNumbers = null;
    			    deleteIntervalTree(intervalTree);
    			    intervalTree = null;
        	
				}
			 		
				if (histoneEnrichment.equals(Commons.DO_HISTONE_ENRICHMENT)){
				    //histone
    			    //generate histone interval tree
    			    intervalTree = generateHistoneIntervalTree(dataFolder,chromName);
    			    annotateWithNumbers = new AnnotateWithNumbers(outputFolder,chromSize,chromName,permutationNumber2RandomlyGeneratedDataHashMap,runNumber,numberofPermutationsinThisRun,writePermutationBasedandParametricBasedAnnotationResultMode,Commons.ZERO, listofAnnotationTasks.size(),listofAnnotationTasks,intervalTree,null,Commons.HISTONE_ANNOTATION,null,null,overlapDefinition);
    			    allMapsWithNumbers = pool.invoke(annotateWithNumbers);    			    
    			    accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.HISTONE_ANNOTATION);
    			    allMapsWithNumbers = null;
    			    deleteIntervalTree(intervalTree);
    			    intervalTree = null;

				}
	    			    
				if ((tfEnrichment.equals(Commons.DO_TF_ENRICHMENT)) && !(tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)) && !(tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT))){
    			    //tf
    			    //generate tf interval tree
    			    intervalTree = generateTfbsIntervalTree(dataFolder,chromName);
    			    annotateWithNumbers = new AnnotateWithNumbers(outputFolder,chromSize,chromName,permutationNumber2RandomlyGeneratedDataHashMap,runNumber,numberofPermutationsinThisRun,writePermutationBasedandParametricBasedAnnotationResultMode,Commons.ZERO, listofAnnotationTasks.size(),listofAnnotationTasks,intervalTree,null,Commons.TFBS_ANNOTATION,null,null,overlapDefinition);
    			    allMapsWithNumbers = pool.invoke(annotateWithNumbers);    			    
    			    accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.TFBS_ANNOTATION);
    			    allMapsWithNumbers = null;
    			    deleteIntervalTree(intervalTree);
    			    intervalTree = null;

				}
				
				
				if (keggPathwayEnrichment.equals(Commons.DO_KEGGPATHWAY_ENRICHMENT) && !(tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)) && !(tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT))){
   			    //ucsc RefSeq Genes
    			    //generate UCSC RefSeq Genes interval tree
    			    intervalTree = generateUcscRefSeqGeneIntervalTree(dataFolder,chromName);
    			    annotateWithNumbers = new AnnotateWithNumbers(outputFolder,chromSize,chromName,permutationNumber2RandomlyGeneratedDataHashMap,runNumber,numberofPermutationsinThisRun,writePermutationBasedandParametricBasedAnnotationResultMode,Commons.ZERO, listofAnnotationTasks.size(),listofAnnotationTasks,intervalTree,null,Commons.UCSC_REFSEQ_GENE_ANNOTATION,null,geneId2KeggPathwayMap,overlapDefinition);
    			    allMapsWithNumbers = pool.invoke(annotateWithNumbers);    			    
    			    accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.UCSC_REFSEQ_GENE_ANNOTATION);
    			    allMapsWithNumbers = null;
    			    deleteIntervalTree(intervalTree);
    			    intervalTree = null;	

				}
	
				
				if (tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)){
					
					//New Functionality START
    				//tfbs 
    				//Kegg Pathway (exon Based, regulation Based, all Based)
    				//tfbs and Kegg Pathway (exon Based, regulation Based, all Based)
    				//generate tf interval tree and ucsc refseq genes interval tree
    				tfIntervalTree = generateTfbsIntervalTree(dataFolder,chromName);
    				ucscRefSeqGenesIntervalTree = generateUcscRefSeqGeneIntervalTree(dataFolder,chromName);
    				annotateWithNumbers = new AnnotateWithNumbers(outputFolder,chromSize,chromName,permutationNumber2RandomlyGeneratedDataHashMap,runNumber,numberofPermutationsinThisRun,writePermutationBasedandParametricBasedAnnotationResultMode,Commons.ZERO, listofAnnotationTasks.size(),listofAnnotationTasks,tfIntervalTree,ucscRefSeqGenesIntervalTree,Commons.TF_KEGG_PATHWAY_ANNOTATION,tfKeggPathwayEnrichment,geneId2KeggPathwayMap,overlapDefinition);
    				allMapsWithNumbers = pool.invoke(annotateWithNumbers);    
      				//Will be used 	for Tf and KeggPathway Enrichment or
      				//				for Tf and CellLine and KeggPathway Enrichment
					accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.TF_KEGG_PATHWAY_ANNOTATION);	
      			    accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.TFBS_ANNOTATION);
      			    accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.UCSC_REFSEQ_GENE_ANNOTATION);
      			  
      			    allMapsWithNumbers = null;
      			    deleteIntervalTree(tfIntervalTree);
      			    deleteIntervalTree(ucscRefSeqGenesIntervalTree);
      			    tfIntervalTree = null;
      			    ucscRefSeqGenesIntervalTree = null;	
      				//New Functionality END
    			
				}else if (tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT)){
    					
    					//New Functionality START
        				//tfbs 
        				//Kegg Pathway (exon Based, regulation Based, all Based)
        				//tfbs and Kegg Pathway (exon Based, regulation Based, all Based)
        				//generate tf interval tree and ucsc refseq genes interval tree
        				tfIntervalTree = generateTfbsIntervalTree(dataFolder,chromName);
        				ucscRefSeqGenesIntervalTree = generateUcscRefSeqGeneIntervalTree(dataFolder,chromName);
        				annotateWithNumbers = new AnnotateWithNumbers(outputFolder,chromSize,chromName,permutationNumber2RandomlyGeneratedDataHashMap,runNumber,numberofPermutationsinThisRun,writePermutationBasedandParametricBasedAnnotationResultMode,Commons.ZERO, listofAnnotationTasks.size(),listofAnnotationTasks,tfIntervalTree,ucscRefSeqGenesIntervalTree,Commons.TF_CELLLINE_KEGG_PATHWAY_ANNOTATION,tfCellLineKeggPathwayEnrichment,geneId2KeggPathwayMap,overlapDefinition);
        				allMapsWithNumbers = pool.invoke(annotateWithNumbers);    
          				//Will be used 	for Tf and KeggPathway Enrichment or
          				//				for Tf and CellLine and KeggPathway Enrichment
    					accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.TF_CELLLINE_KEGG_PATHWAY_ANNOTATION);	
          			    accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.TFBS_ANNOTATION);
          			    accumulate(allMapsWithNumbers, accumulatedAllMapsWithNumbers,Commons.UCSC_REFSEQ_GENE_ANNOTATION);
          			  
          			    allMapsWithNumbers = null;
          			    deleteIntervalTree(tfIntervalTree);
          			    deleteIntervalTree(ucscRefSeqGenesIntervalTree);
          			    tfIntervalTree = null;
          			    ucscRefSeqGenesIntervalTree = null;	
          				//New Functionality END
        			
    			}
    				
				
       		
				System.out.println("Annotate has ended.");
				/*****************************ANNOTATE PERMUTATIONS ENDS***********************************************/					
				/******************************************************************************************************/		


			    long endTime = System.currentTimeMillis();
				System.out.println("RunNumber: " + runNumber  + " For Chromosome: " + chromName + " Annotation of " + numberofPermutationsinThisRun + " permutations took  " + (endTime - startTime) + " milliseconds.");
				System.out.println("Generate Random Data and Annotate has ended.");
			
				System.out.println("Deletion of the tasks has started.");
				deleteAnnotationTasks(listofAnnotationTasks);
				System.out.println("Deletion of the tasks has ended.");
		
			    permutationNumber2RandomlyGeneratedDataHashMap.clear();
			    permutationNumber2RandomlyGeneratedDataHashMap= null;
				listofAnnotationTasks = null;
				annotateWithNumbers = null;
				generateRandomData = null;
				chromosomeBaseOriginalInputLines =null;
				
				
			}//end of if: chromosome based input lines is not null
			
    	}//End of for: each chromosome
    	
    			
    	pool.shutdown();
		
		if (pool.isTerminated()){
			System.out.println("ForkJoinPool is terminated ");
			
		}   	
		
		long endTimeAllPermutations = System.currentTimeMillis();
	
		System.out.println("RUN_NUMBER: " + runNumber + " NUMBER_OF_PERMUTATIONS:  "+ numberofPermutationsinThisRun  + " took "  + (endTimeAllPermutations - startTimeAllPermutations) + " milliseconds.");
	
		/************************************************************************************************************************/
		/*****************************************CONVERT************************************************************************/
		//convert permutation augmented name to only name
		//Fill elementName2ALLMap and originalElementName2KMap in convert methods
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberDnaseCellLineName2KMap(),dnase2AllKMap,originalDnase2KMap);
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberTfNameCellLineName2KMap(),tfbs2AllKMap,originalTfbs2KMap);
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberHistoneNameCellLineName2KMap(),histone2AllKMap,originalHistone2KMap);
		
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberExonBasedKeggPathway2KMap(),exonBasedKeggPathway2AllKMap,originalExonBasedKeggPathway2KMap);
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberRegulationBasedKeggPathway2KMap(),regulationBasedKeggPathway2AllKMap,originalRegulationBasedKeggPathway2KMap);
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberAllBasedKeggPathway2KMap(),allBasedKeggPathway2AllKMap,originalAllBasedKeggPathway2KMap);
			
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberTfExonBasedKeggPathway2KMap(),tfExonBasedKeggPathway2AllKMap,originalTfExonBasedKeggPathway2KMap);
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberTfRegulationBasedKeggPathway2KMap(),tfRegulationBasedKeggPathway2AllKMap,originalTfRegulationBasedKeggPathway2KMap);
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberTfAllBasedKeggPathway2KMap(),tfAllBasedKeggPathway2AllKMap,originalTfAllBasedKeggPathway2KMap);
		
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberTfCellLineExonBasedKeggPathway2KMap(),tfCellLineExonBasedKeggPathway2AllKMap,originalTfCellLineExonBasedKeggPathway2KMap);
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap(),tfCellLineRegulationBasedKeggPathway2AllKMap,originalTfCellLineRegulationBasedKeggPathway2KMap);
		convert(accumulatedAllMapsWithNumbers.getPermutationNumberTfCellLineAllBasedKeggPathway2KMap(),tfCellLineAllBasedKeggPathway2AllKMap,originalTfCellLineAllBasedKeggPathway2KMap);
		/*****************************************CONVERT************************************************************************/
		/************************************************************************************************************************/
		
				
		/************************************************************************************************************************/
		/**********************WRITE PERMUTATION BASED ANNOTATION RESULTS********************************************************/
		//Permutation Based Results
		if (Commons.WRITE_PERMUTATION_BASED_ANNOTATION_RESULT.equals(writePermutationBasedAnnotationResultMode)){
			
			if(dnaseEnrichment.equals(Commons.DO_DNASE_ENRICHMENT)){
				//Dnase
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberDnaseCellLineName2KMap(),permutationNumber2DnaseBufferedWriterHashMap, "dnase" + System.getProperty("file.separator")  , Commons.DNASE);
				closeBufferedWriters(permutationNumber2DnaseBufferedWriterHashMap);
			
			}
			
			if(histoneEnrichment.equals(Commons.DO_HISTONE_ENRICHMENT)){
				//Histone
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberHistoneNameCellLineName2KMap(),permutationNumber2HistoneBufferedWriterHashMap,"histone" + System.getProperty("file.separator") , Commons.HISTONE);
				closeBufferedWriters(permutationNumber2HistoneBufferedWriterHashMap);
		
			}
			
			
			if(tfEnrichment.equals(Commons.DO_TF_ENRICHMENT)  && !(tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)) && !(tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT))){					
				//Transcription Factor 
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfNameCellLineName2KMap(),permutationNumber2TfbsBufferedWriterHashMap, "tfbs" + System.getProperty("file.separator") , Commons.TFBS);
				closeBufferedWriters(permutationNumber2TfbsBufferedWriterHashMap);					
			}
			
	
			if(keggPathwayEnrichment.equals(Commons.DO_KEGGPATHWAY_ENRICHMENT)  && !(tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)) && !(tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT))){					
				//Exon Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberExonBasedKeggPathway2KMap(),permutationNumber2ExonBasedKeggPathwayBufferedWriterHashMap,"keggPathway" + System.getProperty("file.separator") + "exonBased" +System.getProperty("file.separator") , Commons.EXON_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2ExonBasedKeggPathwayBufferedWriterHashMap);
				
				//Regulation Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberRegulationBasedKeggPathway2KMap(),permutationNumber2RegulationBasedKeggPathwayBufferedWriterHashMap, "keggPathway" + System.getProperty("file.separator") + "regulationBased" + System.getProperty("file.separator") , Commons.REGULATION_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2RegulationBasedKeggPathwayBufferedWriterHashMap);
				
				//All Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberAllBasedKeggPathway2KMap(),permutationNumber2AllBasedKeggPathwayBufferedWriterHashMap, "keggPathway" + System.getProperty("file.separator") + "allBased" + System.getProperty("file.separator") , Commons.ALL_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2AllBasedKeggPathwayBufferedWriterHashMap);
			}
			

			
			if(tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)){
				
				//Tf and Exon Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfExonBasedKeggPathway2KMap(),permutationNumber2TfExonBasedKeggPathwayBufferedWriterHashMap, "tfKeggPathwayNumberofOverlaps" + System.getProperty("file.separator") + "tfExonBased" + System.getProperty("file.separator") , Commons.TF_EXON_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2TfExonBasedKeggPathwayBufferedWriterHashMap);
		
				//Tf and Regulation Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfRegulationBasedKeggPathway2KMap(),permutationNumber2TfRegulationBasedKeggPathwayBufferedWriterHashMap, "tfKeggPathwayNumberofOverlaps"+ System.getProperty("file.separator") + "tfRegulationBased" + System.getProperty("file.separator") , Commons.TF_REGULATION_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2TfRegulationBasedKeggPathwayBufferedWriterHashMap);
		
				//Tf and All Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfAllBasedKeggPathway2KMap(),permutationNumber2TfAllBasedKeggPathwayBufferedWriterHashMap, "tfKeggPathwayNumberofOverlaps"+ System.getProperty("file.separator")+ "tfAllBased" + System.getProperty("file.separator") , Commons.TF_ALL_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2TfAllBasedKeggPathwayBufferedWriterHashMap);
				
				//Tfbs
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfNameCellLineName2KMap(),permutationNumber2TfbsBufferedWriterHashMap, "tfbs" + System.getProperty("file.separator") , Commons.TFBS);
				closeBufferedWriters(permutationNumber2TfbsBufferedWriterHashMap);
				
				//Exon Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberExonBasedKeggPathway2KMap(),permutationNumber2ExonBasedKeggPathwayBufferedWriterHashMap,"keggPathway" + System.getProperty("file.separator") + "exonBased" +System.getProperty("file.separator") , Commons.EXON_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2ExonBasedKeggPathwayBufferedWriterHashMap);
				
				//Regulation Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberRegulationBasedKeggPathway2KMap(),permutationNumber2RegulationBasedKeggPathwayBufferedWriterHashMap, "keggPathway" + System.getProperty("file.separator") + "regulationBased" + System.getProperty("file.separator") , Commons.REGULATION_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2RegulationBasedKeggPathwayBufferedWriterHashMap);
				
				//All Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberAllBasedKeggPathway2KMap(),permutationNumber2AllBasedKeggPathwayBufferedWriterHashMap, "keggPathway" + System.getProperty("file.separator") + "allBased" + System.getProperty("file.separator") , Commons.ALL_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2AllBasedKeggPathwayBufferedWriterHashMap);
			
			}else if(tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT)){
				
							
				//Tfbs
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfNameCellLineName2KMap(),permutationNumber2TfbsBufferedWriterHashMap, "tfbs" + System.getProperty("file.separator") , Commons.TFBS);
				closeBufferedWriters(permutationNumber2TfbsBufferedWriterHashMap);
				
				//Exon Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberExonBasedKeggPathway2KMap(),permutationNumber2ExonBasedKeggPathwayBufferedWriterHashMap,"keggPathway" + System.getProperty("file.separator") + "exonBased" + System.getProperty("file.separator") , Commons.EXON_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2ExonBasedKeggPathwayBufferedWriterHashMap);
				
				//Regulation Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberRegulationBasedKeggPathway2KMap(),permutationNumber2RegulationBasedKeggPathwayBufferedWriterHashMap, "keggPathway" + System.getProperty("file.separator") + "regulationBased" + System.getProperty("file.separator"), Commons.REGULATION_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2RegulationBasedKeggPathwayBufferedWriterHashMap);
				
				//All Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberAllBasedKeggPathway2KMap(),permutationNumber2AllBasedKeggPathwayBufferedWriterHashMap, "keggPathway" + System.getProperty("file.separator") + "allBased" + System.getProperty("file.separator"), Commons.ALL_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2AllBasedKeggPathwayBufferedWriterHashMap);			
				
				//Tf and Cell Line and Exon Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfCellLineExonBasedKeggPathway2KMap(),permutationNumber2TfCellLineExonBasedKeggPathwayBufferedWriterHashMap, "tfCellLineKeggPathwayNumberofOverlaps" + System.getProperty("file.separator") + "tfCellLineExonBased" + System.getProperty("file.separator") , Commons.TF_CELLLINE_EXON_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2TfCellLineExonBasedKeggPathwayBufferedWriterHashMap);
		
				//Tf and Cell Line and Regulation Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfCellLineRegulationBasedKeggPathway2KMap(),permutationNumber2TfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap, "tfCellLineKeggPathwayNumberofOverlaps" + System.getProperty("file.separator") + "tfCellLineRegulationBased" + System.getProperty("file.separator") , Commons.TF_CELLLINE_REGULATION_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2TfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap);
		
				//Tf and Cell Line and All Based Kegg Pathway
				writeAnnotationstoFiles(outputFolder,accumulatedAllMapsWithNumbers.getPermutationNumberTfCellLineAllBasedKeggPathway2KMap(),permutationNumber2TfCellLineAllBasedKeggPathwayBufferedWriterHashMap, "tfCellLineKeggPathwayNumberofOverlaps" + System.getProperty("file.separator") + "tfCellLineAllBased" + System.getProperty("file.separator") , Commons.TF_CELLLINE_ALL_BASED_KEGG_PATHWAY);
				closeBufferedWriters(permutationNumber2TfCellLineAllBasedKeggPathwayBufferedWriterHashMap);
				
		
			}
			
			
		}//End of if: write permutation based results
		/**********************WRITE PERMUTATION BASED ANNOTATION RESULTS********************************************************/
		/************************************************************************************************************************/

			
	}
	//NEW FUNCIONALITY ADDED
	
	
	public void writetoFile(List<FunctionalElement> list, String fileName, String empiricalPValueType, int NUMBER_OF_REPEATS, int NUMBER_OF_PERMUTATIONS, String generateRandomDataMode, String inputDataFileName,float FDR){
		FileWriter fileWriter=null;
		BufferedWriter bufferedWriter;
		
		DecimalFormat df = new DecimalFormat("0.######E0");
		int i;
		try {
			
			//Set the file name
			if (Commons.GENERATE_RANDOM_DATA_WITH_MAPPABILITY_AND_GC_CONTENT.equals(generateRandomDataMode)){
				
				if (inputDataFileName.indexOf("ocd")>=0){
					fileWriter = new FileWriter(fileName + "_OCD_withGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}else if (inputDataFileName.indexOf("HIV1")>=0){
					fileWriter = new FileWriter(fileName + "_HIV1_withGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");			
				}else if(inputDataFileName.indexOf("positive_control")>=0){
					fileWriter = new FileWriter(fileName + "_K562_GATA1_withGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}else{
					fileWriter = new FileWriter(fileName + "_withGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}
			}else if(Commons.GENERATE_RANDOM_DATA_WITHOUT_MAPPABILITY_AND_GC_CONTENT.equals(generateRandomDataMode)){
				if (inputDataFileName.indexOf("ocd")>=0){
						fileWriter = new FileWriter(fileName + "_OCD_withoutGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}else if (inputDataFileName.indexOf("HIV1")>=0){
					fileWriter = new FileWriter(fileName + "_HIV1_withoutGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");			
				}else if(inputDataFileName.indexOf("positive_control")>=0){
						fileWriter = new FileWriter(fileName + "_K562_GATA1_withoutGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}else{
					fileWriter = new FileWriter(fileName + "_withoutGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}
			}
			
			bufferedWriter = new BufferedWriter(fileWriter);
			
			//Write header line
			//If BONFERRONI_CORRECTED_EMPIRICAL_P_VALUE first BonfCorrPValue then Empirical P Value
			//Else If EMPIRICAL_P_VALUE first EmpiricalPValue then BonfCorrPValue
			if (Commons.BONFERRONI_CORRECTED_P_VALUE.equals(empiricalPValueType)){	
				bufferedWriter.write("Name" + "\t"+ "OriginalNumberofOverlaps" + "\t" + "NumberofPermutationsHavingOverlapsGreaterThanorEqualto" + "\t" + "NumberofPermutations" + "\t" + "NumberofComparisons" + "\t" + "BonfCorrEmpiricalPValue" + "\t"+ "EmpiricalPValue" + "\t" + "BH FDR Adjusted P Value" + "\t" + "Reject Null Hypothesis for an FDR of "+ FDR + System.getProperty("line.separator"));	
			} else if(Commons.EMPIRICAL_P_VALUE.equals(empiricalPValueType)){
				bufferedWriter.write("Name" + "\t"+ "OriginalNumberofOverlaps" + "\t" + "NumberofPermutationsHavingOverlapsGreaterThanorEqualto" + "\t" + "NumberofPermutations" + "\t" + "NumberofComparisons" + "\t" + "EmpiricalPValue" + "\t"+ "BonfCorrEmpiricalPValue" + "\t" + "BH FDR Adjusted P Value" + "\t" + "Reject Null Hypothesis for an FDR of "+ FDR + System.getProperty("line.separator"));	
			} else if (Commons.BENJAMINI_HOCHBERG_FDR_ADJUSTED_P_VALUE.equals(empiricalPValueType)){
				bufferedWriter.write("Name" + "\t"+ "OriginalNumberofOverlaps" + "\t" + "NumberofPermutationsHavingOverlapsGreaterThanorEqualto" + "\t" + "NumberofPermutations" + "\t" + "NumberofComparisons" + "\t" + "BonfCorrEmpiricalPValue" + "\t"+ "EmpiricalPValue" + "\t" + "BH FDR Adjusted P Value" + "\t" + "Reject Null Hypothesis for an FDR of "+ FDR + System.getProperty("line.separator"));	
			}
			
			
			//For each element in the list
			for(FunctionalElement element : list){
				
				//In case of Functional Element is a kegg pathway
				if(element.getKeggPathwayName()!=null){
					
					if (Commons.BONFERRONI_CORRECTED_P_VALUE.equals(empiricalPValueType)){	
						bufferedWriter.write(element.getName() + "\t"+ element.getOriginalNumberofOverlaps() + "\t" + element.getNumberofPermutationsHavingOverlapsGreaterThanorEqualto() + "\t" + element.getNumberofPermutations() + "\t" + element.getNumberofComparisons() + "\t" + df.format(element.getBonferroniCorrectedEmpiricalPValue())+ "\t"+ df.format(element.getEmpiricalPValue())+ "\t" + df.format(element.getBH_FDR_adjustedPValue()) +"\t" + element.isRejectNullHypothesis() +"\t");	
					} else if(Commons.EMPIRICAL_P_VALUE.equals(empiricalPValueType)){
						bufferedWriter.write(element.getName() + "\t"+ element.getOriginalNumberofOverlaps() + "\t" + element.getNumberofPermutationsHavingOverlapsGreaterThanorEqualto() + "\t" + element.getNumberofPermutations() + "\t" + element.getNumberofComparisons() + "\t" + df.format(element.getEmpiricalPValue()) + "\t" +df.format(element.getBonferroniCorrectedEmpiricalPValue())+ "\t" + df.format(element.getBH_FDR_adjustedPValue()) +"\t" + element.isRejectNullHypothesis() +"\t");	
					} else if(Commons.BENJAMINI_HOCHBERG_FDR_ADJUSTED_P_VALUE.equals(empiricalPValueType)){
						bufferedWriter.write(element.getName() + "\t"+ element.getOriginalNumberofOverlaps() + "\t" + element.getNumberofPermutationsHavingOverlapsGreaterThanorEqualto() + "\t" + element.getNumberofPermutations() + "\t" + element.getNumberofComparisons() + "\t" + df.format(element.getBonferroniCorrectedEmpiricalPValue())+ "\t"+ df.format(element.getEmpiricalPValue())+ "\t" + df.format(element.getBH_FDR_adjustedPValue()) +"\t" + element.isRejectNullHypothesis() +"\t");	
						
					}
					
					bufferedWriter.write(element.getKeggPathwayName()+"\t");
					
					
					if (element.getKeggPathwayGeneIdList().size()>=1){
						//Write the gene ids of the kegg pathway
						for(i =0 ;i < element.getKeggPathwayGeneIdList().size()-1; i++){
							bufferedWriter.write(element.getKeggPathwayGeneIdList().get(i) + ", ");
						}
						bufferedWriter.write(element.getKeggPathwayGeneIdList().get(i) + "\t");
					}
					
					if(element.getKeggPathwayAlternateGeneNameList().size()>=1){
						//Write the alternate gene names of the kegg pathway
						for(i =0 ;i < element.getKeggPathwayAlternateGeneNameList().size()-1; i++){
							bufferedWriter.write(element.getKeggPathwayAlternateGeneNameList().get(i) + ", ");
						}
						bufferedWriter.write(element.getKeggPathwayAlternateGeneNameList().get(i) + System.getProperty("line.separator"));
					
					}					
				}else{
					if (Commons.BONFERRONI_CORRECTED_P_VALUE.equals(empiricalPValueType)){	
						bufferedWriter.write(element.getName() + "\t"+ element.getOriginalNumberofOverlaps() + "\t" + element.getNumberofPermutationsHavingOverlapsGreaterThanorEqualto() + "\t" + element.getNumberofPermutations() + "\t" + element.getNumberofComparisons() + "\t" + df.format(element.getBonferroniCorrectedEmpiricalPValue())+ "\t"+ df.format(element.getEmpiricalPValue())+ "\t" + df.format(element.getBH_FDR_adjustedPValue()) +"\t" + element.isRejectNullHypothesis() + System.getProperty("line.separator"));	
					} else if(Commons.EMPIRICAL_P_VALUE.equals(empiricalPValueType)){
						bufferedWriter.write(element.getName() + "\t"+ element.getOriginalNumberofOverlaps() + "\t" + element.getNumberofPermutationsHavingOverlapsGreaterThanorEqualto() + "\t" + element.getNumberofPermutations() + "\t" + element.getNumberofComparisons() + "\t" + df.format(element.getEmpiricalPValue()) + "\t" +df.format(element.getBonferroniCorrectedEmpiricalPValue())+ "\t" + df.format(element.getBH_FDR_adjustedPValue()) +"\t" + element.isRejectNullHypothesis() + System.getProperty("line.separator"));	
					} else if(Commons.BENJAMINI_HOCHBERG_FDR_ADJUSTED_P_VALUE.equals(empiricalPValueType)){
						bufferedWriter.write(element.getName() + "\t"+ element.getOriginalNumberofOverlaps() + "\t" + element.getNumberofPermutationsHavingOverlapsGreaterThanorEqualto() + "\t" + element.getNumberofPermutations() + "\t" + element.getNumberofComparisons() + "\t" + df.format(element.getBonferroniCorrectedEmpiricalPValue())+ "\t"+ df.format(element.getEmpiricalPValue())+ "\t" + df.format(element.getBH_FDR_adjustedPValue()) +"\t" + element.isRejectNullHypothesis() + System.getProperty("line.separator"));	
								
					}
				}
							
			}
			
			bufferedWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	public void writetoFileSmallValueInsteadofZero(Random random, List<FunctionalElement> list, String fileName, String empiricalPValueType, int NUMBER_OF_REPEATS, int NUMBER_OF_PERMUTATIONS, String generateRandomDataMode, String inputDataFileName){
		FileWriter fileWriter=null;
		BufferedWriter bufferedWriter;
		
		DecimalFormat df = new DecimalFormat("0.######E0");
		Float empiricalPValue;
		Float bonfCorrEmpiricalPValue;
		
		int i;
		try {
			
			if (Commons.GENERATE_RANDOM_DATA_WITH_MAPPABILITY_AND_GC_CONTENT.equals(generateRandomDataMode)){
				
				if (inputDataFileName.indexOf("ocd")>=0){
					fileWriter = new FileWriter(fileName + "_OCD_withGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}else if(inputDataFileName.indexOf("positive_control")>=0){
					fileWriter = new FileWriter(fileName + "_K562_GATA1_withGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}else{
					fileWriter = new FileWriter(fileName + "_withGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}
			}else if(Commons.GENERATE_RANDOM_DATA_WITHOUT_MAPPABILITY_AND_GC_CONTENT.equals(generateRandomDataMode)){
				if (inputDataFileName.indexOf("ocd")>=0){
						fileWriter = new FileWriter(fileName + "_OCD_withoutGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}else if(inputDataFileName.indexOf("positive_control")>=0){
						fileWriter = new FileWriter(fileName + "_K562_GATA1_withoutGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}else{
					fileWriter = new FileWriter(fileName + "_withoutGCMap_"  + NUMBER_OF_REPEATS+ "Rep_" + NUMBER_OF_PERMUTATIONS + "Perm.txt");	
				}
			}
			bufferedWriter = new BufferedWriter(fileWriter);
			
			for(FunctionalElement element : list){
				
				//Write the name of the functional element
				bufferedWriter.write(element.getName() + "\t");
				
				
				//In case of Functional Element is a kegg pathway
				if(element.getKeggPathwayName()!=null){
					
					bufferedWriter.write(element.getKeggPathwayName()+"\t");
					
					
					if (element.getKeggPathwayGeneIdList().size()>=1){
						//Write the gene ids of the kegg pathway
						for(i =0 ;i < element.getKeggPathwayGeneIdList().size()-1; i++){
							bufferedWriter.write(element.getKeggPathwayGeneIdList().get(i) + ", ");
						}
						bufferedWriter.write(element.getKeggPathwayGeneIdList().get(i) + "\t");
					}
					
					if(element.getKeggPathwayAlternateGeneNameList().size()>=1){
						//Write the alternate gene names of the kegg pathway
						for(i =0 ;i < element.getKeggPathwayAlternateGeneNameList().size()-1; i++){
							bufferedWriter.write(element.getKeggPathwayAlternateGeneNameList().get(i) + ", ");
						}
						bufferedWriter.write(element.getKeggPathwayAlternateGeneNameList().get(i) + "\t");
					
					}					
				}
				
				
				
				if (Commons.BONFERRONI_CORRECTED_P_VALUE.equals(empiricalPValueType)){	
					bonfCorrEmpiricalPValue = element.getBonferroniCorrectedEmpiricalPValue();
					if(bonfCorrEmpiricalPValue.equals(Commons.FLOAT_ZERO)){
						element.setBonferroniCorrectedEmpiricalPValue(random.nextFloat()/Commons.FLOAT_TEN_QUADRILLION);
					}
					bufferedWriter.write(df.format(element.getBonferroniCorrectedEmpiricalPValue())+ System.getProperty("line.separator"));	
				} else if(Commons.EMPIRICAL_P_VALUE.equals(empiricalPValueType)){
					empiricalPValue = element.getEmpiricalPValue();
					if (empiricalPValue.equals(Commons.FLOAT_ZERO)){
						element.setEmpiricalPValue(random.nextFloat()/Commons.FLOAT_TEN_QUADRILLION);
					}
					bufferedWriter.write(df.format(element.getEmpiricalPValue())+ System.getProperty("line.separator"));	
				}
				
				
			}
			
			bufferedWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public static void writeToBeCollectedNumberofOverlaps(String outputFolder,Map<Integer,Integer> originalElementNumber2KMap, Map<Integer,List<Integer>> elementNumber2AllKMap,String toBePolledDirectoryName, String runNumber){
		Integer elementNumber;
		Integer originalNumberofOverlaps;
		
		List<Integer> permutationNumberofOverlapsList;
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		try {
			fileWriter = FileOperations.createFileWriter(outputFolder + toBePolledDirectoryName + "_" + runNumber +".txt");
			bufferedWriter = new BufferedWriter(fileWriter);
			
			for(Map.Entry<Integer,Integer> entry: originalElementNumber2KMap.entrySet()){
				
				elementNumber = entry.getKey();
				originalNumberofOverlaps = entry.getValue();
				
				bufferedWriter.write(elementNumber + "\t" + originalNumberofOverlaps + "|" );
				
				permutationNumberofOverlapsList = elementNumber2AllKMap.get(elementNumber);
				
				if (permutationNumberofOverlapsList!=null){
					for (Integer permutationNumberofOverlaps : permutationNumberofOverlapsList){
						bufferedWriter.write(permutationNumberofOverlaps + "," );
					}//End of inner loop
					
				}

				bufferedWriter.write(System.getProperty("line.separator"));
				
				//if permutationNumberofOverlapsList is null 
				//do nothing
				
				
				
			}//End of outer loop
		
		
			bufferedWriter.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public static void writeInformation(){
		System.out.println("Java runtime max memory: " + java.lang.Runtime.getRuntime().maxMemory());
        System.out.println("Java runtime total memory: " + java.lang.Runtime.getRuntime().totalMemory());	
		System.out.println("Java runtime available processors: " + java.lang.Runtime.getRuntime().availableProcessors()); 
	
	}
	
	//args[0]	--->	Input File Name with folder
	//args[1]	--->	GLANET installation folder with System.getProperty("file.separator") at the end. 
	//			--->	This folder will be used for outputFolder and dataFolder.
	//args[2]	--->	Input File Format	
	//			--->	default	Commons.INPUT_FILE_FORMAT_1_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	//			--->			Commons.INPUT_FILE_FORMAT_0_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	//			--->			Commons.INPUT_FILE_FORMAT_DBSNP_IDS_0_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	//			--->			Commons.INPUT_FILE_FORMAT_BED_0_BASED_COORDINATES_START_INCLUSIVE_END_EXCLUSIVE
	//			--->			Commons.INPUT_FILE_FORMAT_GFF3_1_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE	
	//args[3]	--->	Annotation, overlap definition, number of bases, default 1
	//args[4]	--->	Enrichment parameter
	//			--->	default	Commons.DO_ENRICH
	//			--->			Commons.DO_NOT_ENRICH	
	//args[5]	--->	Generate Random Data Mode
	//			--->	default	Commons.GENERATE_RANDOM_DATA_WITH_MAPPABILITY_AND_GC_CONTENT
	//			--->			Commons.GENERATE_RANDOM_DATA_WITHOUT_MAPPABILITY_AND_GC_CONTENT	
	//args[6]	--->	multiple testing parameter, enriched elements will be decided and sorted with respest to this parameter
	//			--->	default Commons.BENJAMINI_HOCHBERG_FDR_ADJUSTED_P_VALUE
	//			--->			Commons.BONFERRONI_CORRECTED_P_VALUE
	//args[7]	--->	Bonferroni Correction Significance Level, default 0.05
	//args[8]	--->	Benjamini Hochberg FDR, default 0.05
	//args[9]	--->	Number of permutations, default 5000
	//args[10]	--->	Dnase Enrichment
	//			--->	default Commons.DO_NOT_DNASE_ENRICHMENT
	//			--->	Commons.DO_DNASE_ENRICHMENT
	//args[11]	--->	Histone Enrichment
	//			--->	default	Commons.DO_NOT_HISTONE_ENRICHMENT
	//			--->			Commons.DO_HISTONE_ENRICHMENT
	//args[12]	--->	Transcription Factor(TF) Enrichment 
	//			--->	default	Commons.DO_NOT_TF_ENRICHMENT
	//			--->			Commons.DO_TF_ENRICHMENT
	//args[13]	--->	KEGG Pathway Enrichment
	//			--->	default	Commons.DO_NOT_KEGGPATHWAY_ENRICHMENT 
	//			--->			Commons.DO_KEGGPATHWAY_ENRICHMENT
	//args[14]	--->	TF and KEGG Pathway Enrichment
	//			--->	default	Commons.DO_NOT_TF_KEGGPATHWAY_ENRICHMENT 
	//			--->			Commons.DO_TF_KEGGPATHWAY_ENRICHMENT
	//args[15]	--->	TF and CellLine and KeggPathway Enrichment
	//			--->	default	Commons.DO_NOT_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT 
	//			--->			Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT
	//args[16]	--->	RSAT parameter
	//			--->	default Commons.DO_NOT_REGULATORY_SEQUENCE_ANALYSIS_USING_RSAT
	//			--->			Commons.DO_REGULATORY_SEQUENCE_ANALYSIS_USING_RSAT
	//args[17]	--->	job name example: ocd_gwas_snps 
	//args[18]	--->	writeGeneratedRandomDataMode checkBox
	//			--->	default Commons.DO_NOT_WRITE_GENERATED_RANDOM_DATA
	//			--->			Commons.WRITE_GENERATED_RANDOM_DATA
	//args[19]	--->	writePermutationBasedandParametricBasedAnnotationResultMode checkBox
	//			--->	default Commons.DO_NOT_WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT
	//			--->			Commons.WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT
	//args[20]	--->	writePermutationBasedAnnotationResultMode checkBox
	//			---> 	default	Commons.DO_NOT_WRITE_PERMUTATION_BASED_ANNOTATION_RESULT
	//			--->			Commons.WRITE_PERMUTATION_BASED_ANNOTATION_RESULT			
	public static void main(String[] args) {
		
		String glanetFolder = args[1];
		String dataFolder 	= glanetFolder + System.getProperty("file.separator") + Commons.DATA + System.getProperty("file.separator") ;
		String outputFolder = glanetFolder + System.getProperty("file.separator") + Commons.OUTPUT + System.getProperty("file.separator") ;
		
		
		int overlapDefinition = Integer.parseInt(args[3]);

		//Number of processors can be used in deciding on paralellism level
		int NUMBER_OF_AVAILABLE_PROCESSORS =  java.lang.Runtime.getRuntime().availableProcessors();
			
		//Set the number of total permutations
		int numberofTotalPermutations = Integer.parseInt(args[9]);
		
		
		//SET the Input Data File
//		String inputDataFileName = Commons.OCD_GWAS_SIGNIFICANT_SNPS_WITHOUT_OVERLAPS;
//		String inputDataFileName = Commons.POSITIVE_CONTROL_OUTPUT_FILE_NAME_WITHOUT_OVERLAPS;
//		String inputDataFileName = Commons.TCGA_INPUT_DATA_WITH_NON_BLANKS_SNP_IDS_WITHOUT_OVERLAPS;
		String inputDataFileName = outputFolder + Commons.REMOVED_OVERLAPS_INPUT_FILE;
				
		//Set the Generate Random Data Mode
//		String generateRandomDataMode = Commons.GENERATE_RANDOM_DATA_WITH_MAPPABILITY_AND_GC_CONTENT;
		String generateRandomDataMode = args[5];
		
		//Set the Write Mode of Generated Random Data
//		String writeGeneratedRandomDataMode = Commons.DO_NOT_WRITE_GENERATED_RANDOM_DATA;
		String writeGeneratedRandomDataMode = args[18];
				
		//Set the Write Mode of Permutation Based and Parametric Based Annotation Result
//		String writePermutationBasedandParametricBasedAnnotationResultMode = Commons.DO_NOT_WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT;
		String writePermutationBasedandParametricBasedAnnotationResultMode = args[19];
		
		//Set the Write Mode of the Permutation Based Annotation Result
//		String writePermutationBasedAnnotationResultMode = Commons.WRITE_PERMUTATION_BASED_ANNOTATION_RESULT;
		String writePermutationBasedAnnotationResultMode = args[20];
		
		//ENRICHMENT
		//Dnase Enrichment, DO or DO_NOT
		String dnaseEnrichment = args[10];
		
		//Histone Enrichment, DO or DO_NOT
		String histoneEnrichment = args[11];
		
		//Transcription Factor Enrichment, DO or DO_NOT
		String tfEnrichment = args[12];
			
		//KEGG Pathway Enrichment, DO or DO_NOT
		String keggPathwayEnrichment = args[13];
						
		//TfKeggPathway Enrichment, DO or DO_NOT
		String tfKeggPathwayEnrichment = args[14];
		
		//TfCellLineKeggPathway Enrichment, DO or DO_NOT
		String tfCellLineKeggPathwayEnrichment = args[15];
		
		//Run Name
		String jobName = args[17] ;
		
		writeInformation();
			
		//Random Class for generating small values instead of zero valued p values
		//Random random = new Random();
		
	
		List<InputLine> originalInputLines = new ArrayList<InputLine>();
		
		//Read original input data lines in to a list
		AnnotatePermutationsWithEnrichmentChoicesWithNumbers.readOriginalInputDataLines(originalInputLines, inputDataFileName);
	
		//For Bonferroni Correction 
		//Set the number of comparisons for DNase, Tfbs, Histone
		//Set the number of comparisons for ExonBasedKeggPathway, RegulationBasedKeggPathway, AllBasedKeggPathway
		//Set the number of comparisons for TfCellLineExonBasedKeggPathway, TfCellLineRegulationBasedKeggPathway, TfCellLineAllBasedKeggPathway
		//Set the number of comparisons for TfExonBasedKeggPathway, TfRegulationBasedKeggPathway, TfAllBasedKeggPathway
		NumberofComparisons  numberofComparisons = new NumberofComparisons();
		NumberofComparisonsforBonferroniCorrectionCalculation.getNumberofComparisonsforBonferroniCorrection(dataFolder,numberofComparisons);
			
		
		/*********************************************/
		//delete old files
		String annotateOutputBaseDirectoryName = outputFolder + Commons.ANNOTATION;
		List<String> notToBeDeleted = new ArrayList<String>();
		notToBeDeleted.add(Commons.GIVENINPUTDATA);
		FileOperations.deleteDirectoriesandFilesUnderThisDirectory(annotateOutputBaseDirectoryName,notToBeDeleted);
		
		//delete old files
		String toBeCollectedOutputBaseDirectoryName = outputFolder + Commons.TO_BE_COLLECTED_DIRECTORY;
		FileOperations.deleteDirectoriesandFilesUnderThisDirectory(toBeCollectedOutputBaseDirectoryName);			
		/*********************************************/

		
		//for loop starts
		//NUMBER_OF_PERMUTATIONS has to be multiple of 1000 like 1000, 5000, 10000, 50000, 100000
		
		int numberofRuns = 0;
		int numberofRemainedPermutations = 0;
		String runName;
		
		numberofRuns = numberofTotalPermutations / Commons.NUMBER_OF_PERMUTATIONS_IN_EACH_RUN;
		numberofRemainedPermutations = numberofTotalPermutations % Commons.NUMBER_OF_PERMUTATIONS_IN_EACH_RUN;
		
		//Increase numberofRuns by 1 for remainder permutations less than Commons.NUMBER_OF_PERMUTATIONS_IN_EACH_RUN
		if (numberofRemainedPermutations> 0){
			numberofRuns += 1;
		}
		
		
		if (tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT) && tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT)){
			System.out.println("Both Tf_KEGG_Pathway_enrichment and  Tf_Cellline_Kegg_Pathway_enrichment can not be selected");
		}
		else{
			
			for(int runNumber=1; runNumber<=numberofRuns;runNumber++){
				
				System.out.println("**************	" + runNumber + ". Run" + "	******************	starts");
				
				runName = jobName + runNumber;
				
				//annotation of original data with permutations	
				//annotation of original data has permutation number zero
				//number of overlaps for the original data: k out of n for the original data
				//ElementName to integer
				Map<Integer,Integer> originalDnase2KMap = new HashMap<Integer,Integer>();
				Map<Integer,Integer> originalTfbs2KMap = new HashMap<Integer,Integer>();
				Map<Integer,Integer> originalHistone2KMap = new HashMap<Integer,Integer>();
				
				Map<Integer,Integer> originalExonBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
				Map<Integer,Integer> originalRegulationBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
				Map<Integer,Integer> originalAllBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
							
				//Tf and KeggPathway Enrichment
				Map<Integer,Integer> originalTfExonBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
				Map<Integer,Integer> originalTfRegulationBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
				Map<Integer,Integer> originalTfAllBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
			
				//Tf and CellLine and KeggPathway Enrichment 
				Map<Integer,Integer> originalTfCellLineExonBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
				Map<Integer,Integer> originalTfCellLineRegulationBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
				Map<Integer,Integer> originalTfCellLineAllBasedKeggPathway2KMap = new HashMap<Integer,Integer>();
									
				//functionalElementName based
				//number of overlaps: k out of n for all permutations
				//ElementName to list of integers
				Map<Integer,List<Integer>> dnase2AllKMap = new HashMap<Integer,List<Integer>>();
				Map<Integer,List<Integer>> histone2AllKMap = new HashMap<Integer,List<Integer>>();
				Map<Integer,List<Integer>> tfbs2AllKMap = new HashMap<Integer,List<Integer>>();
				
				Map<Integer,List<Integer>> exonBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>();
				Map<Integer,List<Integer>> regulationBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>();
				Map<Integer,List<Integer>> allBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>();
				
				//Tf and KeggPathway Enrichment
				Map<Integer,List<Integer>> tfExonBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>();
				Map<Integer,List<Integer>> tfRegulationBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>() ;
				Map<Integer,List<Integer>> tfAllBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>();
			
				//Tf and CellLine and KeggPathway Enrichment 
				Map<Integer,List<Integer>> tfCellLineExonBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>();
				Map<Integer,List<Integer>> tfCellLineRegulationBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>() ;
				Map<Integer,List<Integer>> tfCellLineAllBasedKeggPathway2AllKMap = new HashMap<Integer,List<Integer>>();
				
				/*********************************************************************************************/			
				/**************************ANNOTATE PERMUTATIONS STARTS***************************************/		
				System.out.println("Concurrent programming has been started.");				
				//concurrent programming
				//generate random data
				//then annotate permutations concurrently
				//elementName2AllKMap and originalElementName2KMap will be filled here
				if ((runNumber == numberofRuns) && (numberofRemainedPermutations >0)){
					AnnotatePermutationsWithEnrichmentChoicesWithNumbers.annotateAllPermutationsInThreads(outputFolder,dataFolder,NUMBER_OF_AVAILABLE_PROCESSORS,runNumber,numberofRemainedPermutations,originalInputLines,dnase2AllKMap, tfbs2AllKMap, histone2AllKMap, exonBasedKeggPathway2AllKMap, regulationBasedKeggPathway2AllKMap,allBasedKeggPathway2AllKMap,tfExonBasedKeggPathway2AllKMap,tfRegulationBasedKeggPathway2AllKMap,tfAllBasedKeggPathway2AllKMap,tfCellLineExonBasedKeggPathway2AllKMap,tfCellLineRegulationBasedKeggPathway2AllKMap,tfCellLineAllBasedKeggPathway2AllKMap,generateRandomDataMode,writeGeneratedRandomDataMode,writePermutationBasedandParametricBasedAnnotationResultMode,writePermutationBasedAnnotationResultMode,originalDnase2KMap,originalTfbs2KMap,originalHistone2KMap,originalExonBasedKeggPathway2KMap,originalRegulationBasedKeggPathway2KMap,originalAllBasedKeggPathway2KMap,originalTfExonBasedKeggPathway2KMap,originalTfRegulationBasedKeggPathway2KMap,originalTfAllBasedKeggPathway2KMap,originalTfCellLineExonBasedKeggPathway2KMap,originalTfCellLineRegulationBasedKeggPathway2KMap,originalTfCellLineAllBasedKeggPathway2KMap,dnaseEnrichment,histoneEnrichment,tfEnrichment,keggPathwayEnrichment,tfKeggPathwayEnrichment,tfCellLineKeggPathwayEnrichment,overlapDefinition);						
				}else {
					AnnotatePermutationsWithEnrichmentChoicesWithNumbers.annotateAllPermutationsInThreads(outputFolder,dataFolder,NUMBER_OF_AVAILABLE_PROCESSORS,runNumber,Commons.NUMBER_OF_PERMUTATIONS_IN_EACH_RUN,originalInputLines,dnase2AllKMap, tfbs2AllKMap, histone2AllKMap, exonBasedKeggPathway2AllKMap, regulationBasedKeggPathway2AllKMap,allBasedKeggPathway2AllKMap,tfExonBasedKeggPathway2AllKMap,tfRegulationBasedKeggPathway2AllKMap,tfAllBasedKeggPathway2AllKMap,tfCellLineExonBasedKeggPathway2AllKMap,tfCellLineRegulationBasedKeggPathway2AllKMap,tfCellLineAllBasedKeggPathway2AllKMap,generateRandomDataMode,writeGeneratedRandomDataMode,writePermutationBasedandParametricBasedAnnotationResultMode,writePermutationBasedAnnotationResultMode,originalDnase2KMap,originalTfbs2KMap,originalHistone2KMap,originalExonBasedKeggPathway2KMap,originalRegulationBasedKeggPathway2KMap,originalAllBasedKeggPathway2KMap,originalTfExonBasedKeggPathway2KMap,originalTfRegulationBasedKeggPathway2KMap,originalTfAllBasedKeggPathway2KMap,originalTfCellLineExonBasedKeggPathway2KMap,originalTfCellLineRegulationBasedKeggPathway2KMap,originalTfCellLineAllBasedKeggPathway2KMap,dnaseEnrichment,histoneEnrichment,tfEnrichment, keggPathwayEnrichment, tfKeggPathwayEnrichment,tfCellLineKeggPathwayEnrichment,overlapDefinition);		
					
				}
				System.out.println("Concurrent programming has been ended.");				
				/**************************ANNOTATE PERMUTATIONS ENDS*****************************************/
				/*********************************************************************************************/			
				
				
				/*********************************************************************************************/			
				/**************************WRITE TO BE COLLECTED RESULTS STARTS*******************************/
				if(dnaseEnrichment.equals(Commons.DO_DNASE_ENRICHMENT)){
				
					//Write to be collected files
					writeToBeCollectedNumberofOverlaps(outputFolder,originalDnase2KMap,dnase2AllKMap,Commons.TO_BE_COLLECTED_DNASE_NUMBER_OF_OVERLAPS,runName);
				}
				
				if (histoneEnrichment.equals(Commons.DO_HISTONE_ENRICHMENT)){
					
					//Write to be collected files
					writeToBeCollectedNumberofOverlaps(outputFolder,originalHistone2KMap,histone2AllKMap,Commons.TO_BE_COLLECTED_HISTONE_NUMBER_OF_OVERLAPS,runName);
				}
				
				if (tfEnrichment.equals(Commons.DO_TF_ENRICHMENT) && !(tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)) && !(tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT))){
					
					//Write to be collected files
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfbs2KMap,tfbs2AllKMap,Commons.TO_BE_COLLECTED_TF_NUMBER_OF_OVERLAPS,runName);
				}
					
				if (keggPathwayEnrichment.equals(Commons.DO_KEGGPATHWAY_ENRICHMENT) && !(tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)) && !(tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT))){
					
					//Write to be collected files
					writeToBeCollectedNumberofOverlaps(outputFolder,originalExonBasedKeggPathway2KMap,exonBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_EXON_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalRegulationBasedKeggPathway2KMap,regulationBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_REGULATION_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalAllBasedKeggPathway2KMap,allBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_ALL_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
				}

				if (tfKeggPathwayEnrichment.equals(Commons.DO_TF_KEGGPATHWAY_ENRICHMENT)){
								
					//Write to be collected files
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfbs2KMap,tfbs2AllKMap,Commons.TO_BE_COLLECTED_TF_NUMBER_OF_OVERLAPS,runName);
					
					writeToBeCollectedNumberofOverlaps(outputFolder,originalExonBasedKeggPathway2KMap,exonBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_EXON_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalRegulationBasedKeggPathway2KMap,regulationBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_REGULATION_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalAllBasedKeggPathway2KMap,allBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_ALL_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfExonBasedKeggPathway2KMap,tfExonBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_TF_EXON_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfRegulationBasedKeggPathway2KMap,tfRegulationBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_TF_REGULATION_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfAllBasedKeggPathway2KMap,tfAllBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_TF_ALL_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
				
				}else if(tfCellLineKeggPathwayEnrichment.equals(Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT)){
					
					//Write to be collected files
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfbs2KMap,tfbs2AllKMap,Commons.TO_BE_COLLECTED_TF_NUMBER_OF_OVERLAPS,runName);
					
					writeToBeCollectedNumberofOverlaps(outputFolder,originalExonBasedKeggPathway2KMap,exonBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_EXON_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalRegulationBasedKeggPathway2KMap,regulationBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_REGULATION_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalAllBasedKeggPathway2KMap,allBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_ALL_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);		
													
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfCellLineExonBasedKeggPathway2KMap,tfCellLineExonBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_TF_CELLLINE_EXON_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfCellLineRegulationBasedKeggPathway2KMap,tfCellLineRegulationBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_TF_CELLLINE_REGULATION_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
					writeToBeCollectedNumberofOverlaps(outputFolder,originalTfCellLineAllBasedKeggPathway2KMap,tfCellLineAllBasedKeggPathway2AllKMap,Commons.TO_BE_COLLECTED_TF_CELLLINE_ALL_BASED_KEGG_PATHWAY_NUMBER_OF_OVERLAPS,runName);
				}
				//Calculate Empirical P Values and Bonferroni Corrected Empirical P Values ends
				/**************************WRITE TO BE COLLECTED RESULTS ENDS*********************************/
				/*********************************************************************************************/			
				
				
				originalDnase2KMap = null;
				originalTfbs2KMap = null;
				originalHistone2KMap = null;
				
				originalExonBasedKeggPathway2KMap = null;
				originalRegulationBasedKeggPathway2KMap = null;
				originalAllBasedKeggPathway2KMap = null;
							
				originalTfExonBasedKeggPathway2KMap = null;
				originalTfRegulationBasedKeggPathway2KMap = null;
				originalTfAllBasedKeggPathway2KMap = null;
			
				originalTfCellLineExonBasedKeggPathway2KMap = null;
				originalTfCellLineRegulationBasedKeggPathway2KMap = null;
				originalTfCellLineAllBasedKeggPathway2KMap = null;
									
				//functionalElementName based
				//number of overlaps: k out of n for all permutations
				dnase2AllKMap = null;
				histone2AllKMap = null;
				tfbs2AllKMap = null;
				
				exonBasedKeggPathway2AllKMap = null;
				regulationBasedKeggPathway2AllKMap = null;
				allBasedKeggPathway2AllKMap = null;
				
				//Tf and KeggPathway Enrichment
				tfExonBasedKeggPathway2AllKMap = null;
				tfRegulationBasedKeggPathway2AllKMap = null ;
				tfAllBasedKeggPathway2AllKMap = null;
			
				//Tf and CellLine and KeggPathway Enrichment 
				tfCellLineExonBasedKeggPathway2AllKMap = null;
				tfCellLineRegulationBasedKeggPathway2AllKMap = null ;
				tfCellLineAllBasedKeggPathway2AllKMap = null;
		
				System.out.println("**************	" + runNumber + ". Run" + "	******************	ends");
				
			}
			//end of for each run number						

		}//end of else
								
	
	}//End of main function

}

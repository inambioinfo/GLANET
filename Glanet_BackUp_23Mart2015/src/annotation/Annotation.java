/**
 * @author burcakotlu
 * @date Jun 30, 2014 
 * @time 5:43:38 PM
 */
package annotation;

import gnu.trove.iterator.TIntByteIterator;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.iterator.TShortByteIterator;
import gnu.trove.iterator.TShortIntIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.list.TShortList;
import gnu.trove.map.TIntByteMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.TShortByteMap;
import gnu.trove.map.TShortIntMap;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectShortHashMap;
import gnu.trove.map.hash.TShortByteHashMap;
import gnu.trove.map.hash.TShortIntHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import intervaltree.DnaseIntervalTreeNode;
import intervaltree.DnaseIntervalTreeNodeWithNumbers;
import intervaltree.Interval;
import intervaltree.IntervalTree;
import intervaltree.IntervalTreeNode;
import intervaltree.TforHistoneIntervalTreeNode;
import intervaltree.TforHistoneIntervalTreeNodeWithNumbers;
import intervaltree.UcscRefSeqGeneIntervalTreeNode;
import intervaltree.UcscRefSeqGeneIntervalTreeNodeWithNumbers;
import intervaltree.UserDefinedLibraryIntervalTreeNodeWithNumbers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import keggpathway.ncbigenes.KeggPathwayUtility;

import org.apache.log4j.Logger;

import ui.GlanetRunner;
import userdefined.geneset.UserDefinedGeneSetUtility;
import userdefined.library.UserDefinedLibraryUtility;
import augmentation.humangenes.HumanGenesAugmentation;
import auxiliary.Accumulation;
import auxiliary.FileOperations;
import auxiliary.NumberofComparisons;

import common.Commons;

import enrichment.AllMaps;
import enrichment.AllMapsWithNumbers;
import enrichment.InputLine;
import enumtypes.AnnotationType;
import enumtypes.ChromosomeName;
import enumtypes.CommandLineArguments;
import enumtypes.GeneInformationType;
import enumtypes.GeneOverlapAnalysisFileMode;
import enumtypes.GeneSetAnalysisType;
import enumtypes.GeneSetType;
import enumtypes.GeneratedMixedNumberDescriptionOrderLength;
import enumtypes.IntervalName;
import enumtypes.NodeType;
import enumtypes.UserDefinedLibraryDataFormat;
import enumtypes.WriteElementBasedAnnotationFoundOverlapsMode;

/**
 * Annotate given intervals with annotation options with numbers
 */
public class Annotation {

	final static Logger logger = Logger.getLogger(Annotation.class);
	
	//Write a class such that it can be called from Annotation and Enrichment
	//Annotate Original Given Data and --> Annotation 
	//Annotate Randomly Generated Data --> Enrichment
	static class FindOverlaps extends RecursiveTask<int[]> {


		private static final long serialVersionUID = 4766480987562900233L;
		
		private String outputFolder;
		private ChromosomeName chromName;
		private List<Interval> data;
		private WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode;
		
		private int lowTaskIndex;
		private int highTaskIndex;
		
		private int overlapDefinition;
		private IntervalTree intervalTree;
		private int numberofComparisonsDnase;
		
		
		private TShortObjectMap<String> cellLineNumber2CellLineNameMap;
		private TShortObjectMap<String> fileNumber2FileNameMap;
		
		
		public FindOverlaps(
				String outputFolder, 
				ChromosomeName chromName, 
				List<Interval> data, 
				WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
				int lowTaskIndex,
				int highTaskIndex,
				int overlapDefinition,
				IntervalTree intervalTree,
				int numberofComparisonsDnase,
				TShortObjectMap<String> cellLineNumber2CellLineNameMap,
				TShortObjectMap<String> fileNumber2FileNameMap) {

			this.outputFolder = outputFolder;
			this.chromName = chromName;
			this.data = data;
			
			this.writeElementBasedAnnotationFoundOverlapsMode = writeElementBasedAnnotationFoundOverlapsMode;
			
			this.lowTaskIndex = lowTaskIndex;
			this.highTaskIndex = highTaskIndex;

			this.overlapDefinition = overlapDefinition;
			this.intervalTree = intervalTree;
			
			this.numberofComparisonsDnase = numberofComparisonsDnase;
			
			
			this.cellLineNumber2CellLineNameMap = cellLineNumber2CellLineNameMap;
			this.fileNumber2FileNameMap = fileNumber2FileNameMap;

		
		}

		@Override
		protected int[] compute() {

			int middleTaskIndex;
			
			int[] leftKArray = null;
			int[]  rightKArray = null;
			int[]  kArray = null; 
		
			
			Interval interval;
			
			int NUMBER_OF_FIND_OVERLAPS_DONE_SEQUENTIALLY_BY_EACH_PROCESS = data.size() / Commons.NUMBER_OF_AVAILABLE_PROCESSORS;
			
			
			if(NUMBER_OF_FIND_OVERLAPS_DONE_SEQUENTIALLY_BY_EACH_PROCESS == 0){
				NUMBER_OF_FIND_OVERLAPS_DONE_SEQUENTIALLY_BY_EACH_PROCESS = 8;
			}
			
			// DIVIDE
			if (highTaskIndex - lowTaskIndex > NUMBER_OF_FIND_OVERLAPS_DONE_SEQUENTIALLY_BY_EACH_PROCESS) {
				middleTaskIndex = lowTaskIndex + (highTaskIndex - lowTaskIndex) / 2;
				FindOverlaps left = new FindOverlaps(outputFolder,chromName, data, writeElementBasedAnnotationFoundOverlapsMode, lowTaskIndex, middleTaskIndex, overlapDefinition,intervalTree,numberofComparisonsDnase,cellLineNumber2CellLineNameMap,fileNumber2FileNameMap);
				
				//logger.info("Find Overlaps Left" + "\t" + "lowTaskIndex:" + "\t" + lowTaskIndex + "\t" + "middleTaskIndex:" +  "\t" + middleTaskIndex); 
				
				FindOverlaps right = new FindOverlaps(outputFolder,chromName, data, writeElementBasedAnnotationFoundOverlapsMode, middleTaskIndex, highTaskIndex, overlapDefinition,intervalTree,numberofComparisonsDnase,cellLineNumber2CellLineNameMap,fileNumber2FileNameMap);
				
				//logger.info("Find Overlaps Right" + "\t" + "middleTaskIndex:" + "\t" + middleTaskIndex + "\t" + "highTaskIndex:" +  "\t" + highTaskIndex); 
				
				left.fork();
				rightKArray = right.compute();
				leftKArray = left.join();

				// Add the contents of leftMap into the rightMap
				accumulate(leftKArray, rightKArray);

				leftKArray = null;
				return rightKArray;
			}
			// CONQUER
			else {

				kArray = new  int[numberofComparisonsDnase];

				//logger.info(chromName.convertEnumtoString() + "\t" + "Before " + "\t" + "lowTaskIndex" + "\t"+ lowTaskIndex + "\t" +  "highTaskIndex" + "\t"+ highTaskIndex + "\t" + "elementNumber2KMap.size()" + "\t" +   elementNumber2KMap.size());
				
				for (int i = lowTaskIndex; i < highTaskIndex; i++) {
					
					interval = data.get(i);
					
					//logger.info(chromName.convertEnumtoString() + "\t"  + "Interval Index:" + i);

					int[] oneOrZeroArray = new int[numberofComparisonsDnase]; 

					
					if (intervalTree.getRoot().getNodeName().isNotSentinel()) {
						
						intervalTree.findAllOverlappingDnaseIntervalsWithNumbers(
								outputFolder, 
								writeElementBasedAnnotationFoundOverlapsMode,
								intervalTree.getRoot(), 
								interval, 
								chromName,
								oneOrZeroArray, 
								overlapDefinition, 
								cellLineNumber2CellLineNameMap, 
								fileNumber2FileNameMap);
					}
					
					//logger.info(chromName.convertEnumtoString() + "\t"  + "Interval Index:" + i + "\t" + "elementNumber2OneorZeroMap.size()" + "\t" +elementNumber2OneorZeroMap.size());

					accumulate(oneOrZeroArray, kArray);
					
					
					oneOrZeroArray = null;
					
				}// End of FOR
				
				//logger.info(chromName.convertEnumtoString() + "\t" + "After " + "\t" + "lowTaskIndex" + "\t"+ lowTaskIndex + "\t" +  "highTaskIndex" + "\t" + highTaskIndex +  "\t" + "elementNumber2KMap.size()"+ "\t"+   elementNumber2KMap.size());
				

				return kArray;
			}

			
		}//End of Compute method
		
		// Add the content of leftArray to rightArray
		// Clear(removed) and null leftMap
		protected void accumulate(int[] leftArray, int[] rightArray) {
			
			Arrays.setAll(rightArray, i -> leftArray[i] + rightArray[i]);

		}
		
		
	}


	// Empirical P value Calculation
	// For Thread Version for
	public void createChromBaseSeachInputFiles(String outputFolder, String permutationNumber, List<BufferedWriter> bufferedWriterList) {
		try {
			FileWriter fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr1_input_file.txt");
			FileWriter fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr2_input_file.txt");
			FileWriter fileWriter3 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr3_input_file.txt");
			FileWriter fileWriter4 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr4_input_file.txt");
			FileWriter fileWriter5 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr5_input_file.txt");
			FileWriter fileWriter6 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr6_input_file.txt");
			FileWriter fileWriter7 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr7_input_file.txt");
			FileWriter fileWriter8 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr8_input_file.txt");
			FileWriter fileWriter9 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr9_input_file.txt");
			FileWriter fileWriter10 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr10_input_file.txt");
			FileWriter fileWriter11 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr11_input_file.txt");
			FileWriter fileWriter12 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr12_input_file.txt");
			FileWriter fileWriter13 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr13_input_file.txt");
			FileWriter fileWriter14 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr14_input_file.txt");
			FileWriter fileWriter15 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr15_input_file.txt");
			FileWriter fileWriter16 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr16_input_file.txt");
			FileWriter fileWriter17 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr17_input_file.txt");
			FileWriter fileWriter18 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr18_input_file.txt");
			FileWriter fileWriter19 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr19_input_file.txt");
			FileWriter fileWriter20 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr20_input_file.txt");
			FileWriter fileWriter21 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr21_input_file.txt");
			FileWriter fileWriter22 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chr22_input_file.txt");
			FileWriter fileWriter23 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chrX_input_file.txt");
			FileWriter fileWriter24 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + "_" + permutationNumber + "_" + "search_chrY_input_file.txt");

			BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
			BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);
			BufferedWriter bufferedWriter3 = new BufferedWriter(fileWriter3);
			BufferedWriter bufferedWriter4 = new BufferedWriter(fileWriter4);
			BufferedWriter bufferedWriter5 = new BufferedWriter(fileWriter5);
			BufferedWriter bufferedWriter6 = new BufferedWriter(fileWriter6);
			BufferedWriter bufferedWriter7 = new BufferedWriter(fileWriter7);
			BufferedWriter bufferedWriter8 = new BufferedWriter(fileWriter8);
			BufferedWriter bufferedWriter9 = new BufferedWriter(fileWriter9);
			BufferedWriter bufferedWriter10 = new BufferedWriter(fileWriter10);
			BufferedWriter bufferedWriter11 = new BufferedWriter(fileWriter11);
			BufferedWriter bufferedWriter12 = new BufferedWriter(fileWriter12);
			BufferedWriter bufferedWriter13 = new BufferedWriter(fileWriter13);
			BufferedWriter bufferedWriter14 = new BufferedWriter(fileWriter14);
			BufferedWriter bufferedWriter15 = new BufferedWriter(fileWriter15);
			BufferedWriter bufferedWriter16 = new BufferedWriter(fileWriter16);
			BufferedWriter bufferedWriter17 = new BufferedWriter(fileWriter17);
			BufferedWriter bufferedWriter18 = new BufferedWriter(fileWriter18);
			BufferedWriter bufferedWriter19 = new BufferedWriter(fileWriter19);
			BufferedWriter bufferedWriter20 = new BufferedWriter(fileWriter20);
			BufferedWriter bufferedWriter21 = new BufferedWriter(fileWriter21);
			BufferedWriter bufferedWriter22 = new BufferedWriter(fileWriter22);
			BufferedWriter bufferedWriter23 = new BufferedWriter(fileWriter23);
			BufferedWriter bufferedWriter24 = new BufferedWriter(fileWriter24);

			bufferedWriterList.add(bufferedWriter1);
			bufferedWriterList.add(bufferedWriter2);
			bufferedWriterList.add(bufferedWriter3);
			bufferedWriterList.add(bufferedWriter4);
			bufferedWriterList.add(bufferedWriter5);
			bufferedWriterList.add(bufferedWriter6);
			bufferedWriterList.add(bufferedWriter7);
			bufferedWriterList.add(bufferedWriter8);
			bufferedWriterList.add(bufferedWriter9);
			bufferedWriterList.add(bufferedWriter10);
			bufferedWriterList.add(bufferedWriter11);
			bufferedWriterList.add(bufferedWriter12);
			bufferedWriterList.add(bufferedWriter13);
			bufferedWriterList.add(bufferedWriter14);
			bufferedWriterList.add(bufferedWriter15);
			bufferedWriterList.add(bufferedWriter16);
			bufferedWriterList.add(bufferedWriter17);
			bufferedWriterList.add(bufferedWriter18);
			bufferedWriterList.add(bufferedWriter19);
			bufferedWriterList.add(bufferedWriter20);
			bufferedWriterList.add(bufferedWriter21);
			bufferedWriterList.add(bufferedWriter22);
			bufferedWriterList.add(bufferedWriter23);
			bufferedWriterList.add(bufferedWriter24);

		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	public void writeChromBaseSearchInputFile(ChromosomeName chromName, String strLine, List<BufferedWriter> bufList) {
		try {

			if (chromName.equals(ChromosomeName.CHROMOSOME1)) {
				bufList.get(0).write(strLine + System.getProperty("line.separator"));
				bufList.get(0).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME2)) {
				bufList.get(1).write(strLine + System.getProperty("line.separator"));
				bufList.get(1).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME3)) {
				bufList.get(2).write(strLine + System.getProperty("line.separator"));
				bufList.get(2).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME4)) {
				bufList.get(3).write(strLine + System.getProperty("line.separator"));
				bufList.get(3).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME5)) {
				bufList.get(4).write(strLine + System.getProperty("line.separator"));
				bufList.get(4).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME6)) {
				bufList.get(5).write(strLine + System.getProperty("line.separator"));
				bufList.get(5).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME7)) {
				bufList.get(6).write(strLine + System.getProperty("line.separator"));
				bufList.get(6).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME8)) {
				bufList.get(7).write(strLine + System.getProperty("line.separator"));
				bufList.get(7).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME9)) {
				bufList.get(8).write(strLine + System.getProperty("line.separator"));
				bufList.get(8).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME10)) {
				bufList.get(9).write(strLine + System.getProperty("line.separator"));
				bufList.get(9).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME11)) {
				bufList.get(10).write(strLine + System.getProperty("line.separator"));
				bufList.get(10).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME12)) {
				bufList.get(11).write(strLine + System.getProperty("line.separator"));
				bufList.get(11).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME13)) {
				bufList.get(12).write(strLine + System.getProperty("line.separator"));
				bufList.get(12).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME14)) {
				bufList.get(13).write(strLine + System.getProperty("line.separator"));
				bufList.get(13).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME15)) {
				bufList.get(14).write(strLine + System.getProperty("line.separator"));
				bufList.get(14).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME16)) {
				bufList.get(15).write(strLine + System.getProperty("line.separator"));
				bufList.get(15).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME17)) {
				bufList.get(16).write(strLine + System.getProperty("line.separator"));
				bufList.get(16).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME18)) {
				bufList.get(17).write(strLine + System.getProperty("line.separator"));
				bufList.get(17).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME19)) {
				bufList.get(18).write(strLine + System.getProperty("line.separator"));
				bufList.get(18).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME20)) {
				bufList.get(19).write(strLine + System.getProperty("line.separator"));
				bufList.get(19).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME21)) {
				bufList.get(20).write(strLine + System.getProperty("line.separator"));
				bufList.get(20).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOME22)) {
				bufList.get(21).write(strLine + System.getProperty("line.separator"));
				bufList.get(21).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOMEX)) {
				bufList.get(22).write(strLine + System.getProperty("line.separator"));
				bufList.get(22).flush();
			} else if (chromName.equals(ChromosomeName.CHROMOSOMEY)) {
				bufList.get(23).write(strLine + System.getProperty("line.separator"));
				bufList.get(23).flush();
			} else {
				GlanetRunner.appendLog("Unknown chromosome");
			}

		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	public void partitionSearchInputFilePerChromName(String inputFileName, List<BufferedWriter> bufferedWriterList) {

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		String strLine;
		int indexofFirstTab;
		ChromosomeName chromName;

		try {
			fileReader = FileOperations.createFileReader(inputFileName);
			bufferedReader = new BufferedReader(fileReader);

			while ((strLine = bufferedReader.readLine()) != null) {

				indexofFirstTab = strLine.indexOf('\t');
				chromName = ChromosomeName.convertStringtoEnum(strLine.substring(0, indexofFirstTab));
				writeChromBaseSearchInputFile(chromName, strLine, bufferedWriterList);

			} // End of While

		} catch (FileNotFoundException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		}

		try {
			bufferedReader.close();
			fileReader.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	// Empirical P value Computation
	public void partitionSearchInputFilePerChromName(List<InputLine> inputLines, List<BufferedWriter> bufferedWriterList) {

		String strLine;

		ChromosomeName chrName;
		int low;
		int high;

		InputLine inputLine;

		for (int i = 0; i < inputLines.size(); i++) {

			inputLine = inputLines.get(i);

			chrName = inputLine.getChrName();
			low = inputLine.getLow();
			high = inputLine.getHigh();

			strLine = chrName + "\t" + low + "\t" + high;

			writeChromBaseSearchInputFile(chrName, strLine, bufferedWriterList);

		} // End of While

	}

	// Generate Dnase Interval Tree with Numbers starts
	// For Annotation and Enrichment
	// Empirical P Value Calculation
	public static IntervalTree generateEncodeDnaseIntervalTreeWithNumbers(BufferedReader bufferedReader) {
		IntervalTree dnaseIntervalTree = new IntervalTree();
		String strLine = null;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;

		int startPosition = 0;
		int endPosition = 0;

		ChromosomeName chromName;
		short cellLineNumber;
		short fileNumber;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {

				// old example strLine
				// chr1 91852781 91853156 GM12878
				// idrPool.GM12878-DS9432-DS10671.z_OV_GM12878-DS10671.z_VS_GM12878-DS9432.z.npk2.narrowPeak

				// new example line with numbers
				// chrY 2709520 2709669 1 1

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);

				chromName = ChromosomeName.convertStringtoEnum(strLine.substring(0, indexofFirstTab));

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				cellLineNumber = Short.parseShort(strLine.substring(indexofThirdTab + 1, indexofFourthTab));
				fileNumber = Short.parseShort(strLine.substring(indexofFourthTab + 1));

				// important note
				// while constructing the dnaseIntervalTree
				// we don't check for overlaps
				// we insert any given interval without overlap check

				// Creating millions of nodes with six attributes causes out of
				// memory error
				DnaseIntervalTreeNodeWithNumbers node = new DnaseIntervalTreeNodeWithNumbers(chromName, startPosition, endPosition, cellLineNumber, fileNumber, NodeType.ORIGINAL);
				dnaseIntervalTree.intervalTreeInsert(dnaseIntervalTree, node);

			} // End of While
		} catch (IOException e) {
			logger.error(e.toString());
		}

		return dnaseIntervalTree;
	}

	// Generate Interval Tree with Numbers ends

	// Empirical P Value Calculation
	public static IntervalTree generateEncodeDnaseIntervalTree(BufferedReader bufferedReader) {
		IntervalTree dnaseIntervalTree = new IntervalTree();
		String strLine = null;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;

		int startPosition = 0;
		int endPosition = 0;

		String chromName;
		String cellLineName;
		String fileName;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// example strLine
				// chr1 91852781 91853156 GM12878
				// idrPool.GM12878-DS9432-DS10671.z_OV_GM12878-DS10671.z_VS_GM12878-DS9432.z.npk2.narrowPeak

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);

				chromName = strLine.substring(0, indexofFirstTab);

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				cellLineName = strLine.substring(indexofThirdTab + 1, indexofFourthTab);

				fileName = strLine.substring(indexofFourthTab + 1);

				// important note
				// while constructing the dnaseIntervalTree
				// we don't check for overlaps
				// we insert any given interval without overlap check

				// Creating millions of nodes with six attributes causes out of
				// memory error
				IntervalTreeNode node = new DnaseIntervalTreeNode(ChromosomeName.convertStringtoEnum(chromName), startPosition, endPosition, cellLineName, fileName, NodeType.ORIGINAL);
				dnaseIntervalTree.intervalTreeInsert(dnaseIntervalTree, node);

				chromName = null;
				cellLineName = null;
				fileName = null;
				strLine = null;

			} // End of While
		} catch (IOException e) {
			logger.error(e.toString());
		}

		return dnaseIntervalTree;
	}

	public IntervalTree generateEncodeDnaseIntervalTree(BufferedReader bufferedReader, List<String> dnaseCellLineNameList) {
		IntervalTree dnaseIntervalTree = new IntervalTree();
		String strLine = null;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;

		int startPosition = 0;
		int endPosition = 0;

		String chromName;
		String cellLineName;
		String fileName;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// example strLine
				// chr1 91852781 91853156 GM12878
				// idrPool.GM12878-DS9432-DS10671.z_OV_GM12878-DS10671.z_VS_GM12878-DS9432.z.npk2.narrowPeak

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);

				chromName = strLine.substring(0, indexofFirstTab);

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				cellLineName = strLine.substring(indexofThirdTab + 1, indexofFourthTab);

				fileName = strLine.substring(indexofFourthTab + 1);

				// important note
				// while constructing the dnaseIntervalTree
				// we don't check for overlaps
				// we insert any given interval without overlap check

				// if dnase exists in dnaseList
				if (dnaseCellLineNameList.contains(cellLineName)) {
					// Creating millions of nodes with six attributes causes out
					// of memory error
					IntervalTreeNode node = new DnaseIntervalTreeNode(ChromosomeName.convertStringtoEnum(chromName), startPosition, endPosition, cellLineName, fileName, NodeType.ORIGINAL);
					dnaseIntervalTree.intervalTreeInsert(dnaseIntervalTree, node);
				} // End of If

				chromName = null;
				cellLineName = null;
				fileName = null;
				strLine = null;

			} // End of While
		} catch (IOException e) {
			logger.error(e.toString());
		}

		return dnaseIntervalTree;
	}

	// starts
	public static IntervalTree generateUserDefinedLibraryIntervalTreeWithNumbers(BufferedReader bufferedReader) {
		IntervalTree userDefinedLibraryIntervalTree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;

		ChromosomeName chromName;
		int startPosition = 0;
		int endPosition = 0;
		int elementTypeNumber;
		int elementNumber;
		int fileNumber;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// example strLine
				// chr6 133593985 133594135 1 1 1
				// chrX 109134213 109134446 1 1 343

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = (indexofFirstTab > 0) ? strLine.indexOf('\t', indexofFirstTab + 1) : -1;
				indexofThirdTab = (indexofSecondTab > 0) ? strLine.indexOf('\t', indexofSecondTab + 1) : -1;
				indexofFourthTab = (indexofThirdTab > 0) ? strLine.indexOf('\t', indexofThirdTab + 1) : -1;
				indexofFifthTab = (indexofFourthTab > 0) ? strLine.indexOf('\t', indexofFourthTab + 1) : -1;

				chromName = ChromosomeName.convertStringtoEnum(strLine.substring(0, indexofFirstTab));

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));
				elementTypeNumber = Integer.parseInt(strLine.substring(indexofThirdTab + 1, indexofFourthTab));
				elementNumber = Integer.parseInt(strLine.substring(indexofFourthTab + 1, indexofFifthTab));
				fileNumber = Integer.parseInt(strLine.substring(indexofFifthTab + 1));

				// Creating millions of nodes with six attributes causes out of
				// memory error
				UserDefinedLibraryIntervalTreeNodeWithNumbers node = new UserDefinedLibraryIntervalTreeNodeWithNumbers(chromName, startPosition, endPosition, elementTypeNumber, elementNumber, fileNumber, NodeType.ORIGINAL);
				userDefinedLibraryIntervalTree.intervalTreeInsert(userDefinedLibraryIntervalTree, node);

				chromName = null;
				strLine = null;

			}
		} catch (NumberFormatException e) {

			logger.error(e.toString());

		} catch (IOException e) {

			logger.error(e.toString());
		}// End of WHILE

		return userDefinedLibraryIntervalTree;

	}

	// ends

	// @todo
	// Empirical P Value Calculation
	public static IntervalTree generateEncodeTfbsIntervalTreeWithNumbers(BufferedReader bufferedReader) {
		IntervalTree tfbsIntervalTree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;

		int startPosition = 0;
		int endPosition = 0;

		ChromosomeName chromName;
		Short tfNumber;
		Short cellLineNumber;
		Short fileNumber;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// exampple strLine
				// chrY 2804079 2804213 Ctcf H1hesc
				// spp.optimal.wgEncodeBroadHistoneH1hescCtcfStdAlnRep0_VS_wgEncodeBroadHistoneH1hescControlStdAlnRep0.narrowPeak

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);
				indexofFifthTab = strLine.indexOf('\t', indexofFourthTab + 1);

				chromName = ChromosomeName.convertStringtoEnum(strLine.substring(0, indexofFirstTab));

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				tfNumber = Short.parseShort(strLine.substring(indexofThirdTab + 1, indexofFourthTab));

				cellLineNumber = Short.parseShort(strLine.substring(indexofFourthTab + 1, indexofFifthTab));

				fileNumber = Short.parseShort(strLine.substring(indexofFifthTab + 1));

				// Creating millions of nodes with six attributes causes out of
				// memory error
				TforHistoneIntervalTreeNodeWithNumbers node = new TforHistoneIntervalTreeNodeWithNumbers(chromName, startPosition, endPosition, tfNumber, cellLineNumber, fileNumber, NodeType.ORIGINAL);
				tfbsIntervalTree.intervalTreeInsert(tfbsIntervalTree, node);

				chromName = null;
				strLine = null;

			}
		} catch (IOException e) {

			logger.error(e.toString());
		}

		return tfbsIntervalTree;
	}

	// @todo

	// Empirical P Value Calculation
	public static IntervalTree generateEncodeTfbsIntervalTree(BufferedReader bufferedReader) {
		IntervalTree tfbsIntervalTree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;

		int startPosition = 0;
		int endPosition = 0;

		String chromName;
		String tfbsName;
		String cellLineName;
		String fileName;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// exampple strLine
				// chrY 2804079 2804213 Ctcf H1hesc
				// spp.optimal.wgEncodeBroadHistoneH1hescCtcfStdAlnRep0_VS_wgEncodeBroadHistoneH1hescControlStdAlnRep0.narrowPeak

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);
				indexofFifthTab = strLine.indexOf('\t', indexofFourthTab + 1);

				chromName = strLine.substring(0, indexofFirstTab);

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				tfbsName = strLine.substring(indexofThirdTab + 1, indexofFourthTab);

				cellLineName = strLine.substring(indexofFourthTab + 1, indexofFifthTab);

				fileName = strLine.substring(indexofFifthTab + 1);

				// Creating millions of nodes with six attributes causes out of
				// memory error
				IntervalTreeNode node = new TforHistoneIntervalTreeNode(ChromosomeName.convertStringtoEnum(chromName), startPosition, endPosition, tfbsName, cellLineName, fileName, NodeType.ORIGINAL);
				tfbsIntervalTree.intervalTreeInsert(tfbsIntervalTree, node);

				chromName = null;
				tfbsName = null;
				cellLineName = null;
				fileName = null;
				strLine = null;

			}
		} catch (IOException e) {

			logger.error(e.toString());
		}

		return tfbsIntervalTree;
	}

	public IntervalTree generateEncodeTfbsIntervalTree(BufferedReader bufferedReader, List<String> tfbsNameList) {
		IntervalTree tfbsIntervalTree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;

		int startPosition = 0;
		int endPosition = 0;

		String chromName;
		String tfbsName;
		String cellLineName;
		String fileName;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// example strLine
				// chrY 2804079 2804213 Ctcf H1hesc
				// spp.optimal.wgEncodeBroadHistoneH1hescCtcfStdAlnRep0_VS_wgEncodeBroadHistoneH1hescControlStdAlnRep0.narrowPeak

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);
				indexofFifthTab = strLine.indexOf('\t', indexofFourthTab + 1);

				chromName = strLine.substring(0, indexofFirstTab);

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				tfbsName = strLine.substring(indexofThirdTab + 1, indexofFourthTab);

				cellLineName = strLine.substring(indexofFourthTab + 1, indexofFifthTab);

				fileName = strLine.substring(indexofFifthTab + 1);

				// if tfbs exists in tfbsList
				if (tfbsNameList.contains(tfbsName)) {
					// Creating millions of nodes with six attributes causes out
					// of memory error
					IntervalTreeNode node = new TforHistoneIntervalTreeNode(ChromosomeName.convertStringtoEnum(chromName), startPosition, endPosition, tfbsName, cellLineName, fileName, NodeType.ORIGINAL);
					tfbsIntervalTree.intervalTreeInsert(tfbsIntervalTree, node);
				}

				chromName = null;
				tfbsName = null;
				cellLineName = null;
				fileName = null;
				strLine = null;

			}
		} catch (IOException e) {

			logger.error(e.toString());
		}

		return tfbsIntervalTree;
	}

	// @todo
	// Empirical P Value Calculation
	public static IntervalTree generateEncodeHistoneIntervalTreeWithNumbers(BufferedReader bufferedReader) {
		IntervalTree histoneIntervalTree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;

		ChromosomeName chromName;
		int startPosition = 0;
		int endPosition = 0;

		short histoneNumber;
		short cellLineNumber;
		short fileNumber;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// old example strLine
				// chr9 131533188 131535395 H2az Gm12878
				// wgEncodeBroadHistoneGm12878H2azStdAln.narrowPeak
				// new example strLine
				// chr22 20747267 20749217 1 17 654

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);
				indexofFifthTab = strLine.indexOf('\t', indexofFourthTab + 1);

				chromName = ChromosomeName.convertStringtoEnum(strLine.substring(0, indexofFirstTab));

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				histoneNumber = Short.parseShort(strLine.substring(indexofThirdTab + 1, indexofFourthTab));

				cellLineNumber = Short.parseShort(strLine.substring(indexofFourthTab + 1, indexofFifthTab));

				fileNumber = Short.parseShort(strLine.substring(indexofFifthTab + 1));

				// Creating millions of nodes with six attributes causes out of
				// memory error
				TforHistoneIntervalTreeNodeWithNumbers node = new TforHistoneIntervalTreeNodeWithNumbers(chromName, startPosition, endPosition, histoneNumber, cellLineNumber, fileNumber, NodeType.ORIGINAL);
				histoneIntervalTree.intervalTreeInsert(histoneIntervalTree, node);

				chromName = null;
				strLine = null;

			}
		} catch (IOException e) {

			logger.error(e.toString());
		}

		return histoneIntervalTree;
	}

	// @todo

	// Empirical P Value Calculation
	public static IntervalTree generateEncodeHistoneIntervalTree(BufferedReader bufferedReader) {
		IntervalTree histoneIntervalTree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;

		String chromName;
		int startPosition = 0;
		int endPosition = 0;

		String histoneName;
		String cellLineName;
		String fileName;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// example strLine
				// chr9 131533188 131535395 H2az Gm12878
				// wgEncodeBroadHistoneGm12878H2azStdAln.narrowPeak

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);
				indexofFifthTab = strLine.indexOf('\t', indexofFourthTab + 1);

				chromName = strLine.substring(0, indexofFirstTab);

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				histoneName = strLine.substring(indexofThirdTab + 1, indexofFourthTab);

				cellLineName = strLine.substring(indexofFourthTab + 1, indexofFifthTab);

				fileName = strLine.substring(indexofFifthTab + 1);

				// Creating millions of nodes with six attributes causes out of
				// memory error
				IntervalTreeNode node = new TforHistoneIntervalTreeNode(ChromosomeName.convertStringtoEnum(chromName), startPosition, endPosition, histoneName, cellLineName, fileName, NodeType.ORIGINAL);
				histoneIntervalTree.intervalTreeInsert(histoneIntervalTree, node);

				chromName = null;
				histoneName = null;
				cellLineName = null;
				fileName = null;
				strLine = null;

			}
		} catch (IOException e) {

			logger.error(e.toString());
		}

		return histoneIntervalTree;
	}

	public IntervalTree generateEncodeHistoneIntervalTree(BufferedReader bufferedReader, List<String> histoneNameList) {
		IntervalTree histoneIntervalTree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;

		String chromName;
		int startPosition = 0;
		int endPosition = 0;

		String histoneName;
		String cellLineName;
		String fileName;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// example strLine
				// chr9 131533188 131535395 H2az Gm12878
				// wgEncodeBroadHistoneGm12878H2azStdAln.narrowPeak

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);
				indexofFifthTab = strLine.indexOf('\t', indexofFourthTab + 1);

				chromName = strLine.substring(0, indexofFirstTab);

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				histoneName = strLine.substring(indexofThirdTab + 1, indexofFourthTab);

				cellLineName = strLine.substring(indexofFourthTab + 1, indexofFifthTab);

				fileName = strLine.substring(indexofFifthTab + 1);

				if (histoneNameList.contains(histoneName)) {
					// Creating millions of nodes with six attributes causes out
					// of memory error
					IntervalTreeNode node = new TforHistoneIntervalTreeNode(ChromosomeName.convertStringtoEnum(chromName), startPosition, endPosition, histoneName, cellLineName, fileName, NodeType.ORIGINAL);
					histoneIntervalTree.intervalTreeInsert(histoneIntervalTree, node);
				}

				chromName = null;
				histoneName = null;
				cellLineName = null;
				fileName = null;
				strLine = null;

			}
		} catch (IOException e) {

			logger.error(e.toString());
		}

		return histoneIntervalTree;
	}

	// @todo
	public static IntervalTree generateUcscRefSeqGenesIntervalTreeWithNumbers(BufferedReader bufferedReader) {
		IntervalTree tree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;
		int indexofSixthTab = 0;
		int indexofSeventhTab = 0;
		int indexofEighthTab = 0;

		int startPosition = 0;
		int endPosition = 0;

		ChromosomeName chromName;
		IntervalName intervalName;
		Integer intervalNumber;

		Integer geneEntrezId;
		Integer refSeqGeneNameNumber;
		Integer geneHugoSymbolNumber;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// example strLine
				// chrY 16733888 16734470 NR_028319 22829 EXON 2 + NLGN4Y
				// chr22 25170288 25170686 38084 440822 EXON 21 - 22048
				// chr22 25170687 25172686 38084 440822 5P1 - 22048

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);
				indexofFifthTab = strLine.indexOf('\t', indexofFourthTab + 1);
				indexofSixthTab = strLine.indexOf('\t', indexofFifthTab + 1);
				indexofSeventhTab = strLine.indexOf('\t', indexofSixthTab + 1);
				indexofEighthTab = strLine.indexOf('\t', indexofSeventhTab + 1);

				chromName = ChromosomeName.convertStringtoEnum(strLine.substring(0, indexofFirstTab));

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				refSeqGeneNameNumber = Integer.parseInt(strLine.substring(indexofThirdTab + 1, indexofFourthTab));
				geneEntrezId = Integer.parseInt(strLine.substring(indexofFourthTab + 1, indexofFifthTab));

				intervalName = IntervalName.convertStringtoEnum(strLine.substring(indexofFifthTab + 1, indexofSixthTab));
				intervalNumber = Integer.parseInt(strLine.substring(indexofSixthTab + 1, indexofSeventhTab));

				geneHugoSymbolNumber = Integer.parseInt(strLine.substring(indexofEighthTab + 1));

				// Creating millions of nodes with seven attributes causes out
				// of memory error
				UcscRefSeqGeneIntervalTreeNodeWithNumbers node = new UcscRefSeqGeneIntervalTreeNodeWithNumbers(chromName, startPosition, endPosition, geneEntrezId, refSeqGeneNameNumber, geneHugoSymbolNumber, intervalName, intervalNumber, NodeType.ORIGINAL);
				tree.intervalTreeInsert(tree, node);

				chromName = null;
				intervalName = null;

				geneEntrezId = null;
				refSeqGeneNameNumber = null;
				geneHugoSymbolNumber = null;

				strLine = null;

			}// end of While
		} catch (IOException e) {
			logger.error(e.toString());
		}

		return tree;
	}

	// @todo

	public static IntervalTree generateUcscRefSeqGenesIntervalTree(BufferedReader bufferedReader) {
		IntervalTree tree = new IntervalTree();
		String strLine;

		int indexofFirstTab = 0;
		int indexofSecondTab = 0;
		int indexofThirdTab = 0;
		int indexofFourthTab = 0;
		int indexofFifthTab = 0;
		int indexofSixthTab = 0;
		int indexofSeventhTab = 0;
		int indexofEighthTab = 0;

		int startPosition = 0;
		int endPosition = 0;

		ChromosomeName chromName;
		String refSeqGeneName;
		Integer geneEntrezId;
		IntervalName intervalName;
		int intervalNumber;
		String geneHugoSymbol;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				// example strLine
				// chr17 67074846 67075215 NM_080284 23460 Exon 1 - ABCA6

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);
				indexofThirdTab = strLine.indexOf('\t', indexofSecondTab + 1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab + 1);
				indexofFifthTab = strLine.indexOf('\t', indexofFourthTab + 1);
				indexofSixthTab = strLine.indexOf('\t', indexofFifthTab + 1);
				indexofSeventhTab = strLine.indexOf('\t', indexofSixthTab + 1);
				indexofEighthTab = strLine.indexOf('\t', indexofSeventhTab + 1);

				chromName = ChromosomeName.convertStringtoEnum(strLine.substring(0, indexofFirstTab));

				startPosition = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
				endPosition = Integer.parseInt(strLine.substring(indexofSecondTab + 1, indexofThirdTab));

				refSeqGeneName = strLine.substring(indexofThirdTab + 1, indexofFourthTab);

				geneEntrezId = Integer.parseInt(strLine.substring(indexofFourthTab + 1, indexofFifthTab));

				intervalName = IntervalName.convertStringtoEnum(strLine.substring(indexofFifthTab + 1, indexofSixthTab));
				intervalNumber = Integer.parseInt(strLine.substring(indexofSixthTab + 1, indexofSeventhTab));

				geneHugoSymbol = strLine.substring(indexofEighthTab + 1);

				// Creating millions of nodes with seven attributes causes out
				// of memory error
				IntervalTreeNode node = new UcscRefSeqGeneIntervalTreeNode(chromName, startPosition, endPosition, refSeqGeneName, geneEntrezId, intervalName, intervalNumber, geneHugoSymbol, NodeType.ORIGINAL);
				tree.intervalTreeInsert(tree, node);

				chromName = null;
				refSeqGeneName = null;
				geneEntrezId = null;
				intervalName = null;
				geneHugoSymbol = null;
				strLine = null;
			}// end of While
		} catch (IOException e) {
			logger.error(e.toString());
		}

		return tree;
	}

	public static IntervalTree createUserDefinedIntervalTreeWithNumbers(String dataFolder, int elementTypeNumber, String elementType, ChromosomeName chromName) {
		IntervalTree userDefinedLibraryIntervalTree = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {

			fileReader = FileOperations.createFileReader(dataFolder + Commons.USER_DEFINED_LIBRARY + System.getProperty("file.separator") + elementType + System.getProperty("file.separator"), chromName.convertEnumtoString() + Commons.UNSORTED_USERDEFINEDLIBRARY_FILE_WITH_NUMBERS);
			bufferedReader = new BufferedReader(fileReader);
			userDefinedLibraryIntervalTree = generateUserDefinedLibraryIntervalTreeWithNumbers(bufferedReader);

		} catch (IOException e) {

			logger.error(e.toString());
		}

		return userDefinedLibraryIntervalTree;

	}

	// Generate Interval Tree
	// With Number starts
	// For Annotation and Enrichment
	public static IntervalTree createDnaseIntervalTreeWithNumbers(String dataFolder, ChromosomeName chromName) {
		IntervalTree dnaseIntervalTree = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {

			fileReader = FileOperations.createFileReader(dataFolder + Commons.BYGLANET_ENCODE_DNASE_DIRECTORY, chromName.convertEnumtoString() + Commons.UNSORTED_ENCODE_DNASE_FILE_WITH_NUMBERS);
			bufferedReader = new BufferedReader(fileReader);
			dnaseIntervalTree = generateEncodeDnaseIntervalTreeWithNumbers(bufferedReader);

		} catch (FileNotFoundException e) {
			logger.error(e.toString());
		} catch (IOException e) {

			logger.error(e.toString());
		}

		return dnaseIntervalTree;
	}

	// With Number ends

	// @todo
	// Empirical P Value Calculation
	public static IntervalTree createTfbsIntervalTreeWithNumbers(String dataFolder, ChromosomeName chromName) {
		IntervalTree tfbsIntervalTree = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {

			fileReader = FileOperations.createFileReader(dataFolder + Commons.BYGLANET_ENCODE_TF_DIRECTORY, chromName.convertEnumtoString() + Commons.UNSORTED_ENCODE_TF_FILE_WITH_NUMBERS);
			bufferedReader = new BufferedReader(fileReader);
			tfbsIntervalTree = generateEncodeTfbsIntervalTreeWithNumbers(bufferedReader);

		} catch (FileNotFoundException e) {
			logger.error(e.toString());
		} catch (IOException e) {

			logger.error(e.toString());
		}

		return tfbsIntervalTree;
	}

	// @todo

	// @todo @test
	// Empirical P Value Calculation
	public static IntervalTree createHistoneIntervalTreeWithNumbers(String dataFolder, ChromosomeName chromName) {
		IntervalTree histoneIntervalTree = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {
			fileReader = FileOperations.createFileReader(dataFolder + Commons.BYGLANET_ENCODE_HISTONE_DIRECTORY, chromName.convertEnumtoString() + Commons.UNSORTED_ENCODE_HISTONE_FILE_WITH_NUMBERS);
			bufferedReader = new BufferedReader(fileReader);
			histoneIntervalTree = generateEncodeHistoneIntervalTreeWithNumbers(bufferedReader);

		} catch (IOException e) {

			logger.error(e.toString());
		}

		return histoneIntervalTree;
	}

	// @todo @test

	public static IntervalTree createUcscRefSeqGenesIntervalTreeWithNumbers(String dataFolder, ChromosomeName chromName) {
		IntervalTree ucscRefSeqGenesIntervalTree = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {
			fileReader = FileOperations.createFileReader(dataFolder + Commons.BYGLANET_UCSCGENOME_HG19_REFSEQ_GENES_DIRECTORY, chromName.convertEnumtoString() + Commons.UNSORTED_UCSCGENOME_HG19_REFSEQ_GENES_FILE_WITH_NUMBERS);
			bufferedReader = new BufferedReader(fileReader);
			ucscRefSeqGenesIntervalTree = generateUcscRefSeqGenesIntervalTreeWithNumbers(bufferedReader);

		} catch (IOException e) {

			logger.error(e.toString());
		}

		return ucscRefSeqGenesIntervalTree;

	}

	// Enrichment
	// With IO
	// With Numbers
	// Empirical P Value Calculation
	// with IO
	public static void searchDnaseWithIOWithNumbers(String outputFolder, int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree dnaseIntervalTree, TIntObjectMap<BufferedWriter> dnaseBufferedWriterHashMap, TIntIntMap permutationNumberDnaseCellLineNumber2KMap, int overlapDefinition) {
		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {
			TIntIntMap permutationNumberDnaseCellLineNumber2ZeroorOneMap = new TIntIntHashMap();

			inputLine = inputLines.get(i);
			low = inputLine.getLow();
			high = inputLine.getHigh();

			Interval interval = new Interval(low, high);

			if (dnaseIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				dnaseIntervalTree.findAllOverlappingDnaseIntervalsWithIOWithNumbers(outputFolder, permutationNumber, dnaseIntervalTree.getRoot(), interval, chromName, dnaseBufferedWriterHashMap, permutationNumberDnaseCellLineNumber2ZeroorOneMap, overlapDefinition);
			}

			// accumulate search results of dnaseCellLine2OneorZeroMap in
			// permutationNumberDnaseCellLineName2KMap
			for (TIntIntIterator it = permutationNumberDnaseCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {
				it.advance();

				if (!(permutationNumberDnaseCellLineNumber2KMap.containsKey(it.key()))) {
					permutationNumberDnaseCellLineNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberDnaseCellLineNumber2KMap.put(it.key(), permutationNumberDnaseCellLineNumber2KMap.get(it.key()) + it.value());
				}

			}// End of for

			interval = null;

		}// End of for
	}

	// with numbers ends
	// @todo

	// Enrichment
	// Without IO
	// With Numbers
	// Empirical P Value Calculation
	public static void searchDnaseWithoutIOWithNumbers(int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree dnaseIntervalTree, TIntIntMap permutationNumberDnaseCellLineName2KMap, int overlapDefinition) {
		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {
			TIntIntMap permutationNumberDnaseCellLineName2ZeroorOneMap = new TIntIntHashMap();

			inputLine = inputLines.get(i);
			low = inputLine.getLow();
			high = inputLine.getHigh();

			Interval interval = new Interval(low, high);

			if (dnaseIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				dnaseIntervalTree.findAllOverlappingDnaseIntervalsWithoutIOWithNumbers(permutationNumber, dnaseIntervalTree.getRoot(), interval, chromName, permutationNumberDnaseCellLineName2ZeroorOneMap, overlapDefinition);
			}

			// accumulate search results of dnaseCellLine2OneorZeroMap in
			// permutationNumberDnaseCellLineName2KMap
			for (TIntIntIterator it = permutationNumberDnaseCellLineName2ZeroorOneMap.iterator(); it.hasNext();) {

				it.advance();

				if (!(permutationNumberDnaseCellLineName2KMap.containsKey(it.key()))) {
					permutationNumberDnaseCellLineName2KMap.put(it.key(), it.value());
				} else {
					permutationNumberDnaseCellLineName2KMap.put(it.key(), permutationNumberDnaseCellLineName2KMap.get(it.key()) + it.value());
				}

			}// End of for

			interval = null;

		}// End of for
	}

	// with number ends

	// Annotation
	// With Numbers
	public void searchDnaseWithNumbers(
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree dnaseIntervalTree, 
			TShortIntMap dnaseCellLineNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap) {

		String strLine = null;
		int indexofFirstTab = 0;
		int indexofSecondTab = 0;

		int low;
		int high;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {
				
				TShortByteMap dnaseCellLineNumber2OneorZeroMap = new TShortByteHashMap();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);

				low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0)
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				else
					high = low;

				Interval interval = new Interval(low, high);

				if (dnaseIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					
					dnaseIntervalTree.findAllOverlappingDnaseIntervalsWithNumbers(
							outputFolder, 
							writeElementBasedAnnotationFoundOverlapsMode,
							dnaseIntervalTree.getRoot(), 
							interval, 
							chromName,
							dnaseCellLineNumber2OneorZeroMap, 
							overlapDefinition, 
							cellLineNumber2CellLineNameMap, 
							fileNumber2FileNameMap);
				}

				for (TShortByteIterator it = dnaseCellLineNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();
					if (!dnaseCellLineNumber2KMap.containsKey(it.key())) {
						dnaseCellLineNumber2KMap.put(it.key(), it.value());
					} else {
						dnaseCellLineNumber2KMap.put(it.key(), dnaseCellLineNumber2KMap.get(it.key()) + it.value());

					}
				}/* End of For */

			}//End of while
		} catch (NumberFormatException e) {

			logger.error(e.toString());
		} catch (IOException e) {

			logger.error(e.toString());
		} // End of while

	}

	// @todo ends

	// Enrichment
	// With IO
	// With Numbers
	// Empirical P Value Calculation
	public static void searchUserDefinedLibraryWithIOWithNumbers(String outputFolder, int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree intervalTree, TLongObjectMap<BufferedWriter> permutationNumberElementTypeNumberElementNumberr2BufferedWriterMap, TLongIntMap permutationNumberElementTypeNumberElementNumber2KMap, int overlapDefinition) {

		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {
			TLongIntMap permutationNumberElementTypeNumberElementNumber2ZeroorOneMap = new TLongIntHashMap();

			inputLine = inputLines.get(i);

			low = inputLine.getLow();
			high = inputLine.getHigh();
			Interval interval = new Interval(low, high);

			if (intervalTree.getRoot().getNodeName().isNotSentinel()) {
				intervalTree.findAllOverlappingUserDefinedLibraryIntervalsWithIOWithNumbers(outputFolder, permutationNumber, intervalTree.getRoot(), interval, chromName, permutationNumberElementTypeNumberElementNumberr2BufferedWriterMap, permutationNumberElementTypeNumberElementNumber2ZeroorOneMap, overlapDefinition);
			}

			// accumulate search results of tfbsNameandCellLineName2ZeroorOneMap
			// in tfbsNameandCellLineName2KMap
			for (TLongIntIterator it = permutationNumberElementTypeNumberElementNumber2ZeroorOneMap.iterator(); it.hasNext();) {

				it.advance();

				if (!(permutationNumberElementTypeNumberElementNumber2KMap.containsKey(it.key()))) {
					permutationNumberElementTypeNumberElementNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberElementTypeNumberElementNumber2KMap.put(it.key(), permutationNumberElementTypeNumberElementNumber2KMap.get(it.key()) + it.value());

				}

			}// End of for
		}// End of for

	}

	// Enrichment
	// With IO
	// With Numbers
	// Empirical P Value Calculation
	// with IO
	public static void searchTfbsWithIOWithNumbers(String outputFolder, int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree tfbsIntervalTree, TLongObjectMap<BufferedWriter> tfbsBufferedWriterHashMap, TLongIntMap permutationNumberTfNumberCellLineNumber2KMap, int overlapDefinition) {

		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {
			TLongIntMap permutationNumberTfNumberCellLineNumber2ZeroorOneMap = new TLongIntHashMap();

			inputLine = inputLines.get(i);

			low = inputLine.getLow();
			high = inputLine.getHigh();
			Interval interval = new Interval(low, high);

			if (tfbsIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				tfbsIntervalTree.findAllOverlappingTfbsIntervalsWithIOWithNumbers(outputFolder, permutationNumber, tfbsIntervalTree.getRoot(), interval, chromName, tfbsBufferedWriterHashMap, permutationNumberTfNumberCellLineNumber2ZeroorOneMap, overlapDefinition);
			}

			// accumulate search results of tfbsNameandCellLineName2ZeroorOneMap
			// in tfbsNameandCellLineName2KMap
			for (TLongIntIterator it = permutationNumberTfNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {

				it.advance();

				if (!(permutationNumberTfNumberCellLineNumber2KMap.containsKey(it.key()))) {
					permutationNumberTfNumberCellLineNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberTfNumberCellLineNumber2KMap.put(it.key(), permutationNumberTfNumberCellLineNumber2KMap.get(it.key()) + it.value());

				}

			}// End of for
		}// End of for
	}

	// with Numbers
	// @todo

	// Enrichment
	// Without IO
	// With Numbers
	// Empirical P Value Calculation
	public static void searchTfbsWithoutIOWithNumbers(int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree tfbsIntervalTree, TLongIntMap permutationNumberTfbsNameCellLineName2KMap, int overlapDefinition) {

		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {
			TLongIntMap permutationNumberTfbsNameCellLineName2ZeroorOneMap = new TLongIntHashMap();

			inputLine = inputLines.get(i);

			low = inputLine.getLow();
			high = inputLine.getHigh();
			Interval interval = new Interval(low, high);

			if (tfbsIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				tfbsIntervalTree.findAllOverlappingTfbsIntervalsWithoutIOWithNumbers(permutationNumber, tfbsIntervalTree.getRoot(), interval, chromName, permutationNumberTfbsNameCellLineName2ZeroorOneMap, overlapDefinition);
			}

			// accumulate search results of tfbsNameandCellLineName2ZeroorOneMap
			// in tfbsNameandCellLineName2KMap
			for (TLongIntIterator it = permutationNumberTfbsNameCellLineName2ZeroorOneMap.iterator(); it.hasNext();) {
				it.advance();

				if (!(permutationNumberTfbsNameCellLineName2KMap.containsKey(it.key()))) {
					permutationNumberTfbsNameCellLineName2KMap.put(it.key(), it.value());
				} else {
					permutationNumberTfbsNameCellLineName2KMap.put(it.key(), permutationNumberTfbsNameCellLineName2KMap.get(it.key()) + it.value());

				}

			}// End of for

		}// End of for
	}

	// @todo

	// Gene
	// Annotation
	// with numbers
	public void searchGeneWithNumbers(
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			TIntObjectMap<String> givenIntervalNumber2GivenIntervalNameMap, 
			TIntObjectMap<OverlapInformation> givenIntervalNumber2OverlapInformationMap, 
			TObjectIntMap<ChromosomeName> chromosomeName2CountMap, 
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree ucscRefSeqGenesIntervalTree, 
			TIntIntMap entrezGeneId2KMap, 
			int overlapDefinition, 
			TIntObjectMap<String> officialGeneSymbolNumber2NameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap) {

		String strLine = null;
		int indexofFirstTab = 0;
		int indexofSecondTab = 0;

		int low;
		int high;

		String givenIntervalName = null;
		int givenIntervalNumber = 0;
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;


		try {
			
			if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
				if (bufferedWriter == null) {
					fileWriter = FileOperations.createFileWriter(outputFolder + Commons.HG19_REFSEQ_GENE_ANNOTATION_DIRECTORY + Commons.HG19_REFSEQ_GENE + ".txt", true);
					bufferedWriter = new BufferedWriter(fileWriter);
					bufferedWriter.write("#Searched_for_chr" + "\t" + "interval_Low" + "\t" + "interval_High" + "\t" + "ucscRefSeqGene_node_ChromName" + "\t" + "node_Low" + "\t" + "node_High" + "\t" + "node_RNA_Nucleotide_Accession" + "\t" + "node_IntervalName" + "\t" + "node_IntervalNumber" + "\t" + "node_GeneHugoSymbol" + "\t" + "node_GeneEntrezId" + System.getProperty("line.separator"));
					
				}
			}
			
			
			
			while ((strLine = bufferedReader.readLine()) != null) {

				TIntByteMap entrezGeneId2OneorZeroMap = new TIntByteHashMap();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);

				low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0)
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				else
					high = low;

				Interval interval = new Interval(low, high);

				/***************************************************************************************/
				/************* GIVEN INTERVAL NUMBER 2 GIVEN INTERVAL NAME MAP****starts ***************/
				/************* GIVEN INTERVAL NUMBER 2 NUMBEROFOVERLAPS MAP****starts ******************/
				/***************************************************************************************/
				givenIntervalName = chromName.convertEnumtoString() + "_" + low + "_" + high;

			

				if (!givenIntervalNumber2GivenIntervalNameMap.containsValue(givenIntervalName)) {

					// Set Given Interval Number
					givenIntervalNumber = givenIntervalNumber2GivenIntervalNameMap.size() + 1;

					givenIntervalNumber2GivenIntervalNameMap.put(givenIntervalNumber, givenIntervalName);
					chromosomeName2CountMap.put(chromName, chromosomeName2CountMap.get(chromName) + 1);

				}
				/***************************************************************************************/
				/************* GIVEN INTERVAL NUMBER 2 GIVEN INTERVAL NAME MAP****ends *****************/
				/************* GIVEN INTERVAL NUMBER 2 NUMBEROFOVERLAPS MAP****ends ********************/
				/***************************************************************************************/

				// UCSCRefSeqGenes Search starts here
				if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					ucscRefSeqGenesIntervalTree.findAllGeneOverlappingUcscRefSeqGenesIntervalsWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode,bufferedWriter,givenIntervalNumber, givenIntervalNumber2OverlapInformationMap, ucscRefSeqGenesIntervalTree.getRoot(), interval, chromName, entrezGeneId2OneorZeroMap, Commons.NCBI_GENE_ID, overlapDefinition, officialGeneSymbolNumber2NameMap, refSeqGeneNumber2RefSeqGeneNameMap);
				}
				// UCSCRefSeqGenes Search ends here

				// accumulate search results of
				// exonBasedKeggPathway2OneorZeroMap in
				// exonBasedKeggPathway2KMap
				for (TIntByteIterator it = entrezGeneId2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					if (!entrezGeneId2KMap.containsKey(it.key())) {
						entrezGeneId2KMap.put(it.key(), it.value());
					} else {
						entrezGeneId2KMap.put(it.key(), entrezGeneId2KMap.get(it.key()) + it.value());
					}

				}// End of for
				
				

			}// End of while
			
			
			if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
				
				//Close BufferedWriter
				if(bufferedWriter!=null){
					bufferedWriter.close();
				}
				
			}
				
			
			
			
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while
	}

	// @todo Gene Annotation with numbers ends

	// Annotation
	// with numbers
	// GeneSet
	public void searchGeneSetWithNumbers(
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree ucscRefSeqGenesIntervalTree, 
			TShortIntMap exonBasedGeneSetNumber2KMap,
			TShortIntMap regulationBasedGeneSetNumber2KMap, 
			TShortIntMap allBasedGeneSetNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> geneSetNumber2GeneSetNameMap, 
			TIntObjectMap<TShortList> geneId2ListofGeneSetNumberMap, 
			TIntObjectMap<String> geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap, 
			String geneSetName, 
			GeneSetType geneSetType) {

		String strLine = null;
		int indexofFirstTab = 0;
		int indexofSecondTab = 0;

		int low;
		int high;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {

				// KEGGPathway
				TShortByteMap exonBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();
				TShortByteMap regulationBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();
				TShortByteMap allBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);

				low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0)
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				else
					high = low;

				Interval interval = new Interval(low, high);

				// UCSCRefSeqGenes Search starts here
				if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					
					ucscRefSeqGenesIntervalTree.findAllOverlappingUcscRefSeqGenesIntervalsWithNumbers(
							outputFolder, 
							writeElementBasedAnnotationFoundOverlapsMode,
							ucscRefSeqGenesIntervalTree.getRoot(), 
							interval, 
							chromName, 
							exonBasedKeggPathway2OneorZeroMap, 
							regulationBasedKeggPathway2OneorZeroMap, 
							allBasedKeggPathway2OneorZeroMap, 
							Commons.NCBI_GENE_ID, 
							overlapDefinition, 
							geneSetNumber2GeneSetNameMap, 
							geneId2ListofGeneSetNumberMap, 
							geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
							refSeqGeneNumber2RefSeqGeneNameMap, 
							geneSetName, 
							geneSetType);
				}
				// UCSCRefSeqGenes Search ends here


				// accumulate search results of
				// exonBasedKeggPathway2OneorZeroMap in
				// exonBasedKeggPathway2KMap
				for (TShortByteIterator it = exonBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					if (!exonBasedGeneSetNumber2KMap.containsKey(it.key())) {
						exonBasedGeneSetNumber2KMap.put(it.key(), it.value());
					} else {
						exonBasedGeneSetNumber2KMap.put(it.key(), exonBasedGeneSetNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

				// accumulate search results of
				// regulationBasedKeggPathway2OneorZeroMap in
				// regulationBasedKeggPathway2KMap
				for (TShortByteIterator it = regulationBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!regulationBasedGeneSetNumber2KMap.containsKey(it.key())) {
						regulationBasedGeneSetNumber2KMap.put(it.key(), it.value());
					} else {
						regulationBasedGeneSetNumber2KMap.put(it.key(), regulationBasedGeneSetNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

				// accumulate search results of allBasedKeggPathway2OneorZeroMap
				// in allBasedKeggPathway2KMap
				for (TShortByteIterator it = allBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!allBasedGeneSetNumber2KMap.containsKey(it.key())) {
						allBasedGeneSetNumber2KMap.put(it.key(), it.value());
					} else {
						allBasedGeneSetNumber2KMap.put(it.key(), allBasedGeneSetNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for

			}// End of while
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while
	}

	// GeneSet
	// @todo Annotation with numbers ends

	// Annotation
	// With Numbers
	// TF KEGGPathway
	public void searchTfKEGGPathwayWithNumbers(
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree tfbsIntervalTree, 
			IntervalTree ucscRefSeqGenesIntervalTree, 
			TIntIntMap tfNumberCellLineNumber2KMap, 
			TShortIntMap exonBasedKeggPathwayNumber2KMap, 
			TShortIntMap regulationBasedKeggPathwayNumber2KMap, 
			TShortIntMap allBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberExonBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberRegulationBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberAllBasedKeggPathwayNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> tfNumber2TfNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap, 
			TShortObjectMap<String> keggPathwayNumber2KeggPathwayNameMap, 
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap, 
			TIntObjectMap<String> geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap) {

		String strLine = null;
		int indexofFirstTab = 0;
		int indexofSecondTab = 0;

		int low;
		int high;

		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		short tfNumber;
		short cellLineNumber;
		short keggPathwayNumber;

		int tfNumberCellLineNumber;
		int tfNumberKeggPathwayNumber;
		int tfNumberCellLineNumberKeggPathwayNumber;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {

				// TF CellLine
				TIntByteMap tfNumberCellLineNumber2ZeroorOneMap = new TIntByteHashMap();

				// KEGGPathway
				TShortByteMap exonBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();
				TShortByteMap regulationBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();
				TShortByteMap allBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();

				// TF KEGGPathway
				TIntByteMap tfExonBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();
				TIntByteMap tfRegulationBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();
				TIntByteMap tfAllBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();

				// Fill these lists during search for tfs and search for
				// ucscRefSeqGenes
				List<TfCellLineOverlapWithNumbers> tfandCellLineOverlapList = new ArrayList<TfCellLineOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> exonBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> regulationBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> allBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);

				low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0)
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				else
					high = low;

				Interval interval = new Interval(low, high);

				// TF Search starts here
				if (tfbsIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					tfbsIntervalTree.findAllOverlappingTfbsIntervalsWithNumbers(
							outputFolder, 
							writeElementBasedAnnotationFoundOverlapsMode,
							tfbsIntervalTree.getRoot(), 
							interval, 
							chromName, 
							tfNumberCellLineNumber2ZeroorOneMap, 
							tfandCellLineOverlapList, 
							overlapDefinition, 
							tfNumber2TfNameMap, 
							cellLineNumber2CellLineNameMap, 
							fileNumber2FileNameMap);
				}

			

				// accumulate search results of
				// tfbsNameandCellLineName2ZeroorOneMap in
				// tfbsNameandCellLineName2KMap
				for (TIntByteIterator it = tfNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!tfNumberCellLineNumber2KMap.containsKey(it.key())) {
						tfNumberCellLineNumber2KMap.put(it.key(), it.value());
					} else {
						tfNumberCellLineNumber2KMap.put(it.key(), tfNumberCellLineNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for
					// TF Search ends here

				// UCSCRefSeqGenes Search starts here
				if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					ucscRefSeqGenesIntervalTree.findAllOverlappingUcscRefSeqGenesIntervalsWithNumbers(
							outputFolder, 
							writeElementBasedAnnotationFoundOverlapsMode,
							ucscRefSeqGenesIntervalTree.getRoot(), 
							interval, 
							chromName, 
							geneId2ListofKeggPathwayNumberMap, 
							exonBasedKeggPathway2OneorZeroMap, 
							regulationBasedKeggPathway2OneorZeroMap, 
							allBasedKeggPathway2OneorZeroMap, 
							Commons.NCBI_GENE_ID, 
							exonBasedKeggPathwayOverlapList, 
							regulationBasedKeggPathwayOverlapList, 
							allBasedKeggPathwayOverlapList, 
							overlapDefinition, 
							keggPathwayNumber2KeggPathwayNameMap, 
							geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
							refSeqGeneNumber2RefSeqGeneNameMap);
				}
				// UCSCRefSeqGenes Search ends here


				// accumulate search results of
				// exonBasedKeggPathway2OneorZeroMap in
				// exonBasedKeggPathway2KMap
				for (TShortByteIterator it = exonBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					if (!exonBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						exonBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						exonBasedKeggPathwayNumber2KMap.put(it.key(), exonBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

				// accumulate search results of
				// regulationBasedKeggPathway2OneorZeroMap in
				// regulationBasedKeggPathway2KMap
				for (TShortByteIterator it = regulationBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!regulationBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						regulationBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						regulationBasedKeggPathwayNumber2KMap.put(it.key(), regulationBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

				// accumulate search results of allBasedKeggPathway2OneorZeroMap
				// in allBasedKeggPathway2KMap
				for (TShortByteIterator it = allBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!allBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						allBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						allBasedKeggPathwayNumber2KMap.put(it.key(), allBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for
					// code will be added here

				// New search for given input SNP or interval case, does not
				// matter.
				// starts here
				// for each tf overlap
				// for each ucscRefSeqGene overlap
				// if these overlaps overlaps
				// then write common overlap to output files
				// question will the overlapDefinition apply here?
				for (TfCellLineOverlapWithNumbers tfOverlap : tfandCellLineOverlapList) {

					tfNumberCellLineNumber = tfOverlap.getTfNumberCellLineNumber();
					tfNumber = IntervalTree.getShortElementNumber(tfNumberCellLineNumber,GeneratedMixedNumberDescriptionOrderLength.INT_4DIGITS_ELEMENTNUMBER_3DIGITS_CELLLINENUMBER_3DIGITS_KEGGPATHWAYNUMBER);
					cellLineNumber = IntervalTree.getCellLineNumber(tfNumberCellLineNumber);

					/****************************************************************************/
					/**************TF and Exon Based KEGGPathway starts**************************/
					/****************************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : exonBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {

							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();

								tfNumberCellLineNumberKeggPathwayNumber = tfNumberCellLineNumber + keggPathwayNumber;
								tfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumber(tfNumberCellLineNumberKeggPathwayNumber);
								
								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									fileWriter = FileOperations.createFileWriter(outputFolder + Commons.TF_EXON_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter = new BufferedWriter(fileWriter);
									
									if (!tfExonBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfExonBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}
									
									bufferedWriter.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter.close();
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfExonBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfExonBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/


							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/****************************************************************************/
					/**************TF and Exon Based KEGGPathway ends****************************/
					/****************************************************************************/

					/****************************************************************************/
					/**************TF and Regulation Based KEGGPathway starts********************/
					/****************************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : regulationBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {
							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();

								tfNumberCellLineNumberKeggPathwayNumber = tfOverlap.getTfNumberCellLineNumber() + keggPathwayNumber;
								tfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumber(tfNumberCellLineNumberKeggPathwayNumber);

								
								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									fileWriter = FileOperations.createFileWriter(outputFolder + Commons.TF_REGULATION_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter = new BufferedWriter(fileWriter);
									
									if (!tfRegulationBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfRegulationBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}

									bufferedWriter.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter.close();
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfRegulationBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfRegulationBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/
								
								

							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/****************************************************************************/
					/**************TF and Regulation Based KEGGPathway ends**********************/
					/****************************************************************************/

					/****************************************************************************/
					/**************TF and ALL Based KEGGPathway starts***************************/
					/****************************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : allBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {
							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();

								tfNumberCellLineNumberKeggPathwayNumber = tfOverlap.getTfNumberCellLineNumber() + keggPathwayNumber;
								tfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumber(tfNumberCellLineNumberKeggPathwayNumber);

								
								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									fileWriter = FileOperations.createFileWriter(outputFolder + Commons.TF_ALL_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter = new BufferedWriter(fileWriter);
								
									if (!tfAllBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfAllBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}

									bufferedWriter.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter.close();
								
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfAllBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfAllBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/
							
							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/****************************************************************************/
					/**************TF and ALL Based KEGGPathway ends*****************************/
					/****************************************************************************/

				}// for each tfOverlap for the given query
				// ends here


				// TF EXONBASED_KEGGPATHWAY
				// Fill tfExonBasedKeggPathway2KMap using
				// tfExonBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfExonBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					tfNumberKeggPathwayNumber = it.key();

					// new
					if (!tfNumberExonBasedKeggPathwayNumber2KMap.containsKey(tfNumberKeggPathwayNumber)) {
						tfNumberExonBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberExonBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, tfNumberExonBasedKeggPathwayNumber2KMap.get(tfNumberKeggPathwayNumber) + it.value());
					}
					// new
				}// End of for inner loop

				// TF REGULATIONBASED_KEGGPATHWAY
				// Fill tfRegulationBasedKeggPathway2KMap using
				// tfRegulationBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfRegulationBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();
					tfNumberKeggPathwayNumber = it.key();

					// new
					if (!tfNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(tfNumberKeggPathwayNumber)) {
						tfNumberRegulationBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberRegulationBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, tfNumberRegulationBasedKeggPathwayNumber2KMap.get(tfNumberKeggPathwayNumber) + it.value());
					}
					// new
				}// End of for inner loop

				// TF ALLBASED_KEGGPATHWAY
				// Fill tfAllBasedKeggPathway2KMap using
				// tfAllBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfAllBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();
					tfNumberKeggPathwayNumber = it.key();

					// new
					if (!tfNumberAllBasedKeggPathwayNumber2KMap.containsKey(tfNumberKeggPathwayNumber)) {
						tfNumberAllBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberAllBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, tfNumberAllBasedKeggPathwayNumber2KMap.get(tfNumberKeggPathwayNumber) + it.value());
					}
					// new

				}// End of for inner loop

				// added here ends

			}
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while
	}

	// @todo TF KEGGPathway Annotation with Numbers starts

	// Annotation
	// With Numbers
	// TF CELLLINE KEGGPATHWAY
	public void searchTfCellLineKEGGPathwayWithNumbers(
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree tfbsIntervalTree, 
			IntervalTree ucscRefSeqGenesIntervalTree, 
			TIntIntMap tfNumberCellLineNumber2KMap, 
			TShortIntMap exonBasedKeggPathwayNumber2KMap, 
			TShortIntMap regulationBasedKeggPathwayNumber2KMap, 
			TShortIntMap allBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> tfNumber2TfNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap, 
			TShortObjectMap<String> keggPathwayNumber2KeggPathwayNameMap, 
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap, 
			TIntObjectMap<String> geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap) {

		String strLine = null;
		int indexofFirstTab = 0;
		int indexofSecondTab = 0;

		int low;
		int high;

		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		short tfNumber;
		short cellLineNumber;
		short keggPathwayNumber;

		int tfNumberCellLineNumber;
		int tfNumberCellLineNumberKeggPathwayNumber;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {

				// TF CellLine
				TIntByteMap tfNumberCellLineNumber2ZeroorOneMap = new TIntByteHashMap();

				// KEGGPathway
				TShortByteMap exonBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();
				TShortByteMap regulationBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();
				TShortByteMap allBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();

				// TF CellLine KEGGPathway
				TIntByteMap tfCellLineExonBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();
				TIntByteMap tfCellLineRegulationBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();
				TIntByteMap tfCellLineAllBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();

				// Fill these lists during search for tfs and search for
				// ucscRefSeqGenes
				List<TfCellLineOverlapWithNumbers> tfandCellLineOverlapList = new ArrayList<TfCellLineOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> exonBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> regulationBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> allBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);

				low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0)
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				else
					high = low;

				Interval interval = new Interval(low, high);

				// TF Search starts here
				if (tfbsIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					
					tfbsIntervalTree.findAllOverlappingTfbsIntervalsWithNumbers(
							outputFolder, 
							writeElementBasedAnnotationFoundOverlapsMode,
							tfbsIntervalTree.getRoot(), 
							interval, 
							chromName, 
							tfNumberCellLineNumber2ZeroorOneMap, 
							tfandCellLineOverlapList, 
							overlapDefinition, 
							tfNumber2TfNameMap, 
							cellLineNumber2CellLineNameMap, 
							fileNumber2FileNameMap);
				}

			

				// accumulate search results of
				// tfbsNameandCellLineName2ZeroorOneMap in
				// tfbsNameandCellLineName2KMap
				for (TIntByteIterator it = tfNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!tfNumberCellLineNumber2KMap.containsKey(it.key())) {
						tfNumberCellLineNumber2KMap.put(it.key(), it.value());
					} else {
						tfNumberCellLineNumber2KMap.put(it.key(), tfNumberCellLineNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for
					// TF Search ends here

				// UCSCRefSeqGenes Search starts here
				if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					ucscRefSeqGenesIntervalTree.findAllOverlappingUcscRefSeqGenesIntervalsWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode,ucscRefSeqGenesIntervalTree.getRoot(), interval, chromName, geneId2ListofKeggPathwayNumberMap, exonBasedKeggPathway2OneorZeroMap, regulationBasedKeggPathway2OneorZeroMap, allBasedKeggPathway2OneorZeroMap, Commons.NCBI_GENE_ID, exonBasedKeggPathwayOverlapList, regulationBasedKeggPathwayOverlapList, allBasedKeggPathwayOverlapList, overlapDefinition, keggPathwayNumber2KeggPathwayNameMap, geneHugoSymbolNumber2GeneHugoSymbolNameMap, refSeqGeneNumber2RefSeqGeneNameMap);
				}
				// UCSCRefSeqGenes Search ends here

			

				// accumulate search results of
				// exonBasedKeggPathway2OneorZeroMap in
				// exonBasedKeggPathway2KMap
				for (TShortByteIterator it = exonBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					if (!exonBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						exonBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						exonBasedKeggPathwayNumber2KMap.put(it.key(), exonBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

				// accumulate search results of
				// regulationBasedKeggPathway2OneorZeroMap in
				// regulationBasedKeggPathway2KMap
				for (TShortByteIterator it = regulationBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!regulationBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						regulationBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						regulationBasedKeggPathwayNumber2KMap.put(it.key(), regulationBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

				// accumulate search results of allBasedKeggPathway2OneorZeroMap
				// in allBasedKeggPathway2KMap
				for (TShortByteIterator it = allBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!allBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						allBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						allBasedKeggPathwayNumber2KMap.put(it.key(), allBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for
					// code will be added here

				// New search for given input SNP or interval case, does not
				// matter.
				// starts here
				// for each tf overlap
				// for each ucscRefSeqGene overlap
				// if these overlaps overlaps
				// then write common overlap to output files
				// question will the overlapDefinition apply here?
				for (TfCellLineOverlapWithNumbers tfOverlap : tfandCellLineOverlapList) {

					tfNumberCellLineNumber = tfOverlap.getTfNumberCellLineNumber();
					tfNumber = IntervalTree.getShortElementNumber(tfNumberCellLineNumber,GeneratedMixedNumberDescriptionOrderLength.INT_4DIGITS_ELEMENTNUMBER_3DIGITS_CELLLINENUMBER_3DIGITS_KEGGPATHWAYNUMBER);
					cellLineNumber = IntervalTree.getCellLineNumber(tfNumberCellLineNumber);

					/***********************************************************************/
					/***********TF CellLine ExonBasedKEGGPathway starts*********************/
					/***********************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : exonBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {

							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();
								tfNumberCellLineNumberKeggPathwayNumber = tfNumberCellLineNumber + keggPathwayNumber;
								
								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									
									fileWriter = FileOperations.createFileWriter(outputFolder + Commons.TF_CELLLINE_EXON_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter = new BufferedWriter(fileWriter);
									
									if (!tfCellLineExonBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineExonBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}

									bufferedWriter.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter.close();	
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfCellLineExonBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineExonBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/
							
							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/***********************************************************************/
					/***********TF CellLine ExonBasedKEGGPathway ends***********************/
					/***********************************************************************/

					/***********************************************************************/
					/***********TF CellLine RegulationBasedKEGGPathway starts***************/
					/***********************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : regulationBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {
							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();
								tfNumberCellLineNumberKeggPathwayNumber = tfOverlap.getTfNumberCellLineNumber() + keggPathwayNumber;
								
								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									
									fileWriter = FileOperations.createFileWriter(outputFolder + Commons.TF_CELLLINE_REGULATION_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter = new BufferedWriter(fileWriter);
									
									if (!tfCellLineRegulationBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineRegulationBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}

									bufferedWriter.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter.close();	
									
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfCellLineRegulationBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineRegulationBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/

								
							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/***********************************************************************/
					/***********TF CellLine RegulationBasedKEGGPathway ends*****************/
					/***********************************************************************/

					
					
					/***********************************************************************/
					/***********TF CellLine AllBasedKEGGPathway starts**********************/
					/***********************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : allBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {
							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();
								tfNumberCellLineNumberKeggPathwayNumber = tfOverlap.getTfNumberCellLineNumber() + keggPathwayNumber;

								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									fileWriter = FileOperations.createFileWriter(outputFolder + Commons.TF_CELLLINE_ALL_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter = new BufferedWriter(fileWriter);
									
									if (!tfCellLineAllBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineAllBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));	
									}

									bufferedWriter.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter.close();
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfCellLineAllBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineAllBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/
							
							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/***********************************************************************/
					/***********TF CellLine AllBasedKEGGPathway ends************************/
					/***********************************************************************/

				}// for each tfOverlap for the given query
				// ends here


				// TF CELLLINE EXONBASED_KEGGPATHWAY
				// Fill tfbsAndCellLineAndExonBasedKeggPathway2KMap using
				// tfandExonBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfCellLineExonBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					tfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
						tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.get(tfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE REGULATIONBASED_KEGGPATHWAY
				// Fill tfbsAndCellLineAndRegulationBasedKeggPathway2KMap using
				// tfandRegulationBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfCellLineRegulationBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					tfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
						tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.get(tfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE ALLBASED_KEGGPATHWAY
				// Fill tfbsAndCellLineAndAllBasedKeggPathway2KMap using
				// tfandAllBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfCellLineAllBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					tfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
						tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.get(tfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

			}
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while
	}

	// @todo TF CELLLINE KEGGPATHWAY Annotation with Numbers ends

	// Annotation
	// With Numbers
	// TF and KEGGPathway
	// TF and CellLine and KEGGPathway
	public void searchTfandKeggPathwayWithNumbers(
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree tfbsIntervalTree, 
			IntervalTree ucscRefSeqGenesIntervalTree, 
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap, 
			TIntIntMap tfNumberCellLineNumber2KMap, 
			TShortIntMap exonBasedKeggPathwayNumber2KMap, 
			TShortIntMap regulationBasedKeggPathwayNumber2KMap, 
			TShortIntMap allBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberExonBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberRegulationBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberAllBasedKeggPathwayNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> tfNumber2TfNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap, 
			TShortObjectMap<String> keggPathwayNumber2KeggPathwayNameMap, 
			TIntObjectMap<String> geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap) {

		String strLine = null;
		int indexofFirstTab = 0;
		int indexofSecondTab = 0;

		int low;
		int high;

		FileWriter fileWriter1 = null;
		FileWriter fileWriter2 = null;
		BufferedWriter bufferedWriter1 = null;
		BufferedWriter bufferedWriter2 = null;

		short tfNumber;
		short cellLineNumber;
		short keggPathwayNumber;

		int tfNumberCellLineNumber;
		int tfNumberKeggPathwayNumber;
		int tfNumberCellLineNumberKeggPathwayNumber;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {

				// TF CellLine
				TIntByteMap tfNumberCellLineNumber2ZeroorOneMap = new TIntByteHashMap();

				// KEGGPathway
				TShortByteMap exonBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();
				TShortByteMap regulationBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();
				TShortByteMap allBasedKeggPathway2OneorZeroMap = new TShortByteHashMap();

				// TF KEGGPathway
				TIntByteMap tfExonBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();
				TIntByteMap tfRegulationBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();
				TIntByteMap tfAllBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();

				// TF CellLine KEGGPathway
				TIntByteMap tfCellLineExonBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();
				TIntByteMap tfCellLineRegulationBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();
				TIntByteMap tfCellLineAllBasedKeggPathway2OneorZeroMap = new TIntByteHashMap();

				// Fill these lists during search for tfs and search for
				// ucscRefSeqGenes
				List<TfCellLineOverlapWithNumbers> tfandCellLineOverlapList = new ArrayList<TfCellLineOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> exonBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> regulationBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();
				List<UcscRefSeqGeneOverlapWithNumbers> allBasedKeggPathwayOverlapList = new ArrayList<UcscRefSeqGeneOverlapWithNumbers>();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);

				low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0)
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				else
					high = low;

				Interval interval = new Interval(low, high);

				// TF Search starts here
				if (tfbsIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					
					tfbsIntervalTree.findAllOverlappingTfbsIntervalsWithNumbers(
							outputFolder, 
							writeElementBasedAnnotationFoundOverlapsMode,
							tfbsIntervalTree.getRoot(), 
							interval, 
							chromName, 
							tfNumberCellLineNumber2ZeroorOneMap, 
							tfandCellLineOverlapList, 
							overlapDefinition, 
							tfNumber2TfNameMap, 
							cellLineNumber2CellLineNameMap, 
							fileNumber2FileNameMap);
				}

				

				// accumulate search results of
				// tfbsNameandCellLineName2ZeroorOneMap in
				// tfbsNameandCellLineName2KMap
				for (TIntByteIterator it = tfNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!tfNumberCellLineNumber2KMap.containsKey(it.key())) {
						tfNumberCellLineNumber2KMap.put(it.key(), it.value());
					} else {
						tfNumberCellLineNumber2KMap.put(it.key(), tfNumberCellLineNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for
					// TF Search ends here

				// UCSCRefSeqGenes Search starts here
				if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					ucscRefSeqGenesIntervalTree.findAllOverlappingUcscRefSeqGenesIntervalsWithNumbers(
							outputFolder,
							writeElementBasedAnnotationFoundOverlapsMode, 
							ucscRefSeqGenesIntervalTree.getRoot(), 
							interval, 
							chromName, 
							geneId2ListofKeggPathwayNumberMap, 
							exonBasedKeggPathway2OneorZeroMap, 
							regulationBasedKeggPathway2OneorZeroMap, 
							allBasedKeggPathway2OneorZeroMap, 
							Commons.NCBI_GENE_ID, 
							exonBasedKeggPathwayOverlapList, 
							regulationBasedKeggPathwayOverlapList, 
							allBasedKeggPathwayOverlapList, 
							overlapDefinition, 
							keggPathwayNumber2KeggPathwayNameMap, 
							geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
							refSeqGeneNumber2RefSeqGeneNameMap);
				}
				// UCSCRefSeqGenes Search ends here

				

				// accumulate search results of
				// exonBasedKeggPathway2OneorZeroMap in
				// exonBasedKeggPathway2KMap
				for (TShortByteIterator it = exonBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					if (!exonBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						exonBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						exonBasedKeggPathwayNumber2KMap.put(it.key(), exonBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

				// accumulate search results of
				// regulationBasedKeggPathway2OneorZeroMap in
				// regulationBasedKeggPathway2KMap
				for (TShortByteIterator it = regulationBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!regulationBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						regulationBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						regulationBasedKeggPathwayNumber2KMap.put(it.key(), regulationBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

				// accumulate search results of allBasedKeggPathway2OneorZeroMap
				// in allBasedKeggPathway2KMap
				for (TShortByteIterator it = allBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!allBasedKeggPathwayNumber2KMap.containsKey(it.key())) {
						allBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						allBasedKeggPathwayNumber2KMap.put(it.key(), allBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for
					// code will be added here

				// New search for given input SNP or interval case, does not
				// matter.
				// starts here
				// for each tf overlap
				// for each ucscRefSeqGene overlap
				// if these overlaps overlaps
				// then write common overlap to output files
				// question will the overlapDefinition apply here?
				for (TfCellLineOverlapWithNumbers tfOverlap : tfandCellLineOverlapList) {

					tfNumberCellLineNumber = tfOverlap.getTfNumberCellLineNumber();
					tfNumber = IntervalTree.getShortElementNumber(tfNumberCellLineNumber,GeneratedMixedNumberDescriptionOrderLength.INT_4DIGITS_ELEMENTNUMBER_3DIGITS_CELLLINENUMBER_3DIGITS_KEGGPATHWAYNUMBER);
					cellLineNumber = IntervalTree.getCellLineNumber(tfNumberCellLineNumber);

					/**************************************************************************/
					/*********************************BOTH*************************************/
					/****************TF ExonBased KEGG Pathway starts**************************/
					/****************TF CellLine ExonBased KEGG Pathway starts*****************/
					/**************************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : exonBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {

							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();
								tfNumberCellLineNumberKeggPathwayNumber = tfNumberCellLineNumber + keggPathwayNumber;
								tfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumber(tfNumberCellLineNumberKeggPathwayNumber);

								
								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									
									/*************************************************************/
									fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.TF_CELLLINE_EXON_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter1 = new BufferedWriter(fileWriter1);
									
									if (!tfCellLineExonBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineExonBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter1.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}
									
									bufferedWriter1.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter1.close();;
									/*************************************************************/
									
									/*************************************************************/
									fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.TF_EXON_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter2 = new BufferedWriter(fileWriter2);
									
									if (!tfExonBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfExonBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter2.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}

									bufferedWriter2.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter2.close();
									/*************************************************************/
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfCellLineExonBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineExonBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
									}
									if (!tfExonBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfExonBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/
								

							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/**************************************************************************/
					/*********************************BOTH*************************************/
					/****************TF ExonBased KEGG Pathway ends****************************/
					/****************TF CellLine ExonBased KEGG Pathway ends*******************/
					/**************************************************************************/

					/**************************************************************************/
					/*********************************BOTH*************************************/
					/****************TF RegulationBased KEGG Pathway starts********************/
					/****************TF CellLine RegulationBased KEGG Pathway starts***********/
					/**************************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : regulationBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {
							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();
								tfNumberCellLineNumberKeggPathwayNumber = tfOverlap.getTfNumberCellLineNumber() + keggPathwayNumber;
								tfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumber(tfNumberCellLineNumberKeggPathwayNumber);
								
								
								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									
									/***********************************************************/
									fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.TF_CELLLINE_REGULATION_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter1 = new BufferedWriter(fileWriter1);
									
									if (!tfCellLineRegulationBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineRegulationBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter1.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
										
									}
									
									bufferedWriter1.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter1.close();
									/***********************************************************/
									
									/***********************************************************/
									fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.TF_REGULATION_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter2 = new BufferedWriter(fileWriter2);
									
									if (!tfRegulationBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfRegulationBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter2.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}
									
									bufferedWriter2.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter2.close();
									/***********************************************************/
									
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfCellLineRegulationBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineRegulationBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
									}
									if (!tfRegulationBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfRegulationBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/

								
								
								
								
							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/**************************************************************************/
					/*********************************BOTH*************************************/
					/****************TF RegulationBased KEGG Pathway ends**********************/
					/****************TF CellLine RegulationBased KEGG Pathway ends*************/
					/**************************************************************************/

					
					/**************************************************************************/
					/*********************************BOTH*************************************/
					/****************TF ALLBased KEGG Pathway starts***************************/
					/****************TF CellLine ALLBased KEGG Pathway starts******************/
					/**************************************************************************/
					for (UcscRefSeqGeneOverlapWithNumbers ucscRefSeqGeneOverlapWithNumbers : allBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(tfOverlap.getLow(), tfOverlap.getHigh(), ucscRefSeqGeneOverlapWithNumbers.getLow(), ucscRefSeqGeneOverlapWithNumbers.getHigh())) {
							for (TShortIterator it = ucscRefSeqGeneOverlapWithNumbers.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();
								tfNumberCellLineNumberKeggPathwayNumber = tfOverlap.getTfNumberCellLineNumber() + keggPathwayNumber;
								tfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumber(tfNumberCellLineNumberKeggPathwayNumber);
								
								
								/*******************************************************************/
								//Write Annotation Found Overlaps to element Named File
								if(writeElementBasedAnnotationFoundOverlapsMode.isWriteElementBasedAnnotationFoundOverlaps()){
									
									/*****************************************************************/
									fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.TF_CELLLINE_ALL_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter1 = new BufferedWriter(fileWriter1);
									
									if (!tfCellLineAllBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineAllBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter1.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}
									
									bufferedWriter1.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter1.close();
									/*****************************************************************/
									
									/*****************************************************************/
									fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.TF_ALL_BASED_KEGG_PATHWAY_ANNOTATION + tfNumber2TfNameMap.get(tfNumber) + "_" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + ".txt", true);
									bufferedWriter2 = new BufferedWriter(fileWriter2);
									
									if (!tfAllBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfAllBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
										bufferedWriter2.write(Commons.GLANET_COMMENT_CHARACTER + "Search for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));
									}
									
									bufferedWriter2.write(ChromosomeName.convertEnumtoString(chromName) + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + tfNumber2TfNameMap.get(tfNumber) + "_" + cellLineNumber2CellLineNameMap.get(cellLineNumber) + "\t" + tfOverlap.getLow() + "\t" + tfOverlap.getHigh() + "\t" + refSeqGeneNumber2RefSeqGeneNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getRefSeqGeneNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getLow() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getHigh() + "\t" + ucscRefSeqGeneOverlapWithNumbers.getIntervalName() + "\t" + geneHugoSymbolNumber2GeneHugoSymbolNameMap.get(ucscRefSeqGeneOverlapWithNumbers.getGeneHugoSymbolNumber()) + "\t" + ucscRefSeqGeneOverlapWithNumbers.getGeneEntrezId() + "\t" + keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber) + System.getProperty("line.separator"));
									bufferedWriter2.close();
									/*****************************************************************/
									
								}
								/*******************************************************************/
								
								
								/*******************************************************************/
								//Do not Write Annotation Found Overlaps to element Named File
								else {
									if (!tfCellLineAllBasedKeggPathway2OneorZeroMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
										tfCellLineAllBasedKeggPathway2OneorZeroMap.put(tfNumberCellLineNumberKeggPathwayNumber, Commons.BYTE_1);
									}
									if (!tfAllBasedKeggPathway2OneorZeroMap.containsKey(tfNumberKeggPathwayNumber)) {
										tfAllBasedKeggPathway2OneorZeroMap.put(tfNumberKeggPathwayNumber, Commons.BYTE_1);
									}
								}
								/*******************************************************************/


								
								
							
							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query
					/**************************************************************************/
					/*********************************BOTH*************************************/
					/****************TF ALLBased KEGG Pathway ends*****************************/
					/****************TF CellLine ALLBased KEGG Pathway ends********************/
					/**************************************************************************/

				}// for each tfOverlap for the given query
					// ends here

		

				// TF CELLLINE EXONBASED_KEGGPATHWAY
				// Fill tfbsAndCellLineAndExonBasedKeggPathway2KMap using
				// tfandExonBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfCellLineExonBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					tfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
						tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.get(tfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE REGULATIONBASED_KEGGPATHWAY
				// Fill tfbsAndCellLineAndRegulationBasedKeggPathway2KMap using
				// tfandRegulationBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfCellLineRegulationBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					tfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
						tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.get(tfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE ALLBASED_KEGGPATHWAY
				// Fill tfbsAndCellLineAndAllBasedKeggPathway2KMap using
				// tfandAllBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfCellLineAllBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					tfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.containsKey(tfNumberCellLineNumberKeggPathwayNumber)) {
						tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.put(tfNumberCellLineNumberKeggPathwayNumber, tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.get(tfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF EXONBASED_KEGGPATHWAY
				// Fill tfExonBasedKeggPathway2KMap using
				// tfExonBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfExonBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					tfNumberKeggPathwayNumber = it.key();

					// new
					if (!tfNumberExonBasedKeggPathwayNumber2KMap.containsKey(tfNumberKeggPathwayNumber)) {
						tfNumberExonBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberExonBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, tfNumberExonBasedKeggPathwayNumber2KMap.get(tfNumberKeggPathwayNumber) + it.value());
					}
					// new
				}// End of for inner loop

				// TF REGULATIONBASED_KEGGPATHWAY
				// Fill tfRegulationBasedKeggPathway2KMap using
				// tfRegulationBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfRegulationBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();
					tfNumberKeggPathwayNumber = it.key();

					// new
					if (!tfNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(tfNumberKeggPathwayNumber)) {
						tfNumberRegulationBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberRegulationBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, tfNumberRegulationBasedKeggPathwayNumber2KMap.get(tfNumberKeggPathwayNumber) + it.value());
					}
					// new
				}// End of for inner loop

				// TF ALLBASED_KEGGPATHWAY
				// Fill tfAllBasedKeggPathway2KMap using
				// tfAllBasedKeggPathway2OneorZeroMap
				for (TIntByteIterator it = tfAllBasedKeggPathway2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();
					tfNumberKeggPathwayNumber = it.key();

					// new
					if (!tfNumberAllBasedKeggPathwayNumber2KMap.containsKey(tfNumberKeggPathwayNumber)) {
						tfNumberAllBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, it.value());
					} else {
						tfNumberAllBasedKeggPathwayNumber2KMap.put(tfNumberKeggPathwayNumber, tfNumberAllBasedKeggPathwayNumber2KMap.get(tfNumberKeggPathwayNumber) + it.value());
					}
					// new

				}// End of for inner loop

				// added here ends

			}
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while
	}

	// @todo Annotation with Numbers ends

	// Enrichment
	// With IO
	// With Numbers
	// Empirical P Value Calculation
	// with IO
	public static void searchHistoneWithIOWithNumbers(String outputFolder, int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree histoneIntervalTree, TLongObjectMap<BufferedWriter> histoneBufferedWriterHashMap, TLongIntMap permutationNumberHistoneNumberCellLineNumber2KMap, int overlapDefinition) {
		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {

			TLongIntMap permutationNumberHistoneNumberCellLineNumber2ZeroorOneMap = new TLongIntHashMap();

			inputLine = inputLines.get(i);

			low = inputLine.getLow();
			high = inputLine.getHigh();

			Interval interval = new Interval(low, high);

			if (histoneIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				histoneIntervalTree.findAllOverlappingHistoneIntervalsWithIOWithNumbers(outputFolder, permutationNumber, histoneIntervalTree.getRoot(), interval, chromName, histoneBufferedWriterHashMap, permutationNumberHistoneNumberCellLineNumber2ZeroorOneMap, overlapDefinition);
			}

			// accumulate search results of dnaseCellLine2OneorZeroMap in
			// dnaseCellLine2KMap
			for (TLongIntIterator it = permutationNumberHistoneNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {

				it.advance();

				if (!(permutationNumberHistoneNumberCellLineNumber2KMap.containsKey(it.key()))) {
					permutationNumberHistoneNumberCellLineNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberHistoneNumberCellLineNumber2KMap.put(it.key(), permutationNumberHistoneNumberCellLineNumber2KMap.get(it.key()) + it.value());

				}

			}// End of for

		}// End of for

	}

	// with Numbers ends
	// @todo

	// 4 NOV 2014
	// Enrichment
	// Without IO
	// With Numbers
	public static void searchUserDefinedLibraryWithoutIOWithNumbers(int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree userDefinedLibraryIntervalTree, TLongIntMap permutationNumberElementTypeNumberElementNumber2KMap, int overlapDefinition) {

		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {

			TLongIntMap permutationNumberElementTypeNumberElementNumber2ZeroorOneMap = new TLongIntHashMap();

			inputLine = inputLines.get(i);

			low = inputLine.getLow();
			high = inputLine.getHigh();

			Interval interval = new Interval(low, high);

			if (userDefinedLibraryIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				userDefinedLibraryIntervalTree.findAllOverlappingUserDefinedLibraryIntervalsWithoutIOWithNumbers(permutationNumber, userDefinedLibraryIntervalTree.getRoot(), interval, chromName, permutationNumberElementTypeNumberElementNumber2ZeroorOneMap, overlapDefinition);
			}

			// accumulate search results of dnaseCellLine2OneorZeroMap in
			// dnaseCellLine2KMap
			for (TLongIntIterator it = permutationNumberElementTypeNumberElementNumber2ZeroorOneMap.iterator(); it.hasNext();) {

				it.advance();

				if (!(permutationNumberElementTypeNumberElementNumber2KMap.containsKey(it.key()))) {
					permutationNumberElementTypeNumberElementNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberElementTypeNumberElementNumber2KMap.put(it.key(), permutationNumberElementTypeNumberElementNumber2KMap.get(it.key()) + it.value());

				}

			}// End of for

		}// End of for each inputLine

	}

	// Enrichment
	// Without IO
	// With Numbers
	// Empirical P Value Calculation
	public static void searchHistoneWithoutIOWithNumbers(int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree histoneIntervalTree, TLongIntMap permutationNumberHistoneNumberCellLineNumber2KMap, int overlapDefinition) {
		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {

			TLongIntMap permutationNumberHistoneNumberCellLineNumber2ZeroorOneMap = new TLongIntHashMap();

			inputLine = inputLines.get(i);

			low = inputLine.getLow();
			high = inputLine.getHigh();

			Interval interval = new Interval(low, high);

			if (histoneIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				histoneIntervalTree.findAllOverlappingHistoneIntervalsWithoutIOWithNumbers(permutationNumber, histoneIntervalTree.getRoot(), interval, chromName, permutationNumberHistoneNumberCellLineNumber2ZeroorOneMap, overlapDefinition);
			}

			// accumulate search results of dnaseCellLine2OneorZeroMap in
			// dnaseCellLine2KMap
			for (TLongIntIterator it = permutationNumberHistoneNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {

				it.advance();

				if (!(permutationNumberHistoneNumberCellLineNumber2KMap.containsKey(it.key()))) {
					permutationNumberHistoneNumberCellLineNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberHistoneNumberCellLineNumber2KMap.put(it.key(), permutationNumberHistoneNumberCellLineNumber2KMap.get(it.key()) + it.value());

				}

			}// End of for

		}// End of for

	}

	// @todo

	// starts
	// Annotation
	// With Numbers
	public void searchUserDefinedLibraryWithNumbers(
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree userDefinedLibraryIntervalTree, 
			TIntIntMap elementNumber2KMap, 
			int overlapDefinition, 
			String elementType, 
			TIntObjectMap<String> elementNumber2ElementNameMap, 
			TIntObjectMap<String> fileNumber2FileNameMap) {

		String strLine = null;
		int indexofFirstTab = -1;
		int indexofSecondTab = -1;

		int low;
		int high;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {

				TIntByteMap elementNumber2ZeroorOneMap = new TIntByteHashMap();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = (indexofFirstTab > 0) ? strLine.indexOf('\t', indexofFirstTab + 1) : -1;

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0) {
					low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				} else {
					low = Integer.parseInt(strLine.substring(indexofFirstTab + 1));
					high = low;

				}

				Interval interval = new Interval(low, high);

				if (userDefinedLibraryIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					userDefinedLibraryIntervalTree.findAllOverlappingUserDefinedLibraryIntervalsWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode, userDefinedLibraryIntervalTree.getRoot(), interval, chromName, elementNumber2ZeroorOneMap, overlapDefinition, elementType, elementNumber2ElementNameMap, fileNumber2FileNameMap);
				}


				// accumulate search results of dnaseCellLine2OneorZeroMap in
				// dnaseCellLine2KMap
				for (TIntByteIterator it = elementNumber2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!elementNumber2KMap.containsKey(it.key())) {
						elementNumber2KMap.put(it.key(), it.value());
					} else {
						elementNumber2KMap.put(it.key(), elementNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for

			}
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while

	}

	// ends

	// Annotation
	// With Numbers
	public void searchTranscriptionFactorWithNumbers(
			String outputFolder,
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree tfIntervalTree, 
			TIntIntMap tfNumberCellLineNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> tfNumber2TFNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap) {
		
		String strLine = null;
		int indexofFirstTab = 0;
		int indexofSecondTab = 0;

		int low;
		int high;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {

				TIntByteMap tfNumberCellLineNumber2ZeroorOneMap = new TIntByteHashMap();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);

				low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0)
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				else
					high = low;

				Interval interval = new Interval(low, high);

				if (tfIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					tfIntervalTree.findAllOverlappingTfbsIntervalsWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode, tfIntervalTree.getRoot(), interval, chromName, tfNumberCellLineNumber2ZeroorOneMap, overlapDefinition, tfNumber2TFNameMap, cellLineNumber2CellLineNameMap, fileNumber2FileNameMap);
				}


				// accumulate search results of dnaseCellLine2OneorZeroMap in
				// dnaseCellLine2KMap
				for (TIntByteIterator it = tfNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!tfNumberCellLineNumber2KMap.containsKey(it.key())) {
						tfNumberCellLineNumber2KMap.put(it.key(), it.value());
					} else {
						tfNumberCellLineNumber2KMap.put(it.key(), tfNumberCellLineNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for

			}
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while
	}

	// @todo searchTranscriptionFactorWithNumbers ends

	// Annotation
	// With Numbers
	public void searchHistoneWithNumbers(
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ChromosomeName chromName, 
			BufferedReader bufferedReader, 
			IntervalTree histoneIntervalTree, 
			TIntIntMap histoneNumberCellLineNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> histoneNumber2HistoneNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap) {
		
		
		String strLine = null;
		int indexofFirstTab = 0;
		int indexofSecondTab = 0;

		int low;
		int high;

		try {
			while ((strLine = bufferedReader.readLine()) != null) {

				TIntByteMap histoneNumberCellLineNumber2ZeroorOneMap = new TIntByteHashMap();

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t', indexofFirstTab + 1);

				low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));

				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0)
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				else
					high = low;

				Interval interval = new Interval(low, high);

				if (histoneIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					histoneIntervalTree.findAllOverlappingHistoneIntervalsWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode, histoneIntervalTree.getRoot(), interval, chromName, histoneNumberCellLineNumber2ZeroorOneMap, overlapDefinition, histoneNumber2HistoneNameMap, cellLineNumber2CellLineNameMap, fileNumber2FileNameMap);
				}


				// accumulate search results of dnaseCellLine2OneorZeroMap in
				// dnaseCellLine2KMap
				for (TIntByteIterator it = histoneNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!histoneNumberCellLineNumber2KMap.containsKey(it.key())) {
						histoneNumberCellLineNumber2KMap.put(it.key(), it.value());
					} else {
						histoneNumberCellLineNumber2KMap.put(it.key(), histoneNumberCellLineNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

			}
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while
	}

	// @todo searchHistoneWithNumbers ends

	// 23 OCT 2014
	// Enrichment
	// With IO
	// With Numbers
	// Empirical P Value Calculation
	// Search GeneSet
	// KEGGPathway or UserDefinedGeneSet
	public static void searchUcscRefSeqGenesWithIOWithNumbers(String outputFolder, int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree ucscRefSeqGenesIntervalTree, TIntObjectMap<BufferedWriter> permutationNumberKeggPathwayNumber2BufferedWriterMap, TLongObjectMap<BufferedWriter> permutationNumberUserDefinedGeneSetNumber2BufferedWriterMap, TIntObjectMap<TShortList> geneId2ListofGeneSetNumberMap, TIntIntMap permutationNumberKeggPathwayNumber2KMap, TLongIntMap permutationNumberUserDefinedGeneSetNumber2KMap, String type, GeneSetAnalysisType geneSetAnalysisType, GeneSetType geneSetType, int overlapDefinition) {

		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {

			TIntIntMap permutationNumberKeggPathwayNumber2OneorZeroMap = null;
			TLongIntMap permutationNumberUserDefinedGeneSetNumber2OneorZeroMap = null;

			if (geneSetType.isKeggPathway()) {
				permutationNumberKeggPathwayNumber2OneorZeroMap = new TIntIntHashMap();
			} else if (geneSetType.isUserDefinedGeneSet()) {
				permutationNumberUserDefinedGeneSetNumber2OneorZeroMap = new TLongIntHashMap();
			}

			inputLine = inputLines.get(i);

			low = inputLine.getLow();
			high = inputLine.getHigh();
			Interval interval = new Interval(low, high);

			if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				ucscRefSeqGenesIntervalTree.findAllOverlappingUcscRefSeqGenesIntervalsWithIOWithNumbers(outputFolder, permutationNumber, ucscRefSeqGenesIntervalTree.getRoot(), interval, chromName, permutationNumberKeggPathwayNumber2BufferedWriterMap, permutationNumberUserDefinedGeneSetNumber2BufferedWriterMap, geneId2ListofGeneSetNumberMap, permutationNumberKeggPathwayNumber2OneorZeroMap, permutationNumberUserDefinedGeneSetNumber2OneorZeroMap, type, geneSetAnalysisType, geneSetType, overlapDefinition);
			}

			if (geneSetType.isKeggPathway()) {

				// accumulate search results of
				// permutationNumberKeggPathwayNumber2OneorZeroMap
				// in permutationNumberKeggPathwayNumber2KMap
				for (TIntIntIterator it = permutationNumberKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					if (!(permutationNumberKeggPathwayNumber2KMap.containsKey(it.key()))) {
						permutationNumberKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						permutationNumberKeggPathwayNumber2KMap.put(it.key(), permutationNumberKeggPathwayNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for

			} else if (geneSetType.isUserDefinedGeneSet()) {

				// accumulate search results of
				// permutationNumberUserDefinedGeneSetNumber2OneorZeroMap
				// in permutationNumberUserDefinedGeneSetNumber2KMap
				for (TLongIntIterator it = permutationNumberUserDefinedGeneSetNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					if (!(permutationNumberUserDefinedGeneSetNumber2KMap.containsKey(it.key()))) {
						permutationNumberUserDefinedGeneSetNumber2KMap.put(it.key(), it.value());
					} else {
						permutationNumberUserDefinedGeneSetNumber2KMap.put(it.key(), permutationNumberUserDefinedGeneSetNumber2KMap.get(it.key()) + it.value());
					}

				}// End of for
			}

		}// End of for each inputline

	}

	// Enrichment
	// With IO
	// With Numbers

	
	
	// 23 OCT 2014
	// Enrichment
	// Without IO
	// With Numbers
	// Empirical P Value Calculation
	// GeneSetType added 14.10.2014
	// Search GeneSet
	public static void searchUcscRefSeqGenesWithoutIOWithNumbers(
			int permutationNumber, 
			ChromosomeName chromName, 
			List<InputLine> inputLines, 
			IntervalTree ucscRefSeqGenesIntervalTree, 
			TIntObjectMap<TShortList> geneId2ListofGeneSetNumberMap, 
			TIntIntMap permutationNumberKeggPathwayNumber2KMap, 
			TLongIntMap permutationNumberUserDefinedGeneSetNumber2KMap, 
			TLongIntMap permutationNumberGeneNumber2KMap,
			String type, 
			GeneSetAnalysisType geneSetAnalysisType, 
			GeneSetType geneSetType, 
			int overlapDefinition) {

		InputLine inputLine;
		int low;
		int high;

		for (int i = 0; i < inputLines.size(); i++) {

			TIntIntMap permutationNumberKeggPathwayNumber2OneorZeroMap = null;
			TLongIntMap permutationNumberUserDefinedGeneSetNumber2OneorZeroMap = null;
			TLongIntMap permutationNumberGeneNumber2OneorZeroMap = null;
			

			if (geneSetType.isKeggPathway()) {
				permutationNumberKeggPathwayNumber2OneorZeroMap = new TIntIntHashMap();
			} else if (geneSetType.isUserDefinedGeneSet()) {
				permutationNumberUserDefinedGeneSetNumber2OneorZeroMap = new TLongIntHashMap();
			} else if (geneSetType.isNoGeneSetTypeDefined()){
				permutationNumberGeneNumber2OneorZeroMap = new TLongIntHashMap();
			}

			inputLine = inputLines.get(i);

			low = inputLine.getLow();
			high = inputLine.getHigh();
			Interval interval = new Interval(low, high);

			if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				ucscRefSeqGenesIntervalTree.findAllOverlappingUcscRefSeqGenesIntervalsWithoutIOWithNumbers(permutationNumber, ucscRefSeqGenesIntervalTree.getRoot(), interval, chromName, geneId2ListofGeneSetNumberMap, permutationNumberKeggPathwayNumber2OneorZeroMap, permutationNumberUserDefinedGeneSetNumber2OneorZeroMap, permutationNumberGeneNumber2OneorZeroMap,type, geneSetAnalysisType, geneSetType, overlapDefinition);
			}

			
			// KEGG Pathway starts
			if (geneSetType.isKeggPathway()) {

				// accumulate search results of keggPathway2OneorZeroMap in
				// keggPathway2KMap
				for (TIntIntIterator it = permutationNumberKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!(permutationNumberKeggPathwayNumber2KMap.containsKey(it.key()))) {
						permutationNumberKeggPathwayNumber2KMap.put(it.key(), it.value());
					} else {
						permutationNumberKeggPathwayNumber2KMap.put(it.key(), permutationNumberKeggPathwayNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for

			}
			// KEGG Pathway ends

			// UserDefinedGeneSet starts
			else if (geneSetType.isUserDefinedGeneSet()) {

				// accumulate search results of keggPathway2OneorZeroMap in
				// keggPathway2KMap
				for (TLongIntIterator it = permutationNumberUserDefinedGeneSetNumber2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!(permutationNumberUserDefinedGeneSetNumber2KMap.containsKey(it.key()))) {
						permutationNumberUserDefinedGeneSetNumber2KMap.put(it.key(), it.value());
					} else {
						permutationNumberUserDefinedGeneSetNumber2KMap.put(it.key(), permutationNumberUserDefinedGeneSetNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for

			}
			// UserDefinedGeneSet ends
			
			
			//Gene starts
			else if (geneSetType.isNoGeneSetTypeDefined()) {

				// accumulate search results of keggPathway2OneorZeroMap in
				// keggPathway2KMap
				for (TLongIntIterator it = permutationNumberGeneNumber2OneorZeroMap.iterator(); it.hasNext();) {

					it.advance();

					if (!(permutationNumberGeneNumber2KMap.containsKey(it.key()))) {
						permutationNumberGeneNumber2KMap.put(it.key(), it.value());
					} else {
						permutationNumberGeneNumber2KMap.put(it.key(), permutationNumberGeneNumber2KMap.get(it.key()) + it.value());

					}

				}// End of for

			}
			//Gene ends

		}// End of for each input line

	}

	// GeneSetType added 14.10.2014

	// Enrichment
	// With IO
	// With Numbers
	// TF and KeggPathway Enrichment or
	// TF and CellLine and KeggPathway Enrichment starts
	// Empirical P Value Calculation
	// Search tf and keggPathway
	// TF CellLine Exon Based Kegg Pathway
	// TF CellLine Regulation Based Kegg Pathway
	// TF CellLine All Based Kegg Pathway
	// TF Exon Based Kegg Pathway
	// TF Regulation Based Kegg Pathway
	// TF All Based Kegg Pathway
	public static void searchTfandKeggPathwayWithIOWithNumbers(String outputFolder, int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree tfIntervalTree, IntervalTree ucscRefSeqGenesIntervalTree, TLongObjectMap<BufferedWriter> tfBufferedWriterHashMap, TIntObjectMap<BufferedWriter> exonBasedKeggPathwayBufferedWriterHashMap, TIntObjectMap<BufferedWriter> regulationBasedKeggPathwayBufferedWriterHashMap, TIntObjectMap<BufferedWriter> allBasedKeggPathwayBufferedWriterHashMap, TLongObjectMap<BufferedWriter> tfExonBasedKeggPathwayBufferedWriterHashMap, TLongObjectMap<BufferedWriter> tfRegulationBasedKeggPathwayBufferedWriterHashMap, TLongObjectMap<BufferedWriter> tfAllBasedKeggPathwayBufferedWriterHashMap, TLongObjectMap<BufferedWriter> tfCellLineExonBasedKeggPathwayBufferedWriterHashMap, TLongObjectMap<BufferedWriter> tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap, TLongObjectMap<BufferedWriter> tfCellLineAllBasedKeggPathwayBufferedWriterHashMap, TIntObjectMap<TShortList> geneId2KeggPathwayNumberMap, TLongIntMap permutationNumberTfNumberCellLineNumber2KMap, TIntIntMap permutationNumberExonBasedKeggPathway2KMap, TIntIntMap permutationNumberRegulationBasedKeggPathway2KMap, TIntIntMap permutationNumberAllBasedKeggPathway2KMap, TLongIntMap permutationNumberTfNumberExonBasedKeggPathway2KMap, TLongIntMap permutationNumberTfNumberRegulationBasedKeggPathway2KMap, TLongIntMap permutationNumberTfNumberAllBasedKeggPathway2KMap, TLongIntMap permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap, TLongIntMap permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap, TLongIntMap permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap, String type, AnnotationType annotationType, int overlapDefinition) {
		int low;
		int high;

		FileWriter fileWriter1 = null;
		BufferedWriter bufferedWriter1 = null;

		FileWriter fileWriter2 = null;
		BufferedWriter bufferedWriter2 = null;

		long permutationNumberTfNumberCellLineNumberKeggPathwayNumber;
		long permutationNumberTfNumberCellLineNumber;
		long permutationNumberTfNumber;
		long permutationNumberTfNumberKeggPathwayNumber;

		short keggPathwayNumber;

		try {
			for (InputLine inputLine : inputLines) {

				// TF Enrichment
				TLongIntMap permutationNumberTfNumberCellLineNumber2ZeroorOneMap = new TLongIntHashMap();

				// KEGGPATHWAY Enrichment
				TIntIntMap permutationNumberExonBasedKeggPathway2ZeroorOneMap = new TIntIntHashMap();
				TIntIntMap permutationNumberRegulationBasedKeggPathway2ZeroorOneMap = new TIntIntHashMap();
				TIntIntMap permutationNumberAllBasedKeggPathway2ZeroorOneMap = new TIntIntHashMap();

				// TF KEGGPATHWAY Enrichment and
				TLongIntMap permutationNumberTfNumberExonBasedKeggPathway2ZeroorOneMap = new TLongIntHashMap();
				TLongIntMap permutationNumberTfNumberRegulationBasedKeggPathway2ZeroorOneMap = new TLongIntHashMap();
				TLongIntMap permutationNumberTfNumberAllBasedKeggPathway2ZeroorOneMap = new TLongIntHashMap();

				// TF CellLine KEGGPATHWAY Enrichment and
				TLongIntMap permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2ZeroorOneMap = new TLongIntHashMap();
				TLongIntMap permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2ZeroorOneMap = new TLongIntHashMap();
				TLongIntMap permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2ZeroorOneMap = new TLongIntHashMap();

				low = inputLine.getLow();
				high = inputLine.getHigh();
				Interval interval = new Interval(low, high);

				// Fill these lists during search for tfs and search for
				// ucscRefSeqGenes
				List<PermutationNumberTfNumberCellLineNumberOverlap> permutationNumberTfNumberCellLineNumberOverlapList = new ArrayList<PermutationNumberTfNumberCellLineNumberOverlap>();
				List<PermutationNumberUcscRefSeqGeneNumberOverlap> permutationNumberExonBasedKeggPathwayOverlapList = new ArrayList<PermutationNumberUcscRefSeqGeneNumberOverlap>();
				List<PermutationNumberUcscRefSeqGeneNumberOverlap> permutationNumberRegulationBasedKeggPathwayOverlapList = new ArrayList<PermutationNumberUcscRefSeqGeneNumberOverlap>();
				List<PermutationNumberUcscRefSeqGeneNumberOverlap> permutationNumberAllBasedKeggPathwayOverlapList = new ArrayList<PermutationNumberUcscRefSeqGeneNumberOverlap>();

				if (tfIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					tfIntervalTree.findAllOverlappingTfbsIntervalsWithIOWithNumbers(outputFolder, permutationNumber, tfIntervalTree.getRoot(), interval, chromName, tfBufferedWriterHashMap, permutationNumberTfNumberCellLineNumber2ZeroorOneMap, permutationNumberTfNumberCellLineNumberOverlapList, overlapDefinition);
				}

				// accumulate search results of
				// permutationNumberTfbsNameCellLineName2ZeroorOneMap in
				// permutationNumberTfbsNameCellLineName2KMap
				for (TLongIntIterator it = permutationNumberTfNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!(permutationNumberTfNumberCellLineNumber2KMap.containsKey(it.key()))) {
						permutationNumberTfNumberCellLineNumber2KMap.put(it.key(), it.value());
					} else {
						permutationNumberTfNumberCellLineNumber2KMap.put(it.key(), permutationNumberTfNumberCellLineNumber2KMap.get(it.key()) + it.value());
					}
				}// End of for

				if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
					ucscRefSeqGenesIntervalTree.findAllOverlappingUcscRefSeqGenesIntervalsWithIOWithNumbers(outputFolder, permutationNumber, ucscRefSeqGenesIntervalTree.getRoot(), interval, chromName, geneId2KeggPathwayNumberMap, exonBasedKeggPathwayBufferedWriterHashMap, regulationBasedKeggPathwayBufferedWriterHashMap, allBasedKeggPathwayBufferedWriterHashMap, permutationNumberExonBasedKeggPathway2ZeroorOneMap, permutationNumberRegulationBasedKeggPathway2ZeroorOneMap, permutationNumberAllBasedKeggPathway2ZeroorOneMap, type, permutationNumberExonBasedKeggPathwayOverlapList, permutationNumberRegulationBasedKeggPathwayOverlapList, permutationNumberAllBasedKeggPathwayOverlapList, overlapDefinition);
				}

				// accumulate search results of
				// permutationNumberExonBasedKeggPathway2ZeroorOneMap in
				// permutationNumberExonBasedKeggPathway2KMap
				for (TIntIntIterator it = permutationNumberExonBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!(permutationNumberExonBasedKeggPathway2KMap.containsKey(it.key()))) {
						permutationNumberExonBasedKeggPathway2KMap.put(it.key(), it.value());
					} else {
						permutationNumberExonBasedKeggPathway2KMap.put(it.key(), permutationNumberExonBasedKeggPathway2KMap.get(it.key()) + it.value());

					}
				}// End of for

				// accumulate search results of
				// permutationNumberRegulationBasedKeggPathway2ZeroorOneMap in
				// permutationNumberRegulationBasedKeggPathway2KMap
				for (TIntIntIterator it = permutationNumberRegulationBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!(permutationNumberRegulationBasedKeggPathway2KMap.containsKey(it.key()))) {
						permutationNumberRegulationBasedKeggPathway2KMap.put(it.key(), it.value());
					} else {
						permutationNumberRegulationBasedKeggPathway2KMap.put(it.key(), permutationNumberRegulationBasedKeggPathway2KMap.get(it.key()) + it.value());

					}
				}// End of for

				// accumulate search results of
				// permutationNumberAllBasedKeggPathway2ZeroorOneMap in
				// permutationNumberAllBasedKeggPathway2KMap
				for (TIntIntIterator it = permutationNumberAllBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
					it.advance();

					if (!(permutationNumberAllBasedKeggPathway2KMap.containsKey(it.key()))) {
						permutationNumberAllBasedKeggPathway2KMap.put(it.key(), it.value());
					} else {
						permutationNumberAllBasedKeggPathway2KMap.put(it.key(), permutationNumberAllBasedKeggPathway2KMap.get(it.key()) + it.value());

					}
				}// End of for

				// New search for given input SNP or interval case, does not
				// matter.
				// for each tf overlap
				// for each ucscRefSeqGene overlap
				// if these overlaps overlaps
				// question will overlapDefinition apply here?
				// then write common overlap to output files
				// Fill
				// permutationNumberTfNameCellLineNameExonBasedKeggPathway2OneorZeroMap
				// Fill
				// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2OneorZeroMap
				// Fill
				// permutationNumberTfNameCellLineNameAllBasedKeggPathway2OneorZeroMap

				// permutation number is same for tf and keggPathway maps
				// for example:
				// permutationNumberTfbsNameCellLineName2ZeroorOneMap
				// for example:
				// permutationNumberExonBasedKeggPathway2ZeroorOneMap
				// input lines are randomly generated data for this permutation
				// number
				// all the entries in these maps have 1 values.
				// if there is an entry, it means that it is 1.

				for (PermutationNumberTfNumberCellLineNumberOverlap permutationNumberTfNumberCellLineNumberOverlap : permutationNumberTfNumberCellLineNumberOverlapList) {

					permutationNumberTfNumberCellLineNumber = permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber();
					permutationNumberTfNumber = IntervalTree.removeCellLineNumber(permutationNumberTfNumberCellLineNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

					// TF and Exon Based Kegg Pathway
					for (PermutationNumberUcscRefSeqGeneNumberOverlap permutationNumberUcscRefSeqGeneNumberOverlap : permutationNumberExonBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(permutationNumberTfNumberCellLineNumberOverlap.getLow(), permutationNumberTfNumberCellLineNumberOverlap.getHigh(), permutationNumberUcscRefSeqGeneNumberOverlap.getLow(), permutationNumberUcscRefSeqGeneNumberOverlap.getHigh())) {

							for (TShortIterator it = permutationNumberUcscRefSeqGeneNumberOverlap.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();

								if (annotationType.doTFKEGGPathwayAnnotation()) {
									/******************************************************/
									permutationNumberTfNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberExonBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
										permutationNumberTfNumberExonBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
									}
									/******************************************************/
									/******************************************************/
									bufferedWriter2 = tfExonBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberKeggPathwayNumber);

									if (bufferedWriter2 == null) {

										fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter2 = new BufferedWriter(fileWriter2);
										tfExonBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberKeggPathwayNumber, bufferedWriter2);
										bufferedWriter2.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));

									}

									bufferedWriter2.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter2.flush();
									/******************************************************/

								} else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {
									/******************************************************/
									permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
										permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
									}
									/******************************************************/

									/******************************************************/
									bufferedWriter1 = tfCellLineExonBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber);

									if (bufferedWriter1 == null) {

										fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_CELLLINE_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberCellLineNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter1 = new BufferedWriter(fileWriter1);
										tfCellLineExonBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, bufferedWriter1);
										bufferedWriter1.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo suymbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter1.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter1.flush();
									/******************************************************/

								} else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

									// TF EXONKEGG
									/******************************************************/
									permutationNumberTfNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberExonBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
										permutationNumberTfNumberExonBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
									}
									/******************************************************/
									/******************************************************/
									bufferedWriter2 = tfExonBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberKeggPathwayNumber);

									if (bufferedWriter2 == null) {

										fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter2 = new BufferedWriter(fileWriter2);
										tfExonBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberKeggPathwayNumber, bufferedWriter2);
										bufferedWriter2.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name " + "\t" + "hugo suymbol" + "\t" + "entrez id" + "\t" + "keggPathwayName" + System.getProperty("line.separator"));

									}

									bufferedWriter2.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter2.flush();
									/******************************************************/

									// TF CELLLINE EXONKEGG
									/******************************************************/
									permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
										permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
									}
									/******************************************************/

									/******************************************************/
									bufferedWriter1 = tfCellLineExonBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber);

									if (bufferedWriter1 == null) {

										fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_CELLLINE_EXON_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberCellLineNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter1 = new BufferedWriter(fileWriter1);
										tfCellLineExonBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, bufferedWriter1);
										bufferedWriter1.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo suymbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter1.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter1.flush();
									/******************************************************/

								}

							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query

					// TF and Regulation Based Kegg Pathway
					for (PermutationNumberUcscRefSeqGeneNumberOverlap permutationNumberUcscRefSeqGeneNumberOverlap : permutationNumberRegulationBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(permutationNumberTfNumberCellLineNumberOverlap.getLow(), permutationNumberTfNumberCellLineNumberOverlap.getHigh(), permutationNumberUcscRefSeqGeneNumberOverlap.getLow(), permutationNumberUcscRefSeqGeneNumberOverlap.getHigh())) {
							for (TShortIterator it = permutationNumberUcscRefSeqGeneNumberOverlap.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();

								if (annotationType.doTFKEGGPathwayAnnotation()) {

									/************************************************/
									permutationNumberTfNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberRegulationBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
										permutationNumberTfNumberRegulationBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
									}
									/************************************************/

									/************************************************/
									bufferedWriter2 = tfRegulationBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberKeggPathwayNumber);

									if (bufferedWriter2 == null) {

										fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter2 = new BufferedWriter(fileWriter2);
										tfRegulationBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberKeggPathwayNumber, bufferedWriter2);
										bufferedWriter2.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo suymbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter2.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter2.flush();
									/************************************************/

								} else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {
									/************************************************/
									permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
										permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
									}
									/************************************************/

									/************************************************/
									bufferedWriter1 = tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber);

									if (bufferedWriter1 == null) {

										fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_CELLLINE_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberCellLineNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter1 = new BufferedWriter(fileWriter1);
										tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, bufferedWriter1);
										bufferedWriter1.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo symbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter1.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter1.flush();
									/************************************************/

								} else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

									// TF RegulationKEGG
									/************************************************/
									permutationNumberTfNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberRegulationBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
										permutationNumberTfNumberRegulationBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
									}
									/************************************************/

									/************************************************/
									bufferedWriter2 = tfRegulationBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberKeggPathwayNumber);

									if (bufferedWriter2 == null) {

										fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter2 = new BufferedWriter(fileWriter2);
										tfRegulationBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberKeggPathwayNumber, bufferedWriter2);
										bufferedWriter2.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo suymbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter2.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter2.flush();
									/************************************************/

									// TF CellLine RegulationKEGG
									/************************************************/
									permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
										permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
									}
									/************************************************/

									/************************************************/
									bufferedWriter1 = tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber);

									if (bufferedWriter1 == null) {

										fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_CELLLINE_REGULATION_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberCellLineNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter1 = new BufferedWriter(fileWriter1);
										tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, bufferedWriter1);
										bufferedWriter1.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo symbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter1.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter1.flush();
									/************************************************/

								}

							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query

					// TF and All Based Kegg Pathway
					for (PermutationNumberUcscRefSeqGeneNumberOverlap permutationNumberUcscRefSeqGeneNumberOverlap : permutationNumberAllBasedKeggPathwayOverlapList) {
						if (IntervalTree.overlaps(permutationNumberTfNumberCellLineNumberOverlap.getLow(), permutationNumberTfNumberCellLineNumberOverlap.getHigh(), permutationNumberUcscRefSeqGeneNumberOverlap.getLow(), permutationNumberUcscRefSeqGeneNumberOverlap.getHigh())) {
							for (TShortIterator it = permutationNumberUcscRefSeqGeneNumberOverlap.getKeggPathwayNumberList().iterator(); it.hasNext();) {

								keggPathwayNumber = it.next();

								if (annotationType.doTFKEGGPathwayAnnotation()) {

									/************************************************/
									permutationNumberTfNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberAllBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
										permutationNumberTfNumberAllBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
									}
									/************************************************/

									/************************************************/
									bufferedWriter2 = tfAllBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberKeggPathwayNumber);

									if (bufferedWriter2 == null) {

										fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter2 = new BufferedWriter(fileWriter2);
										tfAllBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberKeggPathwayNumber, bufferedWriter2);
										bufferedWriter2.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo suymbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter2.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter2.flush();
									/************************************************/

								} else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {
									/************************************************/
									permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
										permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
									}
									/************************************************/

									/************************************************/
									bufferedWriter1 = tfCellLineAllBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber);

									if (bufferedWriter1 == null) {

										fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_CELLLINE_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberCellLineNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter1 = new BufferedWriter(fileWriter1);
										tfCellLineAllBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, bufferedWriter1);
										bufferedWriter1.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo symbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter1.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter1.flush();
									/************************************************/

								} else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

									// TF ALLKEGG
									/************************************************/
									permutationNumberTfNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberAllBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
										permutationNumberTfNumberAllBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
									}
									/************************************************/

									/************************************************/
									bufferedWriter2 = tfAllBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberKeggPathwayNumber);

									if (bufferedWriter2 == null) {

										fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter2 = new BufferedWriter(fileWriter2);
										tfAllBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberKeggPathwayNumber, bufferedWriter2);
										bufferedWriter2.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo suymbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter2.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter2.flush();
									/************************************************/

									// TF CELLLINE ALLKEGG
									/************************************************/
									permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

									if (!(permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2ZeroorOneMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
										permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2ZeroorOneMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
									}
									/************************************************/

									/************************************************/
									bufferedWriter1 = tfCellLineAllBasedKeggPathwayBufferedWriterHashMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber);

									if (bufferedWriter1 == null) {

										fileWriter1 = FileOperations.createFileWriter(outputFolder + Commons.ANNOTATE_PERMUTATIONS_TF_CELLLINE_ALL_BASED_KEGG_PATHWAY_ANALYSIS + Commons.PERMUTATION + permutationNumber + System.getProperty("file.separator") + "_" + permutationNumberTfNumberCellLineNumberKeggPathwayNumber + ".txt", true);
										bufferedWriter1 = new BufferedWriter(fileWriter1);
										tfCellLineAllBasedKeggPathwayBufferedWriterHashMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, bufferedWriter1);
										bufferedWriter1.write("Searched for chr" + "\t" + "given interval low" + "\t" + "given interval high" + "\t" + "tfbs" + "\t" + "tfbs low" + "\t" + "tfbs high" + "\t" + "refseq gene name number" + "\t" + "ucscRefSeqGene low" + "\t" + "ucscRefSeqGene high" + "\t" + "interval name" + "\t" + "interval number" + "\t" + "hugo symbol number" + "\t" + "entrez id" + "\t" + "keggPathwayNumber" + System.getProperty("line.separator"));

									}

									bufferedWriter1.write(chromName + "\t" + interval.getLow() + "\t" + interval.getHigh() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getLow() + "\t" + permutationNumberTfNumberCellLineNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getRefSeqGeneNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getLow() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getHigh() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalName() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getIntervalNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneHugoSymbolNumber() + "\t" + permutationNumberUcscRefSeqGeneNumberOverlap.getGeneEntrezId() + "\t" + keggPathwayNumber + System.getProperty("line.separator"));
									bufferedWriter1.flush();
									/************************************************/

								}

							} // for each kegg pathways having this gene
						}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
					}// for each ucscRefSeqGeneOverlap for the given query

				}// for each tfOverlap for the given query

				if (annotationType.doTFKEGGPathwayAnnotation()) {

					// TF EXON BASED
					// Fill permutationNumberTfNameExonBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameExonBasedKeggPathway2ZeroorOneMap
					for (TLongIntIterator it = permutationNumberTfNumberExonBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberExonBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
							permutationNumberTfNumberExonBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberExonBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberExonBasedKeggPathway2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF REGULATION BASED
					// Fill
					// permutationNumberTfNameRegulationBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameRegulationBasedKeggPathway2ZeroorOneMap
					for (TLongIntIterator it = permutationNumberTfNumberRegulationBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberRegulationBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
							permutationNumberTfNumberRegulationBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberRegulationBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberRegulationBasedKeggPathway2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF ALL BASED
					// Fill permutationNumberTfNameAllBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameAllBasedKeggPathway2ZeroorOneMap
					for (TLongIntIterator it = permutationNumberTfNumberAllBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberAllBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
							permutationNumberTfNumberAllBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberAllBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberAllBasedKeggPathway2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

				} else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {
					// TF CELLLINE EXON BASED
					// Fill
					// permutationNumberTfNameCellLineNameExonBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameCellLineNameExonBasedKeggPathway2OneorZeroMap
					for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
							permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF CELLLINE REGULATION BASED
					// Fill
					// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2OneorZeroMap
					for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
							permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF CELLLINE ALL BASED
					// Fill
					// permutationNumberTfNameCellLineNameAllBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameCellLineNameAllBasedKeggPathway2OneorZeroMap
					for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
							permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

				} else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

					// TF KEGG
					// TF EXONBASED
					// Fill permutationNumberTfNameExonBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameExonBasedKeggPathway2ZeroorOneMap
					for (TLongIntIterator it = permutationNumberTfNumberExonBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberExonBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
							permutationNumberTfNumberExonBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberExonBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberExonBasedKeggPathway2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF REGULATIONBASED
					// Fill
					// permutationNumberTfNameRegulationBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameRegulationBasedKeggPathway2ZeroorOneMap
					for (TLongIntIterator it = permutationNumberTfNumberRegulationBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberRegulationBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
							permutationNumberTfNumberRegulationBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberRegulationBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberRegulationBasedKeggPathway2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF ALLBASED
					// Fill permutationNumberTfNameAllBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameAllBasedKeggPathway2ZeroorOneMap
					for (TLongIntIterator it = permutationNumberTfNumberAllBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberAllBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
							permutationNumberTfNumberAllBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberAllBasedKeggPathway2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberAllBasedKeggPathway2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF CELLLINE KEGG
					// TF CELLLINE EXONBASED
					// Fill
					// permutationNumberTfNameCellLineNameExonBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameCellLineNameExonBasedKeggPathway2OneorZeroMap
					for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
							permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberExonBasedKeggPathway2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF CELLLINE REGULATIONBASED
					// Fill
					// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2OneorZeroMap
					for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
							permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathway2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

					// TF CELLLINE ALLBASED
					// Fill
					// permutationNumberTfNameCellLineNameAllBasedKeggPathway2KMap
					// using
					// permutationNumberTfNameCellLineNameAllBasedKeggPathway2OneorZeroMap
					for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2ZeroorOneMap.iterator(); it.hasNext();) {
						it.advance();

						permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

						if (!(permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
							permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
						} else {
							permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberAllBasedKeggPathway2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
						}

					}// End of for inner loop

				}

			}// End of for each input line

		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		} // End of while

	}

	// TF and KeggPathway Enrichment or
	// TF and CellLine and KeggPathway Enrichment ends
	// with Numbers
	// @todo

	// TF and KeggPathway Enrichment or
	// TF and CellLine and KeggPathway Enrichment ends

	// Enrichment
	// Without IO
	// With Numbers
	// Empirical P Value Calculation
	// Tf and KeggPathway Enrichment or
	// Tf and CellLine and KeggPathway Enrichment starts
	// Or BOTH
	// New Functionality START
	// Empirical P Value Calculation
	// Search tf and keggPathway
	// TF and Exon Based Kegg Pathway
	// TF and Regulation Based Kegg Pathway
	// TF and All Based Kegg Pathway
	public static void searchTfandKeggPathwayWithoutIOWithNumbers(int permutationNumber, ChromosomeName chromName, List<InputLine> inputLines, IntervalTree tfIntervalTree, IntervalTree ucscRefSeqGenesIntervalTree, TIntObjectMap<TShortList> geneId2KeggPathwayNumberMap, TLongIntMap permutationNumberTfNumberCellLineNumber2KMap, TIntIntMap permutationNumberExonBasedKeggPathwayNumber2KMap, TIntIntMap permutationNumberRegulationBasedKeggPathwayNumber2KMap, TIntIntMap permutationNumberAllBasedKeggPathwayNumber2KMap, TLongIntMap permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap, TLongIntMap permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap, TLongIntMap permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap, TLongIntMap permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, TLongIntMap permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, TLongIntMap permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, String type, AnnotationType annotationType, int overlapDefinition) {

		int low;
		int high;

		long permutationNumberTfNumberCellLineNumber;
		long permutationNumberTfNumberKeggPathwayNumber;
		long permutationNumberTfNumberCellLineNumberKeggPathwayNumber;

		short keggPathwayNumber;

		for (InputLine inputLine : inputLines) {

			// Will be filled in tfIntervalTree search
			TLongIntMap permutationNumberTfNumberCellLineNumber2ZeroorOneMap = new TLongIntHashMap();

			// Will be filled in ucscRefSeqGene search
			TIntIntMap permutationNumberExonBasedKeggPathwayNumber2ZeroorOneMap = new TIntIntHashMap();
			TIntIntMap permutationNumberRegulationBasedKeggPathwayNumber2ZeroorOneMap = new TIntIntHashMap();
			TIntIntMap permutationNumberAllBasedKeggPathwayNumber2ZeroorOneMap = new TIntIntHashMap();

			// Will be filled in common overlap check
			// Will be used for tf and kegg pathway enrichment
			TLongIntMap permutationNumberTfNumberExonBasedKeggPathwayNumber2OneorZeroMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberRegulationBasedKeggPathwayNumber2OneorZeroMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberAllBasedKeggPathwayNumber2OneorZeroMap = new TLongIntHashMap();

			// Will be filled in common overlap check
			// Will be used for tf and cell line and kegg pathway enrichment
			TLongIntMap permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2OneorZeroMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2OneorZeroMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2OneorZeroMap = new TLongIntHashMap();

			low = inputLine.getLow();
			high = inputLine.getHigh();
			Interval interval = new Interval(low, high);

			// Fill these lists during search for tfs and search for
			// ucscRefSeqGenes
			List<PermutationNumberTfNumberCellLineNumberOverlap> permutationNumberTfNumberCellLineNumberOverlapList = new ArrayList<PermutationNumberTfNumberCellLineNumberOverlap>();
			List<PermutationNumberUcscRefSeqGeneNumberOverlap> permutationNumberExonBasedKeggPathwayNumberOverlapList = new ArrayList<PermutationNumberUcscRefSeqGeneNumberOverlap>();
			List<PermutationNumberUcscRefSeqGeneNumberOverlap> permutationNumberRegulationBasedKeggPathwayNumberOverlapList = new ArrayList<PermutationNumberUcscRefSeqGeneNumberOverlap>();
			List<PermutationNumberUcscRefSeqGeneNumberOverlap> permutationNumberAllBasedKeggPathwayNumberOverlapList = new ArrayList<PermutationNumberUcscRefSeqGeneNumberOverlap>();

			// First TF
			// Fill permutationNumberTfNumberCellLineNumber2ZeroorOneMap
			if (tfIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				tfIntervalTree.findAllOverlappingTfbsIntervalsWithoutIOWithNumbers(permutationNumber, tfIntervalTree.getRoot(), interval, chromName, permutationNumberTfNumberCellLineNumber2ZeroorOneMap, permutationNumberTfNumberCellLineNumberOverlapList, overlapDefinition);
			}

			// accumulate search results of
			// permutationNumberTfNumberCellLineNumber2ZeroorOneMap in
			// permutationNumberTfNameaCellLineName2KMap
			for (TLongIntIterator it = permutationNumberTfNumberCellLineNumber2ZeroorOneMap.iterator(); it.hasNext();) {
				it.advance();

				if (!(permutationNumberTfNumberCellLineNumber2KMap.containsKey(it.key()))) {
					permutationNumberTfNumberCellLineNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberTfNumberCellLineNumber2KMap.put(it.key(), permutationNumberTfNumberCellLineNumber2KMap.get(it.key()) + it.value());
				}
			}// End of for

			// Second UcscRefSeqGenes
			// Fill permutationNumberExonBasedKeggPathway2ZeroorOneMap
			// Fill permutationNumberRegulationBasedKeggPathway2ZeroorOneMap
			// Fill permutationNumberAllBasedKeggPathway2ZeroorOneMap
			if (ucscRefSeqGenesIntervalTree.getRoot().getNodeName().isNotSentinel()) {
				ucscRefSeqGenesIntervalTree.findAllOverlappingUcscRefSeqGenesIntervalsWithoutIOWithNumbers(permutationNumber, ucscRefSeqGenesIntervalTree.getRoot(), interval, chromName, geneId2KeggPathwayNumberMap, permutationNumberExonBasedKeggPathwayNumber2ZeroorOneMap, permutationNumberRegulationBasedKeggPathwayNumber2ZeroorOneMap, permutationNumberAllBasedKeggPathwayNumber2ZeroorOneMap, type, permutationNumberExonBasedKeggPathwayNumberOverlapList, permutationNumberRegulationBasedKeggPathwayNumberOverlapList, permutationNumberAllBasedKeggPathwayNumberOverlapList, overlapDefinition);
			}

			// accumulate search results of exonBasedKeggPathway2ZeroorOneMap in
			// permutationNumberExonBasedKeggPathway2KMap
			for (TIntIntIterator it = permutationNumberExonBasedKeggPathwayNumber2ZeroorOneMap.iterator(); it.hasNext();) {
				it.advance();

				if (!(permutationNumberExonBasedKeggPathwayNumber2KMap.containsKey(it.key()))) {
					permutationNumberExonBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberExonBasedKeggPathwayNumber2KMap.put(it.key(), permutationNumberExonBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
				}
			}// End of for

			// accumulate search results of
			// regulationBasedKeggPathway2ZeroorOneMap in
			// permutationNumberRegulationBasedKeggPathway2KMap
			for (TIntIntIterator it = permutationNumberRegulationBasedKeggPathwayNumber2ZeroorOneMap.iterator(); it.hasNext();) {
				it.advance();

				if (!(permutationNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(it.key()))) {
					permutationNumberRegulationBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberRegulationBasedKeggPathwayNumber2KMap.put(it.key(), permutationNumberRegulationBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
				}
			}// End of for

			// accumulate search results of allBasedKeggPathway2ZeroorOneMap in
			// permutationNumberAllBasedKeggPathway2KMap
			for (TIntIntIterator it = permutationNumberAllBasedKeggPathwayNumber2ZeroorOneMap.iterator(); it.hasNext();) {
				it.advance();

				if (!(permutationNumberAllBasedKeggPathwayNumber2KMap.containsKey(it.key()))) {
					permutationNumberAllBasedKeggPathwayNumber2KMap.put(it.key(), it.value());
				} else {
					permutationNumberAllBasedKeggPathwayNumber2KMap.put(it.key(), permutationNumberAllBasedKeggPathwayNumber2KMap.get(it.key()) + it.value());
				}
			}// End of for

			// New search for given input SNP or interval case, does not matter.
			// for each tf overlap
			// for each ucscRefSeqGene overlap
			// if these overlaps overlaps
			// then write common overlap to output files
			// Fill
			// permutationNumberTfNameCellLineNameExonBasedKeggPathway2OneorZeroMap
			// Fill
			// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2OneorZeroMap
			// Fill
			// permutationNumberTfNameCellLineNameAllBasedKeggPathway2OneorZeroMap
			// question will overlapDefition apply to here?
			for (PermutationNumberTfNumberCellLineNumberOverlap permutationNumberTfNumberCellLineNumberOverlap : permutationNumberTfNumberCellLineNumberOverlapList) {

				permutationNumberTfNumberCellLineNumber = permutationNumberTfNumberCellLineNumberOverlap.getPermutationNumberTfNumberCellLineNumber();

				// TF and EXON Based Kegg Pathway
				for (PermutationNumberUcscRefSeqGeneNumberOverlap permutationNumberUcscRefSeqGeneNumberOverlap : permutationNumberExonBasedKeggPathwayNumberOverlapList) {
					if (IntervalTree.overlaps(permutationNumberTfNumberCellLineNumberOverlap.getLow(), permutationNumberTfNumberCellLineNumberOverlap.getHigh(), permutationNumberUcscRefSeqGeneNumberOverlap.getLow(), permutationNumberUcscRefSeqGeneNumberOverlap.getHigh())) {
						for (TShortIterator it = permutationNumberUcscRefSeqGeneNumberOverlap.getKeggPathwayNumberList().iterator(); it.hasNext();) {

							keggPathwayNumber = it.next();

							// TF EXONKEGG
							if (annotationType.doTFKEGGPathwayAnnotation()) {
								/***********************************/
								permutationNumberTfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumberAddKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberExonBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
									permutationNumberTfNumberExonBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
								}
								/***********************************/
							}
							// TF CELLLINE EXONKEGG
							else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {
								/***********************************/
								permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
									permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
								}
								/***********************************/
							}
							// TF EXONKEGG and TF CELLLINE EXONKEGG
							else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

								/***********************************/
								permutationNumberTfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumberAddKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberExonBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
									permutationNumberTfNumberExonBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
								}
								/***********************************/

								/***********************************/
								permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
									permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
								}
								/***********************************/
							}

						} // for each kegg pathways having this gene
					}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
				}// for each ucscRefSeqGeneOverlap for the given query

				// TF and REGULATION Based Kegg Pathway
				for (PermutationNumberUcscRefSeqGeneNumberOverlap permutationNumberUcscRefSeqGeneNumberOverlap : permutationNumberRegulationBasedKeggPathwayNumberOverlapList) {
					if (IntervalTree.overlaps(permutationNumberTfNumberCellLineNumberOverlap.getLow(), permutationNumberTfNumberCellLineNumberOverlap.getHigh(), permutationNumberUcscRefSeqGeneNumberOverlap.getLow(), permutationNumberUcscRefSeqGeneNumberOverlap.getHigh())) {

						for (TShortIterator it = permutationNumberUcscRefSeqGeneNumberOverlap.getKeggPathwayNumberList().iterator(); it.hasNext();) {

							keggPathwayNumber = it.next();

							// TF REGULATIONKEGG
							if (annotationType.doTFKEGGPathwayAnnotation()) {
								/***********************************/
								permutationNumberTfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumberAddKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
									permutationNumberTfNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
								}
								/***********************************/

							}
							// TF CELLLINE REGULATIONKEGG
							else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {
								/***********************************/
								permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
									permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
								}
								/***********************************/

							}
							// TF REGULATIONKEGG AND TF CELLLINE REGULATIONKEGG
							else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

								/***********************************/
								permutationNumberTfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumberAddKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
									permutationNumberTfNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
								}
								/***********************************/

								/***********************************/
								permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
									permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
								}
								/***********************************/
							}

						} // for each kegg pathways having this gene
					}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
				}// for each ucscRefSeqGeneOverlap for the given query

				// TF and ALL Based Kegg Pathway
				for (PermutationNumberUcscRefSeqGeneNumberOverlap permutationNumberUcscRefSeqGeneNumberOverlap : permutationNumberAllBasedKeggPathwayNumberOverlapList) {
					if (IntervalTree.overlaps(permutationNumberTfNumberCellLineNumberOverlap.getLow(), permutationNumberTfNumberCellLineNumberOverlap.getHigh(), permutationNumberUcscRefSeqGeneNumberOverlap.getLow(), permutationNumberUcscRefSeqGeneNumberOverlap.getHigh())) {
						for (TShortIterator it = permutationNumberUcscRefSeqGeneNumberOverlap.getKeggPathwayNumberList().iterator(); it.hasNext();) {

							keggPathwayNumber = it.next();

							// TF ALLKEGG
							if (annotationType.doTFKEGGPathwayAnnotation()) {
								/***********************************/
								permutationNumberTfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumberAddKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberAllBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
									permutationNumberTfNumberAllBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
								}
								/***********************************/

							}
							// TF CELLLINE ALLKEGG
							else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {
								/***********************************/
								permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
									permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
								}
								/***********************************/

							}
							// TF ALLKEGG AND TF CELLLINE ALLKEGG
							else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

								/***********************************/
								permutationNumberTfNumberKeggPathwayNumber = IntervalTree.removeCellLineNumberAddKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberAllBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
									permutationNumberTfNumberAllBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberKeggPathwayNumber, 1);
								}
								/***********************************/

								/***********************************/
								permutationNumberTfNumberCellLineNumberKeggPathwayNumber = IntervalTree.addKeggPathwayNumber(permutationNumberTfNumberCellLineNumber, keggPathwayNumber, GeneratedMixedNumberDescriptionOrderLength.LONG_7DIGITS_PERMUTATIONNUMBER_4DIGITS_ELEMENTNUMBER_4DIGITS_CELLLINENUMBER_4DIGITS_KEGGPATHWAYNUMBER);

								if (!(permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2OneorZeroMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
									permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2OneorZeroMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, 1);
								}
								/***********************************/
							}

						} // for each kegg pathways having this gene
					}// if tfOverlap and ucscRefSeqGeneOverlap overlaps
				}// for each ucscRefSeqGeneOverlap for the given query
			}// for each tfOverlap for the given query

			if (annotationType.doTFKEGGPathwayAnnotation()) {

				// new code starts
				// TF EXON BASED
				// Fill permutationNumberTfExonBasedKeggPathway2KMap using
				// permutationNumberTfNameExonBasedKeggPathway2OneorZeroMap

				for (TLongIntIterator it = permutationNumberTfNumberExonBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
						permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF REGULATION BASED
				// Fill permutationNumberTfRegulationBasedKeggPathway2KMap using
				// permutationNumberTfNameRegulationBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
						permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF ALL BASED
				// Fill permutationNumberTfAllBasedKeggPathway2KMap using
				// permutationNumberTfNameAllBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberAllBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
						permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// new code ends

			} else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {

				// TF CELLLINE EXON BASED
				// Fill
				// permutationNumberTfNameCellLineNameExonBasedKeggPathway2KMap
				// using
				// permutationNumberTfNameCellLineNameExonBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
						permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE REGULATION BASED
				// Fill
				// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2KMap
				// using
				// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
						permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE ALL BASED
				// Fill
				// permutationNumberTfNameCellLineNameAllBasedKeggPathway2KMap
				// using
				// permutationNumberTfNameCellLineNameAllBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
						permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

			} else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

				// TF EXON BASED
				// Fill permutationNumberTfExonBasedKeggPathway2KMap using
				// permutationNumberTfNameExonBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberExonBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
						permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF REGULATION BASED
				// Fill permutationNumberTfRegulationBasedKeggPathway2KMap using
				// permutationNumberTfNameRegulationBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
						permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF ALL BASED
				// Fill permutationNumberTfAllBasedKeggPathway2KMap using
				// permutationNumberTfNameAllBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberAllBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberKeggPathwayNumber))) {
						permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberKeggPathwayNumber, permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE EXON BASED
				// Fill
				// permutationNumberTfNameCellLineNameExonBasedKeggPathway2KMap
				// using
				// permutationNumberTfNameCellLineNameExonBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
						permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE REGULATION BASED
				// Fill
				// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2KMap
				// using
				// permutationNumberTfNameCellLineNameRegulationBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
						permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

				// TF CELLLINE ALL BASED
				// Fill
				// permutationNumberTfNameCellLineNameAllBasedKeggPathway2KMap
				// using
				// permutationNumberTfNameCellLineNameAllBasedKeggPathway2OneorZeroMap
				for (TLongIntIterator it = permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2OneorZeroMap.iterator(); it.hasNext();) {
					it.advance();

					permutationNumberTfNumberCellLineNumberKeggPathwayNumber = it.key();

					if (!(permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.containsKey(permutationNumberTfNumberCellLineNumberKeggPathwayNumber))) {
						permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, it.value());
					} else {
						permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.put(permutationNumberTfNumberCellLineNumberKeggPathwayNumber, permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap.get(permutationNumberTfNumberCellLineNumberKeggPathwayNumber) + it.value());
					}

				}// End of for inner loop

			}

		}// End of for each input line

	}

	// New Functionality END
	// Tf and KeggPathway Enrichment or
	// Tf and CellLine and KeggPathway Enrichment ends
	// with Numbers End
	// @todo

	//yeni starts
	// DNASE Annotation
	// KEGG Pathway Annotation
	public void writeResultsWithNumbers(
			int[] kArray,
			TShortObjectMap<String> number2NameMap, 
			String outputFolder, 
			String outputFileName) {

		BufferedWriter bufferedWriter;
		String elementName;
		
		
		List<Element> elementList = null;
		
		Element element = null;
		int elementNumber;
		int numberofOverlaps;
		
		elementList = transformMapToCollection(kArray);	
		
		Collections.sort(elementList, Element.NUMBER_OF_OVERLAPS);
		
		try {

			bufferedWriter = new BufferedWriter(FileOperations.createFileWriter(outputFolder + outputFileName));

			// header line
			bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Element Name" + "\t" + "Number of Overlaps: k out of n given intervals overlaps with the intervals of element" + System.getProperty("line.separator"));

			
			//Write sorted elementList
			for(Iterator<Element> it = elementList.iterator(); it.hasNext(); ){
				
				element = it.next();
				
				elementNumber = element.getElementIntNumber();
				
				elementName = number2NameMap.get((short)elementNumber);
				
				numberofOverlaps = element.getElementNumberofOverlaps();
				
				bufferedWriter.write(elementName + "\t" + numberofOverlaps + System.getProperty("line.separator"));			
				
			}//End of For
			

			//Close BufferedWriter
			bufferedWriter.close();

		} catch (IOException e) {
			logger.error(e.toString());
		}

	}
	//yeni ends
	
	
	// DNASE Annotation
	// KEGG Pathway Annotation
	public void writeResultsWithNumbers(
			TShortIntMap number2KMap,
			TShortObjectMap<String> number2NameMap, 
			String outputFolder, 
			String outputFileName) {

		BufferedWriter bufferedWriter;
		String elementName;
		
		
		List<Element> elementList = null;
		
		Element element = null;
		short elementNumber;
		int numberofOverlaps;
		
		elementList = transformMapToCollection(number2KMap);	
		
		Collections.sort(elementList, Element.NUMBER_OF_OVERLAPS);
		
		try {

			bufferedWriter = new BufferedWriter(FileOperations.createFileWriter(outputFolder + outputFileName));

			// header line
			bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Element Name" + "\t" + "Number of Overlaps: k out of n given intervals overlaps with the intervals of element" + System.getProperty("line.separator"));

			
			//Write sorted elementList
			for(Iterator<Element> it = elementList.iterator(); it.hasNext(); ){
				
				element = it.next();
				
				elementNumber = element.getElementShortNumber();
				
				elementName = number2NameMap.get(elementNumber);
				
				numberofOverlaps = element.getElementNumberofOverlaps();
				
				bufferedWriter.write(elementName + "\t" + numberofOverlaps + System.getProperty("line.separator"));			
				
			}//End of For
			
//			// accessing keys/values through an iterator:
//			for (TShortIntIterator it = number2KMap.iterator(); it.hasNext();) {
//				it.advance();
//				elementName = number2NameMap.get(it.key());
//				bufferedWriter.write(elementName + "\t" + it.value() + System.getProperty("line.separator"));
//			}

			//Close BufferedWriter
			bufferedWriter.close();

		} catch (IOException e) {
			logger.error(e.toString());
		}

	}

	// @todo ends
	
	//yeni starts
	public List<Element> transformMapToCollection(int[] kArray){
		
		int key;
		int value;
		
		List<Element> elementList = new ArrayList<Element>();
		Element element = null;
		
		for(int i = 0; i<kArray.length; i++){
			key = i+1;
			value = kArray[i];
			
			element = new Element(key,value);
			elementList.add(element);
		}
		

		
		return elementList;
	}
	//yeni ends
	
	public List<Element> transformMapToCollection(TIntIntMap number2KMap){
		
		int key;
		int value;
		
		List<Element> elementList = new ArrayList<Element>();
		Element element = null;
		
		for(TIntIntIterator it = number2KMap.iterator(); it.hasNext(); ) {
			it.advance();
			
			key = it.key();
			value = it .value();
			
			element = new Element(key,value);
			elementList.add(element);
			
		}//End of for
		
		return elementList;
	}
	

	//yeni starts
	public List<Element> transformMapToCollection(TShortIntMap number2KMap){
		
		short key;
		int value;
		
		List<Element> elementList = new ArrayList<Element>();
		Element element = null;
		
		for(TShortIntIterator it = number2KMap.iterator(); it.hasNext(); ) {
			it.advance();
			
			key = it.key();
			value = it .value();
			
			element = new Element(key,value);
			elementList.add(element);
			
		}//End of for
		
		return elementList;
	}
	//yeni ends
	
	
	// Gene Annotation
	// UserDefinedLibrary Annotation
	public void writeResultsWithNumbers(
			TIntIntMap number2KMap, 
			TIntObjectMap<String> number2NameMap, 
			String outputFolder,
			String outputFileName) {

		BufferedWriter bufferedWriter;
		String elementName;
		
		List<Element> elementList = null;
		
		Element element = null;
		int elementNumber;
		int numberofOverlaps;
		
		elementList = transformMapToCollection(number2KMap);	
		
		Collections.sort(elementList, Element.NUMBER_OF_OVERLAPS);
		
		try {

			bufferedWriter = new BufferedWriter(FileOperations.createFileWriter(outputFolder + outputFileName));

			// header line
			bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "Element Name" + "\t" + "Number of Overlaps: k out of n given intervals overlaps with the intervals of element" + System.getProperty("line.separator"));

			
			//Write sorted elementList
			for(Iterator<Element> it = elementList.iterator(); it.hasNext(); ){
				
				element = it.next();
				
				elementNumber = element.getElementIntNumber();
				
				elementName = number2NameMap.get(elementNumber);
				numberofOverlaps = element.getElementNumberofOverlaps();
				
				bufferedWriter.write(elementNumber + "\t" + elementName + "\t" + numberofOverlaps + System.getProperty("line.separator"));			
				
			}//End of For
			
			
//			// accessing keys/values through an iterator:
//			for (TIntIntIterator it = number2KMap.iterator(); it.hasNext();) {
//				it.advance();
//				elementName = number2NameMap.get(it.key());
//				bufferedWriter.write(elementName + "\t" + it.value() + System.getProperty("line.separator"));
//			}

			bufferedWriter.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}

	}


	// TF CellLine KEGGPathway Annotation
	public void writeResultsWithNumbers(
			TIntIntMap elementNumberCellLineNumberKeggNumber2KMap, 
			TShortObjectMap<String> elementNumber2ElementNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> keggPathwayNumber2KeggPathwayNameMap, 
			String outputFolder, 
			String outputFileName) {
		
		
		BufferedWriter bufferedWriter;

		int elementNumberCellLineNumberKeggPathwayNumber;
		short elementNumber;
		short cellLineNumber;
		short keggPathwayNumber;

		String elementName;
		String cellLineName;
		String keggPathwayName;
		
		
		List<Element> elementList = null;
		elementList = transformMapToCollection(elementNumberCellLineNumberKeggNumber2KMap);	
		
		Element element = null;
		int numberofOverlaps;
		
		
		Collections.sort(elementList, Element.NUMBER_OF_OVERLAPS);

		try {
			bufferedWriter = new BufferedWriter(FileOperations.createFileWriter(outputFolder + outputFileName));

			// header line
			bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "ElementName" + "_" + "CellLineName" + "_" + "KeggPathwayName" + "\t" + "Number of Overlaps: k out of n given intervals overlaps with the intervals of element" + System.getProperty("line.separator"));

			//Write sorted elementList
			for(Iterator<Element> it = elementList.iterator(); it.hasNext(); ){
				
				element = it.next();
				
				elementNumberCellLineNumberKeggPathwayNumber = element.getElementIntNumber();
				
				elementNumber = IntervalTree.getShortElementNumber(elementNumberCellLineNumberKeggPathwayNumber,GeneratedMixedNumberDescriptionOrderLength.INT_4DIGITS_ELEMENTNUMBER_3DIGITS_CELLLINENUMBER_3DIGITS_KEGGPATHWAYNUMBER);
				elementName = elementNumber2ElementNameMap.get(elementNumber);

				cellLineNumber = IntervalTree.getCellLineNumber(elementNumberCellLineNumberKeggPathwayNumber);
				cellLineName = cellLineNumber2CellLineNameMap.get(cellLineNumber);

				keggPathwayNumber = IntervalTree.getKeggPathwayNumber(elementNumberCellLineNumberKeggPathwayNumber);
				keggPathwayName = keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber);

				numberofOverlaps = element.getElementNumberofOverlaps();
				
				bufferedWriter.write(elementName + "_" + cellLineName + "_" + keggPathwayName + "\t" + numberofOverlaps + System.getProperty("line.separator"));
	
			}//End of For
			
//			// accessing keys/values through an iterator:
//			for (TIntIntIterator it = elementNumberCellLineNumberKeggNumber2KMap.iterator(); it.hasNext();) {
//				it.advance();
//
//				elementNumberCellLineNumberKeggPathwayNumber = it.key();
//
//				elementNumber = IntervalTree.getElementNumber(elementNumberCellLineNumberKeggPathwayNumber);
//				elementName = elementNumber2ElementNameMap.get(elementNumber);
//
//				cellLineNumber = IntervalTree.getCellLineNumber(elementNumberCellLineNumberKeggPathwayNumber);
//				cellLineName = cellLineNumber2CellLineNameMap.get(cellLineNumber);
//
//				keggPathwayNumber = IntervalTree.getKeggPathwayNumber(elementNumberCellLineNumberKeggPathwayNumber);
//				keggPathwayName = keggPathwayNumber2KeggPathwayNameMap.get(keggPathwayNumber);
//
//				bufferedWriter.write(elementName + "_" + cellLineName + "_" + keggPathwayName + "\t" + it.value() + System.getProperty("line.separator"));
//			}

			
			//Close bufferedWriter
			bufferedWriter.close();

		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	// @todo TF CellLine KEGGPathway ends

	// TF KEGG Pathway Annotation
	public void writeTFKEGGPathwayResultsWithNumbers(TIntIntMap elementNumberCellLineNumber2KMap, TShortObjectMap<String> elementNumber2ElementNameMap, TShortObjectMap<String> KEGGPathwayNumber2KEGGPathwayNameMap, String outputFolder, String outputFileName) {

		BufferedWriter bufferedWriter;

		int elementNumberCellLineNumber;
		short elementNumber;
		short keggPathwayNumber;

		String elementName;
		String keggPathwayName;
		
		List<Element> elementList = null;
		elementList = transformMapToCollection(elementNumberCellLineNumber2KMap);	
		
		Collections.sort(elementList, Element.NUMBER_OF_OVERLAPS);
		
		Element element = null;
		int numberofOverlaps;
		
		try {

			bufferedWriter = new BufferedWriter(FileOperations.createFileWriter(outputFolder + outputFileName));

			// header line
			bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "ElementName_KEGGPathwayName" + "\t" + "Number of Overlaps: k out of n given intervals overlaps with the intervals of element" + System.getProperty("line.separator"));
			
			//Write sorted elementList
			for(Iterator<Element> it = elementList.iterator(); it.hasNext(); ){
				
				element = it.next();
				
				elementNumberCellLineNumber = element.getElementIntNumber();
				
				elementNumber = IntervalTree.getShortElementNumber(elementNumberCellLineNumber,GeneratedMixedNumberDescriptionOrderLength.INT_4DIGITS_ELEMENTNUMBER_3DIGITS_CELLLINENUMBER_3DIGITS_KEGGPATHWAYNUMBER);
				elementName = elementNumber2ElementNameMap.get(elementNumber);

				keggPathwayNumber = IntervalTree.getKeggPathwayNumber(elementNumberCellLineNumber);
				keggPathwayName = KEGGPathwayNumber2KEGGPathwayNameMap.get(keggPathwayNumber);
				
				numberofOverlaps = element.getElementNumberofOverlaps();
				
				bufferedWriter.write(elementName + "_" + keggPathwayName + "\t" + numberofOverlaps + System.getProperty("line.separator"));
				
			}//End of For
			
//			// accessing keys/values through an iterator:
//			for (TIntIntIterator it = elementNumberCellLineNumber2KMap.iterator(); it.hasNext();) {
//				it.advance();
//
//				elementNumberCellLineNumber = it.key();
//
//				elementNumber = IntervalTree.getElementNumber(elementNumberCellLineNumber);
//				elementName = elementNumber2ElementNameMap.get(elementNumber);
//
//				keggPathwayNumber = IntervalTree.getKeggPathwayNumber(elementNumberCellLineNumber);
//				keggPathwayName = KEGGPathwayNumber2KEGGPathwayNameMap.get(keggPathwayNumber);
//
//				bufferedWriter.write(elementName + "_" + keggPathwayName + "\t" + it.value() + System.getProperty("line.separator"));
//			}

			//Close BufferedWriter
			bufferedWriter.close();

		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	// @todo ends

	// UserDefinedLibrary
	public void writeResultsWithNumbers(TIntObjectMap<String> userDefinedLibraryElementTypeNumber2ElementTypeMap, TIntObjectMap<TIntIntMap> elementTypeNumber2ElementNumber2KMapMap, TIntObjectMap<TIntObjectMap<String>> elementTypeNumber2ElementNumber2ElementNameMapMap, String outputFolder, String directoryName, String fileName) {

		String elementType;
		int elementTypeNumber;

		TIntIntMap elementNumber2KMap;
		TIntObjectMap<String> elementNumber2ElementNameMap;

		// For each elementTypeNumber
		for (TIntObjectIterator<String> it = userDefinedLibraryElementTypeNumber2ElementTypeMap.iterator(); it.hasNext();) {
			it.advance();

			elementTypeNumber = it.key();
			elementType = it.value();

			// Write these results
			elementNumber2KMap = elementTypeNumber2ElementNumber2KMapMap.get(elementTypeNumber);
			elementNumber2ElementNameMap = elementTypeNumber2ElementNumber2ElementNameMapMap.get(elementTypeNumber);

			writeResultsWithNumbers(elementNumber2KMap, elementNumber2ElementNameMap, outputFolder + directoryName, elementType + fileName);

		}// End of for each elementTypeNumber

	}

	// ends

	// HISTONE Annotation
	// TF Annotation
	public void writeResultsWithNumbers(
			TIntIntMap elementNumberCellLineNumber2KMap, 
			TShortObjectMap<String> elementNumber2ElementNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			String outputFolder, 
			String outputFileName) {

		BufferedWriter bufferedWriter;

		int elementNumberCellLineNumber;
		short elementNumber;
		short cellLineNumber;

		String elementName;
		String cellLineName;
		
		
		List<Element> elementList = null;
		elementList = transformMapToCollection(elementNumberCellLineNumber2KMap);	
		
		Collections.sort(elementList, Element.NUMBER_OF_OVERLAPS);
		
		Element element = null;
		int numberofOverlaps;
		
		
		try {
			bufferedWriter = new BufferedWriter(FileOperations.createFileWriter(outputFolder + outputFileName));

			// header line
			bufferedWriter.write(Commons.GLANET_COMMENT_CHARACTER + "ElementName_CellLineName" + "\t" + "Number of Overlaps: k out of n given intervals overlaps with the intervals of element" + System.getProperty("line.separator"));

			
			//Write sorted elementList
			for(Iterator<Element> it = elementList.iterator(); it.hasNext(); ){
				
				element = it.next();
				
				elementNumberCellLineNumber = element.getElementIntNumber();
				
				elementNumber = IntervalTree.getShortElementNumber(elementNumberCellLineNumber,GeneratedMixedNumberDescriptionOrderLength.INT_4DIGITS_ELEMENTNUMBER_3DIGITS_CELLLINENUMBER_3DIGITS_KEGGPATHWAYNUMBER);
				elementName = elementNumber2ElementNameMap.get(elementNumber);

				cellLineNumber = IntervalTree.getCellLineNumber(elementNumberCellLineNumber);
				cellLineName = cellLineNumber2CellLineNameMap.get(cellLineNumber);

				numberofOverlaps = element.getElementNumberofOverlaps();
				
				bufferedWriter.write(elementName + "_" + cellLineName + "\t" + numberofOverlaps + System.getProperty("line.separator"));
				
			}//End of For
			
			
//			// accessing keys/values through an iterator:
//			for (TIntIntIterator it = elementNumberCellLineNumber2KMap.iterator(); it.hasNext();) {
//				it.advance();
//
//				elementNumberCellLineNumber = it.key();
//
//				elementNumber = IntervalTree.getElementNumber(elementNumberCellLineNumber);
//				elementName = elementNumber2ElementNameMap.get(elementNumber);
//
//				cellLineNumber = IntervalTree.getCellLineNumber(elementNumberCellLineNumber);
//				cellLineName = cellLineNumber2CellLineNameMap.get(cellLineNumber);
//
//				bufferedWriter.write(elementName + "_" + cellLineName + "\t" + it.value() + System.getProperty("line.separator"));
//			}

			//Close bufferedWriter
			bufferedWriter.close();

		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	// @todo ends



	public void closeBufferedWriterList(List<FileWriter> fileWriterList, List<BufferedWriter> bufferedWriterList) {
		BufferedWriter bufferedWriter = null;
		FileWriter fileWriter = null;

		try {
			for (int i = 0; i < bufferedWriterList.size(); i++) {
				bufferedWriter = bufferedWriterList.get(i);
				fileWriter = fileWriterList.get(i);
				bufferedWriter.close();
				fileWriter.close();
			}

		} catch (IOException e) {

			logger.error(e.toString());
		}
	}

	// Annotation
	// with Numbers
	public int[] searchDnaseWithNumbers(
			String dataFolder, 
			String outputFolder, 
			ChromosomeName chromName,
			List<Interval> dataArrayList,
			int overlapDefinition, 
			IntervalTree dnaseIntervalTree,
			int numberofComparisonsDnase,
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap,
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			ForkJoinPool pool) {

		
		FindOverlaps findOverlaps;
		int[] kArray = new int[numberofComparisonsDnase];

		
		findOverlaps = new FindOverlaps(outputFolder,
					chromName, 
					dataArrayList,
					writeElementBasedAnnotationFoundOverlapsMode,
					Commons.ZERO, 
					dataArrayList.size(),
					overlapDefinition,
					dnaseIntervalTree, 
					numberofComparisonsDnase,
					cellLineNumber2CellLineNameMap, 
					fileNumber2FileNameMap);
		
			
		kArray = pool.invoke(findOverlaps);
			
		//logger.info(chromName.convertEnumtoString() + "\t" + "dnaseCellLineNumber2KMap.size() after FindOverlaps call" + "\t" + dnaseCellLineNumber2KMap.size() );
		
		System.gc();
		System.runFinalization();
		
		return kArray;

		

	
	}

	// For Chen Yao
	// Hg19 RefSeq Gene
	// Annotation
	// with numbers
	public void searchGeneWithNumbers(
			String dataFolder, 
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			TIntObjectMap<String> givenIntervalNumber2GivenIntervalNameMap, 
			TIntObjectMap<OverlapInformation> givenIntervalNumber2OverlapInformationMap, 
			TObjectIntMap<ChromosomeName> chromosomeName2CountMap, 
			TIntIntMap entrezGeneId2KMap, 
			int overlapDefinition, 
			TIntObjectMap<String> officialGeneSymbolNumber2NameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap) {

		BufferedReader bufferedReader = null;

		IntervalTree ucscRefSeqGenesIntervalTree;

		// For each ChromosomeName
		for (ChromosomeName chrName : ChromosomeName.values()) {

			ucscRefSeqGenesIntervalTree = createUcscRefSeqGenesIntervalTreeWithNumbers(dataFolder, chrName);
			bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);

			searchGeneWithNumbers(outputFolder, writeElementBasedAnnotationFoundOverlapsMode, givenIntervalNumber2GivenIntervalNameMap, givenIntervalNumber2OverlapInformationMap, chromosomeName2CountMap, chrName, bufferedReader, ucscRefSeqGenesIntervalTree, entrezGeneId2KMap, overlapDefinition, officialGeneSymbolNumber2NameMap, refSeqGeneNumber2RefSeqGeneNameMap);

			emptyIntervalTree(ucscRefSeqGenesIntervalTree.getRoot());
			ucscRefSeqGenesIntervalTree = null;

			System.gc();
			System.runFinalization();

			try {
				bufferedReader.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}

		}// End of for each chromosomeName

	}

	// Annotation
	// KEGG Pathway
	// UserDefinedGeneSet
	// with numbers
	public void searchGeneSetWithNumbers(
			String dataFolder, 
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			TShortIntMap exonBasedGeneSetNumber2KMap, 
			TShortIntMap regulationBasedGeneSetNumber2KMap, 
			TShortIntMap allBasedGeneSetNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> geneSetNumber2GeneSetNameMap, 
			TIntObjectMap<TShortList> geneId2ListofGeneSetNumberMap, 
			TIntObjectMap<String> geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap, 
			String geneSetName, 
			GeneSetType geneSetType) {

		BufferedReader bufferedReader = null;

		IntervalTree ucscRefSeqGenesIntervalTree;


		// For each ChromosomeName
		for (ChromosomeName chrName : ChromosomeName.values()) {

			ucscRefSeqGenesIntervalTree = createUcscRefSeqGenesIntervalTreeWithNumbers(dataFolder, chrName);
			bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);

			searchGeneSetWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode,chrName, bufferedReader, ucscRefSeqGenesIntervalTree, exonBasedGeneSetNumber2KMap, regulationBasedGeneSetNumber2KMap, allBasedGeneSetNumber2KMap, overlapDefinition, geneSetNumber2GeneSetNameMap, geneId2ListofGeneSetNumberMap, geneHugoSymbolNumber2GeneHugoSymbolNameMap, refSeqGeneNumber2RefSeqGeneNameMap, geneSetName, geneSetType);

			emptyIntervalTree(ucscRefSeqGenesIntervalTree.getRoot());
			ucscRefSeqGenesIntervalTree = null;

			System.gc();
			System.runFinalization();

			try {
				bufferedReader.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}

		}// End of for each chromosomeName

	}

	// KEGGPathway annotation with Numbers ends

	// Annotation
	// With Numbers
	// TF KEGGPathway
	public void searchTfKEGGPathwayWithNumbers(
			String dataFolder,
			String outputFolder,
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			TIntIntMap tfNumberCellLineNumber2KMap, 
			TShortIntMap exonBasedKeggPathwayNumber2KMap, 
			TShortIntMap regulationBasedKeggPathwayNumber2KMap, 
			TShortIntMap allBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberExonBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberRegulationBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberAllBasedKeggPathwayNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> tfNumber2TfNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap, 
			TShortObjectMap<String> keggPathwayNumber2KeggPathwayNameMap, 
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap, 
			TIntObjectMap<String> geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap) {

		BufferedReader bufferedReader = null;

		IntervalTree tfbsIntervalTree;
		IntervalTree ucscRefSeqGenesIntervalTree;

		// For each ChromosomeName
		for (ChromosomeName chrName : ChromosomeName.values()) {

			tfbsIntervalTree = createTfbsIntervalTreeWithNumbers(dataFolder, chrName);
			ucscRefSeqGenesIntervalTree = createUcscRefSeqGenesIntervalTreeWithNumbers(dataFolder, chrName);
			bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);

			searchTfKEGGPathwayWithNumbers(
					outputFolder, 
					writeElementBasedAnnotationFoundOverlapsMode,
					chrName, 
					bufferedReader, 
					tfbsIntervalTree, 
					ucscRefSeqGenesIntervalTree, 
					tfNumberCellLineNumber2KMap, 
					exonBasedKeggPathwayNumber2KMap, 
					regulationBasedKeggPathwayNumber2KMap, 
					allBasedKeggPathwayNumber2KMap, 
					tfNumberExonBasedKeggPathwayNumber2KMap, 
					tfNumberRegulationBasedKeggPathwayNumber2KMap, 
					tfNumberAllBasedKeggPathwayNumber2KMap, 
					overlapDefinition, 
					tfNumber2TfNameMap, 
					cellLineNumber2CellLineNameMap, 
					fileNumber2FileNameMap, 
					keggPathwayNumber2KeggPathwayNameMap, 
					geneId2ListofKeggPathwayNumberMap, 
					geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
					refSeqGeneNumber2RefSeqGeneNameMap);

			emptyIntervalTree(tfbsIntervalTree.getRoot());
			tfbsIntervalTree = null;
			emptyIntervalTree(ucscRefSeqGenesIntervalTree.getRoot());
			ucscRefSeqGenesIntervalTree = null;

			System.gc();
			System.runFinalization();

			try {
				bufferedReader.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}

		}// End of for each chromosomeName

	}

	// Annotation
	// With Numbers
	// TF CELLLINE KEGGPATHWAY
	public void searchTfCellLineKEGGPathwayWithNumbers(
			String dataFolder, 
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			TIntIntMap tfNumberCellLineNumber2KMap, 
			TShortIntMap exonBasedKeggPathwayNumber2KMap, 
			TShortIntMap regulationBasedKeggPathwayNumber2KMap, 
			TShortIntMap allBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, 
			int overlapDefinition, TShortObjectMap<String> tfNumber2TfNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap, 
			TShortObjectMap<String> keggPathwayNumber2KeggPathwayNameMap, 
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap, 
			TIntObjectMap<String> geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap) {
		
		BufferedReader bufferedReader = null;

		IntervalTree tfbsIntervalTree;
		IntervalTree ucscRefSeqGenesIntervalTree;

		// For each ChromosomeName
		for (ChromosomeName chrName : ChromosomeName.values()) {

			tfbsIntervalTree = createTfbsIntervalTreeWithNumbers(dataFolder, chrName);
			ucscRefSeqGenesIntervalTree = createUcscRefSeqGenesIntervalTreeWithNumbers(dataFolder, chrName);
			bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);

			searchTfCellLineKEGGPathwayWithNumbers(
					outputFolder, 
					writeElementBasedAnnotationFoundOverlapsMode,
					chrName, 
					bufferedReader, 
					tfbsIntervalTree, 
					ucscRefSeqGenesIntervalTree, 
					tfNumberCellLineNumber2KMap, 
					exonBasedKeggPathwayNumber2KMap, 
					regulationBasedKeggPathwayNumber2KMap, 
					allBasedKeggPathwayNumber2KMap, 
					tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, 
					tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, 
					tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, 
					overlapDefinition, 
					tfNumber2TfNameMap, 
					cellLineNumber2CellLineNameMap, 
					fileNumber2FileNameMap, 
					keggPathwayNumber2KeggPathwayNameMap, 
					geneId2ListofKeggPathwayNumberMap, 
					geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
					refSeqGeneNumber2RefSeqGeneNameMap);

			emptyIntervalTree(tfbsIntervalTree.getRoot());
			tfbsIntervalTree = null;
			emptyIntervalTree(ucscRefSeqGenesIntervalTree.getRoot());
			ucscRefSeqGenesIntervalTree = null;

			System.gc();
			System.runFinalization();

			try {
				bufferedReader.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}

		}// End of for each chromosomeName
	}

	// Annotation
	// With Numbers
	// New Functionality START
	// TF CellLine KEGGPathway
	// TF KEGGPathway
	public void searchTfandKeggPathwayWithNumbers(
			String dataFolder, 
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap, 
			TIntIntMap tfNumberCellLineNumber2KMap, 
			TShortIntMap exonBasedKeggPathwayNumber2KMap, 
			TShortIntMap regulationBasedKeggPathwayNumber2KMap, 
			TShortIntMap allBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberExonBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberRegulationBasedKeggPathwayNumber2KMap, 
			TIntIntMap tfNumberAllBasedKeggPathwayNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> tfNumber2TfNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap, 
			TShortObjectMap<String> keggPathwayNumber2KeggPathwayNameMap, 
			TIntObjectMap<String> geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
			TIntObjectMap<String> refSeqGeneNumber2RefSeqGeneNameMap) {
		
		BufferedReader bufferedReader = null;

		IntervalTree tfbsIntervalTree;
		IntervalTree ucscRefSeqGenesIntervalTree;

		// For each ChromosomeName
		for (ChromosomeName chrName : ChromosomeName.values()) {

			tfbsIntervalTree = createTfbsIntervalTreeWithNumbers(dataFolder, chrName);
			ucscRefSeqGenesIntervalTree = createUcscRefSeqGenesIntervalTreeWithNumbers(dataFolder, chrName);
			bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);

			searchTfandKeggPathwayWithNumbers(
					outputFolder,
					writeElementBasedAnnotationFoundOverlapsMode,
					chrName, 
					bufferedReader, 
					tfbsIntervalTree, 
					ucscRefSeqGenesIntervalTree, 
					geneId2ListofKeggPathwayNumberMap, 
					tfNumberCellLineNumber2KMap, 
					exonBasedKeggPathwayNumber2KMap, 
					regulationBasedKeggPathwayNumber2KMap, 
					allBasedKeggPathwayNumber2KMap, 
					tfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, 
					tfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, 
					tfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, 
					tfNumberExonBasedKeggPathwayNumber2KMap, 
					tfNumberRegulationBasedKeggPathwayNumber2KMap, 
					tfNumberAllBasedKeggPathwayNumber2KMap, 
					overlapDefinition, 
					tfNumber2TfNameMap, 
					cellLineNumber2CellLineNameMap, 
					fileNumber2FileNameMap, 
					keggPathwayNumber2KeggPathwayNameMap, 
					geneHugoSymbolNumber2GeneHugoSymbolNameMap, 
					refSeqGeneNumber2RefSeqGeneNameMap);

			emptyIntervalTree(tfbsIntervalTree.getRoot());
			tfbsIntervalTree = null;
			emptyIntervalTree(ucscRefSeqGenesIntervalTree.getRoot());
			ucscRefSeqGenesIntervalTree = null;

			System.gc();
			System.runFinalization();

			try {
				bufferedReader.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}// End of for each chromosomeName

	}

	// @todo Annotation with Numbers ends

	// Annotation
	// UserDefinedLibrary
	// With Numbers
	public void searchUserDefinedLibraryWithNumbers(
			String dataFolder, 
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			int overlapDefinition, 
			TIntObjectMap<TIntIntMap> 
			elementTypeNumber2ElementNumber2KMapMap, 
			TIntObjectMap<TIntObjectMap<String>> elementTypeNumber2ElementNumber2ElementNameMapMap, 
			TIntObjectMap<String> elementTypeNumber2ElementTypeMap, 
			TIntObjectMap<String> fileNumber2FileNameMap) {

		int elementTypeNumber;
		String elementType;

		// Do Annotation With Numbers
		// For each elementTypeNumber
		for (TIntObjectIterator<String> it = elementTypeNumber2ElementTypeMap.iterator(); it.hasNext();) {
			it.advance();
			elementTypeNumber = it.key();
			elementType = it.value();

			BufferedReader bufferedReader = null;
			IntervalTree userDefinedLibraryIntervalTree;
			
			TIntIntMap elementNumber2KMap = elementTypeNumber2ElementNumber2KMapMap.get(elementTypeNumber);
			TIntObjectMap<String> elementNumber2ElementNameMap = elementTypeNumber2ElementNumber2ElementNameMapMap.get(elementTypeNumber);

			// For each ChromosomeName
			for (ChromosomeName chrName : ChromosomeName.values()) {

				userDefinedLibraryIntervalTree = createUserDefinedIntervalTreeWithNumbers(dataFolder, elementTypeNumber, elementType, chrName);
				bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);
				searchUserDefinedLibraryWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode, chrName, bufferedReader, userDefinedLibraryIntervalTree, elementNumber2KMap, overlapDefinition, elementType, elementNumber2ElementNameMap, fileNumber2FileNameMap);
				emptyIntervalTree(userDefinedLibraryIntervalTree.getRoot());
				userDefinedLibraryIntervalTree = null;

				System.gc();
				System.runFinalization();

				try {
					bufferedReader.close();
				} catch (IOException e) {

					logger.error(e.toString());
				}

			}// End of for each chromosomeName

		}// End of for each elementTypeNumber

	}

	// Annotation
	// With Numbers
	public void searchTranscriptionFactorWithNumbers(
			String dataFolder, 
			String outputFolder,
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode, 
			TIntIntMap tfNumberCellLineNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> tfNumber2TFNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap) {

		BufferedReader bufferedReader = null;

		IntervalTree transcriptionFactorIntervalTree;

		
		// For each ChromosomeName
		for (ChromosomeName chrName : ChromosomeName.values()) {

			transcriptionFactorIntervalTree = createTfbsIntervalTreeWithNumbers(dataFolder, chrName);
			bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);
			searchTranscriptionFactorWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode,chrName, bufferedReader, transcriptionFactorIntervalTree, tfNumberCellLineNumber2KMap, overlapDefinition, tfNumber2TFNameMap, cellLineNumber2CellLineNameMap, fileNumber2FileNameMap);
			emptyIntervalTree(transcriptionFactorIntervalTree.getRoot());
			transcriptionFactorIntervalTree = null;

			System.gc();
			System.runFinalization();

			try {
				bufferedReader.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}

		}// End of for each chromosomeName

	}

	// Annotation
	// With Numbers
	public void searchHistoneWithNumbers(
			String dataFolder, 
			String outputFolder, 
			WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode,
			TIntIntMap histoneNumberCellLineNumber2KMap, 
			int overlapDefinition, 
			TShortObjectMap<String> histoneNumber2HistoneNameMap, 
			TShortObjectMap<String> cellLineNumber2CellLineNameMap, 
			TShortObjectMap<String> fileNumber2FileNameMap) {

		BufferedReader bufferedReader = null;

		IntervalTree histoneIntervalTree;

		
		// For each ChromosomeName
		for (ChromosomeName chrName : ChromosomeName.values()) {

			histoneIntervalTree = createHistoneIntervalTreeWithNumbers(dataFolder, chrName);
			bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);
			searchHistoneWithNumbers(outputFolder,writeElementBasedAnnotationFoundOverlapsMode,chrName, bufferedReader, histoneIntervalTree, histoneNumberCellLineNumber2KMap, overlapDefinition, histoneNumber2HistoneNameMap, cellLineNumber2CellLineNameMap, fileNumber2FileNameMap);
			emptyIntervalTree(histoneIntervalTree.getRoot());
			histoneIntervalTree = null;

			System.gc();
			System.runFinalization();

			try {
				bufferedReader.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}

		}// End of for each chromosomeName

	}

	public void emptyIntervalTree(IntervalTreeNode node) {
		if (node != null) {
			emptyIntervalTree(node.getRight());
			emptyIntervalTree(node.getLeft());

			// if(node.getCellLineName()!=null){
			// node.setCellLineName(null);
			// }
			//
			// if(node.getFileName()!=null){
			// node.setFileName(null);
			// }
			//
			// if(node.getTfbsorHistoneName()!=null){
			// node.setTfbsorHistoneName(null);
			// }
			//
			// if(node.getGeneHugoSymbol()!=null){
			// node.setGeneHugoSymbol(null);
			// }
			//
			// if(node.getIntervalName()!=null){
			// node.setIntervalName(null);
			// }
			//
			// if(node.getGeneEntrezId()!=null){
			// node.setGeneEntrezId(null);
			// }
			//
			// if(node.getRefSeqGeneName()!=null){
			// node.setRefSeqGeneName(null);
			// }

			if (node.getChromName() != null) {
				node.setChromName(null);
			}

			if (node.getLeft() != null) {
				node.setLeft(null);
			}

			if (node.getRight() != null) {
				node.setRight(null);
			}

			if (node.getParent() != null) {
				node.setParent(null);
			}

			node = null;
		}
	}

	public static void writeOverlaps(TIntObjectMap<List<UcscRefSeqGeneIntervalTreeNodeWithNumbers>> geneId2OverlapListMap, int givenIntervalNumber, TIntIntMap givenIntervalNumber2NumberofGeneOverlapsMap, GeneOverlapAnalysisFileMode geneOverlapAnalysisFileMode, IntervalName intervalName, NumberofGeneOverlaps geneOverlaps, BufferedWriter bufferedWriter, TIntObjectMap<String> geneHugoSymbolNumber2NameMap) throws IOException {

		int overlapCount = 0;
		String overlapStringInformation = "";

		List<UcscRefSeqGeneIntervalTreeNodeWithNumbers> overlapList = null;
		UcscRefSeqGeneIntervalTreeNodeWithNumbers overlapNode = null;

		// Accumulate overlapCount and overlapStringInformation for each geneID
		// For each geneId
		// Iterate Over Map
		for (TIntObjectIterator<List<UcscRefSeqGeneIntervalTreeNodeWithNumbers>> it1 = geneId2OverlapListMap.iterator(); it1.hasNext();) {
			it1.advance();

			overlapList = it1.value();

			overlapCount = overlapCount + overlapList.size();

			// For each overlap
			// Iterate Over List
			for (Iterator<UcscRefSeqGeneIntervalTreeNodeWithNumbers> it2 = overlapList.iterator(); it2.hasNext();) {

				overlapNode = it2.next();

				switch (overlapNode.getIntervalName()) {

					// Write IntervalNumber
					case EXON:
					case INTRON:
						overlapStringInformation = overlapStringInformation + "[" + overlapNode.getGeneEntrezId() + "_" + geneHugoSymbolNumber2NameMap.get(overlapNode.getGeneHugoSymbolNumber()) + "_" + overlapNode.getIntervalName().convertEnumtoString() + "_" + overlapNode.getIntervalNumber() + "] ";

						break;

					// Do Not Write IntervalNumber
					case FIVE_P_ONE:
					case FIVE_P_TWO:
					case FIVE_D:
					case THREE_P_ONE:
					case THREE_P_TWO:
					case THREE_D:
						overlapStringInformation = overlapStringInformation + "[" + overlapNode.getGeneEntrezId() + "_" + geneHugoSymbolNumber2NameMap.get(overlapNode.getGeneHugoSymbolNumber()) + "_" + overlapNode.getIntervalName().convertEnumtoString() + "] ";

						break;

				}// End of Switch

			}// End of For: each Overlap

		}// End of For: each geneID

		// Accumulate
		givenIntervalNumber2NumberofGeneOverlapsMap.put(givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap.get(givenIntervalNumber) + overlapCount);

		// Accumulate
		switch (intervalName) {

			case EXON:
				geneOverlaps.setNumberofExonOverlaps(geneOverlaps.getNumberofExonOverlaps() + overlapCount);
				break;

			case INTRON:
				geneOverlaps.setNumberofIntronOverlaps(geneOverlaps.getNumberofIntronOverlaps() + overlapCount);
				break;

			case FIVE_P_ONE:
				geneOverlaps.setNumberof5p1Overlaps(geneOverlaps.getNumberof5p1Overlaps() + overlapCount);
				break;

			case FIVE_P_TWO:
				geneOverlaps.setNumberof5p2Overlaps(geneOverlaps.getNumberof5p2Overlaps() + overlapCount);
				break;

			case FIVE_D:
				geneOverlaps.setNumberof5dOverlaps(geneOverlaps.getNumberof5dOverlaps() + overlapCount);
				break;

			case THREE_P_ONE:
				geneOverlaps.setNumberof3p1Overlaps(geneOverlaps.getNumberof3p1Overlaps() + overlapCount);
				break;

			case THREE_P_TWO:
				geneOverlaps.setNumberof3p2Overlaps(geneOverlaps.getNumberof3p2Overlaps() + overlapCount);
				break;

			case THREE_D:
				geneOverlaps.setNumberof3dOverlaps(geneOverlaps.getNumberof3dOverlaps() + overlapCount);
				break;

		}

		switch (geneOverlapAnalysisFileMode) {
			case WITH_OVERLAP_INFORMATION:
				bufferedWriter.write(overlapCount + "\t" + overlapStringInformation + "\t");
				break;
			case WITHOUT_OVERLAP_INFORMATION:
				bufferedWriter.write(overlapCount + "\t");
				break;
		}// End of switch

	}

	public static void writeChromosomeDistribution(TObjectIntMap<ChromosomeName> chromosomeName2CountMap, BufferedWriter bufferedWriter) throws IOException {

		bufferedWriter.write(System.getProperty("line.separator"));
		bufferedWriter.write(System.getProperty("line.separator"));

		int[] counts = chromosomeName2CountMap.values();
		int totalCount = 0;
		int count = 0;
		double percentage = 0;
		double totalPercentage = 0;

		for (int i = 0; i < counts.length; i++) {
			totalCount = totalCount + counts[i];
		}

		// Write Header Line
		bufferedWriter.write("" + "\t" + "Count" + "\t" + "Percentage" + System.getProperty("line.separator"));

		for (ChromosomeName chrName : ChromosomeName.values()) {

			count = chromosomeName2CountMap.get(chrName);
			percentage = (count * 100.0) / totalCount;

			totalPercentage = totalPercentage + percentage;

			bufferedWriter.write(chrName.convertEnumtoString() + "\t" + count + "\t" + percentage + "%" + System.getProperty("line.separator"));

		}// For each chromosomeName

		// Write Last Line
		bufferedWriter.write("Total" + "\t" + totalCount + "\t" + totalPercentage + "%" + System.getProperty("line.separator"));

	}

	public static void writeGeneOverlapAnalysisFile(String outputFolder, String outputFileName, GeneOverlapAnalysisFileMode geneOverlapAnalysisFileMode, TIntObjectMap<String> givenIntervalNumber2GivenIntervalNameMap, TIntObjectMap<OverlapInformation> givenIntervalNumber2OverlapInformationMap, TIntIntMap givenIntervalNumber2NumberofGeneOverlapsMap, TObjectIntMap<ChromosomeName> chromosomeName2CountMap, TIntObjectMap<String> geneHugoSymbolNumber2NameMap) {

		BufferedWriter bufferedWriter = null;

		int givenIntervalNumber = 0;
		String givenIntervalName = null;
		OverlapInformation overlapInformation = null;

		TIntObjectMap<List<UcscRefSeqGeneIntervalTreeNodeWithNumbers>> geneId2OverlapListMap = null;

		NumberofGeneOverlaps geneOverlaps = new NumberofGeneOverlaps();

		try {
			bufferedWriter = new BufferedWriter(FileOperations.createFileWriter(outputFolder + outputFileName));

			// Write Header Line
			switch (geneOverlapAnalysisFileMode) {

				case WITH_OVERLAP_INFORMATION:
					bufferedWriter.write("GivenIntervalNumber" + "\t" + "GivenInteval" + "\t" + "#ofExonOverlaps" + "\t" + "ExonOverlapsInformation" + "\t" + "#ofIntronOverlaps" + "\t" + "IntronOverlapsInformation" + "\t" + "#of5p1Overlaps" + "\t" + "5p1OverlapsInformation" + "\t" + "#of5p2Overlaps" + "\t" + "5p2OverlapsInformation" + "\t" + "#of5dOverlaps" + "\t" + "5dOverlapsInformation" + "\t" + "#of3p1Overlaps" + "\t" + "3p1OverlapsInformation" + "\t" + "#of3p2Overlaps" + "\t" + "3p2OverlapsInformation" + "\t" + "#of3dOverlaps" + "\t" + "3dOverlapsInformation" + "\t" + "#ofGeneOverlapsPerGivenInterval" + System.getProperty("line.separator"));
					break;

				case WITHOUT_OVERLAP_INFORMATION:
					bufferedWriter.write("GivenIntervalNumber" + "\t" + "GivenInteval" + "\t" + "#ofExonOverlaps" + "\t" + "#ofIntronOverlaps" + "\t" + "#of5p1Overlaps" + "\t" + "#of5p2Overlaps" + "\t" + "#of5dOverlaps" + "\t" + "#of3p1Overlaps" + "\t" + "#of3p2Overlaps" + "\t" + "#of3dOverlaps" + "\t" + "#ofGeneOverlapsPerGivenInterval" + System.getProperty("line.separator"));
					break;
			}// End of SWITCH

			// For Each GivenInterval
			for (int i = 1; i <= givenIntervalNumber2GivenIntervalNameMap.size(); i++) {

				givenIntervalNumber = i;

				givenIntervalName = givenIntervalNumber2GivenIntervalNameMap.get(givenIntervalNumber);

				overlapInformation = givenIntervalNumber2OverlapInformationMap.get(givenIntervalNumber);

				bufferedWriter.write(givenIntervalNumber + "\t" + givenIntervalName + "\t");

				if (overlapInformation != null) {

					givenIntervalNumber2NumberofGeneOverlapsMap.put(givenIntervalNumber, 0);

					/***************************************************************/
					/*********** geneId 2 ExonOverlapsList Map starts ****************/
					/***************************************************************/
					// geneId2ExonOverlapsListMap
					geneId2OverlapListMap = overlapInformation.getGeneId2ExonOverlapListMap();

					// Write Number of Exon Overlaps
					writeOverlaps(geneId2OverlapListMap, givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap, geneOverlapAnalysisFileMode, IntervalName.EXON, geneOverlaps, bufferedWriter, geneHugoSymbolNumber2NameMap);
					/***************************************************************/
					/*********** geneId 2 ExonOverlapsList Map ends ******************/
					/***************************************************************/

					/***************************************************************/
					/*********** geneId 2 IntronOverlapsList Map starts **************/
					/***************************************************************/
					// geneId2IntronOverlapsListMap
					geneId2OverlapListMap = overlapInformation.getGeneId2IntronOverlapListMap();

					// Write Number of Intron Overlaps
					writeOverlaps(geneId2OverlapListMap, givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap, geneOverlapAnalysisFileMode, IntervalName.INTRON, geneOverlaps, bufferedWriter, geneHugoSymbolNumber2NameMap);
					/***************************************************************/
					/*********** geneId 2 IntronOverlapsList Map ends ****************/
					/***************************************************************/

					/***************************************************************/
					/*********** geneId 2 Fivep1OverlapsList Map starts **************/
					/***************************************************************/
					geneId2OverlapListMap = overlapInformation.getGeneId2Fivep1OverlapListMap();

					writeOverlaps(geneId2OverlapListMap, givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap, geneOverlapAnalysisFileMode, IntervalName.FIVE_P_ONE, geneOverlaps, bufferedWriter, geneHugoSymbolNumber2NameMap);
					/***************************************************************/
					/*********** geneId 2 Fivep1OverlapsList Map ends ****************/
					/***************************************************************/

					/***************************************************************/
					/*********** geneId 2 Fivep2OverlapsList Map starts **************/
					/***************************************************************/
					geneId2OverlapListMap = overlapInformation.getGeneId2Fivep2OverlapListMap();

					writeOverlaps(geneId2OverlapListMap, givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap, geneOverlapAnalysisFileMode, IntervalName.FIVE_P_TWO, geneOverlaps, bufferedWriter, geneHugoSymbolNumber2NameMap);
					/***************************************************************/
					/*********** geneId 2 Fivep2OverlapsList Map ends ****************/
					/***************************************************************/

					/***************************************************************/
					/*********** geneId 2 FivedOverlapsList Map starts **************/
					/***************************************************************/
					geneId2OverlapListMap = overlapInformation.getGeneId2FivedOverlapListMap();

					writeOverlaps(geneId2OverlapListMap, givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap, geneOverlapAnalysisFileMode, IntervalName.FIVE_D, geneOverlaps, bufferedWriter, geneHugoSymbolNumber2NameMap);
					/***************************************************************/
					/*********** geneId 2 FivedOverlapsList Map ends ****************/
					/***************************************************************/

					/***************************************************************/
					/*********** geneId 2 Threep1OverlapsList Map starts *************/
					/***************************************************************/
					geneId2OverlapListMap = overlapInformation.getGeneId2Threep1OverlapListMap();

					writeOverlaps(geneId2OverlapListMap, givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap, geneOverlapAnalysisFileMode, IntervalName.THREE_P_ONE, geneOverlaps, bufferedWriter, geneHugoSymbolNumber2NameMap);
					/***************************************************************/
					/*********** geneId 2 Threep1OverlapsList Map ends ***************/
					/***************************************************************/

					/***************************************************************/
					/*********** geneId 2 Threep2OverlapsList Map starts *************/
					/***************************************************************/
					geneId2OverlapListMap = overlapInformation.getGeneId2Threep2OverlapListMap();

					writeOverlaps(geneId2OverlapListMap, givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap, geneOverlapAnalysisFileMode, IntervalName.THREE_P_TWO, geneOverlaps, bufferedWriter, geneHugoSymbolNumber2NameMap);
					/***************************************************************/
					/*********** geneId 2 Threep2OverlapsList Map ends ***************/
					/***************************************************************/

					/***************************************************************/
					/*********** geneId 2 ThreedOverlapsList Map starts **************/
					/***************************************************************/
					geneId2OverlapListMap = overlapInformation.getGeneId2ThreedOverlapListMap();

					writeOverlaps(geneId2OverlapListMap, givenIntervalNumber, givenIntervalNumber2NumberofGeneOverlapsMap, geneOverlapAnalysisFileMode, IntervalName.THREE_D, geneOverlaps, bufferedWriter, geneHugoSymbolNumber2NameMap);
					/***************************************************************/
					/*********** geneId 2 ThreedOverlapsList Map ends ****************/
					/***************************************************************/

					bufferedWriter.write("" + givenIntervalNumber2NumberofGeneOverlapsMap.get(givenIntervalNumber));

				}// End of IF: overlapInformation is not null
				else {
					// For this given interval there is no overlap
					// OverlapInformation is null

					switch (geneOverlapAnalysisFileMode) {
						case WITH_OVERLAP_INFORMATION:
							bufferedWriter.write("0" + "\t" + "" + "\t" + "0" + "\t" + "" + "\t" + "0" + "\t" + "" + "\t" + "0" + "\t" + "" + "\t" + "0" + "\t" + "" + "\t" + "0" + "\t" + "" + "\t" + "0" + "\t" + "" + "\t" + "0" + "\t" + "" + "\t" + "0");
							break;

						case WITHOUT_OVERLAP_INFORMATION:
							bufferedWriter.write("0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0");
							break;
					}// End of switch
				}

				// Go to next line for next given interval
				bufferedWriter.write(System.getProperty("line.separator"));

			}// End of FOR: each given interval

			// Do calculations
			geneOverlaps.calculate();

			// Write Last Lines
			// Write Number of Gene Overlaps
			switch (geneOverlapAnalysisFileMode) {

				case WITH_OVERLAP_INFORMATION:
					bufferedWriter.write("Number of Overlaps" + "\t" + "" + "\t" + geneOverlaps.getNumberofExonOverlaps() + "\t" + "" + "\t" + geneOverlaps.getNumberofIntronOverlaps() + "\t" + "" + "\t" + geneOverlaps.getNumberof5p1Overlaps() + "\t" + "" + "\t" + geneOverlaps.getNumberof5p2Overlaps() + "\t" + "" + "\t" + geneOverlaps.getNumberof5dOverlaps() + "\t" + "" + "\t" + geneOverlaps.getNumberof3p1Overlaps() + "\t" + "" + "\t" + geneOverlaps.getNumberof3p2Overlaps() + "\t" + "" + "\t" + geneOverlaps.getNumberof3dOverlaps() + "\t" + "" + "\t" + geneOverlaps.getTotalNumberofOverlaps() + System.getProperty("line.separator"));

					bufferedWriter.write("" + "\t" + "" + "\t" + geneOverlaps.getExonPercentage() + "%" + "\t" + "" + "\t" + geneOverlaps.getIntronPercentage() + "%" + "\t" + "" + "\t" + geneOverlaps.getFivep1Percentage() + "%" + "\t" + "" + "\t" + geneOverlaps.getFivep2Percentage() + "%" + "\t" + "" + "\t" + geneOverlaps.getFivedPercentage() + "%" + "\t" + "" + "\t" + geneOverlaps.getThreep1Percentage() + "%" + "\t" + "" + "\t" + geneOverlaps.getThreep2Percentage() + "%" + "\t" + "" + "\t" + geneOverlaps.getThreedPercentage() + "%" + "\t" + "" + "\t" + geneOverlaps.getAllPercentage() + "%" + System.getProperty("line.separator"));

					break;

				case WITHOUT_OVERLAP_INFORMATION:
					bufferedWriter.write("Number of Overlaps" + "\t" + "" + "\t" + geneOverlaps.getNumberofExonOverlaps() + "\t" + geneOverlaps.getNumberofIntronOverlaps() + "\t" + geneOverlaps.getNumberof5p1Overlaps() + "\t" + geneOverlaps.getNumberof5p2Overlaps() + "\t" + geneOverlaps.getNumberof5dOverlaps() + "\t" + geneOverlaps.getNumberof3p1Overlaps() + "\t" + geneOverlaps.getNumberof3p2Overlaps() + "\t" + geneOverlaps.getNumberof3dOverlaps() + "\t" + geneOverlaps.getTotalNumberofOverlaps() + System.getProperty("line.separator"));

					bufferedWriter.write("" + "\t" + "" + "\t" + geneOverlaps.getExonPercentage() + "%" + "\t" + geneOverlaps.getIntronPercentage() + "%" + "\t" + geneOverlaps.getFivep1Percentage() + "%" + "\t" + geneOverlaps.getFivep2Percentage() + "%" + "\t" + geneOverlaps.getFivedPercentage() + "%" + "\t" + geneOverlaps.getThreep1Percentage() + "%" + "\t" + geneOverlaps.getThreep2Percentage() + "%" + "\t" + geneOverlaps.getThreedPercentage() + "%" + "\t" + geneOverlaps.getAllPercentage() + "%" + System.getProperty("line.separator"));

					break;

			}// End of switch

			// Write ChromosomeName 2 NumberofGivenIntervals and Percentage
			writeChromosomeDistribution(chromosomeName2CountMap, bufferedWriter);

			// Close
			bufferedWriter.close();

		} catch (IOException e) {

			logger.error(e.toString());
		}

	}
	
	
	public static void readDataIntoArray(BufferedReader bufferedReader, List<Interval> dataArrayList){
		
		String strLine;
		int indexofFirstTab;
		int indexofSecondTab;
		int low;
		int high;
		
		
		
		try {
			
			while((strLine = bufferedReader.readLine())!=null) {
				
				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = (indexofFirstTab>=0) ? strLine.indexOf('\t', indexofFirstTab + 1) : -1;

			
				// indexofSecondTab must be greater than zero if it exists since
				// indexofFirstTab must exists and can be at least zero
				// therefore indexofSecondTab can be at least one.
				if (indexofSecondTab > 0){
					low = Integer.parseInt(strLine.substring(indexofFirstTab + 1, indexofSecondTab));
					high = Integer.parseInt(strLine.substring(indexofSecondTab + 1));
				}
				
				else{
					low = Integer.parseInt(strLine.substring(indexofFirstTab + 1));
					high = low;
				}
					

				Interval interval = new Interval(low, high);
				dataArrayList.add(interval);
				
			}//End of while
			
		} catch (IOException e) {
			logger.error(e.toString());
		}
		
	}
	

	// //Empirical P Value Calculation
	// //args[0] must have input file name with folder
	// //args[1] must have GLANET output folder
	// //args[2] must have GLANET data folder (necessary data for annotation and
	// augmentation)
	// //args[3] must have Input File Format
	// public AllName2KMaps annotateOriginalData(String[] args){
	//
	// String outputFolder = args[1];
	// String dataFolder = args[2];
	// int overlapDefinition = Integer.parseInt(args[3]);
	//
	// String inputFileName = outputFolder +
	// Commons.REMOVED_OVERLAPS_INPUT_FILE;
	//
	// AllName2KMaps allName2KMaps = new AllName2KMaps();
	//
	// //Prepare chromosome based partitioned input interval files to be
	// searched for
	// List<BufferedWriter> bufferedWriterList = new
	// ArrayList<BufferedWriter>();
	// //Create Buffered Writers for writing chromosome based input files
	// createChromBaseSeachInputFiles(outputFolder,bufferedWriterList);
	//
	// //Partition the input file into 24 chromosome based input files
	// partitionSearchInputFilePerChromName(inputFileName,bufferedWriterList);
	//
	// //Close Buffered Writers
	// closeBufferedWriterList(bufferedWriterList);
	//
	// //DNASE
	// //Search input interval files for dnase
	// List<String> dnaseCellLineNameList = new ArrayList<String>();
	// //This dnaseCellLine2KMap hash map will contain the dnase cell line name
	// to number of dnase cell line:k for the given search input size:n
	// Map<String,Integer> dnaseCellLine2KMap = new HashMap<String,Integer>();
	//
	// fillList(dnaseCellLineNameList,dataFolder ,
	// Commons.WRITE_ALL_POSSIBLE_NAMES_OUTPUT_DIRECTORYNAME +
	// Commons.WRITE_ALL_POSSIBLE_ENCODE_CELL_LINE_NAMES_OUTPUT_FILENAME);
	// searchDnase(dataFolder,outputFolder,dnaseCellLineNameList,dnaseCellLine2KMap,overlapDefinition);
	// writeResults(dnaseCellLine2KMap, outputFolder,
	// Commons.ANNOTATE_INTERVALS_DNASE_RESULTS_GIVEN_SEARCH_INPUT);
	// allName2KMaps.setDnaseCellLineName2NumberofOverlapsMap(dnaseCellLine2KMap);
	//
	// //TFBS
	// //Search input interval files for tfbs
	// List<String> tfbsNameList = new ArrayList<String>();
	// //This tfbsNameandCellLineName2KMap hash map will contain the
	// tfbsNameandCellLineName to number of tfbsNameandCellLineName: k for the
	// given search input size: n
	// Map<String,Integer> tfbsNameandCellLineName2KMap = new
	// HashMap<String,Integer>();
	//
	// fillList(tfbsNameList,dataFolder ,
	// Commons.WRITE_ALL_POSSIBLE_NAMES_OUTPUT_DIRECTORYNAME +
	// Commons.WRITE_ALL_POSSIBLE_ENCODE_TF_NAMES_OUTPUT_FILENAME);
	// searchTfbs(dataFolder,outputFolder,tfbsNameList,tfbsNameandCellLineName2KMap);
	// writeResults(tfbsNameandCellLineName2KMap, outputFolder,
	// Commons.ANNOTATE_INTERVALS_TF_RESULTS_GIVEN_SEARCH_INPUT);
	// allName2KMaps.setTfbsNameandCellLineName2NumberofOverlapsMap(tfbsNameandCellLineName2KMap);
	//
	// //HISTONE
	// //Search input interval files for histone
	// List<String> histoneNameList = new ArrayList<String>();
	// //This histoneNameandCellLineName2KMap hash map will contain the
	// histoneNameandCellLineName to number of histoneNameandCellLineName: k for
	// the given search input size: n
	// Map<String,Integer> histoneNameandCellLineName2KMap = new
	// HashMap<String,Integer>();
	//
	// fillList(histoneNameList,dataFolder
	// ,Commons.WRITE_ALL_POSSIBLE_NAMES_OUTPUT_DIRECTORYNAME +
	// Commons.WRITE_ALL_POSSIBLE_ENCODE_HISTONE_NAMES_OUTPUT_FILENAME);
	// searchHistone(dataFolder,outputFolder,histoneNameList,histoneNameandCellLineName2KMap,overlapDefinition);
	// writeResults(histoneNameandCellLineName2KMap, outputFolder,
	// Commons.ANNOTATE_INTERVALS_HISTONE_RESULTS_GIVEN_SEARCH_INPUT);
	// allName2KMaps.setHistoneNameandCellLineName2NumberofOverlapsMap(histoneNameandCellLineName2KMap);
	//
	//
	// //KEGG PATHWAY
	// //Search input interval files for kegg Pathway
	// List<String> keggPathwayNameList = new ArrayList<String>();
	//
	// //Fill keggPathwayNameList
	// fillList(keggPathwayNameList,dataFolder ,
	// Commons.WRITE_ALL_POSSIBLE_NAMES_OUTPUT_DIRECTORYNAME +
	// Commons.WRITE_ALL_POSSIBLE_KEGG_PATHWAY_NAMES_OUTPUT_FILENAME);
	//
	// Map<String,List<String>> geneId2KeggPathwayMap = new HashMap<String,
	// List<String>>();
	// KeggPathwayUtility.createNcbiGeneId2KeggPathwayMap(dataFolder,
	// Commons.KEGG_PATHWAY_2_NCBI_GENE_IDS_INPUT_FILE, geneId2KeggPathwayMap);
	//
	// //Exon Based Kegg Pathway Analysis
	// //This exonBasedKeggPathway2KMap hash map will contain the kegg pathway
	// name to number of kegg pathway:k for the given search input size:n
	// Map<String,Integer> exonBasedKeggPathway2KMap = new
	// HashMap<String,Integer>();
	//
	// searchKeggPathway(dataFolder,outputFolder,geneId2KeggPathwayMap,keggPathwayNameList,
	// exonBasedKeggPathway2KMap, Commons.EXON_BASED_KEGG_PATHWAY_ANALYSIS);
	// writeResults(exonBasedKeggPathway2KMap,
	// outputFolder,Commons.ANNOTATE_INTERVALS_EXON_BASED_KEGG_PATHWAY_RESULTS_GIVEN_SEARCH_INPUT);
	// allName2KMaps.setExonBasedKeggPathway2NumberofOverlapsMap(exonBasedKeggPathway2KMap);
	//
	//
	// //Regulation Based Kegg Pathway Analysis
	// //This regulationBasedKeggPathway2KMap hash map will contain the kegg
	// pathway name to number of kegg pathway:k for the given search input
	// size:n
	// Map<String,Integer> regulationBasedKeggPathway2KMap = new
	// HashMap<String,Integer>();
	//
	// searchKeggPathway(dataFolder,outputFolder,geneId2KeggPathwayMap,keggPathwayNameList,
	// regulationBasedKeggPathway2KMap,
	// Commons.REGULATION_BASED_KEGG_PATHWAY_ANALYSIS);
	// writeResults(regulationBasedKeggPathway2KMap, outputFolder ,
	// Commons.ANNOTATE_INTERVALS_REGULATION_BASED_KEGG_PATHWAY_RESULTS_GIVEN_SEARCH_INPUT);
	// allName2KMaps.setRegulationBasedKeggPathway2NumberofOverlapsMap(regulationBasedKeggPathway2KMap);
	//
	// //All results Kegg Pathway Analysis
	// //This regulationBasedKeggPathway2KMap hash map will contain the kegg
	// pathway name to number of kegg pathway:k for the given search input
	// size:n
	// Map<String,Integer> allResultsKeggPathway2KMap = new
	// HashMap<String,Integer>();
	//
	// searchKeggPathway(dataFolder,outputFolder,
	// geneId2KeggPathwayMap,keggPathwayNameList, allResultsKeggPathway2KMap,
	// Commons.ALL_BASED_KEGG_PATHWAY_ANALYSIS);
	// writeResults(allResultsKeggPathway2KMap, outputFolder ,
	// Commons.ANNOTATE_INTERVALS_ALL_BASED_KEGG_PATHWAY_RESULTS_GIVEN_SEARCH_INPUT);
	//
	// return allName2KMaps;
	// }

	// args[0] ---> Input File Name with folder
	// args[1] ---> GLANET installation folder with "\\" at the end. This folder
	// will be used for outputFolder and dataFolder.
	// args[2] ---> Input File Format
	// ---> default
	// Commons.INPUT_FILE_FORMAT_1_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// --->
	// Commons.INPUT_FILE_FORMAT_0_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// --->
	// Commons.INPUT_FILE_FORMAT_DBSNP_IDS_0_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// --->
	// Commons.INPUT_FILE_FORMAT_BED_0_BASED_COORDINATES_START_INCLUSIVE_END_EXCLUSIVE
	// --->
	// Commons.INPUT_FILE_FORMAT_GFF3_1_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// args[3] ---> Annotation, overlap definition, number of bases,
	// default 1
	// args[4] ---> Perform Enrichment parameter
	// ---> default Commons.DO_ENRICH
	// ---> Commons.DO_NOT_ENRICH
	// args[5] ---> Generate Random Data Mode
	// ---> default Commons.GENERATE_RANDOM_DATA_WITH_MAPPABILITY_AND_GC_CONTENT
	// ---> Commons.GENERATE_RANDOM_DATA_WITHOUT_MAPPABILITY_AND_GC_CONTENT
	// args[6] ---> multiple testing parameter, enriched elements will be
	// decided and sorted with respect to this parameter
	// ---> default Commons.BENJAMINI_HOCHBERG_FDR
	// ---> Commons.BONFERRONI_CORRECTION
	// args[7] ---> Bonferroni Correction Significance Level, default 0.05
	// args[8] ---> Bonferroni Correction Significance Criteria, default 0.05
	// args[9] ---> Number of permutations, default 5000
	// args[10] ---> Dnase Enrichment
	// ---> default Commons.DO_NOT_DNASE_ENRICHMENT
	// ---> Commons.DO_DNASE_ENRICHMENT
	// args[11] ---> Histone Enrichment
	// ---> default Commons.DO_NOT_HISTONE_ENRICHMENT
	// ---> Commons.DO_HISTONE_ENRICHMENT
	// args[12] ---> Transcription Factor(TF) Enrichment
	// ---> default Commons.DO_NOT_TF_ENRICHMENT
	// ---> Commons.DO_TF_ENRICHMENT
	// args[13] ---> KEGG Pathway Enrichment
	// ---> default Commons.DO_NOT_KEGGPATHWAY_ENRICHMENT
	// ---> Commons.DO_KEGGPATHWAY_ENRICHMENT
	// args[14] ---> TF and KEGG Pathway Enrichment
	// ---> default Commons.DO_NOT_TF_KEGGPATHWAY_ENRICHMENT
	// ---> Commons.DO_TF_KEGGPATHWAY_ENRICHMENT
	// args[15] ---> TF and CellLine and KeggPathway Enrichment
	// ---> default Commons.DO_NOT_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT
	// ---> Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT
	// args[16] ---> RSAT parameter
	// ---> default Commons.DO_NOT_REGULATORY_SEQUENCE_ANALYSIS_USING_RSAT
	// ---> Commons.DO_REGULATORY_SEQUENCE_ANALYSIS_USING_RSAT
	// args[17] ---> job name example: ocd_gwas_snps
	// args[18] ---> writeGeneratedRandomDataMode checkBox
	// ---> default Commons.DO_NOT_WRITE_GENERATED_RANDOM_DATA
	// ---> Commons.WRITE_GENERATED_RANDOM_DATA
	// args[19] ---> writePermutationBasedandParametricBasedAnnotationResultMode
	// checkBox
	// ---> default
	// Commons.DO_NOT_WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT
	// --->
	// Commons.WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT
	// args[20] ---> writePermutationBasedAnnotationResultMode checkBox
	// ---> default Commons.DO_NOT_WRITE_PERMUTATION_BASED_ANNOTATION_RESULT
	// ---> Commons.WRITE_PERMUTATION_BASED_ANNOTATION_RESULT
	// args[21] ---> number of permutations in each run. Default is 2000
	// args[22] ---> UserDefinedGeneSet Enrichment
	// default Commons.DO_NOT_USER_DEFINED_GENESET_ENRICHMENT
	// Commons.DO_USER_DEFINED_GENESET_ENRICHMENT
	// args[23] ---> UserDefinedGeneSet InputFile
	// args[24] ---> UserDefinedGeneSet GeneInformationType
	// default Commons.GENE_ID
	// Commons.GENE_SYMBOL
	// Commons.RNA_NUCLEOTIDE_ACCESSION
	// args[25] ---> UserDefinedGeneSet Name
	// args[26] ---> UserDefinedGeneSet Optional GeneSet Description InputFile
	// args[27] ---> UserDefinedLibrary Enrichment
	// default Commons.DO_NOT_USER_DEFINED_LIBRARY_ENRICHMENT
	// Commons.DO_USER_DEFINED_LIBRARY_ENRICHMENT
	// args[28] ---> UserDefinedLibrary InputFile
	// args[29] ---> User Defined Library DataFormat
	// default
	// Commons.USERDEFINEDLIBRARY_DATAFORMAT_0_BASED_COORDINATES_START_INCLUSIVE_END_EXCLUSIVE
	// Commons.USERDEFINEDLIBRARY_DATAFORMAT_0_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// Commons.USERDEFINEDLIBRARY_DATAFORMAT_1_BASED_COORDINATES_START_INCLUSIVE_END_EXCLUSIVE
	// Commons.USERDEFINEDLIBRARY_DATAFORMAT_1_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// args[29] - args[args.length-1] ---> Note that the selected cell lines are
	// always inserted at the end of the args array because it's size
	// is not fixed. So for not (until the next change on args array) the
	// selected cell
	// lines can be reached starting from 22th index up until (args.length-1)th
	// index.
	// If no cell line selected so the args.length-1 will be 22-1 = 21. So it
	// will never
	// give an out of boundry exception in a for loop with this approach.
	public void annotate(String[] args) {

		String glanetFolder = args[CommandLineArguments.GlanetFolder.value()];

		// jobName starts
		String jobName = args[CommandLineArguments.JobName.value()].trim();
		if (jobName.isEmpty()) {
			jobName = Commons.NO_NAME;
		}
		// jobName ends

		String dataFolder = glanetFolder + Commons.DATA + System.getProperty("file.separator");
		String outputFolder = glanetFolder + Commons.OUTPUT + System.getProperty("file.separator") + jobName + System.getProperty("file.separator");
		String givenInputDataFolder = outputFolder + Commons.GIVENINPUTDATA + System.getProperty("file.separator");
	
		
		WriteElementBasedAnnotationFoundOverlapsMode writeElementBasedAnnotationFoundOverlapsMode = WriteElementBasedAnnotationFoundOverlapsMode.convertStringtoEnum(args[CommandLineArguments.WriteElementBasedAnnotationFoundOverlapsMode.value()]);
		
	
		
		/***********************************************************************************/
		/************************** USER DEFINED GENESET ***********************************/
		// User Defined GeneSet Enrichment, DO or DO_NOT
		AnnotationType userDefinedGeneSetAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.UserDefinedGeneSetAnnotation.value()]);

		String userDefinedGeneSetInputFile = args[CommandLineArguments.UserDefinedGeneSetInput.value()];

		GeneInformationType geneInformationType = GeneInformationType.convertStringtoEnum(args[CommandLineArguments.UserDefinedGeneSetGeneInformation.value()]);

		String userDefinedGeneSetName = args[CommandLineArguments.UserDefinedGeneSetName.value()];
		/************************** USER DEFINED GENESET ***********************************/
		/***********************************************************************************/

		
		
		/***********************************************************************************/
		/************************** USER DEFINED LIBRARY ***********************************/
		// User Defined Library Enrichment, DO or DO_NOT
		AnnotationType userDefinedLibraryAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.UserDefinedLibraryAnnotation.value()]);
	
		String userDefinedLibraryInputFile = args[CommandLineArguments.UserDefinedLibraryInput.value()];
	
		UserDefinedLibraryDataFormat userDefinedLibraryDataFormat = UserDefinedLibraryDataFormat.convertStringtoEnum(args[CommandLineArguments.UserDefinedLibraryDataFormat.value()]);
		/************************** USER DEFINED LIBRARY ***********************************/
		/***********************************************************************************/

		int overlapDefinition = Integer.parseInt(args[CommandLineArguments.NumberOfBasesRequiredForOverlap.value()]);

		String inputFileName;

		// ENRICHMENT choice will be used as ANNOTATION choice
		// Dnase Enrichment, DO or DO_NOT
		AnnotationType dnaseAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.DnaseAnnotation.value()]);

		// Histone Enrichment, DO or DO_NOT
		AnnotationType histoneAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.HistoneAnnotation.value()]);

		// Transcription Factor Enrichment, DO or DO_NOT
		AnnotationType tfAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.TfAnnotation.value()]);

		// Gene Annotation
		AnnotationType geneAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.GeneAnnotation.value()]);

		// KEGG Pathway Enrichment, DO or DO_NOT
		AnnotationType keggPathwayAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.KeggPathwayAnnotation.value()]);

		// TfKeggPathway Enrichment, DO or DO_NOT
		AnnotationType tfKeggPathwayAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.TfAndKeggPathwayAnnotation.value()]);

		// TfCellLineKeggPathway Enrichment, DO or DO_NOT
		AnnotationType tfCellLineKeggPathwayAnnotationType = AnnotationType.convertStringtoEnum(args[CommandLineArguments.CellLineBasedTfAndKeggPathwayAnnotation.value()]);

		
		
		/**********************************************************************/
		/*********** delete old files starts **********************************/
		String annotateOutputBaseDirectoryName = outputFolder + Commons.ANNOTATION;

		FileOperations.deleteOldFiles(annotateOutputBaseDirectoryName);
		/*********** delete old files ends ************************************/
		/**********************************************************************/

		
		
		
		/*****************************************************************************************/
		/************************* GIVEN INPUT DATA starts ***************************************/
		inputFileName = givenInputDataFolder + Commons.REMOVED_OVERLAPS_INPUT_FILE_0BASED_START_END_GRCh37_p13;

		List<FileWriter> fileWriterList = new ArrayList<FileWriter>();

		// Prepare chromosome based partitioned input interval files to be
		// searched for
		List<BufferedWriter> bufferedWriterList = new ArrayList<BufferedWriter>();
		// Create Buffered Writers for writing chromosome based input files
		FileOperations.createChromBaseSearchInputFiles(outputFolder, fileWriterList, bufferedWriterList);

		// Partition the input file into 24 chromosome based input files
		partitionSearchInputFilePerChromName(inputFileName, bufferedWriterList);

		// Close Buffered Writers
		closeBufferedWriterList(fileWriterList, bufferedWriterList);
		/************************* GIVEN INPUT DATA ends *****************************************/
		/*****************************************************************************************/

		
		
		/********************************************************************************************************/
		/*************** FILL NUMBER 2 NAME MAPS*****starts *****************************************************/
		/********************************************************************************************************/
		TShortObjectMap<String> dnaseCellLineNumber2NameMap = new TShortObjectHashMap<String>();
		TShortObjectMap<String> cellLineNumber2NameMap = new TShortObjectHashMap<String>();
		TShortObjectMap<String> fileNumber2NameMap = new TShortObjectHashMap<String>();
		TShortObjectMap<String> histoneNumber2NameMap = new TShortObjectHashMap<String>();
		TShortObjectMap<String> tfNumber2NameMap = new TShortObjectHashMap<String>();
		TShortObjectMap<String> keggPathwayNumber2NameMap = new TShortObjectHashMap<String>();

		TIntObjectMap<String> geneHugoSymbolNumber2NameMap = new TIntObjectHashMap<String>();
		TIntObjectMap<String> geneEntrezId2GeneOfficialSymbolMap = new TIntObjectHashMap<String>();
		TIntObjectMap<String> refSeqRNANucleotideAccessionNumber2NameMap = new TIntObjectHashMap<String>();

		FileOperations.fillNumber2NameMap(dnaseCellLineNumber2NameMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_ENCODE_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_ENCODE_DNASE_CELLLINE_NUMBER_2_NAME_OUTPUT_FILENAME);
		FileOperations.fillNumber2NameMap(cellLineNumber2NameMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_ENCODE_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_ENCODE_CELLLINE_NUMBER_2_NAME_OUTPUT_FILENAME);
		FileOperations.fillNumber2NameMap(fileNumber2NameMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_ENCODE_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_ENCODE_FILE_NUMBER_2_NAME_OUTPUT_FILENAME);
		FileOperations.fillNumber2NameMap(histoneNumber2NameMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_ENCODE_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_ENCODE_HISTONE_NUMBER_2_NAME_OUTPUT_FILENAME);
		FileOperations.fillNumber2NameMap(tfNumber2NameMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_ENCODE_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_ENCODE_TF_NUMBER_2_NAME_OUTPUT_FILENAME);
		FileOperations.fillNumber2NameMap(keggPathwayNumber2NameMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_KEGGPATHWAY_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_KEGGPATHWAY_NUMBER_2_NAME_OUTPUT_FILENAME);
		FileOperations.fillNumber2NameMap(geneHugoSymbolNumber2NameMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_UCSCGENOME_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_UCSCGENOME_HG19_REFSEQ_GENES_GENESYMBOL_NUMBER_2_NAME_OUTPUT_FILENAME);
		FileOperations.fillNumber2NameMap(refSeqRNANucleotideAccessionNumber2NameMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_UCSCGENOME_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_UCSCGENOME_HG19_REFSEQ_GENES_RNANUCLEOTIDEACCESSION_NUMBER_2_NAME_OUTPUT_FILENAME);
		
		HumanGenesAugmentation.fillGeneId2GeneHugoSymbolMap(dataFolder, geneEntrezId2GeneOfficialSymbolMap);
		/********************************************************************************************************/
		/*************** FILL NUMBER 2 NAME MAPS*****ends *******************************************************/
		/********************************************************************************************************/
		
		
		/********************************************************/
		/*************** FILL NAME 2 NUMBER MAPS******starts ****/
		/********************************************************/
		TObjectShortMap<String> keggPathwayName2NumberMap = new TObjectShortHashMap<String>();

		FileOperations.fillName2NumberMap(keggPathwayName2NumberMap, dataFolder, Commons.ALL_POSSIBLE_NAMES_KEGGPATHWAY_OUTPUT_DIRECTORYNAME + Commons.ALL_POSSIBLE_KEGGPATHWAY_NAME_2_NUMBER_OUTPUT_FILENAME);
		/********************************************************/
		/*************** FILL NAME 2 NUMBER MAPS*****ends *******/
		/********************************************************/
		
	
	
		// if you want to see the current year and day etc. change the line of
		// code below with:
		// DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		long dateBefore = Long.MIN_VALUE;
		long dateAfter = Long.MIN_VALUE;
		// Not needed
		// Instant
		// dnaseStart,histoneStart,transcriptionFactorStart,KEGGPathwayStart,tfKEGGPathwayStart,tfCellLineKEGGPathwayStart,tfCellLineKEGGPathway_and_TFKEGGPathwayStart;
		// Instant
		// dnaseEnd,histoneEnd,transcriptionFactorEnd,KEGGPathwayEnd,tfKEGGPathwayEnd,tfCellLineKEGGPathwayEnd,tfCellLineKEGGPathway_and_TFKEGGPathwayEnd;

		/**************************************************************************************/
		/********************Concurrent Programming Initizalization starts*********************/
		/**************************************************************************************/
		ForkJoinPool pool = new ForkJoinPool(Commons.NUMBER_OF_AVAILABLE_PROCESSORS);
		
		int[] allChromosomesDnaseCellLineKArray = null;
		int numberofComparisonsDnase = 0;
		
		int[] allChromosomesTFCellLineKEGGPathwayKArray = null;
		int numberofComparisonsTFCellLineKEGGPathway = 0;
		
		
		if (dnaseAnnotationType.doDnaseAnnotation()) {
			
			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("CellLine Based DNASE annotation starts: " + new Date());
			dateBefore = System.currentTimeMillis();
			
			numberofComparisonsDnase = NumberofComparisons.getNumberofComparisonsforBonferroniCorrection(dataFolder,dnaseAnnotationType);
		
			allChromosomesDnaseCellLineKArray = new int[numberofComparisonsDnase];
			
		}
		
		
		if(tfCellLineKeggPathwayAnnotationType.doTFCellLineKEGGPathwayAnnotation()){
			
			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("CellLine Based TF KEGG Pathway annotation starts: " + new Date());
			dateBefore = System.currentTimeMillis();
			
			numberofComparisonsTFCellLineKEGGPathway = NumberofComparisons.getNumberofComparisonsforBonferroniCorrection(dataFolder,tfCellLineKeggPathwayAnnotationType);
		
			
			allChromosomesTFCellLineKEGGPathwayKArray = new int[numberofComparisonsTFCellLineKEGGPathway];
		}
		/**************************************************************************************/
		/********************Concurrent Programming Initizalization ends***********************/
		/**************************************************************************************/
			
			

		// For each ChromosomeName
		for (ChromosomeName chrName : ChromosomeName.values()) {
			
			
			List<Interval> dataArrayList =new ArrayList<Interval>();
			BufferedReader bufferedReader = FileOperations.createBufferedReader(outputFolder, Commons.ANNOTATE_CHROMOSOME_BASED_INPUT_FILE_DIRECTORY + ChromosomeName.convertEnumtoString(chrName) + Commons.CHROMOSOME_BASED_GIVEN_INPUT);
			
		
			readDataIntoArray(bufferedReader,dataArrayList);
			
			
			/*******************************************************************************/
			/************ DNASE**ANNOTATION****starts **************************************/
			/*******************************************************************************/
			if (dnaseAnnotationType.doDnaseAnnotation()) {
				
				// DNASE
				int[] dnaseCellLineKArray = null;
				IntervalTree dnaseIntervalTree = createDnaseIntervalTreeWithNumbers(dataFolder, chrName);
			

			
				//logger.info(chrName.convertEnumtoString() + "\t" + "number of given interval" + "\t" + dataArrayList.size());
				
				dnaseCellLineKArray = searchDnaseWithNumbers(
						dataFolder,
						outputFolder,
						chrName,
						dataArrayList,
						overlapDefinition,
						dnaseIntervalTree,
						numberofComparisonsDnase,
						cellLineNumber2NameMap,
						fileNumber2NameMap,
						writeElementBasedAnnotationFoundOverlapsMode,
						pool);
						
				
				//logger.info("Check It" + "\t" + chrName.convertEnumtoString() + "\t" + "dnaseCellLineNumber2KMap.size()" + "\t" + dnaseCellLineNumber2KMap.size());
				
				Accumulation.accumulate(dnaseCellLineKArray,allChromosomesDnaseCellLineKArray);
				
			
				dnaseCellLineKArray = null;
				emptyIntervalTree(dnaseIntervalTree.getRoot());
				dnaseIntervalTree = null;

				System.gc();
				System.runFinalization();

				
				

			}
			/*******************************************************************************/
			/************ DNASE***ANNOTATION********ends ***********************************/
			/*******************************************************************************/
			
		}//End of for each Chromosome

			
		
		

		/**************************************************************************************/
		/********************Concurrent Programming Finalization starts************************/
		/**************************************************************************************/
		
		pool.shutdown();
		
		if (dnaseAnnotationType.doDnaseAnnotation()) {
			
			writeResultsWithNumbers(allChromosomesDnaseCellLineKArray, dnaseCellLineNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_DNASE);

			dateAfter = System.currentTimeMillis();
			GlanetRunner.appendLog("CellLine Based DNASE annotation ends: " + new Date());
			GlanetRunner.appendLog("CellLine Based Dnase annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			
			allChromosomesDnaseCellLineKArray = null;
			System.gc();
			System.runFinalization();

			
		}
		/**************************************************************************************/
		/********************Concurrent Programming Finalization ends**************************/
		/**************************************************************************************/
	
		
		
		
		
		
		
		/*******************************************************************************/
		/************ HISTONE****ANNOTATION***starts ***********************************/
		/*******************************************************************************/
		if (histoneAnnotationType.doHistoneAnnotation()) {
			
			// Histone
			TIntIntMap histoneNumberCellLineNumber2KMap = new TIntIntHashMap();

			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("CellLine Based Histone annotation starts: " + new Date());

			dateBefore = System.currentTimeMillis();
			searchHistoneWithNumbers(dataFolder, outputFolder, writeElementBasedAnnotationFoundOverlapsMode,histoneNumberCellLineNumber2KMap, overlapDefinition, histoneNumber2NameMap, cellLineNumber2NameMap, fileNumber2NameMap);
			writeResultsWithNumbers(histoneNumberCellLineNumber2KMap, histoneNumber2NameMap, cellLineNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_HISTONE);
			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("CellLine Based Histone annotation ends: " + new Date());

			GlanetRunner.appendLog("CellLine Based Histone annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			histoneNumberCellLineNumber2KMap = null;
			
			System.gc();
			System.runFinalization();

		}
		/*******************************************************************************/
		/************ HISTONE*****ANNOTATION***ends ************************************/
		/*******************************************************************************/

		
		
		
		
		/*******************************************************************************/
		/************ TF******ANNOTATION******starts ***********************************/
		/*******************************************************************************/
		if (tfAnnotationType.doTFAnnotation() && 
				!(tfKeggPathwayAnnotationType.doTFKEGGPathwayAnnotation()) && 
				!(tfCellLineKeggPathwayAnnotationType.doTFCellLineKEGGPathwayAnnotation())) {
			
			// TF
			TIntIntMap tfNumberCellLineNumber2KMap = new TIntIntHashMap();


			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("CellLine Based TF annotation starts: " + new Date());

			dateBefore = System.currentTimeMillis();
			searchTranscriptionFactorWithNumbers(dataFolder, outputFolder, writeElementBasedAnnotationFoundOverlapsMode, tfNumberCellLineNumber2KMap, overlapDefinition, tfNumber2NameMap, cellLineNumber2NameMap, fileNumber2NameMap);
			writeResultsWithNumbers(tfNumberCellLineNumber2KMap, tfNumber2NameMap, cellLineNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF);
			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("CellLine Based TF annotation ends: " + new Date());

			GlanetRunner.appendLog("CellLine Based TF annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			tfNumberCellLineNumber2KMap = null;
			
			System.gc();
			System.runFinalization();
		}
		/*******************************************************************************/
		/************ TF*******ANNOTATION*****ends *************************************/
		/*******************************************************************************/

		
		
		/*******************************************************************************/
		/************* HG19 Refseq GENE*****ANNOTATION***starts ************************/
		/*******************************************************************************/
		/***************** This has been coded for Chen Yao ****************************/
		/*******************************************************************************/
		if (geneAnnotationType.doGeneAnnotation()){
			
			// Hg19 RefSeq Genes
			// For Encode Collaboration Chen Yao Paper
			TIntIntMap entrezGeneId2KMap = new TIntIntHashMap();

			
			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("Hg19 RefSeq Gene annotation starts: " + new Date());
			dateBefore = System.currentTimeMillis();

			// 10 February 2015
			TIntObjectMap<String> givenIntervalNumber2GivenIntervalNameMap = new TIntObjectHashMap<String>();
			TIntObjectMap<OverlapInformation> givenIntervalNumber2OverlapInformationMap = new TIntObjectHashMap<OverlapInformation>();
			TIntIntMap givenIntervalNumber2NumberofGeneOverlapsMap = new TIntIntHashMap();

			// 13 February 2015
			TObjectIntMap<ChromosomeName> chromosomeName2CountMap = new TObjectIntHashMap<ChromosomeName>();

			searchGeneWithNumbers(dataFolder,outputFolder,writeElementBasedAnnotationFoundOverlapsMode,givenIntervalNumber2GivenIntervalNameMap, givenIntervalNumber2OverlapInformationMap, chromosomeName2CountMap, entrezGeneId2KMap, overlapDefinition, geneHugoSymbolNumber2NameMap, refSeqRNANucleotideAccessionNumber2NameMap);

			GeneOverlapAnalysisFileMode geneOverlapAnalysisFileMode = GeneOverlapAnalysisFileMode.WITH_OVERLAP_INFORMATION;

			writeGeneOverlapAnalysisFile(outputFolder, Commons.HG19_REFSEQ_GENE_ANNOTATION_DIRECTORY + Commons.OVERLAP_ANALYSIS_FILE, geneOverlapAnalysisFileMode, givenIntervalNumber2GivenIntervalNameMap, givenIntervalNumber2OverlapInformationMap, givenIntervalNumber2NumberofGeneOverlapsMap, chromosomeName2CountMap, geneHugoSymbolNumber2NameMap);

			//@todo
			writeResultsWithNumbers(entrezGeneId2KMap, geneEntrezId2GeneOfficialSymbolMap, outputFolder, Commons.ANNOTATE_INTERVALS_GENE_ALTERNATE_NAME_RESULTS_GIVEN_SEARCH_INPUT);
			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("Hg19 RefSeq Gene annotation ends: " + new Date());

			GlanetRunner.appendLog("Hg19 RefSeq Gene annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			
			entrezGeneId2KMap = null;
			
			System.gc();
			System.runFinalization();

		}
		/*******************************************************************************/
		/***************** This is done by default for Chen Yao ************************/
		/*******************************************************************************/
		/************* HG19 RefSeq GENE*****ANNOTATION***ends **************************/
		/*******************************************************************************/
		
		
		
		/*******************************************************************************/
		/************ KEGG PATHWAY*****ANNOTATION***starts *****************************/
		/*******************************************************************************/
		if (keggPathwayAnnotationType.doKEGGPathwayAnnotation() && 
				!(tfKeggPathwayAnnotationType.doTFKEGGPathwayAnnotation()) && 
				!(tfCellLineKeggPathwayAnnotationType.doTFCellLineKEGGPathwayAnnotation())) {
			
			// KEGGPathway
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap = new TIntObjectHashMap<TShortList>();
			KeggPathwayUtility.createNcbiGeneId2ListofKeggPathwayNumberMap(
					dataFolder,
					Commons.KEGG_PATHWAY_2_NCBI_GENE_IDS_INPUT_FILE, 
					keggPathwayName2NumberMap, 
					geneId2ListofKeggPathwayNumberMap);

			TShortIntMap exonBasedKeggPathway2KMap = new TShortIntHashMap();
			TShortIntMap regulationBasedKeggPathway2KMap = new TShortIntHashMap();
			TShortIntMap allBasedKeggPathway2KMap = new TShortIntHashMap();


			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("KEGG Pathway annotation starts: " + new Date());

			dateBefore = System.currentTimeMillis();
			searchGeneSetWithNumbers(dataFolder, outputFolder, writeElementBasedAnnotationFoundOverlapsMode, exonBasedKeggPathway2KMap, regulationBasedKeggPathway2KMap, allBasedKeggPathway2KMap, overlapDefinition, keggPathwayNumber2NameMap, geneId2ListofKeggPathwayNumberMap, geneHugoSymbolNumber2NameMap, refSeqRNANucleotideAccessionNumber2NameMap, Commons.KEGG_PATHWAY, GeneSetType.KEGGPATHWAY);

			writeResultsWithNumbers(exonBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_EXON_BASED_KEGGPATHWAY_FILE);
			writeResultsWithNumbers(regulationBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_REGULATION_BASED_KEGGPATHWAY_FILE);
			writeResultsWithNumbers(allBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_ALL_BASED_KEGGPATHWAY_FILE);

			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("KEGG Pathway annotation ends: " + new Date());

			GlanetRunner.appendLog("KEGG Pathway annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			
			exonBasedKeggPathway2KMap = null;
			regulationBasedKeggPathway2KMap = null;
			allBasedKeggPathway2KMap = null;

			System.gc();
			System.runFinalization();

		}
		/*******************************************************************************/
		/************ KEGG PATHWAY****ANNOTATION*ends **********************************/
		/*******************************************************************************/

		
		
		/*******************************************************************************/
		/************ USER DEFINED GENESET*****ANNOTATION***starts *********************/
		/*******************************************************************************/
		if (userDefinedGeneSetAnnotationType.doUserDefinedGeneSetAnnotation()) {
			
			// UserDefinedGeneSet
			TShortIntMap exonBasedUserDefinedGeneSet2KMap = new TShortIntHashMap();
			TShortIntMap regulationBasedUserDefinedGeneSet2KMap = new TShortIntHashMap();
			TShortIntMap allBasedUserDefinedGeneSet2KMap = new TShortIntHashMap();


			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("User Defined GeneSet annotation starts: " + new Date());

			dateBefore = System.currentTimeMillis();

			// used in write results
			TShortObjectMap<String> userDefinedGeneSetNumber2UserDefinedGeneSetNameMap = new TShortObjectHashMap<String>();
			// used in filling geneId2ListofUserDefinedGeneSetNumberMap
			TObjectShortMap<String> userDefinedGeneSetName2UserDefinedGeneSetNumberMap = new TObjectShortHashMap<String>();

			// User Defined GeneSet
			// Fill userDefinedGeneSetName2UserDefinedGeneSetNumber
			// Fill userDefinedGeneSetNumber2UserDefinedGeneSetName files
			// Fill geneId2ListofUserDefinedGeneSetNumberMap
			TIntObjectMap<TShortList> geneId2ListofUserDefinedGeneSetNumberMap = new TIntObjectHashMap<TShortList>();
			UserDefinedGeneSetUtility.createNcbiGeneId2ListofUserDefinedGeneSetNumberMap(dataFolder, userDefinedGeneSetInputFile, geneInformationType, userDefinedGeneSetName2UserDefinedGeneSetNumberMap, userDefinedGeneSetNumber2UserDefinedGeneSetNameMap, geneId2ListofUserDefinedGeneSetNumberMap);

			WriteAllPossibleNames.writeAllPossibleUserDefinedGeneSetNames(dataFolder, userDefinedGeneSetName2UserDefinedGeneSetNumberMap, userDefinedGeneSetNumber2UserDefinedGeneSetNameMap);

			searchGeneSetWithNumbers(dataFolder, outputFolder, writeElementBasedAnnotationFoundOverlapsMode,exonBasedUserDefinedGeneSet2KMap, regulationBasedUserDefinedGeneSet2KMap, allBasedUserDefinedGeneSet2KMap, overlapDefinition, userDefinedGeneSetNumber2UserDefinedGeneSetNameMap, geneId2ListofUserDefinedGeneSetNumberMap, geneHugoSymbolNumber2NameMap, refSeqRNANucleotideAccessionNumber2NameMap, userDefinedGeneSetName, GeneSetType.USERDEFINEDGENESET);

			writeResultsWithNumbers(exonBasedUserDefinedGeneSet2KMap, userDefinedGeneSetNumber2UserDefinedGeneSetNameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_USERDEFINEDGENESET_DIRECTORY + userDefinedGeneSetName + System.getProperty("file.separator") + userDefinedGeneSetName + Commons.ANNOTATION_RESULTS_FOR_EXON_BASED_USERDEFINEDGENESET_FILE);
			writeResultsWithNumbers(regulationBasedUserDefinedGeneSet2KMap, userDefinedGeneSetNumber2UserDefinedGeneSetNameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_USERDEFINEDGENESET_DIRECTORY + userDefinedGeneSetName + System.getProperty("file.separator") + userDefinedGeneSetName + Commons.ANNOTATION_RESULTS_FOR_REGULATION_BASED_USERDEFINEDGENESET_FILE);
			writeResultsWithNumbers(allBasedUserDefinedGeneSet2KMap, userDefinedGeneSetNumber2UserDefinedGeneSetNameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_USERDEFINEDGENESET_DIRECTORY + userDefinedGeneSetName + System.getProperty("file.separator") + userDefinedGeneSetName + Commons.ANNOTATION_RESULTS_FOR_ALL_BASED_USERDEFINEDGENESET_FILE);

			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("User Defined GeneSet annotation ends: " + new Date());

			GlanetRunner.appendLog("User Defined GeneSet annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			
			exonBasedUserDefinedGeneSet2KMap = null;
			regulationBasedUserDefinedGeneSet2KMap = null;
			allBasedUserDefinedGeneSet2KMap = null;

			System.gc();
			System.runFinalization();

		}
		/*******************************************************************************/
		/************ USER DEFINED GENESET*****ANNOTATION***ends ***********************/
		/*******************************************************************************/

		/*******************************************************************************/
		/************ USER DEFINED LIBRARY*****ANNOTATION***starts *********************/
		/*******************************************************************************/
		if (userDefinedLibraryAnnotationType.doUserDefinedLibraryAnnotation()) {

			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("User Defined Library Annotation starts: " + new Date());

			dateBefore = System.currentTimeMillis();

			// used in write results
			TObjectIntMap<String> userDefinedLibraryElementType2ElementTypeNumberMap = new TObjectIntHashMap<String>();
			TIntObjectMap<String> userDefinedLibraryElementTypeNumber2ElementTypeMap = new TIntObjectHashMap<String>();

			// This has to be ElementType Specific for Bonferroni Correction
			TIntObjectMap<TObjectIntMap<String>> elementTypeNumber2ElementName2ElementNumberMapMap = new TIntObjectHashMap<TObjectIntMap<String>>();
			TIntObjectMap<TIntObjectMap<String>> elementTypeNumber2ElementNumber2ElementNameMapMap = new TIntObjectHashMap<TIntObjectMap<String>>();

			TObjectIntMap<String> userDefinedLibraryFileName2FileNumberMap = new TObjectIntHashMap<String>();
			TIntObjectMap<String> userDefinedLibraryFileNumber2FileNameMap = new TIntObjectHashMap<String>();

			// UserDefinedLibrary
			// Read UserDefinedLibraryInputFile
			// Read each file written in UserDefinedLibraryInputFile
			// FileName FileNumber
			// Fill userDefinedLibraryFileName2UserDefinedLibraryFileNumber Map
			// Fill userDefinedLibraryFileNumber2UserDefinedLibraryFileName Map
			// ElementType ElementTypeNumber
			// Fill userDefinedLibraryElementType2ElementTypeNumber Map
			// Fill userDefinedLibraryElementTypeNumber2ElementType Map
			// Fill ElementTypeNumber specific ElementName2ElementNumber
			// Fill ElementTypeNumber specific ElementNumber2ElementName
			// For each elementType
			// Create UserDefinedLibrary unsorted chromosome based interval
			// files with numbers

			UserDefinedLibraryUtility.readUserDefinedLibraryInputFileCreateUnsortedChromosomeBasedFilesWithNumbersFillMapsWriteMaps(dataFolder, userDefinedLibraryInputFile, userDefinedLibraryDataFormat, userDefinedLibraryElementType2ElementTypeNumberMap, userDefinedLibraryElementTypeNumber2ElementTypeMap, elementTypeNumber2ElementName2ElementNumberMapMap, elementTypeNumber2ElementNumber2ElementNameMapMap, userDefinedLibraryFileName2FileNumberMap, userDefinedLibraryFileNumber2FileNameMap);

			TIntObjectMap<TIntIntMap> elementTypeNumber2ElementNumber2KMapMap = new TIntObjectHashMap<TIntIntMap>();

			// For each elementTypeNumber
			// Initialize elementNumber2KMap
			for (TIntObjectIterator<String> it = userDefinedLibraryElementTypeNumber2ElementTypeMap.iterator(); it.hasNext();) {
				it.advance();
				TIntIntMap map = new TIntIntHashMap();
				elementTypeNumber2ElementNumber2KMapMap.put(it.key(), map);
			}// End of for each elementTypeNumber initialize elementNumber2KMap

			searchUserDefinedLibraryWithNumbers(dataFolder,outputFolder,writeElementBasedAnnotationFoundOverlapsMode,overlapDefinition, elementTypeNumber2ElementNumber2KMapMap, elementTypeNumber2ElementNumber2ElementNameMapMap, userDefinedLibraryElementTypeNumber2ElementTypeMap, userDefinedLibraryFileNumber2FileNameMap);

			// UserDefinedLibrary
			writeResultsWithNumbers(userDefinedLibraryElementTypeNumber2ElementTypeMap, elementTypeNumber2ElementNumber2KMapMap, elementTypeNumber2ElementNumber2ElementNameMapMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_USERDEFINEDLIBRARY_DIRECTORY, Commons.ANNOTATION_RESULTS_FOR_USERDEFINEDLIBRARY_FILE);

			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("User Defined Library annotation ends: " + new Date());

			GlanetRunner.appendLog("User Defined Library annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			userDefinedLibraryElementType2ElementTypeNumberMap = null;
			userDefinedLibraryElementTypeNumber2ElementTypeMap = null;
			
			elementTypeNumber2ElementName2ElementNumberMapMap = null;
			elementTypeNumber2ElementNumber2ElementNameMapMap = null;
			
			userDefinedLibraryFileName2FileNumberMap = null;
			userDefinedLibraryFileNumber2FileNameMap = null;
			
			System.gc();
			System.runFinalization();

		}
		/*******************************************************************************/
		/************ USER DEFINED LIBRARY*****ANNOTATION***ends ***********************/
		/*******************************************************************************/

		
		
		/*******************************************************************************/
		/************ TF KEGGPATHWAY***ANNOTATION*****starts ***************************/
		/*******************************************************************************/
		if (tfKeggPathwayAnnotationType.doTFKEGGPathwayAnnotation() && 
				!(tfCellLineKeggPathwayAnnotationType.doTFCellLineKEGGPathwayAnnotation())) {
			
			// KEGGPathway
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap = new TIntObjectHashMap<TShortList>();
			KeggPathwayUtility.createNcbiGeneId2ListofKeggPathwayNumberMap(
					dataFolder,
					Commons.KEGG_PATHWAY_2_NCBI_GENE_IDS_INPUT_FILE, 
					keggPathwayName2NumberMap, 
					geneId2ListofKeggPathwayNumberMap);
			
			//TF
			TIntIntMap tfNumberCellLineNumber2KMap = new TIntIntHashMap();
			
			//KEGG Pathway
			TShortIntMap exonBasedKeggPathway2KMap = new TShortIntHashMap();
			TShortIntMap regulationBasedKeggPathway2KMap = new TShortIntHashMap();
			TShortIntMap allBasedKeggPathway2KMap = new TShortIntHashMap();
			
			//TF KEGGPathway
			TIntIntMap tfExonBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap tfRegulationBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap tfAllBasedKeggPathway2KMap = new TIntIntHashMap();


			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("TF KEGGPathway annotation starts: " + new Date());

			dateBefore = System.currentTimeMillis();
			
			searchTfKEGGPathwayWithNumbers(dataFolder, outputFolder, writeElementBasedAnnotationFoundOverlapsMode,tfNumberCellLineNumber2KMap, exonBasedKeggPathway2KMap, regulationBasedKeggPathway2KMap, allBasedKeggPathway2KMap, tfExonBasedKeggPathway2KMap, tfRegulationBasedKeggPathway2KMap, tfAllBasedKeggPathway2KMap, overlapDefinition, tfNumber2NameMap, cellLineNumber2NameMap, fileNumber2NameMap, keggPathwayNumber2NameMap, geneId2ListofKeggPathwayNumberMap, geneHugoSymbolNumber2NameMap, refSeqRNANucleotideAccessionNumber2NameMap);

			// TF
			writeResultsWithNumbers(tfNumberCellLineNumber2KMap, tfNumber2NameMap, cellLineNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF);

			// KEGGPathway
			writeResultsWithNumbers(exonBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_EXON_BASED_KEGGPATHWAY_FILE);
			writeResultsWithNumbers(regulationBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_REGULATION_BASED_KEGGPATHWAY_FILE);
			writeResultsWithNumbers(allBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_ALL_BASED_KEGGPATHWAY_FILE);

			// TF KEGGPathway
			writeTFKEGGPathwayResultsWithNumbers(tfExonBasedKeggPathway2KMap, tfNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_EXON_BASED_KEGG_PATHWAY);
			writeTFKEGGPathwayResultsWithNumbers(tfRegulationBasedKeggPathway2KMap, tfNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_REGULATION_BASED_KEGG_PATHWAY);
			writeTFKEGGPathwayResultsWithNumbers(tfAllBasedKeggPathway2KMap, tfNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_ALL_BASED_KEGG_PATHWAY);
			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("TF KEGGPathway annotation ends: " + new Date());
			GlanetRunner.appendLog("TF KEGGPathway annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			//null
			//TF
			tfNumberCellLineNumber2KMap = null;
			
			//KEGG Pathway
			exonBasedKeggPathway2KMap = null;
			regulationBasedKeggPathway2KMap = null;
			allBasedKeggPathway2KMap = null;
			
			//TF KEGG Pathway
			tfExonBasedKeggPathway2KMap = null;
			tfRegulationBasedKeggPathway2KMap = null;
			tfAllBasedKeggPathway2KMap = null;

			geneId2ListofKeggPathwayNumberMap = null;

			
			System.gc();
			System.runFinalization();

		}
		/*******************************************************************************/
		/************ TF KEGGPATHWAY*****ANNOTATION***ends *****************************/
		/*******************************************************************************/

		
		
		/*******************************************************************************/
		/************ TF CELLLINE KEGGPATHWAY***ANNOTATION*****starts ******************/
		/*******************************************************************************/
		if (tfCellLineKeggPathwayAnnotationType.doTFCellLineKEGGPathwayAnnotation() && 
				!(tfKeggPathwayAnnotationType.doTFKEGGPathwayAnnotation())) {
			
			// KEGGPathway
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap = new TIntObjectHashMap<TShortList>();
			KeggPathwayUtility.createNcbiGeneId2ListofKeggPathwayNumberMap(
					dataFolder,
					Commons.KEGG_PATHWAY_2_NCBI_GENE_IDS_INPUT_FILE, 
					keggPathwayName2NumberMap, 
					geneId2ListofKeggPathwayNumberMap);
			
			//TF
			TIntIntMap tfNumberCellLineNumber2KMap = new TIntIntHashMap();
			
			//KEGG Pathway
			TShortIntMap exonBasedKeggPathway2KMap = new TShortIntHashMap();
			TShortIntMap regulationBasedKeggPathway2KMap = new TShortIntHashMap();
			TShortIntMap allBasedKeggPathway2KMap = new TShortIntHashMap();
			
			// TF CellLine KEGGPathway
			TIntIntMap tfCellLineExonBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap tfCellLineRegulationBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap tfCellLineAllBasedKeggPathway2KMap = new TIntIntHashMap();

			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("CellLine Based TF KEGGPATHWAY annotation starts: " + new Date());

			dateBefore = System.currentTimeMillis();
			
			searchTfCellLineKEGGPathwayWithNumbers(dataFolder, outputFolder, writeElementBasedAnnotationFoundOverlapsMode,tfNumberCellLineNumber2KMap, exonBasedKeggPathway2KMap, regulationBasedKeggPathway2KMap, allBasedKeggPathway2KMap, tfCellLineExonBasedKeggPathway2KMap, tfCellLineRegulationBasedKeggPathway2KMap, tfCellLineAllBasedKeggPathway2KMap, overlapDefinition, tfNumber2NameMap, cellLineNumber2NameMap, fileNumber2NameMap, keggPathwayNumber2NameMap, geneId2ListofKeggPathwayNumberMap, geneHugoSymbolNumber2NameMap, refSeqRNANucleotideAccessionNumber2NameMap);

			// TF
			writeResultsWithNumbers(tfNumberCellLineNumber2KMap, tfNumber2NameMap, cellLineNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF);

			// KEGGPathway
			writeResultsWithNumbers(exonBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_EXON_BASED_KEGGPATHWAY_FILE);
			writeResultsWithNumbers(regulationBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_REGULATION_BASED_KEGGPATHWAY_FILE);
			writeResultsWithNumbers(allBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_ALL_BASED_KEGGPATHWAY_FILE);

			// TF CellLine KEGGPathway
			writeResultsWithNumbers(tfCellLineExonBasedKeggPathway2KMap, tfNumber2NameMap, cellLineNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_CELLLINE_EXON_BASED_KEGG_PATHWAY);
			writeResultsWithNumbers(tfCellLineRegulationBasedKeggPathway2KMap, tfNumber2NameMap, cellLineNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_CELLLINE_REGULATION_BASED_KEGG_PATHWAY);
			writeResultsWithNumbers(tfCellLineAllBasedKeggPathway2KMap, tfNumber2NameMap, cellLineNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_CELLLINE_ALL_BASED_KEGG_PATHWAY);
			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("CellLine Based TF KEGGPATHWAY annotation ends: " + new Date());
			GlanetRunner.appendLog("CellLine Based TF KEGGPATHWAY annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");

			//null
			//TF
			tfNumberCellLineNumber2KMap = null;
			
			//KEGG Pathway
			exonBasedKeggPathway2KMap = null;
			regulationBasedKeggPathway2KMap = null;
			allBasedKeggPathway2KMap = null;
			
			//TF CellLine KEGGPathway
			tfCellLineExonBasedKeggPathway2KMap = null;
			tfCellLineRegulationBasedKeggPathway2KMap = null;
			tfCellLineAllBasedKeggPathway2KMap = null;

			geneId2ListofKeggPathwayNumberMap= null;
			
			System.gc();
			System.runFinalization();

		}
		/*******************************************************************************/
		/************ TF CELLLINE KEGGPATHWAY*****ANNOTATION***ends ********************/
		/*******************************************************************************/
		
		

	

		/*********************************************************************************/
		/************ Search input interval files for TF *********************************/
		/************ Search input interval files for KEGG PATHWAY ***********************/
		/************ Search input interval files for TF AND KEGG PATHWAY ****************/
		/************ Search input interval files for TF AND CELLLINE AND KEGG PATHWAY ***/
		/*********************************************************************************/
		if (tfKeggPathwayAnnotationType.doTFKEGGPathwayAnnotation() && 
				tfCellLineKeggPathwayAnnotationType.doTFCellLineKEGGPathwayAnnotation()) {
			
			// KEGGPathway
			TIntObjectMap<TShortList> geneId2ListofKeggPathwayNumberMap = new TIntObjectHashMap<TShortList>();
			KeggPathwayUtility.createNcbiGeneId2ListofKeggPathwayNumberMap(
					dataFolder,
					Commons.KEGG_PATHWAY_2_NCBI_GENE_IDS_INPUT_FILE, 
					keggPathwayName2NumberMap, 
					geneId2ListofKeggPathwayNumberMap);
			
			//TF
			TIntIntMap tfNumberCellLineNumber2KMap = new TIntIntHashMap();
			
			//KEGG Pathway
			TShortIntMap exonBasedKeggPathway2KMap = new TShortIntHashMap();
			TShortIntMap regulationBasedKeggPathway2KMap = new TShortIntHashMap();
			TShortIntMap allBasedKeggPathway2KMap = new TShortIntHashMap();
			
			//TF KEGGPathway
			TIntIntMap tfExonBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap tfRegulationBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap tfAllBasedKeggPathway2KMap = new TIntIntHashMap();

			// TF CellLine KEGGPathway
			TIntIntMap tfCellLineExonBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap tfCellLineRegulationBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap tfCellLineAllBasedKeggPathway2KMap = new TIntIntHashMap();

			GlanetRunner.appendLog("**********************************************************");
			GlanetRunner.appendLog("Both TFKEGGPathway and TFCellLineKEGGPathway annotation starts: " + new Date());

			dateBefore = System.currentTimeMillis();
			
			searchTfandKeggPathwayWithNumbers(dataFolder, outputFolder, writeElementBasedAnnotationFoundOverlapsMode,geneId2ListofKeggPathwayNumberMap, tfNumberCellLineNumber2KMap, exonBasedKeggPathway2KMap, regulationBasedKeggPathway2KMap, allBasedKeggPathway2KMap, tfCellLineExonBasedKeggPathway2KMap, tfCellLineRegulationBasedKeggPathway2KMap, tfCellLineAllBasedKeggPathway2KMap, tfExonBasedKeggPathway2KMap, tfRegulationBasedKeggPathway2KMap, tfAllBasedKeggPathway2KMap, overlapDefinition, tfNumber2NameMap, cellLineNumber2NameMap, fileNumber2NameMap, keggPathwayNumber2NameMap, geneHugoSymbolNumber2NameMap, refSeqRNANucleotideAccessionNumber2NameMap);

			// TF
			writeResultsWithNumbers(tfNumberCellLineNumber2KMap, tfNumber2NameMap, cellLineNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF);

			// KEGGPathway
			writeResultsWithNumbers(exonBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_EXON_BASED_KEGGPATHWAY_FILE);
			writeResultsWithNumbers(regulationBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_REGULATION_BASED_KEGGPATHWAY_FILE);
			writeResultsWithNumbers(allBasedKeggPathway2KMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_KEGGPATHWAY + Commons.ANNOTATION_RESULTS_FOR_ALL_BASED_KEGGPATHWAY_FILE);

			// TF KEGGPathway
			writeTFKEGGPathwayResultsWithNumbers(tfExonBasedKeggPathway2KMap, tfNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_EXON_BASED_KEGG_PATHWAY);
			writeTFKEGGPathwayResultsWithNumbers(tfRegulationBasedKeggPathway2KMap, tfNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_REGULATION_BASED_KEGG_PATHWAY);
			writeTFKEGGPathwayResultsWithNumbers(tfAllBasedKeggPathway2KMap, tfNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_ALL_BASED_KEGG_PATHWAY);

			// TF CellLine KEGGPathway
			writeResultsWithNumbers(tfCellLineExonBasedKeggPathway2KMap, tfNumber2NameMap, cellLineNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_CELLLINE_EXON_BASED_KEGG_PATHWAY);
			writeResultsWithNumbers(tfCellLineRegulationBasedKeggPathway2KMap, tfNumber2NameMap, cellLineNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_CELLLINE_REGULATION_BASED_KEGG_PATHWAY);
			writeResultsWithNumbers(tfCellLineAllBasedKeggPathway2KMap, tfNumber2NameMap, cellLineNumber2NameMap, keggPathwayNumber2NameMap, outputFolder, Commons.ANNOTATION_RESULTS_FOR_TF_CELLLINE_ALL_BASED_KEGG_PATHWAY);

			dateAfter = System.currentTimeMillis();

			GlanetRunner.appendLog("TFCellLineKEGGPathway and  TFKEGGPathway annotation ends: " + new Date());
			GlanetRunner.appendLog("TFCellLineKEGGPathway and  TFKEGGPathway annotation took: " + (float) ((dateAfter - dateBefore) / 1000) + " seconds");
			GlanetRunner.appendLog("**********************************************************");
			
			//null
			//TF
			tfNumberCellLineNumber2KMap = null;
			
			//KEGG Pathway
			exonBasedKeggPathway2KMap = null;
			regulationBasedKeggPathway2KMap = null;
			allBasedKeggPathway2KMap = null;

			// TF KEGGPathway
			tfExonBasedKeggPathway2KMap = null;
			tfRegulationBasedKeggPathway2KMap = null;
			tfAllBasedKeggPathway2KMap = null;

			// TF CellLine KEGGPathway
			tfCellLineExonBasedKeggPathway2KMap = null;
			tfCellLineRegulationBasedKeggPathway2KMap = null;
			tfCellLineAllBasedKeggPathway2KMap = null;

			geneId2ListofKeggPathwayNumberMap = null;

			System.gc();
			System.runFinalization();

		}
		/*********************************************************************************/
		/************ Search input interval files for TF *********************************/
		/************ Search input interval files for KEGG PATHWAY ***********************/
		/************ Search input interval files for TF AND KEGG PATHWAY ****************/
		/************ Search input interval files for TF AND CELLLINE AND KEGG PATHWAY ***/
		/*********************************************************************************/

	}

	// Empirical P Value Calculation
	// ExecutorService
	public AllMaps annotatPermutation(String dataFolder, Integer permutationNumber, String chrName, List<InputLine> inputLines, IntervalTree dnaseIntervalTree, IntervalTree tfbsIntervalTree, IntervalTree histoneIntervalTree, IntervalTree ucscRefSeqGeneIntervalTree) {
		AllMaps allMaps = new AllMaps();

		// DNASE
		// This dnaseCellLine2KMap hash map will contain the dnase cell line
		// name to number of dnase cell line:k for the given search input size:n
		Map<String, Integer> dnaseCellLine2KMap = new HashMap<String, Integer>();
		// Map<String,BufferedWriter> dnaseBufferedWriterHashMap = new
		// HashMap<String,BufferedWriter>();
		// searchDnase(permutationNumber,chrName,inputLines, dnaseIntervalTree,
		// dnaseBufferedWriterHashMap, dnaseCellLine2KMap);
		allMaps.setPermutationNumberDnaseCellLineName2KMap(dnaseCellLine2KMap);

		// TFBS
		// This tfbsNameandCellLineName2KMap hash map will contain the
		// tfbsNameandCellLineName to number of tfbsNameandCellLineName: k for
		// the given search input size: n
		Map<String, Integer> tfbsNameandCellLineName2KMap = new HashMap<String, Integer>();
		// Map<String,BufferedWriter> tfbsBufferedWriterHashMap = new
		// HashMap<String,BufferedWriter>();
		// searchTfbs(permutationNumber,chrName,inputLines,tfbsIntervalTree,tfbsBufferedWriterHashMap,tfbsNameandCellLineName2KMap);
		allMaps.setPermutationNumberTfNameCellLineName2KMap(tfbsNameandCellLineName2KMap);

		// HISTONE
		// This histoneNameandCellLineName2KMap hash map will contain the
		// histoneNameandCellLineName to number of histoneNameandCellLineName: k
		// for the given search input size: n
		Map<String, Integer> histoneNameandCellLineName2KMap = new HashMap<String, Integer>();
		// Map<String,BufferedWriter> histoneBufferedWriterHashMap = new
		// HashMap<String,BufferedWriter>();
		// searchHistone(permutationNumber,chrName,inputLines,histoneIntervalTree,histoneBufferedWriterHashMap,histoneNameandCellLineName2KMap);
		allMaps.setPermutationNumberHistoneNameCellLineName2KMap(histoneNameandCellLineName2KMap);

		// KEGG PATHWAY
		// Search input interval files for kegg Pathway
		Map<String, List<String>> geneId2KeggPathwayMap = new HashMap<String, List<String>>();
		KeggPathwayUtility.createNcbiGeneId2KeggPathwayMap(dataFolder, Commons.KEGG_PATHWAY_2_NCBI_GENE_IDS_INPUT_FILE, geneId2KeggPathwayMap);

		// Exon Based Kegg Pathway Analysis
		// This exonBasedKeggPathway2KMap hash map will contain the kegg pathway
		// name to number of kegg pathway:k for the given search input size:n
		Map<String, Integer> exonBasedKeggPathway2KMap = new HashMap<String, Integer>();
		// Map<String,BufferedWriter> exonBasedKeggPathwayBufferedWriterHashMap
		// = new HashMap<String,BufferedWriter>();
		// searchUcscRefSeqGenes(permutationNumber, chrName, inputLines,
		// ucscRefSeqGeneIntervalTree,
		// exonBasedKeggPathwayBufferedWriterHashMap, geneId2KeggPathwayMap,
		// exonBasedKeggPathway2KMap,
		// Commons.NCBI_GENE_ID,Commons.EXON_BASED_KEGG_PATHWAY_ANALYSIS);
		allMaps.setPermutationNumberExonBasedKeggPathway2KMap(exonBasedKeggPathway2KMap);

		// Regulation Based Kegg Pathway Analysis
		// This regulationBasedKeggPathway2KMap hash map will contain the kegg
		// pathway name to number of kegg pathway:k for the given search input
		// size:n
		Map<String, Integer> regulationBasedKeggPathway2KMap = new HashMap<String, Integer>();
		// Map<String,BufferedWriter>
		// regulationBasedKeggPathwayBufferedWriterHashMap = new
		// HashMap<String,BufferedWriter>();
		// searchUcscRefSeqGenes(permutationNumber, chrName, inputLines,
		// ucscRefSeqGeneIntervalTree,
		// regulationBasedKeggPathwayBufferedWriterHashMap,
		// geneId2KeggPathwayMap, regulationBasedKeggPathway2KMap,
		// Commons.NCBI_GENE_ID,Commons.REGULATION_BASED_KEGG_PATHWAY_ANALYSIS);
		allMaps.setPermutationNumberRegulationBasedKeggPathway2KMap(regulationBasedKeggPathway2KMap);

		return allMaps;

	}

	// TShortObjectMap<BufferedWriter> version
	public static void closeBufferedWritersWithNumbers(TShortObjectMap<BufferedWriter> bufferedWriterHashMap) {
		BufferedWriter bufferedWriter = null;
		for (TShortObjectIterator<BufferedWriter> it = bufferedWriterHashMap.iterator(); it.hasNext();) {
			it.advance();
			try {
				bufferedWriter = it.value();
				bufferedWriter.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
	}

	// TIntObjectMap<BufferedWriter> version
	public static void closeBufferedWritersWithNumbers(TIntObjectMap<BufferedWriter> bufferedWriterHashMap) {
		BufferedWriter bufferedWriter = null;
		for (TIntObjectIterator<BufferedWriter> it = bufferedWriterHashMap.iterator(); it.hasNext();) {

			it.advance();

			try {
				bufferedWriter = it.value();

				bufferedWriter.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
	}

	// TLongObjectMap<BufferedWriter> version
	public static void closeBufferedWritersWithNumbers(TLongObjectMap<BufferedWriter> bufferedWriterHashMap) {
		BufferedWriter bufferedWriter = null;
		for (TLongObjectIterator<BufferedWriter> it = bufferedWriterHashMap.iterator(); it.hasNext();) {

			it.advance();

			try {
				bufferedWriter = it.value();

				bufferedWriter.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
	}

	// Enrichment
	// With IO
	// With Numbers
	// Tf and KeggPathway Enrichment or
	// Tf and CellLine and KeggPathway Enrichment starts
	public static AllMapsWithNumbers annotatePermutationWithIOWithNumbers(String outputFolder, int permutationNumber, ChromosomeName chrName, List<InputLine> randomlyGeneratedData, IntervalTree intervalTree, IntervalTree ucscRefSeqGenesIntervalTree, AnnotationType annotationType, TIntObjectMap<TShortList> geneId2ListofGeneSetNumberMap, int overlapDefinition) {
		AllMapsWithNumbers allMapsWithNumbers = new AllMapsWithNumbers();

		if (annotationType.doDnaseAnnotation()) {
			// DNASE
			// This dnaseCellLine2KMap hash map will contain the dnase cell line
			// name to number of dnase cell line:k for the given search input
			// size:n
			TIntIntMap permutationNumberDnaseCellLineNumber2KMap = new TIntIntHashMap();
			TIntObjectMap<BufferedWriter> dnaseBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();
			searchDnaseWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, dnaseBufferedWriterHashMap, permutationNumberDnaseCellLineNumber2KMap, overlapDefinition);
			closeBufferedWritersWithNumbers(dnaseBufferedWriterHashMap);
			allMapsWithNumbers.setPermutationNumberDnaseCellLineNumber2KMap(permutationNumberDnaseCellLineNumber2KMap);

		} else if (annotationType.doTFAnnotation()) {
			// TFBS
			// This tfbsNameandCellLineName2KMap hash map will contain the
			// tfbsNameandCellLineName to number of tfbsNameandCellLineName: k
			// for the given search input size: n
			TLongIntMap permutationNumberTfNumberCellLineNumber2KMap = new TLongIntHashMap();
			TLongObjectMap<BufferedWriter> tfBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			searchTfbsWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, tfBufferedWriterHashMap, permutationNumberTfNumberCellLineNumber2KMap, overlapDefinition);
			closeBufferedWritersWithNumbers(tfBufferedWriterHashMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumber2KMap(permutationNumberTfNumberCellLineNumber2KMap);

		} else if (annotationType.doHistoneAnnotation()) {
			// HISTONE
			// This histoneNameandCellLineName2KMap hash map will contain the
			// histoneNameandCellLineName to number of
			// histoneNameandCellLineName: k for the given search input size: n
			TLongIntMap permutationNumberHistoneNumberCellLineNumber2KMap = new TLongIntHashMap();
			TLongObjectMap<BufferedWriter> histoneBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			searchHistoneWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, histoneBufferedWriterHashMap, permutationNumberHistoneNumberCellLineNumber2KMap, overlapDefinition);
			closeBufferedWritersWithNumbers(histoneBufferedWriterHashMap);
			allMapsWithNumbers.setPermutationNumberHistoneNumberCellLineNumber2KMap(permutationNumberHistoneNumberCellLineNumber2KMap);

		} else if (annotationType.doUserDefinedGeneSetAnnotation()) {
			// USER DEFINED GENESET
			// Search input interval files for USER DEFINED GENESET

			// Exon Based UserDefinedGeneSet Analysis
			// This exonBasedKeggPathway2KMap hash map will contain the kegg
			// pathway name to number of kegg pathway:k for the given search
			// input size:n
			TLongIntMap permutationNumberExonBasedUserDefinedGeneSetNumber2KMap = new TLongIntHashMap();
			TLongObjectMap<BufferedWriter> permutationNumberExonBasedUserDefinedGeneSetNumber2BufferedWriterMap = new TLongObjectHashMap<BufferedWriter>();
			searchUcscRefSeqGenesWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, null, permutationNumberExonBasedUserDefinedGeneSetNumber2BufferedWriterMap, geneId2ListofGeneSetNumberMap, null, permutationNumberExonBasedUserDefinedGeneSetNumber2KMap, Commons.NCBI_GENE_ID, GeneSetAnalysisType.EXONBASEDGENESETANALYSIS, GeneSetType.USERDEFINEDGENESET, overlapDefinition);
			closeBufferedWritersWithNumbers(permutationNumberExonBasedUserDefinedGeneSetNumber2BufferedWriterMap);
			allMapsWithNumbers.setPermutationNumberExonBasedUserDefinedGeneSetNumber2KMap(permutationNumberExonBasedUserDefinedGeneSetNumber2KMap);

			// Regulation Based UserDefinedGeneSet Analysis
			// This regulationBasedKeggPathway2KMap hash map will contain the
			// kegg pathway name to number of kegg pathway:k for the given
			// search input size:n
			TLongIntMap permutationNumberRegulationBasedUserDefinedGeneSetNumber2KMap = new TLongIntHashMap();
			TLongObjectMap<BufferedWriter> permutationNumberRegulationBasedUserDefinedGeneSetNumber2BufferedWriterMap = new TLongObjectHashMap<BufferedWriter>();
			searchUcscRefSeqGenesWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, null, permutationNumberRegulationBasedUserDefinedGeneSetNumber2BufferedWriterMap, geneId2ListofGeneSetNumberMap, null, permutationNumberRegulationBasedUserDefinedGeneSetNumber2KMap, Commons.NCBI_GENE_ID, GeneSetAnalysisType.REGULATIONBASEDGENESETANALYSIS, GeneSetType.USERDEFINEDGENESET, overlapDefinition);
			closeBufferedWritersWithNumbers(permutationNumberRegulationBasedUserDefinedGeneSetNumber2BufferedWriterMap);
			allMapsWithNumbers.setPermutationNumberRegulationBasedUserDefinedGeneSetNumber2KMap(permutationNumberRegulationBasedUserDefinedGeneSetNumber2KMap);

			// All Based UserDefinedGeneSet Analysis
			// This allBasedKeggPathway2KMap hash map will contain the kegg
			// pathway name to number of kegg pathway:k for the given search
			// input size:n
			TLongIntMap permutationNumberAllBasedUserDefinedGeneSetNumber2KMap = new TLongIntHashMap();
			TLongObjectMap<BufferedWriter> permutationNumberAllBasedUserDefinedGeneSetNumber2BufferedWriterMap = new TLongObjectHashMap<BufferedWriter>();
			searchUcscRefSeqGenesWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, null, permutationNumberAllBasedUserDefinedGeneSetNumber2BufferedWriterMap, geneId2ListofGeneSetNumberMap, null, permutationNumberAllBasedUserDefinedGeneSetNumber2KMap, Commons.NCBI_GENE_ID, GeneSetAnalysisType.ALLBASEDGENESETANALYSIS, GeneSetType.USERDEFINEDGENESET, overlapDefinition);
			closeBufferedWritersWithNumbers(permutationNumberAllBasedUserDefinedGeneSetNumber2BufferedWriterMap);
			allMapsWithNumbers.setPermutationNumberAllBasedUserDefinedGeneSetNumber2KMap(permutationNumberAllBasedUserDefinedGeneSetNumber2KMap);

		} else if (annotationType.doUserDefinedLibraryAnnotation()) {

			// USER DEFINED LIBRARY
			TLongIntMap permutationNumberElementTypeNumberElementNumber2KMap = new TLongIntHashMap();
			TLongObjectMap<BufferedWriter> permutationNumberElementTypeNumberElementNumberr2BufferedWriterMap = new TLongObjectHashMap<BufferedWriter>();
			searchUserDefinedLibraryWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberElementTypeNumberElementNumberr2BufferedWriterMap, permutationNumberElementTypeNumberElementNumber2KMap, overlapDefinition);
			closeBufferedWritersWithNumbers(permutationNumberElementTypeNumberElementNumberr2BufferedWriterMap);
			allMapsWithNumbers.setPermutationNumberElementTypeNumberElementNumber2KMap(permutationNumberElementTypeNumberElementNumber2KMap);

		} else if (annotationType.doKEGGPathwayAnnotation()) {
			// KEGG PATHWAY
			// Search input interval files for kegg Pathway

			// Exon Based Kegg Pathway Analysis
			// This exonBasedKeggPathway2KMap hash map will contain the kegg
			// pathway name to number of kegg pathway:k for the given search
			// input size:n
			TIntIntMap permutationNumberExonBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntObjectMap<BufferedWriter> permutationNumberExonBasedKeggPathwayNumber2BufferedWriterMap = new TIntObjectHashMap<BufferedWriter>();
			searchUcscRefSeqGenesWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberExonBasedKeggPathwayNumber2BufferedWriterMap, null, geneId2ListofGeneSetNumberMap, permutationNumberExonBasedKeggPathwayNumber2KMap, null, Commons.NCBI_GENE_ID, GeneSetAnalysisType.EXONBASEDGENESETANALYSIS, GeneSetType.KEGGPATHWAY, overlapDefinition);
			closeBufferedWritersWithNumbers(permutationNumberExonBasedKeggPathwayNumber2BufferedWriterMap);
			allMapsWithNumbers.setPermutationNumberExonBasedKeggPathwayNumber2KMap(permutationNumberExonBasedKeggPathwayNumber2KMap);

			// Regulation Based Kegg Pathway Analysis
			// This regulationBasedKeggPathway2KMap hash map will contain the
			// kegg pathway name to number of kegg pathway:k for the given
			// search input size:n
			TIntIntMap permutationNumberRegulationBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntObjectMap<BufferedWriter> permutationNumberRegulationBasedKeggPathwayNumber2BufferedWriterMap = new TIntObjectHashMap<BufferedWriter>();
			searchUcscRefSeqGenesWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberRegulationBasedKeggPathwayNumber2BufferedWriterMap, null, geneId2ListofGeneSetNumberMap, permutationNumberRegulationBasedKeggPathwayNumber2KMap, null, Commons.NCBI_GENE_ID, GeneSetAnalysisType.REGULATIONBASEDGENESETANALYSIS, GeneSetType.KEGGPATHWAY, overlapDefinition);
			closeBufferedWritersWithNumbers(permutationNumberRegulationBasedKeggPathwayNumber2BufferedWriterMap);
			allMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberRegulationBasedKeggPathwayNumber2KMap);

			// All Based Kegg Pathway Analysis
			// This allBasedKeggPathway2KMap hash map will contain the kegg
			// pathway name to number of kegg pathway:k for the given search
			// input size:n
			TIntIntMap permutationNumberAllBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntObjectMap<BufferedWriter> permutationNumberAllBasedKeggPathwayNumber2BufferedWriterMap = new TIntObjectHashMap<BufferedWriter>();
			searchUcscRefSeqGenesWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberAllBasedKeggPathwayNumber2BufferedWriterMap, null, geneId2ListofGeneSetNumberMap, permutationNumberAllBasedKeggPathwayNumber2KMap, null, Commons.NCBI_GENE_ID, GeneSetAnalysisType.ALLBASEDGENESETANALYSIS, GeneSetType.KEGGPATHWAY, overlapDefinition);
			closeBufferedWritersWithNumbers(permutationNumberAllBasedKeggPathwayNumber2BufferedWriterMap);
			allMapsWithNumbers.setPermutationNumberAllBasedKeggPathwayNumber2KMap(permutationNumberAllBasedKeggPathwayNumber2KMap);

		} else if (annotationType.doTFKEGGPathwayAnnotation()) {

			// start adding new functionality
			/***************************************************************************/
			/***************************************************************************/
			// New Functionality
			// TF and Kegg Pathway
			TLongIntMap permutationNumberTfNumberCellLineNumber2KMap = new TLongIntHashMap();

			TIntIntMap permutationNumberExonBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberRegulationBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberAllBasedKeggPathway2KMap = new TIntIntHashMap();

			// Will be used for tf and keggPathway enrichment
			TLongIntMap permutationNumberTfNumberExonBasedKeggPathway2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberRegulationBasedKeggPathway2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberAllBasedKeggPathway2KMap = new TLongIntHashMap();

			TLongObjectMap<BufferedWriter> tfBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TIntObjectMap<BufferedWriter> exonBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();
			TIntObjectMap<BufferedWriter> regulationBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();
			TIntObjectMap<BufferedWriter> allBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();

			// Will be used for tf and keggPathway enrichment
			TLongObjectMap<BufferedWriter> tfExonBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TLongObjectMap<BufferedWriter> tfRegulationBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TLongObjectMap<BufferedWriter> tfAllBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();

			searchTfandKeggPathwayWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, ucscRefSeqGenesIntervalTree, tfBufferedWriterHashMap, exonBasedKeggPathwayBufferedWriterHashMap, regulationBasedKeggPathwayBufferedWriterHashMap, allBasedKeggPathwayBufferedWriterHashMap, tfExonBasedKeggPathwayBufferedWriterHashMap, tfRegulationBasedKeggPathwayBufferedWriterHashMap, tfAllBasedKeggPathwayBufferedWriterHashMap, null, null, null, geneId2ListofGeneSetNumberMap, permutationNumberTfNumberCellLineNumber2KMap, permutationNumberExonBasedKeggPathway2KMap, permutationNumberRegulationBasedKeggPathway2KMap, permutationNumberAllBasedKeggPathway2KMap, permutationNumberTfNumberExonBasedKeggPathway2KMap, permutationNumberTfNumberRegulationBasedKeggPathway2KMap, permutationNumberTfNumberAllBasedKeggPathway2KMap, null, null, null, Commons.NCBI_GENE_ID, annotationType, overlapDefinition);

			closeBufferedWritersWithNumbers(tfBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(exonBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(regulationBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(allBasedKeggPathwayBufferedWriterHashMap);

			closeBufferedWritersWithNumbers(tfExonBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(tfRegulationBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(tfAllBasedKeggPathwayBufferedWriterHashMap);

			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumber2KMap(permutationNumberTfNumberCellLineNumber2KMap);
			allMapsWithNumbers.setPermutationNumberExonBasedKeggPathwayNumber2KMap(permutationNumberExonBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberRegulationBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberAllBasedKeggPathwayNumber2KMap(permutationNumberAllBasedKeggPathway2KMap);

			allMapsWithNumbers.setPermutationNumberTfNumberExonBasedKeggPathwayNumber2KMap(permutationNumberTfNumberExonBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberTfNumberRegulationBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberAllBasedKeggPathwayNumber2KMap(permutationNumberTfNumberAllBasedKeggPathway2KMap);

			/***************************************************************************/
			/***************************************************************************/
			// start adding new functionality
		} else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {

			// start adding new functionality
			/***************************************************************************/
			/***************************************************************************/
			// New Functionality

			// TF Enrichment
			TLongIntMap permutationNumberTfNumberCellLineNumber2KMap = new TLongIntHashMap();

			// KEGG PATHWAY Enrichment
			TIntIntMap permutationNumberExonBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberRegulationBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberAllBasedKeggPathway2KMap = new TIntIntHashMap();

			// TF and CELLLINE and KEGG Pathway enrichment
			TLongIntMap permutationNumberTfCellLineExonBasedKeggPathway2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfCellLineRegulationBasedKeggPathway2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfCellLineAllBasedKeggPathway2KMap = new TLongIntHashMap();

			// TF BufferedWriters
			TLongObjectMap<BufferedWriter> tfBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();

			// KEGG Pathway BufferedWriters
			TIntObjectMap<BufferedWriter> exonBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();
			TIntObjectMap<BufferedWriter> regulationBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();
			TIntObjectMap<BufferedWriter> allBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();

			// TF CELLINE KEGG Pathway BufferedWriters
			TLongObjectMap<BufferedWriter> tfCellLineExonBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TLongObjectMap<BufferedWriter> tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TLongObjectMap<BufferedWriter> tfCellLineAllBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();

			searchTfandKeggPathwayWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, ucscRefSeqGenesIntervalTree, tfBufferedWriterHashMap, exonBasedKeggPathwayBufferedWriterHashMap, regulationBasedKeggPathwayBufferedWriterHashMap, allBasedKeggPathwayBufferedWriterHashMap, null, null, null, tfCellLineExonBasedKeggPathwayBufferedWriterHashMap, tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap, tfCellLineAllBasedKeggPathwayBufferedWriterHashMap, geneId2ListofGeneSetNumberMap, permutationNumberTfNumberCellLineNumber2KMap, permutationNumberExonBasedKeggPathway2KMap, permutationNumberRegulationBasedKeggPathway2KMap, permutationNumberAllBasedKeggPathway2KMap, null, null, null, permutationNumberTfCellLineExonBasedKeggPathway2KMap, permutationNumberTfCellLineRegulationBasedKeggPathway2KMap, permutationNumberTfCellLineAllBasedKeggPathway2KMap, Commons.NCBI_GENE_ID, annotationType, overlapDefinition);

			closeBufferedWritersWithNumbers(tfBufferedWriterHashMap);

			closeBufferedWritersWithNumbers(exonBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(regulationBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(allBasedKeggPathwayBufferedWriterHashMap);

			closeBufferedWritersWithNumbers(tfCellLineExonBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(tfCellLineAllBasedKeggPathwayBufferedWriterHashMap);

			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumber2KMap(permutationNumberTfNumberCellLineNumber2KMap);

			allMapsWithNumbers.setPermutationNumberExonBasedKeggPathwayNumber2KMap(permutationNumberExonBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberRegulationBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberAllBasedKeggPathwayNumber2KMap(permutationNumberAllBasedKeggPathway2KMap);

			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap(permutationNumberTfCellLineExonBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberTfCellLineRegulationBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap(permutationNumberTfCellLineAllBasedKeggPathway2KMap);
			/***************************************************************************/
			/***************************************************************************/
			// start adding new functionality
		} else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

			// TF Enrichment
			TLongIntMap permutationNumberTfNumberCellLineNumber2KMap = new TLongIntHashMap();

			// KEGGPathway Enrichment
			TIntIntMap permutationNumberExonBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberRegulationBasedKeggPathway2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberAllBasedKeggPathway2KMap = new TIntIntHashMap();

			// TF KEGGPathway Enrichment
			TLongIntMap permutationNumberTfNumberExonBasedKeggPathway2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberRegulationBasedKeggPathway2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberAllBasedKeggPathway2KMap = new TLongIntHashMap();

			// TF CELLLINE KEGGPathway Enrichment
			TLongIntMap permutationNumberTfCellLineExonBasedKeggPathway2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfCellLineRegulationBasedKeggPathway2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfCellLineAllBasedKeggPathway2KMap = new TLongIntHashMap();

			// TF BufferedWriters
			TLongObjectMap<BufferedWriter> tfBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();

			// KEGGPathway BufferedWriters
			TIntObjectMap<BufferedWriter> exonBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();
			TIntObjectMap<BufferedWriter> regulationBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();
			TIntObjectMap<BufferedWriter> allBasedKeggPathwayBufferedWriterHashMap = new TIntObjectHashMap<BufferedWriter>();

			// TF KEGGPathway BufferedWriters
			TLongObjectMap<BufferedWriter> tfExonBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TLongObjectMap<BufferedWriter> tfRegulationBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TLongObjectMap<BufferedWriter> tfAllBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();

			// TF CELLINE KEGGPathway BufferedWriters
			TLongObjectMap<BufferedWriter> tfCellLineExonBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TLongObjectMap<BufferedWriter> tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();
			TLongObjectMap<BufferedWriter> tfCellLineAllBasedKeggPathwayBufferedWriterHashMap = new TLongObjectHashMap<BufferedWriter>();

			searchTfandKeggPathwayWithIOWithNumbers(outputFolder, permutationNumber, chrName, randomlyGeneratedData, intervalTree, ucscRefSeqGenesIntervalTree, tfBufferedWriterHashMap, exonBasedKeggPathwayBufferedWriterHashMap, regulationBasedKeggPathwayBufferedWriterHashMap, allBasedKeggPathwayBufferedWriterHashMap, tfExonBasedKeggPathwayBufferedWriterHashMap, tfRegulationBasedKeggPathwayBufferedWriterHashMap, tfAllBasedKeggPathwayBufferedWriterHashMap, tfCellLineExonBasedKeggPathwayBufferedWriterHashMap, tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap, tfCellLineAllBasedKeggPathwayBufferedWriterHashMap, geneId2ListofGeneSetNumberMap, permutationNumberTfNumberCellLineNumber2KMap, permutationNumberExonBasedKeggPathway2KMap, permutationNumberRegulationBasedKeggPathway2KMap, permutationNumberAllBasedKeggPathway2KMap, permutationNumberTfNumberExonBasedKeggPathway2KMap, permutationNumberTfNumberRegulationBasedKeggPathway2KMap, permutationNumberTfNumberAllBasedKeggPathway2KMap, permutationNumberTfCellLineExonBasedKeggPathway2KMap, permutationNumberTfCellLineRegulationBasedKeggPathway2KMap, permutationNumberTfCellLineAllBasedKeggPathway2KMap, Commons.NCBI_GENE_ID, annotationType, overlapDefinition);

			// TF
			closeBufferedWritersWithNumbers(tfBufferedWriterHashMap);

			// KEGG
			closeBufferedWritersWithNumbers(exonBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(regulationBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(allBasedKeggPathwayBufferedWriterHashMap);

			// TF KEGG
			closeBufferedWritersWithNumbers(tfExonBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(tfRegulationBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(tfAllBasedKeggPathwayBufferedWriterHashMap);

			// TF CELLLINE KEGG
			closeBufferedWritersWithNumbers(tfCellLineExonBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(tfCellLineRegulationBasedKeggPathwayBufferedWriterHashMap);
			closeBufferedWritersWithNumbers(tfCellLineAllBasedKeggPathwayBufferedWriterHashMap);

			// TF
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumber2KMap(permutationNumberTfNumberCellLineNumber2KMap);

			// KEGG
			allMapsWithNumbers.setPermutationNumberExonBasedKeggPathwayNumber2KMap(permutationNumberExonBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberRegulationBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberAllBasedKeggPathwayNumber2KMap(permutationNumberAllBasedKeggPathway2KMap);

			// TF KEGG
			allMapsWithNumbers.setPermutationNumberTfNumberExonBasedKeggPathwayNumber2KMap(permutationNumberTfNumberExonBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberTfNumberRegulationBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberAllBasedKeggPathwayNumber2KMap(permutationNumberTfNumberAllBasedKeggPathway2KMap);

			// TF CELLLINE KEGG
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap(permutationNumberTfCellLineExonBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberTfCellLineRegulationBasedKeggPathway2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap(permutationNumberTfCellLineAllBasedKeggPathway2KMap);

		}

		return allMapsWithNumbers;

	}

	// Tf and KeggPathway Enrichment or
	// Tf and CellLine and KeggPathway Enrichment ends
	// with number ends

	// Enrichment
	// Without IO
	// With Numbers
	// TF_AND_KEGGPATHWAY_ENRICHMENT OR
	// TF_AND_CELLLINE_AND_KEGGPATHWAY_ENRICHMENT
	// OR BOTH enrichment at the same time
	// each time one interval tree EXCEPT NEW FUNCTIONALITY
	// Using fork join framework
	// Empirical P Value Calculation
	public static AllMapsWithNumbers annotatePermutationWithoutIOWithNumbers(int permutationNumber, ChromosomeName chrName, List<InputLine> randomlyGeneratedData, IntervalTree intervalTree, IntervalTree ucscRefSeqGenesIntervalTree, AnnotationType annotationType, TIntObjectMap<TShortList> geneId2ListofGeneSetNumberMap, int overlapDefinition) {
		AllMapsWithNumbers allMapsWithNumbers = new AllMapsWithNumbers();

		if (annotationType.doDnaseAnnotation()) {
			// DNASE
			// This dnaseCellLine2KMap hash map will contain the dnase cell line
			// name to number of dnase cell line:k for the given search input
			// size:n
			TIntIntMap permutationNumberDnaseCellLineNumber2KMap = new TIntIntHashMap();
			searchDnaseWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberDnaseCellLineNumber2KMap, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberDnaseCellLineNumber2KMap(permutationNumberDnaseCellLineNumber2KMap);

		} else if (annotationType.doTFAnnotation()) {
			// TFBS
			// This tfbsNameandCellLineName2KMap hash map will contain the
			// tfbsNameandCellLineName to number of tfbsNameandCellLineName: k
			// for the given search input size: n
			TLongIntMap permutationNumberTfNumberCellLineNumber2KMap = new TLongIntHashMap();
			searchTfbsWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberTfNumberCellLineNumber2KMap, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumber2KMap(permutationNumberTfNumberCellLineNumber2KMap);

		} else if (annotationType.doHistoneAnnotation()) {
			// HISTONE
			// This histoneNameandCellLineName2KMap hash map will contain the
			// histoneNameandCellLineName to number of
			// histoneNameandCellLineName: k for the given search input size: n
			TLongIntMap permutationNumberHistoneNumberCellLineNumber2KMap = new TLongIntHashMap();
			searchHistoneWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberHistoneNumberCellLineNumber2KMap, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberHistoneNumberCellLineNumber2KMap(permutationNumberHistoneNumberCellLineNumber2KMap);

		} else if (annotationType.doGeneAnnotation()){
			
			//Gene 
			TLongIntMap permutationNumberGeneNumber2KMap = new TLongIntHashMap();
			//searchGeneWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberGeneNumber2KMap, overlapDefinition);
			searchUcscRefSeqGenesWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, null, null, null,permutationNumberGeneNumber2KMap, Commons.NCBI_GENE_ID, GeneSetAnalysisType.NO_GENESET_ANALYSIS_TYPE_IS_DEFINED, GeneSetType.NO_GENESET_TYPE_IS_DEFINED, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberGeneNumber2KMap(permutationNumberGeneNumber2KMap);

		}
		
		
		
		else if (annotationType.doUserDefinedGeneSetAnnotation()) {

			// USER DEFINED GENESET
			// Search input interval files for USER DEFINED GENESET

			// Exon Based USER DEFINED GENESET Analysis
			// This exonBasedUserDefinedGeneSet2KMap hash map will contain the
			// kegg pathway name to number of kegg pathway:k for the given
			// search input size:n
			TLongIntMap permutationNumberExonBasedUserDefinedGeneSetNumber2KMap = new TLongIntHashMap();
			searchUcscRefSeqGenesWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, geneId2ListofGeneSetNumberMap, null, permutationNumberExonBasedUserDefinedGeneSetNumber2KMap, null, Commons.NCBI_GENE_ID, GeneSetAnalysisType.EXONBASEDGENESETANALYSIS, GeneSetType.USERDEFINEDGENESET, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberExonBasedUserDefinedGeneSetNumber2KMap(permutationNumberExonBasedUserDefinedGeneSetNumber2KMap);

			// Regulation Based USER DEFINED GENESET Analysis
			// This regulationBasedUserDefinedGeneSet2KMap hash map will contain
			// the kegg pathway name to number of kegg pathway:k for the given
			// search input size:n
			TLongIntMap permutationNumberRegulationBasedUserDefinedGeneSetNumber2KMap = new TLongIntHashMap();
			searchUcscRefSeqGenesWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, geneId2ListofGeneSetNumberMap, null, permutationNumberRegulationBasedUserDefinedGeneSetNumber2KMap, null,Commons.NCBI_GENE_ID, GeneSetAnalysisType.REGULATIONBASEDGENESETANALYSIS, GeneSetType.USERDEFINEDGENESET, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberRegulationBasedUserDefinedGeneSetNumber2KMap(permutationNumberRegulationBasedUserDefinedGeneSetNumber2KMap);

			// All Based USER DEFINED GENESET Analysis
			// This allBasedUserDefinedGeneSet2KMap hash map will contain the
			// kegg pathway name to number of kegg pathway:k for the given
			// search input size:n
			TLongIntMap permutationNumberAllBasedUserDefinedGeneSetNumber2KMap = new TLongIntHashMap();
			searchUcscRefSeqGenesWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, geneId2ListofGeneSetNumberMap, null, permutationNumberAllBasedUserDefinedGeneSetNumber2KMap, null, Commons.NCBI_GENE_ID, GeneSetAnalysisType.ALLBASEDGENESETANALYSIS, GeneSetType.USERDEFINEDGENESET, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberAllBasedUserDefinedGeneSetNumber2KMap(permutationNumberAllBasedUserDefinedGeneSetNumber2KMap);

		} else if (annotationType.doUserDefinedLibraryAnnotation()) {
			// USER DEFINED LIBRARY
			TLongIntMap permutationNumberElementTypeNumberElementNumber2KMap = new TLongIntHashMap();
			searchUserDefinedLibraryWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, permutationNumberElementTypeNumberElementNumber2KMap, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberElementTypeNumberElementNumber2KMap(permutationNumberElementTypeNumberElementNumber2KMap);

		} else if (annotationType.doKEGGPathwayAnnotation()) {
			// KEGG PATHWAY
			// Search input interval files for kegg Pathway

			// Exon Based Kegg Pathway Analysis
			// This exonBasedKeggPathway2KMap hash map will contain the kegg
			// pathway name to number of kegg pathway:k for the given search
			// input size:n
			TIntIntMap permutationNumberExonBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			searchUcscRefSeqGenesWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, geneId2ListofGeneSetNumberMap, permutationNumberExonBasedKeggPathwayNumber2KMap, null,null, Commons.NCBI_GENE_ID, GeneSetAnalysisType.EXONBASEDGENESETANALYSIS, GeneSetType.KEGGPATHWAY, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberExonBasedKeggPathwayNumber2KMap(permutationNumberExonBasedKeggPathwayNumber2KMap);

			// Regulation Based Kegg Pathway Analysis
			// This regulationBasedKeggPathway2KMap hash map will contain the
			// kegg pathway name to number of kegg pathway:k for the given
			// search input size:n
			TIntIntMap permutationNumberRegulationBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			searchUcscRefSeqGenesWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, geneId2ListofGeneSetNumberMap, permutationNumberRegulationBasedKeggPathwayNumber2KMap, null, null,Commons.NCBI_GENE_ID, GeneSetAnalysisType.REGULATIONBASEDGENESETANALYSIS, GeneSetType.KEGGPATHWAY, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberRegulationBasedKeggPathwayNumber2KMap);

			// All Based Kegg Pathway Analysis
			// This allBasedKeggPathway2KMap hash map will contain the kegg
			// pathway name to number of kegg pathway:k for the given search
			// input size:n
			TIntIntMap permutationNumberAllBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			searchUcscRefSeqGenesWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, geneId2ListofGeneSetNumberMap, permutationNumberAllBasedKeggPathwayNumber2KMap, null,null, Commons.NCBI_GENE_ID, GeneSetAnalysisType.ALLBASEDGENESETANALYSIS, GeneSetType.KEGGPATHWAY, overlapDefinition);
			allMapsWithNumbers.setPermutationNumberAllBasedKeggPathwayNumber2KMap(permutationNumberAllBasedKeggPathwayNumber2KMap);

		} else if (annotationType.doTFKEGGPathwayAnnotation()) {

			/***************************************************************************/
			/***************************************************************************/
			// New Functionality
			// TF
			TLongIntMap permutationNumberTfNumberCellLineNumber2KMap = new TLongIntHashMap();

			// KEGG
			TIntIntMap permutationNumberExonBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberRegulationBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberAllBasedKeggPathwayNumber2KMap = new TIntIntHashMap();

			// TF KEGG
			TLongIntMap permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap = new TLongIntHashMap();

			searchTfandKeggPathwayWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, ucscRefSeqGenesIntervalTree, geneId2ListofGeneSetNumberMap, permutationNumberTfNumberCellLineNumber2KMap, permutationNumberExonBasedKeggPathwayNumber2KMap, permutationNumberRegulationBasedKeggPathwayNumber2KMap, permutationNumberAllBasedKeggPathwayNumber2KMap, permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap, permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap, permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap, null, null, null, Commons.NCBI_GENE_ID, annotationType, overlapDefinition);

			// TF
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumber2KMap(permutationNumberTfNumberCellLineNumber2KMap);

			// KEGG
			allMapsWithNumbers.setPermutationNumberExonBasedKeggPathwayNumber2KMap(permutationNumberExonBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberRegulationBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberAllBasedKeggPathwayNumber2KMap(permutationNumberAllBasedKeggPathwayNumber2KMap);

			// TF KEGG
			allMapsWithNumbers.setPermutationNumberTfNumberExonBasedKeggPathwayNumber2KMap(permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberAllBasedKeggPathwayNumber2KMap(permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap);
			/***************************************************************************/
			/***************************************************************************/

		} else if (annotationType.doTFCellLineKEGGPathwayAnnotation()) {

			/***************************************************************************/
			/***************************************************************************/
			// New Functionality
			// TF
			TLongIntMap permutationNumberTfNumberCellLineNumber2KMap = new TLongIntHashMap();

			// KEGG
			TIntIntMap permutationNumberExonBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberRegulationBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberAllBasedKeggPathwayNumber2KMap = new TIntIntHashMap();

			// TF CELLINE KEGG
			TLongIntMap permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap = new TLongIntHashMap();

			searchTfandKeggPathwayWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, ucscRefSeqGenesIntervalTree, geneId2ListofGeneSetNumberMap, permutationNumberTfNumberCellLineNumber2KMap, permutationNumberExonBasedKeggPathwayNumber2KMap, permutationNumberRegulationBasedKeggPathwayNumber2KMap, permutationNumberAllBasedKeggPathwayNumber2KMap, null, null, null, permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, Commons.NCBI_GENE_ID, annotationType, overlapDefinition);

			// TF
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumber2KMap(permutationNumberTfNumberCellLineNumber2KMap);

			// KEGG
			allMapsWithNumbers.setPermutationNumberExonBasedKeggPathwayNumber2KMap(permutationNumberExonBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberRegulationBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberAllBasedKeggPathwayNumber2KMap(permutationNumberAllBasedKeggPathwayNumber2KMap);

			// TF CELLLINE KEGG
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap(permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap(permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap);
			/***************************************************************************/
			/***************************************************************************/

		} else if (annotationType.doBothTFKEGGPathwayAndTFCellLineKEGGPathwayAnnotation()) {

			// TF
			TLongIntMap permutationNumberTfNumberCellLineNumber2KMap = new TLongIntHashMap();

			// KEGG
			TIntIntMap permutationNumberExonBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberRegulationBasedKeggPathwayNumber2KMap = new TIntIntHashMap();
			TIntIntMap permutationNumberAllBasedKeggPathwayNumber2KMap = new TIntIntHashMap();

			// TF KEGG
			TLongIntMap permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap = new TLongIntHashMap();

			// TF CELLLINE KEGG
			TLongIntMap permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap = new TLongIntHashMap();
			TLongIntMap permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap = new TLongIntHashMap();

			searchTfandKeggPathwayWithoutIOWithNumbers(permutationNumber, chrName, randomlyGeneratedData, intervalTree, ucscRefSeqGenesIntervalTree, geneId2ListofGeneSetNumberMap, permutationNumberTfNumberCellLineNumber2KMap, permutationNumberExonBasedKeggPathwayNumber2KMap, permutationNumberRegulationBasedKeggPathwayNumber2KMap, permutationNumberAllBasedKeggPathwayNumber2KMap, permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap, permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap, permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap, permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap, permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap, permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap, Commons.NCBI_GENE_ID, annotationType, overlapDefinition);

			// TF
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumber2KMap(permutationNumberTfNumberCellLineNumber2KMap);

			// KEGG
			allMapsWithNumbers.setPermutationNumberExonBasedKeggPathwayNumber2KMap(permutationNumberExonBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberRegulationBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberAllBasedKeggPathwayNumber2KMap(permutationNumberAllBasedKeggPathwayNumber2KMap);

			// TF KEGG
			allMapsWithNumbers.setPermutationNumberTfNumberExonBasedKeggPathwayNumber2KMap(permutationNumberTfNumberExonBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberTfNumberRegulationBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberAllBasedKeggPathwayNumber2KMap(permutationNumberTfNumberAllBasedKeggPathwayNumber2KMap);

			// TF CELLINE KEGG
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap(permutationNumberTfNumberCellLineNumberExonBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap(permutationNumberTfNumberCellLineNumberRegulationBasedKeggPathwayNumber2KMap);
			allMapsWithNumbers.setPermutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap(permutationNumberTfNumberCellLineNumberAllBasedKeggPathwayNumber2KMap);

		}

		return allMapsWithNumbers;

	}

	// with numbers ends

	// args[0] ---> Input File Name with folder
	// args[1] ---> GLANET installation folder with "\\" at the end. This folder
	// will be used for outputFolder and dataFolder.
	// args[2] ---> Input File Format
	// ---> default
	// Commons.INPUT_FILE_FORMAT_1_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// --->
	// Commons.INPUT_FILE_FORMAT_0_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// --->
	// Commons.INPUT_FILE_FORMAT_DBSNP_IDS_0_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// --->
	// Commons.INPUT_FILE_FORMAT_BED_0_BASED_COORDINATES_START_INCLUSIVE_END_EXCLUSIVE
	// --->
	// Commons.INPUT_FILE_FORMAT_GFF3_1_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// args[3] ---> Annotation, overlap definition, number of bases,
	// default 1
	// args[4] ---> Perform Enrichment parameter
	// ---> default Commons.DO_ENRICH
	// ---> Commons.DO_NOT_ENRICH
	// args[5] ---> Generate Random Data Mode
	// ---> default Commons.GENERATE_RANDOM_DATA_WITH_MAPPABILITY_AND_GC_CONTENT
	// ---> Commons.GENERATE_RANDOM_DATA_WITHOUT_MAPPABILITY_AND_GC_CONTENT
	// args[6] ---> multiple testing parameter, enriched elements will be
	// decided and sorted with respect to this parameter
	// ---> default Commons.BENJAMINI_HOCHBERG_FDR
	// ---> Commons.BONFERRONI_CORRECTION
	// args[7] ---> Bonferroni Correction Significance Level, default 0.05
	// args[8] ---> Bonferroni Correction Significance Criteria, default 0.05
	// args[9] ---> Number of permutations, default 5000
	// args[10] ---> Dnase Enrichment
	// ---> default Commons.DO_NOT_DNASE_ENRICHMENT
	// ---> Commons.DO_DNASE_ENRICHMENT
	// args[11] ---> Histone Enrichment
	// ---> default Commons.DO_NOT_HISTONE_ENRICHMENT
	// ---> Commons.DO_HISTONE_ENRICHMENT
	// args[12] ---> Transcription Factor(TF) Enrichment
	// ---> default Commons.DO_NOT_TF_ENRICHMENT
	// ---> Commons.DO_TF_ENRICHMENT
	// args[13] ---> KEGG Pathway Enrichment
	// ---> default Commons.DO_NOT_KEGGPATHWAY_ENRICHMENT
	// ---> Commons.DO_KEGGPATHWAY_ENRICHMENT
	// args[14] ---> TF and KEGG Pathway Enrichment
	// ---> default Commons.DO_NOT_TF_KEGGPATHWAY_ENRICHMENT
	// ---> Commons.DO_TF_KEGGPATHWAY_ENRICHMENT
	// args[15] ---> TF and CellLine and KeggPathway Enrichment
	// ---> default Commons.DO_NOT_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT
	// ---> Commons.DO_TF_CELLLINE_KEGGPATHWAY_ENRICHMENT
	// args[16] ---> RSAT parameter
	// ---> default Commons.DO_NOT_REGULATORY_SEQUENCE_ANALYSIS_USING_RSAT
	// ---> Commons.DO_REGULATORY_SEQUENCE_ANALYSIS_USING_RSAT
	// args[17] ---> job name example: ocd_gwas_snps
	// args[18] ---> writeGeneratedRandomDataMode checkBox
	// ---> default Commons.DO_NOT_WRITE_GENERATED_RANDOM_DATA
	// ---> Commons.WRITE_GENERATED_RANDOM_DATA
	// args[19] ---> writePermutationBasedandParametricBasedAnnotationResultMode
	// checkBox
	// ---> default
	// Commons.DO_NOT_WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT
	// --->
	// Commons.WRITE_PERMUTATION_BASED_AND_PARAMETRIC_BASED_ANNOTATION_RESULT
	// args[20] ---> writePermutationBasedAnnotationResultMode checkBox
	// ---> default Commons.DO_NOT_WRITE_PERMUTATION_BASED_ANNOTATION_RESULT
	// ---> Commons.WRITE_PERMUTATION_BASED_ANNOTATION_RESULT
	// args[21] ---> number of permutations in each run. Default is 2000
	// args[22] ---> UserDefinedGeneSet Enrichment
	// default Commons.DO_NOT_USER_DEFINED_GENESET_ENRICHMENT
	// Commons.DO_USER_DEFINED_GENESET_ENRICHMENT
	// args[23] ---> UserDefinedGeneSet InputFile
	// args[24] ---> UserDefinedGeneSet GeneInformationType
	// default Commons.GENE_ID
	// Commons.GENE_SYMBOL
	// Commons.RNA_NUCLEOTIDE_ACCESSION
	// args[25] ---> UserDefinedGeneSet Name
	// args[26] ---> UserDefinedGeneSet Optional GeneSet Description InputFile
	// args[27] ---> UserDefinedLibrary Enrichment
	// default Commons.DO_NOT_USER_DEFINED_LIBRARY_ENRICHMENT
	// Commons.DO_USER_DEFINED_LIBRARY_ENRICHMENT
	// args[28] ---> UserDefinedLibrary InputFile
	// args[29] ---> UserDefinedLibrary DataFormat
	// default
	// Commons.USERDEFINEDLIBRARY_DATAFORMAT_0_BASED_COORDINATES_START_INCLUSIVE_END_EXCLUSIVE
	// Commons.USERDEFINEDLIBRARY_DATAFORMAT_0_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// Commons.USERDEFINEDLIBRARY_DATAFORMAT_1_BASED_COORDINATES_START_INCLUSIVE_END_EXCLUSIVE
	// Commons.USERDEFINEDLIBRARY_DATAFORMAT_1_BASED_COORDINATES_START_INCLUSIVE_END_INCLUSIVE
	// args[30] - args[args.length-1] ---> Note that the selected cell lines are
	// always inserted at the end of the args array because it's size
	// is not fixed. So for not (until the next change on args array) the
	// selected cell
	// lines can be reached starting from 22th index up until (args.length-1)th
	// index.
	// If no cell line selected so the args.length-1 will be 22-1 = 21. So it
	// will never
	// give an out of boundry exception in a for loop with this approach.
	public static void main(String[] args) {

		Annotation annotateIntervals = new Annotation();
		annotateIntervals.annotate(args);
	}

}
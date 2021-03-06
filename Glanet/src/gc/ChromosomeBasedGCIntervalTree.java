/**
 * 
 */
package gc;

import intervaltree.GCIntervalTreeNode;
import intervaltree.GCIsochoreIntervalTreeNode;
import intervaltree.Interval;
import intervaltree.IntervalTree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import auxiliary.FileOperations;

import common.Commons;

import enumtypes.ChromosomeName;
import enumtypes.CommandLineArguments;
import enumtypes.IsochoreFamily;

/**
 * @author Burcak Otlu
 * @date Apr 10, 2015
 * @project Glanet 
 *
 */
public class ChromosomeBasedGCIntervalTree {

	public static void fillIsochoreFamilyPool( String dataFolder, ChromosomeName chromName,
			IsochoreFamily isochoreFamily, List<Interval> gcIsochoreFamilyPool) {

		String isochoreFamilyPoolFileEnd = null;

		switch( isochoreFamily){

		case L1:
			isochoreFamilyPoolFileEnd = Commons.GC_ISOCHOREFAMILY_L1_POOL_FILE_END;
			break;
		case L2:
			isochoreFamilyPoolFileEnd = Commons.GC_ISOCHOREFAMILY_L2_POOL_FILE_END;
			break;
		case H1:
			isochoreFamilyPoolFileEnd = Commons.GC_ISOCHOREFAMILY_H1_POOL_FILE_END;
			break;
		case H2:
			isochoreFamilyPoolFileEnd = Commons.GC_ISOCHOREFAMILY_H2_POOL_FILE_END;
			break;
		case H3:
			isochoreFamilyPoolFileEnd = Commons.GC_ISOCHOREFAMILY_H3_POOL_FILE_END;
			break;

		}

		String gcIsochoreFamilyPoolFile = Commons.GC + System.getProperty( "file.separator") + Commons.GC_ISOCHORE_FAMILY_POOL_DATA + System.getProperty( "file.separator") + chromName.convertEnumtoString() + isochoreFamilyPoolFileEnd;

		FileReader fileReader;
		BufferedReader bufferedReader;

		String strLine;

		int indexofFirstTab;

		int lowZeroBased;
		int highZeroBased;

		// example line
		// low and high are zeroBased and they are both inclusive
		// 244500000 244599999

		try{
			fileReader = FileOperations.createFileReader( dataFolder + gcIsochoreFamilyPoolFile);
			bufferedReader = new BufferedReader( fileReader);

			/*****************************************************************/
			while( ( strLine = bufferedReader.readLine()) != null){

				indexofFirstTab = strLine.indexOf( '\t');

				lowZeroBased = Integer.parseInt( strLine.substring( 0, indexofFirstTab));
				highZeroBased = Integer.parseInt( strLine.substring( indexofFirstTab + 1));

				Interval interval = new Interval( lowZeroBased, highZeroBased);

				gcIsochoreFamilyPool.add( interval);

			}// End of WHILE
			/*****************************************************************/

			// Close
			bufferedReader.close();

		}catch( IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void fillIsochoreIntervalTree( 
			String dataFolder, 
			ChromosomeName chromName,
			IntervalTree gcIsochoreIntervalTree) {

		String gcIsochoreIntervalTreeDataFile = Commons.GC + System.getProperty( "file.separator") + Commons.GC_ISOCHORE_INTERVAL_TREE_DATA + System.getProperty( "file.separator") + chromName.convertEnumtoString() + Commons.GC_ISOCHORES_FILE_END;

		FileReader fileReader;
		BufferedReader bufferedReader;

		String strLine;

		int indexofFirstTab;
		int indexofSecondTab;
		int indexofThirdTab;
		int indexofFourthTab;

		int lowZeroBased;
		int highZeroBased;
		int numberofGCs = 0;
		IsochoreFamily isochoreFamily = null;

		GCIsochoreIntervalTreeNode node = null;

		try{

			fileReader = FileOperations.createFileReader( dataFolder + gcIsochoreIntervalTreeDataFile);
			bufferedReader = new BufferedReader( fileReader);

			// skip header line
			// #OBasedStart OBasedEnd numberofGCsInTheInterval GCContentPercentage Isochore Family
			strLine = bufferedReader.readLine();

			// example line
			// 0Based End Inclusive Coordinates
			// low high numberofGCs
			// 500000 599999 31353 31.35% L1

			/*****************************************************************/
			while( ( strLine = bufferedReader.readLine()) != null){

				indexofFirstTab = strLine.indexOf( '\t');
				indexofSecondTab = ( indexofFirstTab > 0)?strLine.indexOf( '\t', indexofFirstTab + 1):-1;
				indexofThirdTab = ( indexofSecondTab > 0)?strLine.indexOf( '\t', indexofSecondTab + 1):-1;
				indexofFourthTab = ( indexofThirdTab > 0)?strLine.indexOf( '\t', indexofThirdTab + 1):-1;

				lowZeroBased = Integer.parseInt( strLine.substring( 0, indexofFirstTab));
				highZeroBased = Integer.parseInt( strLine.substring( indexofFirstTab + 1, indexofSecondTab));
				numberofGCs = Integer.parseInt( strLine.substring( indexofSecondTab + 1, indexofThirdTab));
				isochoreFamily = IsochoreFamily.convertStringtoEnum( strLine.substring( indexofFourthTab + 1));

				// Create the Interval Tree Node
				// Insert the created node into the interval tree
				// if numberofGCs is greater than 0
				// Think about it
				// Is it logical?
				// No, since the given input interval can be on such a region with numberofGCs is equal to zero.
				// If we don't add this node, then we can not find overlap

				// Create new interval tree node
				node = new GCIsochoreIntervalTreeNode( lowZeroBased, highZeroBased, numberofGCs, isochoreFamily);

				// Add to the interval tree
				gcIsochoreIntervalTree.intervalTreeInsert( gcIsochoreIntervalTree, node);

			}// End of WHILE
			/*****************************************************************/

			// Close
			bufferedReader.close();

		}catch( FileNotFoundException e){
			e.printStackTrace();
		}catch( IOException e){
			e.printStackTrace();
		}

	}

	public static void fillIntervalTree( String dataFolder, ChromosomeName chromName, int intervalLength, IntervalTree gcIntervalTree) {

		//example filename
		//chr1_gc_intervalLength_10000_consecutiveZerosMerged.txt
		String gcIntervalsConsecutiveZerosMergedFile = Commons.GC + System.getProperty( "file.separator") + Commons.GC_INTERVAL_TREE_DATA + System.getProperty( "file.separator") + chromName.convertEnumtoString() + Commons.GC_INTERVALS_CONSECUTIVE_ZEROS_MERGED_FILE_START + intervalLength + Commons.GC_INTERVALS_CONSECUTIVE_ZEROS_MERGED_FILE_END;

		FileReader fileReader;
		BufferedReader bufferedReader;

		String strLine;
		int indexofFirstTab;
		int indexofSecondTab;

		int lowZeroBased;
		int highZeroBased;
		short numberofGCs = 0;

		GCIntervalTreeNode node = null;

		try{
			
			fileReader = FileOperations.createFileReader( dataFolder + gcIntervalsConsecutiveZerosMergedFile);
			bufferedReader = new BufferedReader( fileReader);

			// example line
			// 0Based End Inclusive Coordinates
			// low high numberofGCs
			// 63300 63399 57

			/*****************************************************************/
			while( ( strLine = bufferedReader.readLine()) != null){

				indexofFirstTab = strLine.indexOf( '\t');
				indexofSecondTab = ( indexofFirstTab > 0)?strLine.indexOf( '\t', indexofFirstTab + 1):-1;

				lowZeroBased = Integer.parseInt( strLine.substring( 0, indexofFirstTab));
				highZeroBased = Integer.parseInt( strLine.substring( indexofFirstTab + 1, indexofSecondTab));
				numberofGCs = Short.parseShort( strLine.substring( indexofSecondTab + 1));

				// Create the Interval Tree Node
				// Insert the created node into the interval tree
				// if numberofGCs is greater than 0
				if( numberofGCs > 0){
					// Create new interval tree node
					node = new GCIntervalTreeNode( lowZeroBased, highZeroBased, numberofGCs);

					// Add to the interval tree
					gcIntervalTree.intervalTreeInsert( gcIntervalTree, node);
				}

			}// End of WHILE
			/*****************************************************************/

		}catch( FileNotFoundException e){
			e.printStackTrace();
		}catch( IOException e){
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main( String[] args) {

		String glanetFolder = args[CommandLineArguments.GlanetFolder.value()];
		String dataFolder = glanetFolder + Commons.DATA + System.getProperty( "file.separator");

		IntervalTree gcIntervalTree = new IntervalTree();
		fillIntervalTree( dataFolder, ChromosomeName.CHROMOSOME1, Commons.INTERVAL_LENGTH_100, gcIntervalTree);

		System.out.println( "GC Interval Tree NumberofNonOverlappingBases:" + gcIntervalTree.getNumberofNonOverlappingBases());
		System.out.println( "GC Interval Tree NumberofNodes: " + gcIntervalTree.getNumberofNodes());

	}

}

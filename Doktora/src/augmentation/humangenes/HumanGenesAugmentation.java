/**
 * @author Burcak Otlu
 * Sep 24, 2013
 * 1:18:27 PM
 * 2013
 *
 * 
 */
package augmentation.humangenes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Commons;

public class HumanGenesAugmentation {
	
	public static void fillHumanGeneId2RefSeqGeneNameMap(Map<String,List<String>> humanGeneId2RefSeqGeneNameListMap){
		FileReader fileReader;
		BufferedReader bufferedReader;
		String strLine;
		
		int indexofFirstTab;
		int indexofSecondTab;
		int indexofThirdTab;
		int indexofFourthTab;
		
		int indexofDot;
		
		String geneId;
		String refSeqGeneName;
		
		List<String> refSeqGeneNameList;
		
		try {
			fileReader =  new FileReader(Commons.NCBI_HUMAN_GENE_TO_REF_SEQ);
			bufferedReader = new BufferedReader(fileReader);
			
			while((strLine = bufferedReader.readLine())!=null) {
				//example line
				//the second column is gene id column
				//the fourth column is the refseq gene name column
				//9606	63976	REVIEWED	NM_022114.3	289547572	NP_071397.3	289547573	NC_000001.10	224589800	2985741	3355184	+	Reference GRCh37.p10 Primary Assembly
				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t',indexofFirstTab+1);
				indexofThirdTab = strLine.indexOf('\t',indexofSecondTab+1);
				indexofFourthTab = strLine.indexOf('\t',indexofThirdTab+1);

				geneId = strLine.substring(indexofFirstTab+1,indexofSecondTab);
				refSeqGeneName = strLine.substring(indexofThirdTab+1,indexofFourthTab);
				
				indexofDot = refSeqGeneName.indexOf('.');
				
				if (indexofDot>= 0){
					refSeqGeneName = refSeqGeneName.substring(0,indexofDot);
				}
				
				refSeqGeneNameList =humanGeneId2RefSeqGeneNameListMap.get(geneId);
				
				if (!refSeqGeneName.equals("-")){
					if(refSeqGeneNameList==null){
						refSeqGeneNameList = new ArrayList<String>();
						refSeqGeneNameList.add(refSeqGeneName);	
						
						humanGeneId2RefSeqGeneNameListMap.put(geneId, refSeqGeneNameList);
					}else{
						if (!refSeqGeneNameList.contains(refSeqGeneName)){
							refSeqGeneNameList.add(refSeqGeneName);	
						}
					}
				}
				
			}//End of while
			
			bufferedReader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void fillHumanRefSeqGeneName2AlternateGeneNameMap(Map<String,List<String>> humanRefSeqGeneName2AlternateGeneNameListMap){
		
		FileReader fileReader;
		BufferedReader bufferedReader;
		String strLine;
		
		int indexofFirstTab;
		int indexofSecondTab;
		int indexofThirdTab;
		int indexofFourthTab;
		int indexofFifthTab;
		int indexofSixthTab;
		int indexofSeventhTab;
		int indexofEighthTab;
		int indexofNinethTab;
		int indexofTenthTab;
		int indexofEleventhTab;
		int indexofTwelfthTab;
		int indexofThirteenthTab;
		
		String refSeqName;
		String alternateGeneName;
		
		List<String> alternateGeneNameList;
		
		
		
		try {
			fileReader = new FileReader(Commons.FTP_HG19_REFSEQ_GENES);
			bufferedReader = new BufferedReader(fileReader);
			
			while((strLine = bufferedReader.readLine())!=null) {
				//header line
				//#bin	name	chrom	strand	txStart	txEnd	cdsStart	cdsEnd	exonCount	exonStarts	exonEnds	score	name2	cdsStartStat	cdsEndStat	exonFrames
				//example line
				//0	NM_032291	chr1	+	66999824	67210768	67000041	67208778	25	66999824,67091529,67098752,67101626,67105459,67108492,67109226,67126195,67133212,67136677,67137626,67138963,67142686,67145360,67147551,67154830,67155872,67161116,67184976,67194946,67199430,67205017,67206340,67206954,67208755,	67000051,67091593,67098777,67101698,67105516,67108547,67109402,67126207,67133224,67136702,67137678,67139049,67142779,67145435,67148052,67154958,67155999,67161176,67185088,67195102,67199563,67205220,67206405,67207119,67210768,	0	SGIP1	cmpl	cmpl	0,1,2,0,0,0,1,0,0,0,1,2,1,1,1,1,0,1,1,2,2,0,2,1,1,
				
				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t',indexofFirstTab+1);
				indexofThirdTab = strLine.indexOf('\t',indexofSecondTab+1);
				indexofFourthTab = strLine.indexOf('\t',indexofThirdTab+1);
				indexofFifthTab = strLine.indexOf('\t',indexofFourthTab+1);
				indexofSixthTab = strLine.indexOf('\t',indexofFifthTab+1);
				indexofSeventhTab = strLine.indexOf('\t',indexofSixthTab+1);
				indexofEighthTab = strLine.indexOf('\t',indexofSeventhTab+1);
				indexofNinethTab = strLine.indexOf('\t',indexofEighthTab+1);
				indexofTenthTab = strLine.indexOf('\t',indexofNinethTab+1);
				indexofEleventhTab = strLine.indexOf('\t',indexofTenthTab+1);
				indexofTwelfthTab = strLine.indexOf('\t',indexofEleventhTab+1);
				indexofThirteenthTab = strLine.indexOf('\t',indexofTwelfthTab+1);
				
				refSeqName = strLine.substring(indexofFirstTab+1, indexofSecondTab);
				alternateGeneName = strLine.substring(indexofTwelfthTab+1, indexofThirteenthTab);
				
				alternateGeneNameList = humanRefSeqGeneName2AlternateGeneNameListMap.get(refSeqName);
				
				if (alternateGeneNameList == null){
					alternateGeneNameList = new ArrayList<String>();
					alternateGeneNameList.add(alternateGeneName);
					
					humanRefSeqGeneName2AlternateGeneNameListMap.put(refSeqName,alternateGeneNameList);
				} else{
					if (!alternateGeneNameList.contains(alternateGeneName)){
						alternateGeneNameList.add(alternateGeneName);
					}
				}
				
			}//End of WHILE
			
			bufferedReader.close();
			
		} catch (FileNotFoundException e) {
				e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void augmentGeneIdWithRefSeqGeneName(List<String> keggPathwayGeneIdList, List<String>  keggPathwayRefSeqGeneNameList,Map<String,List<String>> humanGeneId2RefSeqGeneNameListMap){
		List<String> refSeqGeneNameList;
		
		
		for(String geneId: keggPathwayGeneIdList) {
			refSeqGeneNameList = humanGeneId2RefSeqGeneNameListMap.get(geneId);
			
			if (refSeqGeneNameList!=null){
				for(String refSeqGeneName: refSeqGeneNameList){
					if (!keggPathwayRefSeqGeneNameList.contains(refSeqGeneName)){
						keggPathwayRefSeqGeneNameList.add(refSeqGeneName);
					}
				}
			}
		}
		
		
	}
	
	public static void augmentRefSeqGeneNamewithAlternateGeneName(List<String> keggPathwayRefSeqGeneNameList, List<String> keggPathwayAlternateGeneNameList,Map<String,List<String>> humanRefSeqGeneName2AlternateGeneNameListMap){
		List<String> alternateGeneNameList;
		
		for(String refSeqGeneName: keggPathwayRefSeqGeneNameList){
			alternateGeneNameList = humanRefSeqGeneName2AlternateGeneNameListMap.get(refSeqGeneName);
			
			if (alternateGeneNameList!=null){
				for(String alternateGeneName: alternateGeneNameList){
					if (!keggPathwayAlternateGeneNameList.contains(alternateGeneName)){
						keggPathwayAlternateGeneNameList.add(alternateGeneName);
					}
				}
			}
			
		}
	}
	

	/**
	 * 
	 */
	public HumanGenesAugmentation() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// for testing purposes
		Map<String,List<String>> humanGeneId2RefSeqGeneNameListMap = new HashMap<String, List<String>>();
		fillHumanGeneId2RefSeqGeneNameMap(humanGeneId2RefSeqGeneNameListMap);
		
		Map<String,List<String>> humanRefSeqGeneName2AlternateGeneNameListMap = new HashMap<String,List<String>>();
		fillHumanRefSeqGeneName2AlternateGeneNameMap(humanRefSeqGeneName2AlternateGeneNameListMap);
		
		
		List<String> keggPathwayGeneIdList = new ArrayList<String>();
		keggPathwayGeneIdList.add("10000");
		keggPathwayGeneIdList.add("1050");
		keggPathwayGeneIdList.add("11040");
		keggPathwayGeneIdList.add("1147");
		keggPathwayGeneIdList.add("1978");
		keggPathwayGeneIdList.add("207");
		keggPathwayGeneIdList.add("208");
		keggPathwayGeneIdList.add("2322");
		keggPathwayGeneIdList.add("23533");
		
		List<String>  keggPathwayRefSeqGeneNameList =  new ArrayList<String>();
		List<String>  keggPathwayAlternateGeneNameList =  new ArrayList<String>();
		
		
		augmentGeneIdWithRefSeqGeneName(keggPathwayGeneIdList, keggPathwayRefSeqGeneNameList,humanGeneId2RefSeqGeneNameListMap);		
		augmentRefSeqGeneNamewithAlternateGeneName(keggPathwayRefSeqGeneNameList, keggPathwayAlternateGeneNameList, humanRefSeqGeneName2AlternateGeneNameListMap);
		
		System.out.println("come here");
	}

}
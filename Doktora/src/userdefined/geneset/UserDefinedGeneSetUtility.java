/**
 * @author burcakotlu
 * @date Sep 26, 2014 
 * @time 5:06:55 PM
 */
package userdefined.geneset;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import augmentation.humangenes.HumanGenesAugmentation;
import auxiliary.FileOperations;
import ui.GlanetRunner;
import enumtypes.UserDefinedGeneSetInputType;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.TShortObjectMap;

/**
 * 
 */
public class UserDefinedGeneSetUtility {
	
	
	
	
	public static void updateMap(
			Integer geneID,
			TIntObjectMap<TShortArrayList> geneId2ListofUserDefinedGeneSetNumberMap,
			short userDefinedGeneSetNumber){
		TShortArrayList existingUserDefinedGeneSetNumberList = null;

		//fill geneId2ListofUserDefinedGenesetHashMap
		//Hash Map does not contain this ncbiGeneId
		if (!geneId2ListofUserDefinedGeneSetNumberMap.containsKey(geneID)){					
			TShortArrayList userDefinedGeneSetNumberList = new TShortArrayList();
			userDefinedGeneSetNumberList.add(userDefinedGeneSetNumber);
			geneId2ListofUserDefinedGeneSetNumberMap.put(geneID, userDefinedGeneSetNumberList);
			}
		//Hash Map contains this geneId
		else{
			existingUserDefinedGeneSetNumberList = geneId2ListofUserDefinedGeneSetNumberMap.get(geneID);
			
			if(!(existingUserDefinedGeneSetNumberList.contains(userDefinedGeneSetNumber))){
				existingUserDefinedGeneSetNumberList.add(userDefinedGeneSetNumber);
			} 
			
			geneId2ListofUserDefinedGeneSetNumberMap.put(geneID, existingUserDefinedGeneSetNumberList);
		}
	}
	
	
	public static void createNcbiGeneId2ListofUserDefinedGeneSetNumberMap(
			String dataFolder,
			String userDefinedGeneSetInputFile, 
			UserDefinedGeneSetInputType userDefinedGeneSetInputType,
			TObjectShortMap<String> userDefinedGeneSetName2UserDefinedGeneSetNumberMap,
			TShortObjectMap<String> userDefinedGeneSetNumber2UserDefinedGeneSetNameMap,
			TIntObjectMap<TShortArrayList> geneId2ListofUserDefinedGeneSetNumberMap){
		
		//Read the user defined geneset inputFile
		String strLine;
		int indexofFirstTab;
		String GO_ID;
		String geneInformation;
		
		short userDefinedGeneSetNumber = 0;
		short currentUserDefinedGeneSetNumber = 0;
		
		//In case of need: First fill these conversion maps
		Map<String,List<String>> geneSymbol2ListofRNANucleotideAccessionMap = null;
		Map<String,List<Integer>> RNANucleotideAccession2ListofGeneIdMap = null;
		
		List<String> RNANucleotideAccessionList = null;
		List<Integer> geneIDList = null;
		
		int numberofReadLines= 0;
		int numberofGeneSymbol2GeneIDLines = 0;
		
		 if(userDefinedGeneSetInputType.isGeneSymbol()){
	    	geneSymbol2ListofRNANucleotideAccessionMap = new HashMap<String,List<String>>();	    	
	    	RNANucleotideAccession2ListofGeneIdMap = new HashMap<String,List<Integer>>();
	    	
	    	HumanGenesAugmentation.fillGeneSymbol2ListofRNANucleotideAccessionMap(dataFolder,geneSymbol2ListofRNANucleotideAccessionMap);
	    	HumanGenesAugmentation.fillRNANucleotideAccession2ListofGeneIdMap(dataFolder,RNANucleotideAccession2ListofGeneIdMap);
	    	
	    }else if (userDefinedGeneSetInputType.isRNANucleotideAccession()){
	    	RNANucleotideAccession2ListofGeneIdMap = new HashMap<String,List<Integer>>();	    	
	      	
	    	HumanGenesAugmentation.fillRNANucleotideAccession2ListofGeneIdMap(dataFolder,RNANucleotideAccession2ListofGeneIdMap);   
	    }
	
	
		try {
			FileReader fileReader = FileOperations.createFileReader(userDefinedGeneSetInputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			while((strLine = bufferedReader.readLine())!=null){
				
				numberofReadLines++;
				
				indexofFirstTab = strLine.indexOf('\t');
				GO_ID = strLine.substring(0,indexofFirstTab);
				//geneInformation can be geneID, geneSymbol or RNANucleotideAccession
				geneInformation = strLine.substring(indexofFirstTab+1);
				
				//Fill name2number map
				//Fill number2name map
				if (!(userDefinedGeneSetName2UserDefinedGeneSetNumberMap.containsKey(GO_ID))){
					userDefinedGeneSetName2UserDefinedGeneSetNumberMap.put(GO_ID, userDefinedGeneSetNumber);
					userDefinedGeneSetNumber2UserDefinedGeneSetNameMap.put(userDefinedGeneSetNumber, GO_ID);
					
					//Increment UserDefinedGeneSetNumber
					userDefinedGeneSetNumber++;
					
					currentUserDefinedGeneSetNumber = (short) (userDefinedGeneSetNumber-1);
				
				}//End of IF
				
				if (userDefinedGeneSetInputType.isGeneSymbol()){
					
					//Convert geneSymbol to geneID
					RNANucleotideAccessionList = geneSymbol2ListofRNANucleotideAccessionMap.get(geneInformation);
					if (RNANucleotideAccessionList!=null){
							for(String RNANucleotideAccession:RNANucleotideAccessionList ){
							geneIDList = RNANucleotideAccession2ListofGeneIdMap.get(RNANucleotideAccession);
							if(geneIDList!=null){							
								for(int geneID: geneIDList){									
									numberofGeneSymbol2GeneIDLines++;
									updateMap(geneID,geneId2ListofUserDefinedGeneSetNumberMap,currentUserDefinedGeneSetNumber);									
								}//End of For: each geneID in the geneIDList
							}							
						}//End of For:  each RNANucleotideAccession in the RNANucleotideAccessionList
					}
										
				}else if (userDefinedGeneSetInputType.isRNANucleotideAccession()){
					//Convert RNANucleotideAccession to geneID
					geneIDList = RNANucleotideAccession2ListofGeneIdMap.get(geneInformation);
					if (geneIDList!=null){
						for(Integer geneID: geneIDList){
							updateMap(geneID,geneId2ListofUserDefinedGeneSetNumberMap,currentUserDefinedGeneSetNumber);		
						}//End of For: each geneID in the geneIDList
					}
					
				}else if (userDefinedGeneSetInputType.isGeneID()){
					//No conversion is needed
					int geneID = Integer.parseInt(geneInformation);
					updateMap(geneID,geneId2ListofUserDefinedGeneSetNumberMap,currentUserDefinedGeneSetNumber);
					
				}
							
				System.out.println("Number of Read Lines: " + numberofReadLines);
							
			}//End of While
					
			GlanetRunner.appendLog("Number of user defined genesets: " + userDefinedGeneSetNumber);
			GlanetRunner.appendLog("Number of Read Lines: " + numberofReadLines);
			GlanetRunner.appendLog("Number of Converted GeneSymbol 2 GeneID Lines: " + numberofGeneSymbol2GeneIDLines);
				
			bufferedReader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
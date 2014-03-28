/*
 * HumanRefSeq2Gene.java reads gene2refseq.txt
 * It filters the homo sapiens lines of gene2refseq.txt file and write it to human_gene2refseq.txt file.
 * 
 * Later on it converts the order and writes it to human_refseq2gene.txt  and human_refseq2gene2.txt file.
 * 
 * Example 	refseq gene id and 	gene id
 * 			NR_046536			100873973
 */

package ncbi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import auxiliary.FileOperations;

import common.Commons;


public class HumanRefSeq2Gene {
	
	public static void humanGene2RefSeq(String dataFolder,String outputFolder){
		
		FileReader fileReader;
		FileWriter fileWriter;
		
		BufferedReader bufferedReader;
		BufferedWriter bufferedWriter;
		
		String strLine;
		int indexofFirstTab;
		Integer taxId;
		int numberofHumanGene2RefseqLines = 0;
		
		try {
			fileReader = FileOperations.createFileReader(dataFolder + Commons.NCBI_GENE_TO_REF_SEQ);
			fileWriter = FileOperations.createFileWriter(outputFolder + Commons.NCBI_HUMAN_GENE_TO_REF_SEQ_DIRECTORYNAME,Commons.NCBI_HUMAN_GENE_TO_REF_SEQ_FILENAME);
		
			bufferedReader = new BufferedReader(fileReader);
			bufferedWriter = new BufferedWriter(fileWriter);
			
			//Skip first line
			strLine = bufferedReader.readLine();
			
			while((strLine=bufferedReader.readLine())!=null){
								
				indexofFirstTab = strLine.indexOf('\t');
				taxId = Integer.parseInt(strLine.substring(0, indexofFirstTab));
				
				if (taxId.equals(9606)){
					bufferedWriter.write(strLine+"\n");
					numberofHumanGene2RefseqLines++;
				}
							
			}
			
			System.out.print("Number of human gene 2 refseq lines " + numberofHumanGene2RefseqLines +"\n");
			
			bufferedWriter.close();
			bufferedReader.close();
			
						
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public static void humanRefSeq2Gene(String outputFolder){
		
		FileReader fileReader;
		FileWriter fileWriter;
		FileWriter fileWriter2;
		
		BufferedReader bufferedReader;
		BufferedWriter bufferedWriter;
		BufferedWriter bufferedWriter2;
		
		
		String strLine;
		int indexofFirstTab;
		int indexofSecondTab;
		int indexofThirdTab;
		int indexofFourthTab;
		int indexofDot;
		
		int geneId;
		String refSeqName =null;
		String refSeqNamewithDot = null;
		
		int numberofHumanRefSeq2Gene = 0;
		int numberofRefSeqNames = 0;
		
		Set<RefSeq2Gene> refSeq2GeneSet = new HashSet<RefSeq2Gene>();
//		Set<String> refSeqNames = new HashSet<String>();
		RefSeq2Gene refSeq2Gene = null;
		
		try {
			fileReader = FileOperations.createFileReader(outputFolder + Commons.NCBI_HUMAN_GENE_TO_REF_SEQ_DIRECTORYNAME, Commons.NCBI_HUMAN_GENE_TO_REF_SEQ_FILENAME);
			fileWriter = FileOperations.createFileWriter(outputFolder + Commons.NCBI_HUMAN_REF_SEQ_TO_GENE_DIRECTORYNAME, Commons.NCBI_HUMAN_REF_SEQ_TO_GENE_FILENAME);
			fileWriter2 = FileOperations.createFileWriter(outputFolder + Commons.NCBI_HUMAN_REF_SEQ_TO_GENE_2_DIRECTORYNAME,Commons.NCBI_HUMAN_REF_SEQ_TO_GENE_2_FILENAME);
			
			bufferedReader = new BufferedReader(fileReader);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter2 = new BufferedWriter(fileWriter2);
			
			while ((strLine= bufferedReader.readLine())!=null){
				//example Line from file Commons.NCBI_HUMAN_GENE_TO_REF_SEQ
				//9606	63976	REVIEWED	NM_022114.3	289547572	NP_071397.3	289547573	NC_000001.10	224589800	2985741	3355184	+	Reference GRCh37.p10 Primary Assembly

				indexofFirstTab = strLine.indexOf('\t');
				indexofSecondTab = strLine.indexOf('\t',indexofFirstTab+1);
				indexofThirdTab = strLine.indexOf('\t',indexofSecondTab+1);
				indexofFourthTab = strLine.indexOf('\t', indexofThirdTab+1);
				
				geneId = Integer.parseInt(strLine.substring(indexofFirstTab+1, indexofSecondTab));
				refSeqNamewithDot = strLine.substring(indexofThirdTab+1, indexofFourthTab);
		
				indexofDot = refSeqNamewithDot.indexOf('.');
				if (indexofDot>=0){
					refSeqName = refSeqNamewithDot.substring(0, indexofDot);
				}
				
				if (!(refSeqNamewithDot.equals("-"))){
					refSeq2Gene = new RefSeq2Gene();
					
					refSeq2Gene.setGeneId(geneId);
					refSeq2Gene.setRefSeqName(refSeqName);
					refSeq2Gene.setRefSeqNamewithDot(refSeqNamewithDot);
					
					if (!(refSeq2GeneSet.contains(refSeq2Gene))){
						refSeq2GeneSet.add(refSeq2Gene);
						bufferedWriter.write(refSeqName + "\t" + geneId + "\n" );
						bufferedWriter2.write(refSeqNamewithDot + "\t" + geneId + "\n");
						numberofHumanRefSeq2Gene++;									
					}else{
						refSeq2Gene= null;
					}
															
				}							
			} // End of While
			
			System.out.println("Number of human refseq to gene lines " + numberofHumanRefSeq2Gene);			
			System.out.println("Number of refseq names " + numberofRefSeqNames);			
			
			bufferedWriter.close();
			bufferedWriter2.close();
			bufferedReader.close();
			refSeq2GeneSet = null;
						
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
	public static void run(String[] args) {
		
		String glanetFolder = args[1];
		String dataFolder = glanetFolder + System.getProperty("file.separator") + Commons.DATA + System.getProperty("file.separator") ;
		String outputFolder = glanetFolder + System.getProperty("file.separator") + Commons.OUTPUT + System.getProperty("file.separator") ;
		
		humanGene2RefSeq(dataFolder,outputFolder);
		humanRefSeq2Gene(outputFolder);

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
	public static void main(String[] args) {
		
		String glanetFolder = args[1];
		String dataFolder = glanetFolder + System.getProperty("file.separator") + Commons.DATA + System.getProperty("file.separator") ;
		String outputFolder = glanetFolder + System.getProperty("file.separator") + Commons.OUTPUT + System.getProperty("file.separator") ;
		
		humanGene2RefSeq(dataFolder,outputFolder);
		humanRefSeq2Gene(outputFolder);

	}

}

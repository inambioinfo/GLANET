/**
 * @author burcakotlu
 * @date Apr 4, 2014 
 * @time 4:08:27 PM
 */
package jaxbxjctool;

import java.util.List;

/**
 * 
 */
public class SNP{
	
	String SNPKey;
	
	String chrNamewithoutPreceedingChr;
	
	
	int snpOneBasedCoordinate;
	int snpZeroBasedCoordinate;
	
	List<String> observedAlleles;
	List<String> alteredSequences;
	
	String referenceSequence;
	String fastaFile;
	


	
	public int getSnpZeroBasedCoordinate() {
		return snpZeroBasedCoordinate;
	}




	public void setSnpZeroBasedCoordinate(int snpZeroBasedCoordinate) {
		this.snpZeroBasedCoordinate = snpZeroBasedCoordinate;
	}




	public String getFastaFile() {
		return fastaFile;
	}




	public void setFastaFile(String fastaFile) {
		this.fastaFile = fastaFile;
	}




	public String getChrNamewithoutPreceedingChr() {
		return chrNamewithoutPreceedingChr;
	}




	public void setChrNamewithoutPreceedingChr(String chrNamewithoutPreceedingChr) {
		this.chrNamewithoutPreceedingChr = chrNamewithoutPreceedingChr;
	}




	public int getSnpOneBasedCoordinate() {
		return snpOneBasedCoordinate;
	}




	public void setSnpOneBasedCoordinate(int snpOneBasedCoordinate) {
		this.snpOneBasedCoordinate = snpOneBasedCoordinate;
	}




	public List<String> getObservedAlleles() {
		return observedAlleles;
	}




	public void setObservedAlleles(List<String> observedAlleles) {
		this.observedAlleles = observedAlleles;
	}




	public String getReferenceSequence() {
		return referenceSequence;
	}




	public void setReferenceSequence(String referenceSequence) {
		this.referenceSequence = referenceSequence;
	}




	public List<String> getAlteredSequences() {
		return alteredSequences;
	}




	public void setAlteredSequences(List<String> alteredSequences) {
		this.alteredSequences = alteredSequences;
	}




	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

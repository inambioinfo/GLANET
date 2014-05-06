/**
 * @author burcakotlu
 * @date May 2, 2014 
 * @time 2:20:29 PM
 */
package intervaltree;

import common.Commons;

/**
 * 
 */
public class UcscRefSeqGeneIntervalTreeNode extends IntervalTreeNode{
	
//	Just for search UCSC RefSeq Gene output
	char strand;
	String  refSeqGeneName;
	Integer geneEntrezId;
	String intervalName;
	String geneHugoSymbol;
	
	public String getChromName() {
		return chromName;
	}


	public void setChromName(String chromName) {
		this.chromName = chromName;
	}


	public String getRefSeqGeneName() {
		return refSeqGeneName;
	}


	public void setRefSeqGeneName(String refSeqGeneName) {
		this.refSeqGeneName = refSeqGeneName;
	}


	public String getIntervalName() {
		return intervalName;
	}


	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}


	public String getGeneHugoSymbol() {
		return geneHugoSymbol;
	}


	public void setGeneHugoSymbol(String geneHugoSymbol) {
		this.geneHugoSymbol = geneHugoSymbol;
	}

	
	public char getStrand() {
		return strand;
	}


	public void setStrand(char strand) {
		this.strand = strand;
	}


	public Integer getGeneEntrezId() {
		return geneEntrezId;
	}
	
	
	public void setGeneEntrezId(Integer geneEntrezId) {
		this.geneEntrezId = geneEntrezId;
	}
	

	
	//For Exon Based Kegg Pathway Enrichment Analysis Ucsc gene
	public UcscRefSeqGeneIntervalTreeNode(String chromName, int low, int high, Integer geneEntrezId, String intervalName,String nodeType) {
			super();
			this.low = low;
			this.high = high;
			this.chromName = chromName;
			this.geneEntrezId = geneEntrezId;
			this.intervalName = intervalName;
			
			this.left = new IntervalTreeNode();
			this.right = new IntervalTreeNode();
			this.parent = new IntervalTreeNode();
			this.name = Commons.NOT_SENTINEL;
			
			this.nodeType = nodeType;
			this.numberofBases = high-low+1;
			
			
	}
	
	
	//For Ucsc gene without strand attribute
	public UcscRefSeqGeneIntervalTreeNode(String chromName, int low, int high, String refSeqGeneName, Integer geneEntrezId, String intervalName, String geneHugoSymbol,String nodeType) {
		super();
		this.low = low;
		this.high = high;
		this.chromName = chromName;
		this.refSeqGeneName = refSeqGeneName;
		this.geneEntrezId = geneEntrezId;
		this.intervalName = intervalName;
		this.geneHugoSymbol = geneHugoSymbol;
		
		this.left = new IntervalTreeNode();
		this.right = new IntervalTreeNode();
		this.parent = new IntervalTreeNode();
		this.name = Commons.NOT_SENTINEL;
		this.nodeType = nodeType;
		this.numberofBases = high-low+1;
		
	}
		
		
	//For Ucsc gene with strand attribute
	public UcscRefSeqGeneIntervalTreeNode(String chromName, int low, int high, String refSeqGeneName, Integer geneEntrezId, String intervalName,char strand, String geneHugoSymbol,String nodeType) {
		super();
		this.low = low;
		this.high = high;
		this.chromName = chromName;
		this.refSeqGeneName = refSeqGeneName;
		this.geneEntrezId = geneEntrezId;
		this.intervalName = intervalName;
		this.strand = strand;
		this.geneHugoSymbol = geneHugoSymbol;
		
		this.left = new IntervalTreeNode();
		this.right = new IntervalTreeNode();
		this.parent = new IntervalTreeNode();
		this.name = Commons.NOT_SENTINEL;
		this.nodeType = nodeType;
		this.numberofBases = high-low+1;
		
	}


}
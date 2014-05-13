/**
 * @author burcakotlu
 * @date May 12, 2014 
 * @time 10:47:47 AM
 */
package intervaltree;

import common.Commons;

/**
 * 
 */
public enum IntervalName {
	
	
	EXON(1),
	INTRON(2),
	_5P1(3),
	_5P2(4),
	_3P1(5),
	_3P2(6);

	
	
	
//	public static final String EXON = "EXON";
//	public static final String INTRON = "INTRON";
//	public static final String FIVE_P_ONE = "5P1";
//	public static final String FIVE_P_TWO = "5P2";
//	public static final String THREE_P_ONE = "3P1";
//	public static final String THREE_P_TWO = "3P2";

	
	private final int intervalName;
	
	
  /* 
    * This constructor is private.
    * Legal to declare a non-private constructor, but not legal
    * to use such a constructor outside the enum.
    * Can never use "new" with any enum, even inside the enum 
    * class itself.
    */
    private IntervalName(int intervalName) {
    	this.intervalName = intervalName;
	}
		
    
    public String getIntervalName(){
    	if (this.equals(IntervalName.EXON))
    		return Commons.EXON;
    	else if (this.equals(IntervalName.INTRON))
    		return Commons.INTRON;
    	else if (this.equals(IntervalName._5P1))
    		return Commons.FIVE_P_ONE;
    	else if (this.equals(IntervalName._5P2))
    		return Commons.FIVE_P_TWO;
    	else if (this.equals(IntervalName._3P1))
    		return Commons.THREE_P_ONE;
    	else if (this.equals(IntervalName._3P2))
    		return Commons.THREE_P_TWO;	
    	else
    		return null;   		
    }
    
    /** An added method.  */
    public boolean isExon() {
     return  this == EXON;
    }
    
    /** An added method.  */
    public boolean isIntron() {
     return  this == INTRON;
    }
	     
		
    /** An added method.  */
    public boolean isFivePOne() {
     return  this == _5P1;
    }
    
    /** An added method.  */
    public boolean isFivePTwo() {
     return  this == _5P2;
    }
    
    /** An added method.  */
    public boolean isThreePOne() {
     return  this == _3P1;
    }
    
    /** An added method.  */
    public boolean isThreePTwo() {
     return  this == _3P2;
    }
}
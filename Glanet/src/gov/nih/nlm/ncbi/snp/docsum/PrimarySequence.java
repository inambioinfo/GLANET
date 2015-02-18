//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.11.25 at 09:13:08 AM EET 
//

package gov.nih.nlm.ncbi.snp.docsum;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ncbi.nlm.nih.gov/SNP/docsum}MapLoc" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="dbSnpBuild" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="gi" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="source">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="submitter"/>
 *             &lt;enumeration value="blastmb"/>
 *             &lt;enumeration value="xm"/>
 *             &lt;enumeration value="remap"/>
 *             &lt;enumeration value="hgvs"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="accession" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "mapLoc" })
@XmlRootElement(name = "PrimarySequence")
public class PrimarySequence {

	@XmlElement(name = "MapLoc", required = true)
	protected List<MapLoc> mapLoc;
	@XmlAttribute(name = "dbSnpBuild", required = true)
	protected int dbSnpBuild;
	@XmlAttribute(name = "gi", required = true)
	protected int gi;
	@XmlAttribute(name = "source")
	protected String source;
	@XmlAttribute(name = "accession")
	protected String accession;

	/**
	 * Gets the value of the mapLoc property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the mapLoc property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMapLoc().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link MapLoc }
	 * 
	 * 
	 */
	public List<MapLoc> getMapLoc() {
		if (mapLoc == null) {
			mapLoc = new ArrayList<MapLoc>();
		}
		return this.mapLoc;
	}

	/**
	 * Gets the value of the dbSnpBuild property.
	 * 
	 */
	public int getDbSnpBuild() {
		return dbSnpBuild;
	}

	/**
	 * Sets the value of the dbSnpBuild property.
	 * 
	 */
	public void setDbSnpBuild(int value) {
		this.dbSnpBuild = value;
	}

	/**
	 * Gets the value of the gi property.
	 * 
	 */
	public int getGi() {
		return gi;
	}

	/**
	 * Sets the value of the gi property.
	 * 
	 */
	public void setGi(int value) {
		this.gi = value;
	}

	/**
	 * Gets the value of the source property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the value of the source property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSource(String value) {
		this.source = value;
	}

	/**
	 * Gets the value of the accession property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAccession() {
		return accession;
	}

	/**
	 * Sets the value of the accession property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAccession(String value) {
		this.accession = value;
	}

}

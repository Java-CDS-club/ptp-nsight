//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.22 at 07:40:58 PM CST 
//

package org.eclipse.ptp.rm.jaxb.core.data;

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
 *         &lt;element name="parse-lines" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{}token" maxOccurs="unbounded"/>
 *         &lt;element ref="{}put" maxOccurs="unbounded"/>
 *         &lt;element ref="{}set" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="stderr" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "parseLines", "token", "put", "set" })
@XmlRootElement(name = "stream-parser")
public class StreamParser {

	@XmlElement(name = "parse-lines", required = true)
	protected String parseLines;
	@XmlElement(required = true)
	protected List<Token> token;
	@XmlElement(required = true)
	protected List<Put> put;
	@XmlElement(required = true)
	protected List<Set> set;
	@XmlAttribute(required = true)
	protected String name;
	@XmlAttribute
	protected Boolean stderr;

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the value of the parseLines property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getParseLines() {
		return parseLines;
	}

	/**
	 * Gets the value of the put property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the put property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPut().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Put }
	 * 
	 * 
	 */
	public List<Put> getPut() {
		if (put == null) {
			put = new ArrayList<Put>();
		}
		return this.put;
	}

	/**
	 * Gets the value of the set property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the set property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSet().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Set }
	 * 
	 * 
	 */
	public List<Set> getSet() {
		if (set == null) {
			set = new ArrayList<Set>();
		}
		return this.set;
	}

	/**
	 * Gets the value of the token property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the token property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getToken().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Token }
	 * 
	 * 
	 */
	public List<Token> getToken() {
		if (token == null) {
			token = new ArrayList<Token>();
		}
		return this.token;
	}

	/**
	 * Gets the value of the stderr property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isStderr() {
		return stderr;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Sets the value of the parseLines property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setParseLines(String value) {
		this.parseLines = value;
	}

	/**
	 * Sets the value of the stderr property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setStderr(Boolean value) {
		this.stderr = value;
	}

}

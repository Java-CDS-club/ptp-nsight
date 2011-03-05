//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.03.04 at 11:30:15 PM CST 
//

package org.eclipse.ptp.rm.jaxb.core.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="horizontalSpacing" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="makeColumnsEqualWidth" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="marginBottom" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginHeight" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginLeft" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginRight" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginTop" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginWidth" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="numColumns" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="verticalSpacing" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "grid-layout")
public class GridLayout {

	@XmlAttribute
	protected Integer horizontalSpacing;
	@XmlAttribute
	protected Boolean makeColumnsEqualWidth;
	@XmlAttribute
	protected Integer marginBottom;
	@XmlAttribute
	protected Integer marginHeight;
	@XmlAttribute
	protected Integer marginLeft;
	@XmlAttribute
	protected Integer marginRight;
	@XmlAttribute
	protected Integer marginTop;
	@XmlAttribute
	protected Integer marginWidth;
	@XmlAttribute
	protected Integer numColumns;
	@XmlAttribute
	protected Integer verticalSpacing;

	/**
	 * Gets the value of the horizontalSpacing property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getHorizontalSpacing() {
		return horizontalSpacing;
	}

	/**
	 * Gets the value of the marginBottom property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getMarginBottom() {
		return marginBottom;
	}

	/**
	 * Gets the value of the marginHeight property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getMarginHeight() {
		return marginHeight;
	}

	/**
	 * Gets the value of the marginLeft property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getMarginLeft() {
		return marginLeft;
	}

	/**
	 * Gets the value of the marginRight property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getMarginRight() {
		return marginRight;
	}

	/**
	 * Gets the value of the marginTop property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getMarginTop() {
		return marginTop;
	}

	/**
	 * Gets the value of the marginWidth property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getMarginWidth() {
		return marginWidth;
	}

	/**
	 * Gets the value of the numColumns property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getNumColumns() {
		return numColumns;
	}

	/**
	 * Gets the value of the verticalSpacing property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getVerticalSpacing() {
		return verticalSpacing;
	}

	/**
	 * Gets the value of the makeColumnsEqualWidth property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isMakeColumnsEqualWidth() {
		if (makeColumnsEqualWidth == null) {
			return true;
		} else {
			return makeColumnsEqualWidth;
		}
	}

	/**
	 * Sets the value of the horizontalSpacing property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setHorizontalSpacing(Integer value) {
		this.horizontalSpacing = value;
	}

	/**
	 * Sets the value of the makeColumnsEqualWidth property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setMakeColumnsEqualWidth(Boolean value) {
		this.makeColumnsEqualWidth = value;
	}

	/**
	 * Sets the value of the marginBottom property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setMarginBottom(Integer value) {
		this.marginBottom = value;
	}

	/**
	 * Sets the value of the marginHeight property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setMarginHeight(Integer value) {
		this.marginHeight = value;
	}

	/**
	 * Sets the value of the marginLeft property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setMarginLeft(Integer value) {
		this.marginLeft = value;
	}

	/**
	 * Sets the value of the marginRight property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setMarginRight(Integer value) {
		this.marginRight = value;
	}

	/**
	 * Sets the value of the marginTop property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setMarginTop(Integer value) {
		this.marginTop = value;
	}

	/**
	 * Sets the value of the marginWidth property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setMarginWidth(Integer value) {
		this.marginWidth = value;
	}

	/**
	 * Sets the value of the numColumns property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setNumColumns(Integer value) {
		this.numColumns = value;
	}

	/**
	 * Sets the value of the verticalSpacing property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setVerticalSpacing(Integer value) {
		this.verticalSpacing = value;
	}

}

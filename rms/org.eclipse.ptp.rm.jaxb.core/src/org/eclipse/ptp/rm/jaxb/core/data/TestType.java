//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.29 at 04:46:20 PM EDT 
//


package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for test-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="test-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="test" type="{http://org.eclipse.ptp/rm}test-type" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="2" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="add" type="{http://org.eclipse.ptp/rm}add-type"/>
 *           &lt;element name="append" type="{http://org.eclipse.ptp/rm}append-type"/>
 *           &lt;element name="put" type="{http://org.eclipse.ptp/rm}put-type"/>
 *           &lt;element name="set" type="{http://org.eclipse.ptp/rm}set-type"/>
 *           &lt;element name="throw" type="{http://org.eclipse.ptp/rm}throw-type"/>
 *         &lt;/choice>
 *         &lt;element name="else" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice maxOccurs="unbounded">
 *                   &lt;element name="add" type="{http://org.eclipse.ptp/rm}add-type"/>
 *                   &lt;element name="append" type="{http://org.eclipse.ptp/rm}append-type"/>
 *                   &lt;element name="put" type="{http://org.eclipse.ptp/rm}put-type"/>
 *                   &lt;element name="set" type="{http://org.eclipse.ptp/rm}set-type"/>
 *                   &lt;element name="throw" type="{http://org.eclipse.ptp/rm}throw-type"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="op">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="EQ"/>
 *             &lt;enumeration value="LT"/>
 *             &lt;enumeration value="GT"/>
 *             &lt;enumeration value="LE"/>
 *             &lt;enumeration value="GE"/>
 *             &lt;enumeration value="AND"/>
 *             &lt;enumeration value="OR"/>
 *             &lt;enumeration value="NOT"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "test-type", propOrder = {
    "test",
    "value",
    "addOrAppendOrPut",
    "_else"
})
public class TestType {

    protected List<TestType> test;
    protected List<String> value;
    @XmlElements({
        @XmlElement(name = "set", type = SetType.class),
        @XmlElement(name = "put", type = PutType.class),
        @XmlElement(name = "append", type = AppendType.class),
        @XmlElement(name = "throw", type = ThrowType.class),
        @XmlElement(name = "add", type = AddType.class)
    })
    protected List<Object> addOrAppendOrPut;
    @XmlElement(name = "else")
    protected TestType.Else _else;
    @XmlAttribute
    protected String op;

    /**
     * Gets the value of the test property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the test property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TestType }
     * 
     * 
     */
    public List<TestType> getTest() {
        if (test == null) {
            test = new ArrayList<TestType>();
        }
        return this.test;
    }

    /**
     * Gets the value of the value property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the value property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getValue() {
        if (value == null) {
            value = new ArrayList<String>();
        }
        return this.value;
    }

    /**
     * Gets the value of the addOrAppendOrPut property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addOrAppendOrPut property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddOrAppendOrPut().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SetType }
     * {@link PutType }
     * {@link AppendType }
     * {@link ThrowType }
     * {@link AddType }
     * 
     * 
     */
    public List<Object> getAddOrAppendOrPut() {
        if (addOrAppendOrPut == null) {
            addOrAppendOrPut = new ArrayList<Object>();
        }
        return this.addOrAppendOrPut;
    }

    /**
     * Gets the value of the else property.
     * 
     * @return
     *     possible object is
     *     {@link TestType.Else }
     *     
     */
    public TestType.Else getElse() {
        return _else;
    }

    /**
     * Sets the value of the else property.
     * 
     * @param value
     *     allowed object is
     *     {@link TestType.Else }
     *     
     */
    public void setElse(TestType.Else value) {
        this._else = value;
    }

    /**
     * Gets the value of the op property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOp() {
        return op;
    }

    /**
     * Sets the value of the op property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOp(String value) {
        this.op = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice maxOccurs="unbounded">
     *         &lt;element name="add" type="{http://org.eclipse.ptp/rm}add-type"/>
     *         &lt;element name="append" type="{http://org.eclipse.ptp/rm}append-type"/>
     *         &lt;element name="put" type="{http://org.eclipse.ptp/rm}put-type"/>
     *         &lt;element name="set" type="{http://org.eclipse.ptp/rm}set-type"/>
     *         &lt;element name="throw" type="{http://org.eclipse.ptp/rm}throw-type"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "addOrAppendOrPut"
    })
    public static class Else {

        @XmlElements({
            @XmlElement(name = "throw", type = ThrowType.class),
            @XmlElement(name = "set", type = SetType.class),
            @XmlElement(name = "add", type = AddType.class),
            @XmlElement(name = "put", type = PutType.class),
            @XmlElement(name = "append", type = AppendType.class)
        })
        protected List<Object> addOrAppendOrPut;

        /**
         * Gets the value of the addOrAppendOrPut property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the addOrAppendOrPut property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAddOrAppendOrPut().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ThrowType }
         * {@link SetType }
         * {@link AddType }
         * {@link PutType }
         * {@link AppendType }
         * 
         * 
         */
        public List<Object> getAddOrAppendOrPut() {
            if (addOrAppendOrPut == null) {
                addOrAppendOrPut = new ArrayList<Object>();
            }
            return this.addOrAppendOrPut;
        }

    }

}

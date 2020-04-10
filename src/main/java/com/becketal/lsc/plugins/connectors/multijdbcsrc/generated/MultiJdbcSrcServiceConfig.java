//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.04.10 um 05:19:08 PM CEST 
//


package com.becketal.lsc.plugins.connectors.multijdbcsrc.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="interval" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="requestNameForList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestNameForObject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestNameForNextId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestNameForClean" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="multivaluehook" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "interval",
    "requestNameForList",
    "requestNameForObject",
    "requestNameForNextId",
    "requestNameForClean",
    "multivaluehook"
})
@XmlRootElement(name = "MultiJdbcSrcServiceConfig")
public class MultiJdbcSrcServiceConfig {

    @XmlElement(defaultValue = "5")
    protected Integer interval;
    protected String requestNameForList;
    protected String requestNameForObject;
    protected String requestNameForNextId;
    protected String requestNameForClean;
    @XmlElement(required = true)
    protected String multivaluehook;

    /**
     * Ruft den Wert der interval-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getInterval() {
        return interval;
    }

    /**
     * Legt den Wert der interval-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setInterval(Integer value) {
        this.interval = value;
    }

    /**
     * Ruft den Wert der requestNameForList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestNameForList() {
        return requestNameForList;
    }

    /**
     * Legt den Wert der requestNameForList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestNameForList(String value) {
        this.requestNameForList = value;
    }

    /**
     * Ruft den Wert der requestNameForObject-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestNameForObject() {
        return requestNameForObject;
    }

    /**
     * Legt den Wert der requestNameForObject-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestNameForObject(String value) {
        this.requestNameForObject = value;
    }

    /**
     * Ruft den Wert der requestNameForNextId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestNameForNextId() {
        return requestNameForNextId;
    }

    /**
     * Legt den Wert der requestNameForNextId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestNameForNextId(String value) {
        this.requestNameForNextId = value;
    }

    /**
     * Ruft den Wert der requestNameForClean-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestNameForClean() {
        return requestNameForClean;
    }

    /**
     * Legt den Wert der requestNameForClean-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestNameForClean(String value) {
        this.requestNameForClean = value;
    }

    /**
     * Ruft den Wert der multivaluehook-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMultivaluehook() {
        return multivaluehook;
    }

    /**
     * Legt den Wert der multivaluehook-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMultivaluehook(String value) {
        this.multivaluehook = value;
    }

}

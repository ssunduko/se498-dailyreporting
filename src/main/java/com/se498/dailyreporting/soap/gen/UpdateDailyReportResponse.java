//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.03.03 at 02:57:17 AM PST 
//


package com.se498.dailyreporting.soap.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="dailyReport" type="{http://se498.com/dailyreporting/soap}dailyReportType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dailyReport"
})
@XmlRootElement(name = "updateDailyReportResponse", namespace = "http://se498.com/dailyreporting/soap")
public class UpdateDailyReportResponse {

    @XmlElement(namespace = "http://se498.com/dailyreporting/soap", required = true)
    protected DailyReportType dailyReport;

    /**
     * Gets the value of the dailyReport property.
     * 
     * @return
     *     possible object is
     *     {@link DailyReportType }
     *     
     */
    public DailyReportType getDailyReport() {
        return dailyReport;
    }

    /**
     * Sets the value of the dailyReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link DailyReportType }
     *     
     */
    public void setDailyReport(DailyReportType value) {
        this.dailyReport = value;
    }

}

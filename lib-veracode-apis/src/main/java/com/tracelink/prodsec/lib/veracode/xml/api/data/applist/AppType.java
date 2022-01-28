//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.06.08 at 09:30:33 PM EDT 
//


package com.tracelink.prodsec.lib.veracode.xml.api.data.applist;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * The app type element contains the key elements and attributes
 * that reflect the data we store for an app.
 * <p>
 * * policy_updated_date represents the last time an action occurred which might have affected policy compliance.
 *
 *
 * <p>Java class for AppType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AppType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="app_id" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="app_name" type="{https://analysiscenter.veracode.com/schema/2.0/applist}LongRequiredTextType" /&gt;
 *       &lt;attribute name="vendor_name" type="{https://analysiscenter.veracode.com/schema/2.0/applist}LongTextType" /&gt;
 *       &lt;attribute name="policy_updated_date" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AppType", namespace = "https://analysiscenter.veracode.com/schema/2.0/applist")
public class AppType {

	@XmlAttribute(name = "app_id")
	private Long appId;
	@XmlAttribute(name = "app_name")
	private String appName;
	@XmlAttribute(name = "vendor_name")
	private String vendorName;
	@XmlAttribute(name = "policy_updated_date")
	private String policyUpdatedDate;

	/**
	 * Gets the value of the appId property.
	 *
	 * @return possible object is
	 * {@link Long }
	 */
	public Long getAppId() {
		return appId;
	}

	/**
	 * Sets the value of the appId property.
	 *
	 * @param value allowed object is
	 *              {@link Long }
	 */
	public void setAppId(Long value) {
		this.appId = value;
	}

	/**
	 * Gets the value of the appName property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * Sets the value of the appName property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setAppName(String value) {
		this.appName = value;
	}

	/**
	 * Gets the value of the vendorName property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getVendorName() {
		return vendorName;
	}

	/**
	 * Sets the value of the vendorName property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setVendorName(String value) {
		this.vendorName = value;
	}

	/**
	 * Gets the value of the policyUpdatedDate property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getPolicyUpdatedDate() {
		return policyUpdatedDate;
	}

	/**
	 * Sets the value of the policyUpdatedDate property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setPolicyUpdatedDate(String value) {
		this.policyUpdatedDate = value;
	}

}
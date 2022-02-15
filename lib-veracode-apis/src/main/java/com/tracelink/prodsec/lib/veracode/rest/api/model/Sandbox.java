/*
 * Veracode Applications API Specification
 * Veracode Applications API Documentation  Use one of the following base URLs depending on the region for your account: * https://api.veracode.com/ - Veracode US Region (default) * https://api.veracode.eu/ - Veracode European Region * https://api.veracode.us/ - Veracode US Federal Region
 *
 * OpenAPI spec version: 1.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.tracelink.prodsec.lib.veracode.rest.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.threeten.bp.OffsetDateTime;

import com.google.gson.annotations.SerializedName;

import io.swagger.annotations.ApiModelProperty;

/**
 * Sandbox
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2022-02-13T02:06:38.897Z")
public class Sandbox {
  @SerializedName("application_guid")
  private String applicationGuid = null;

  @SerializedName("auto_recreate")
  private Boolean autoRecreate = null;

  @SerializedName("created")
  private OffsetDateTime created = null;

  @SerializedName("custom_fields")
  private List<CustomNameValue> customFields = null;

  @SerializedName("guid")
  private String guid = null;

  @SerializedName("id")
  private Integer id = null;

  @SerializedName("modified")
  private OffsetDateTime modified = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("organization_id")
  private Integer organizationId = null;

  @SerializedName("owner_username")
  private String ownerUsername = null;

  public Sandbox applicationGuid(String applicationGuid) {
    this.applicationGuid = applicationGuid;
    return this;
  }

   /**
   * Get applicationGuid
   * @return applicationGuid
  **/
  @ApiModelProperty(value = "")
  public String getApplicationGuid() {
    return applicationGuid;
  }

  public void setApplicationGuid(String applicationGuid) {
    this.applicationGuid = applicationGuid;
  }

  public Sandbox autoRecreate(Boolean autoRecreate) {
    this.autoRecreate = autoRecreate;
    return this;
  }

   /**
   * Get autoRecreate
   * @return autoRecreate
  **/
  @ApiModelProperty(value = "")
  public Boolean isAutoRecreate() {
    return autoRecreate;
  }

  public void setAutoRecreate(Boolean autoRecreate) {
    this.autoRecreate = autoRecreate;
  }

   /**
   * The date and time when the sandbox was created. The date/time format is per RFC3339 and ISO-8601, and the timezone is UTC. Example: 2019-04-12T23:20:50.52Z.
   * @return created
  **/
  @ApiModelProperty(value = "The date and time when the sandbox was created. The date/time format is per RFC3339 and ISO-8601, and the timezone is UTC. Example: 2019-04-12T23:20:50.52Z.")
  public OffsetDateTime getCreated() {
    return created;
  }

  public Sandbox customFields(List<CustomNameValue> customFields) {
    this.customFields = customFields;
    return this;
  }

  public Sandbox addCustomFieldsItem(CustomNameValue customFieldsItem) {
    if (this.customFields == null) {
      this.customFields = new ArrayList<CustomNameValue>();
    }
    this.customFields.add(customFieldsItem);
    return this;
  }

   /**
   * Get customFields
   * @return customFields
  **/
  @ApiModelProperty(value = "")
  public List<CustomNameValue> getCustomFields() {
    return customFields;
  }

  public void setCustomFields(List<CustomNameValue> customFields) {
    this.customFields = customFields;
  }

   /**
   * Unique identifier (UUID).
   * @return guid
  **/
  @ApiModelProperty(value = "Unique identifier (UUID).")
  public String getGuid() {
    return guid;
  }

   /**
   * Internal ID.
   * @return id
  **/
  @ApiModelProperty(value = "Internal ID.")
  public Integer getId() {
    return id;
  }

   /**
   * The date and time when the sandbox was modified. The date/time format is per RFC3339 and ISO-8601, and the timezone is UTC. Example: 2019-04-12T23:20:50.52Z.
   * @return modified
  **/
  @ApiModelProperty(value = "The date and time when the sandbox was modified. The date/time format is per RFC3339 and ISO-8601, and the timezone is UTC. Example: 2019-04-12T23:20:50.52Z.")
  public OffsetDateTime getModified() {
    return modified;
  }

  public Sandbox name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The sandbox name
   * @return name
  **/
  @ApiModelProperty(value = "The sandbox name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Sandbox organizationId(Integer organizationId) {
    this.organizationId = organizationId;
    return this;
  }

   /**
   * Get organizationId
   * @return organizationId
  **/
  @ApiModelProperty(value = "")
  public Integer getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(Integer organizationId) {
    this.organizationId = organizationId;
  }

  public Sandbox ownerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
    return this;
  }

   /**
   * Get ownerUsername
   * @return ownerUsername
  **/
  @ApiModelProperty(value = "")
  public String getOwnerUsername() {
    return ownerUsername;
  }

  public void setOwnerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sandbox sandbox = (Sandbox) o;
    return Objects.equals(this.applicationGuid, sandbox.applicationGuid) &&
        Objects.equals(this.autoRecreate, sandbox.autoRecreate) &&
        Objects.equals(this.created, sandbox.created) &&
        Objects.equals(this.customFields, sandbox.customFields) &&
        Objects.equals(this.guid, sandbox.guid) &&
        Objects.equals(this.id, sandbox.id) &&
        Objects.equals(this.modified, sandbox.modified) &&
        Objects.equals(this.name, sandbox.name) &&
        Objects.equals(this.organizationId, sandbox.organizationId) &&
        Objects.equals(this.ownerUsername, sandbox.ownerUsername);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationGuid, autoRecreate, created, customFields, guid, id, modified, name, organizationId, ownerUsername);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Sandbox {\n");
    
    sb.append("    applicationGuid: ").append(toIndentedString(applicationGuid)).append("\n");
    sb.append("    autoRecreate: ").append(toIndentedString(autoRecreate)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
    sb.append("    customFields: ").append(toIndentedString(customFields)).append("\n");
    sb.append("    guid: ").append(toIndentedString(guid)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    modified: ").append(toIndentedString(modified)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    organizationId: ").append(toIndentedString(organizationId)).append("\n");
    sb.append("    ownerUsername: ").append(toIndentedString(ownerUsername)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}


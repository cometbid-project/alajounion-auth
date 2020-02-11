/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alajounion.api.secure.embeddables;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Gbenga
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UsrLoginLoc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4578947272482201036L;

	@JsonProperty("ip_addr")
	@Field(name = "IP_ADDR")
	private String ipAddr;

	@JsonProperty("isp_addr")
	@Field(name = "ISP_ADDR")
	private String ispAddr;

	@JsonProperty("device_id")
	@Field(name = "DEVICE_ID")
	private String deviceId;

	@JsonProperty("device_type")
	@Field(name = "DEVICE_TYPE")
	private String deviceType;

	@JsonProperty("user_agent")
	@Field(name = "USER_AGENT")
	private String userAgent;

	@JsonProperty("country_code")
	@Field(name = "COUNTRY_CODE")
	private String countryCode;

	@JsonProperty("state_code")
	@Field(name = "STATE_CODE")
	private String stateCode;

	@JsonProperty("city")
	@Field(name = "CITY")
	private String city;

	@JsonProperty("longitude")
	@Field(name = "LONGITUDE")
	private Double longitude;

	@JsonProperty("latitude")
	@Field(name = "LATITUDE")
	private Double latitude;

	@JsonProperty("captured_time")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Field(name = "CAPTURED_TIME")
	private LocalDateTime capturedTime;

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alajounion.api.secure.domain;

import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.validators.qualifiers.IpAddress;
import com.cometbid.project.common.validators.qualifiers.ValidDate;
import com.cometbid.project.common.validators.qualifiers.VerifyValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;  
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 *
 * @author Gbenga --- Keep count of failed logins by ipAddress or Username
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "FAILED_LOGIN")
public class FailedLogin implements Serializable {

	private static final long serialVersionUID = 83745908559595756L;

	@Id
	@EqualsAndHashCode.Include
	@Field(name = "ID")
	private String id;

	@Setter
	@IpAddress(message = "{Login.ipAddr.invalid}")
	@JsonProperty("ip_addr")
	@EqualsAndHashCode.Include
	@NotBlank(message = "{Login.ipAddr.notBlank}")
	@Field(name = "IP_ADDR")
	private String ipAddr;

	@Setter
	@JsonProperty("attempts")
	@Max(value = 5, message = "{FailedLogin.loginAttempt.max}")
	@Min(value = 1, message = "{FailedLogin.loginAttempt.min}")
	@NotNull(message = "{FailedLogin.loginAttempt.notNull}")
	@Field(name = "ATTEMPTS")
	private Integer loginAttempt;
	
	@Setter
	@Indexed(name = "failedlogin_status_index")
	@JsonProperty("status")
	@Field(name = "STATUS")
	@VerifyValue(message = "{FailedLogin.status.verifyValue}", value = StatusType.class)
	private String status; //

	@Setter
	@JsonProperty("last_time")
	// @Convert(converter = LocalDateTimeConverter.class)
	@ValidDate(message = "{FailedLogin.lastTime.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message = "{FailedLogin.lastTime.notNull}")
	@Field(name = "LAST_TIME")
	private LocalDateTime lastTime;

	@JsonProperty("expiry_time")
	// @Convert(converter = LocalDateTimeConverter.class)
	@ValidDate(message = "{FailedLogin.expiredTime.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Field(name = "EXPIRED_TIME")
	private LocalDateTime expiredTime; // TIMESTAMP,
	
	@Version
    @Setter(AccessLevel.PROTECTED)
	@JsonIgnore
    @Field(name = "VERSION")
    private Long version;

	public FailedLogin(String ipAddr) {
		this.ipAddr = ipAddr;
	}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alajounion.api.secure.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.alajounion.api.secure.embeddables.UsrLoginLoc;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.validators.qualifiers.IpAddress;
import com.cometbid.project.common.validators.qualifiers.ValidDate;
import com.cometbid.project.common.validators.qualifiers.VerifyValue;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
 * @author Gbenga
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "SUCCESS_LOGIN")
public class SuccessLogin implements Serializable {

	private static final long serialVersionUID = 590984095803496545L;

	@Id
	@EqualsAndHashCode.Include
	@Field(name = "ID")
	private String id;

	@Setter
	@JsonProperty("username")
	@EqualsAndHashCode.Include
	@Indexed(name = "successlogin_username_index")
	@Size(min = 6, max = 330, message = "{Login.username.size}")
	@NotBlank(message = "{Login.username.notBlank}")
	@Field(name = "USERNAME")
	private String username;

	@Setter
	@IpAddress(message = "{Login.ipAddr.invalid}")
	@JsonProperty("ip_addr")
	@Indexed(name = "successlogin_ipAddr_index")
	@NotBlank(message = "{Login.ipAddr.notBlank}")
	@Field(name = "IP_ADDR")
	private String ipAddr;

	@Setter
	@JsonProperty("status")
	@VerifyValue(message = "{Login.status.verifyValue}", value = StatusType.class)
	@Indexed(name = "successlogin_status_index")
	@Field(name = "STATUS")
	private String status; //

	@JsonProperty("login_time")
	// @Convert(converter = LocalDateTimeConverter.class)
	@ValidDate(message = "{SuccessLogin.loginTime.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message = "{SuccessLogin.loginTime.notNull}")
	@Field(name = "LOGIN_TIME")
	private LocalDateTime loginTime;

	@Setter
	@JsonProperty("logout_time")
	// @Convert(converter = LocalDateTimeConverter.class)
	@ValidDate(message = "{SuccessLogin.logoutTime.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Field(name = "LOGOUT_TIME")
	private LocalDateTime logoutTime;

	@JsonIgnore
	@ToString.Exclude
	@Builder.Default
	private Set<UsrLoginLoc> loginLocHis = new HashSet<>();
	
	@Version
    @Setter(AccessLevel.PROTECTED)
	@JsonIgnore
    @Field(name = "VERSION")
    private Long version;

	public boolean addToLoginLocHis(UsrLoginLoc loginLoc) {
		if (loginLoc != null) {
			return loginLocHis.add(loginLoc);
		}
		return false;
	}

}

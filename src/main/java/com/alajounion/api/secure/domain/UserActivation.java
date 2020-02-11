/**
 * 
 */
package com.alajounion.api.secure.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.validators.qualifiers.ValidDate;
import com.cometbid.project.common.validators.qualifiers.VerifyValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Gbenga
 *
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = true)
@Document(collection = "ACTIVATION_TOKEN")
public class UserActivation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6892704864109745632L;

	@Id
	@EqualsAndHashCode.Include
	@Field(name = "ID")
	private String id;
	
	@NotBlank(message = "{Activation.userId.notBlank}")
	@Indexed(name = "activation_userid_index")
	@Field(name = "USER_ID")
	private String userId;

	@Indexed(unique = true, name = "token_unique_index_1")
	@NotBlank(message = "{Activation.token.notBlank}")
	@Field(name = "TOKEN")
	private String token;

	@JsonProperty("creation_time")
	@NotNull(message = "{Activation.creationDate.notNull}")
	@ValidDate(message = "{Activation.creationDate.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Field(name = "CREATED_DTE")
	private LocalDateTime creationDate;
	
	@JsonProperty("expiry_time")
	@ValidDate(message = "{Activation.expiredTime.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Field(name = "EXPIRED_TIME")
	private LocalDateTime expiredTime; // TIMESTAMP,
	
	@Setter
	@JsonProperty("status")
	@VerifyValue(message = "{Activation.status.verifyValue}", value = StatusType.class)
	@Indexed(name = "activation_status_index")
	@Field(name = "STATUS")
	private String status; //

}

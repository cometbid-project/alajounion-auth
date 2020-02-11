/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alajounion.api.secure.embeddables;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Gbenga
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(includeFieldNames = true)
public class RetiredPassword implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2398547521198359628L;

	@JsonIgnore
	@Size(min = 30, max = 1000, message = "{UserPassword.password.size}")
	@NotBlank(message = "{UserPassword.password.notBlank}")
	@Field(name = "HASHED_PASSWD")
	private String hashedPassword;

	@JsonIgnore
	// @Convert(converter = LocalDateTimeConverter.class)
	// @ValidDate(message = "{UserPassword.retiredTime.validDate}")
	//@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	//@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message = "{UserPassword.retiredTime.notNull}")
	@Field(name = "RETIRED_TIME")
	private LocalDateTime retiredTime; // TIMESTAMP,

}

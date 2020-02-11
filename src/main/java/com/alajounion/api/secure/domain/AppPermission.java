/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alajounion.api.secure.domain;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Gbenga
 */
@Getter
@Document(collection = "PERMISSIONS")
@NoArgsConstructor
@ToString(includeFieldNames = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// @CompoundIndex(def = "{'ID':1, 'PERMISSION':1}", unique = true, name = "permission_unique_index_1")
public class AppPermission implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 496593659356385656L;

	@Id
	@EqualsAndHashCode.Include
	@Field(name = "ID")
	private String permissionId;

	@Setter
	@JsonProperty("role") 
	@Indexed(unique = true)
	@Size(min = 1, max = 10, message = "{AppPermissions.permission.size}")
	@NotBlank(message = "{AppPermissions.permission.notBlank}")
	@Field(name = "PERMISSION")
	private String permission;

	@Setter
	@JsonProperty("desc")
	@Size(min = 1, max = 50, message = "{AppPermissions.desc.size}")
	@NotBlank(message = "{AppPermissions.desc.notBlank}")
	@Field(name = "DESCRIPTION")
	private String description;

}

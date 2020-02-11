/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alajounion.api.secure.domain;

import com.cometbid.project.security.audit.Audit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;
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
@Document(collection = "GROUPS")
// @EntityListeners({ AuditingEntityListener.class, AuditListener.class })
public class AppRole implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6489015258797604191L;

	@Id
	@EqualsAndHashCode.Include
	@JsonIgnore
	@Field(name = "ROLE_ID")
	private String roleId;

	@Setter
	@Basic(optional = false)
	@JsonProperty("name")
	@Indexed(unique = true)
	@Size(min = 1, max = 10, message = "{Role.roleName.size}")
	@NotBlank(message = "{Role.roleName.notBlank}")
	@Field(name = "ROLE_NAME")
	private String roleName;

	//@Setter
	//private Set<AppPermission> groupAuthorities = new HashSet<>();

	@Setter
	@Basic(optional = false)
	@JsonProperty("desc")
	@Size(min = 1, max = 100, message = "{Role.desc.size}")
	@NotBlank(message = "{Role.desc.notBlank}")
	@Field(name = "DESCRIPTION")
	private String description;

	@Setter
	@Getter
	private Audit audit;

	public AppRole(String roleName) {
		this.roleName = roleName;
	}

	/*
	public boolean addPermToRole(AppPermission appPermission) {
		if (groupAuthorities != null) {
			return groupAuthorities.add(appPermission);
		}
		return false;
	}

	public boolean removePermFromRole(AppPermission appPermission) {
		if (groupAuthorities != null) {
			return groupAuthorities.remove(appPermission);
		}
		return false;
	}
	*/

	public static boolean isRoleValid(Set<AppRole> setOfRoles, String role) {
		return setOfRoles.stream().anyMatch(p -> p.getRoleName().equalsIgnoreCase(role));
	}

}

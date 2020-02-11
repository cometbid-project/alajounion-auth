/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alajounion.api.secure.domain;

import com.alajounion.api.secure.embeddables.RetiredPassword;
import com.cometbid.project.common.enums.Role;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.validators.qualifiers.VerifyValue;
import com.cometbid.project.security.audit.Audit;
import com.cometbid.project.security.audit.AuditableExtension;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.ToString;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author Gbenga
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
// @CompoundIndex(def = "{'ID': 1, 'USERNAME': 1}", unique = true, name = "user_unique_index_1", sparse = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = true)
@Document(collection = "USERS")
public class UserAuth implements Serializable, AuditableExtension, UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6323707726796164986L;

	@Id
	@EqualsAndHashCode.Include
	@Field(name = "ID")
	private String id;

	@Setter
	@EqualsAndHashCode.Include
	@JsonProperty("username")
	@NotBlank(message = "{User.username.notBlank}")
	@Size(min = 6, max = 330, message = "{User.username.size}")
	@Field(name = "USERNAME")
	private String username;

	@Setter
	@JsonIgnore
	@Size(min = 10, max = 200, message = "{User.password.size}")
	@NotBlank(message = "{User.password.notBlank}")
	@Field(name = "PASSWORD")
	private String password;

	@NotEmpty
	@ToString.Exclude
	@Builder.Default
	@Size(min = 1, max = 1, message = "{User.roles.size}")
	private Set<@NotNull Role> roles = new HashSet<>();

	@Setter
	@JsonProperty("status")
	@Field(name = "STATUS")
	@VerifyValue(message = "{User.status.verifyValue}", value = StatusType.class)
	private String status; //

	@Setter
	@JsonIgnore
	@Field(name = "EXPIRED_TIME")
	private LocalDateTime lastExpiration;

	@Setter
	private Audit audit;

	@Setter
	@JsonIgnore
	@ToString.Exclude
	@Builder.Default
	@Size(max = 3, message = "{User.history.size}")
	private Collection<RetiredPassword> passwrdHis = new ArrayList<>();

	@Setter
	@JsonIgnore
	@ToString.Exclude
	@Builder.Default
	private List<@NotNull Audit> auditHistory = new ArrayList<>();

	@Version
	@Setter(AccessLevel.PROTECTED)
	@JsonIgnore
	@Field(name = "VERSION")
	private Long version;

	public boolean addToUsrRoles(Role usrRole) {
		if (usrRole != null) {
			return this.roles.add(usrRole);
		}

		return false;
	}

	public boolean removeFromUsrRoles(Role usrRole) {
		if (usrRole != null) {
			return this.roles.remove(usrRole);
		}

		return false;
	}

	@Override
	public boolean addToAuditHistory(Audit auditToAdd) {
		// TODO Auto-generated method stub
		if (auditToAdd != null) {
			return auditHistory.add(auditToAdd);
		}

		return false;
	}

	@Override
	public boolean isAccountNonExpired() {
		return StatusType.valueOf(status) != StatusType.EXPIRED;
	}

	@Override
	public boolean isAccountNonLocked() {
		return StatusType.valueOf(status) != StatusType.LOCKED;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return StatusType.valueOf(status) != StatusType.EXPIRED;
	}

	@Override
	public boolean isEnabled() {
		return StatusType.valueOf(status) == StatusType.VALID;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles.stream().map(authority -> new SimpleGrantedAuthority(authority.name()))
				.collect(Collectors.toList());
	}
	
	public void saveOldPassword(int maxAllowed) {
		Collection<RetiredPassword> passwdHis = getPasswrdHis();

		RetiredPassword oldPassword = new RetiredPassword();
		oldPassword.setHashedPassword(getPassword());
		oldPassword
				.setRetiredTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS));

		if (passwdHis.size() >= maxAllowed) {
			CircularFifoQueue<RetiredPassword> fifoQueue = new CircularFifoQueue<>(maxAllowed);
			fifoQueue.addAll(passwdHis);
			fifoQueue.add(oldPassword);
			passwdHis.clear();
			passwdHis.addAll(fifoQueue);
		} else {
			passwdHis.add(oldPassword);
		}

		this.setPasswrdHis(passwdHis);
	}

}

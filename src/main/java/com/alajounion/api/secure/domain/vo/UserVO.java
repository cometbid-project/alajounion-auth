/**
 * 
 */
package com.alajounion.api.secure.domain.vo;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserVO {

	private String id;

	@JsonProperty("username")
	@NotBlank(message = "{User.username.notBlank}")
	@Size(min = 6, max = 330, message = "{User.username.size}")
	private String username;
	
	@JsonIgnore
	@Size(min = 8, message = "{User.password.size}")
	@NotBlank(message = "{UserPassword.password.notBlank}")
	private String password;

	@NotEmpty
	@Size(min = 1, max = 1, message = "{User.roles.size}")
	private Set<@NotBlank String> roles;

	private boolean locked; //
	
	private boolean disabled;
	
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles.stream().map(authority -> new SimpleGrantedAuthority(authority)).collect(Collectors.toList());
	}

}

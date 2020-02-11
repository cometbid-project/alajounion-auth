package com.alajounion.api.secure.rest.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotBlank;

import org.apache.logging.log4j.message.FormattedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alajounion.api.secure.domain.vo.SuccessLoginVO;
import com.alajounion.api.secure.embeddables.UsrLoginLoc;
import com.alajounion.api.secure.services.ActivationTokenService;
import com.alajounion.api.secure.services.LoginService;
import com.alajounion.api.secure.services.UserAuthService;
import com.alajounion.api.server.models.AuthenticationRequest;
import com.alajounion.api.server.models.AuthenticationResponse;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.exceptions.UserAccountDisabledException;
import com.cometbid.project.common.exceptions.UserNotFoundException;
import com.cometbid.project.common.geo.ut.GeoIP;
import com.cometbid.project.common.geo.ut.RawDBDemoGeoIPLocationService;
import com.cometbid.project.common.utils.ActivationToken;
import com.cometbid.project.security.handler.JWTReactiveSignatureAuthManager;
import com.cometbid.project.security.handler.SecurityContextRepository;
import com.cometbid.project.security.jwt.utils.JWTUtil;
import com.cometbid.project.security.jwt.utils.UserUniqueProps;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Gbenga
 *
 */
@Slf4j
@Component
public class AuthHandler {

	@Autowired
	private JWTUtil jwtUtil;

	@Autowired
	private LoginService loginService;

	@Autowired
	private UserAuthService userService;
	
	@Autowired
	private ActivationTokenService tokenService;

	@Autowired
	@Qualifier("signature")
	private JWTReactiveSignatureAuthManager authenticationManager;

	@Autowired
	private SecurityContextRepository securityContextRepository;

	/**
	 * 
	 * @param request
	 * @return
	 */
	public Mono<ServerResponse> createAuthenticationToken(ServerRequest request) {

		Mono<AuthenticationRequest> authReq = request.bodyToMono(AuthenticationRequest.class);
		String userAgent = request.headers().header("User-Agent").get(0);

		log.info("User-Agent login request: {}", userAgent);

		return authReq.flatMap(p -> {
			String ipAddr = p.getIpAddress();
			String id = p.getId();
			String username = p.getUsername();
			String password = p.getPassword();
			String sessionId = UUID.randomUUID().toString();

			UsrLoginLoc loginLocHis = null;
			if (ipAddr != null) {
				loginLocHis = getUserRelativeLocation(ipAddr, userAgent);
			}
			final SuccessLoginVO successLoginVo = SuccessLoginVO.builder().id(sessionId).username(username)
					.ipAddr(ipAddr).loginLocHis(loginLocHis).status(StatusType.VALID.name()).build();
			try {
				Mono<AuthenticationResponse> authResp = userService.authenticate(id, username, password).map(e -> {

					String jwt = jwtUtil.generateToken(new UserUniqueProps(e.getId(), ipAddr, sessionId),
							e.getUsername(), e.getAuthorities());

					return new AuthenticationResponse(username, jwt);
				}).doOnSuccess(l -> log.info("Authentication token generated successfully...")).map(j -> {
					log.info("Success Login {}", successLoginVo);

					loginService.recordSuccessLogin(successLoginVo).subscribe();
					log.info("JWT Response {}", j);

					return j;
				}).doOnSuccess(k -> log.info("Login record created successfully..."));

				return ok().contentType(APPLICATION_JSON).body(authResp, AuthenticationResponse.class)
						.switchIfEmpty(Mono.error(new BadCredentialsException("Incorrect username or password")));

			} catch (BadCredentialsException e) {
				if (ipAddr != null) {
					loginService.incrementFailedLogins(ipAddr)
							.subscribe(c -> log.info("Failed login record created successfully..."));
				}

				return ServerResponse.status(HttpStatus.UNAUTHORIZED)
						.body(new FormattedMessage("Incorrect username or password"), FormattedMessage.class);
			} catch (UserNotFoundException | UserAccountDisabledException e) {
				if (ipAddr != null) {
					loginService.incrementFailedLogins(ipAddr)
							.subscribe(c -> log.info("Failed login record created successfully..."));
				}

				throw e;
			}
		}).doOnSuccess(c -> log.info("User Credential authentication successful."));

	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	// @PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> renewJwtToken(ServerRequest request) {

		Mono<AuthenticationResponse> authResp = securityContextRepository.load(request).flatMap(securityContext -> {
			Authentication e = securityContext.getAuthentication();
			return Mono.justOrEmpty(e);
		}).flatMap(authenticationManager::authenticate).doOnSuccess(u -> log.info("Token verification was successful."))
				.map(e -> {
					String username = (String) e.getPrincipal();
					String jwt = (String) e.getDetails();
					return new AuthenticationResponse(username, jwt);
				});

		return ok().contentType(APPLICATION_JSON).body(authResp, AuthenticationResponse.class).switchIfEmpty(
				Mono.error(new BadCredentialsException("Error occured: failed to renew your token. pls try again")));
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> getActivationToken(ServerRequest request) {

		// final Mono<ActivationToken> token =
		// request.bodyToMono(ActivationToken.class);
		final String userId = request.pathVariable("id");
		log.info("Creating Activation token User...");

		return ServerResponse.ok().contentType(APPLICATION_JSON)
				.body(tokenService.generateActivationToken(userId), ActivationToken.class)
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param principal
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> currentUser(ServerRequest request) {
		// @AuthenticationPrincipal Mono<Principal> principal) {

		Mono<Principal> principal = request.bodyToMono(Principal.class);

		return ok().contentType(APPLICATION_JSON).bodyValue(principal.map(user -> {
			Map<String, Object> map = new HashMap<>();
			map.put("name", user.getName());
			map.put("roles", AuthorityUtils.authorityListToSet(((Authentication) user).getAuthorities()));
			return map;
		}));
	}

	/**
	 * 
	 * @param principal
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> systemUser(ServerRequest request) {

		return ok().contentType(APPLICATION_JSON).bodyValue(userService.getCurrentUser().map(user -> {
			Map<String, Object> map = new HashMap<>();
			map.put("name", user.getUsername());
			map.put("roles", user.getRoles());
			return map;
		}));
	}

	/**
	 * 
	 * @param session
	 * @param req
	 * @return
	 */
	@PreAuthorize("hasRole('SECRETARY') or hasRole('ADMIN') or hasRole('MANAGER')")
	public Mono<ServerResponse> logout(ServerRequest request) {

		// session.invalidate();
		log.info("logout User Session ....");

		return request.principal().map(Principal::getName).flatMap(username -> {
			return ReactiveSecurityContextHolder.getContext().map(p -> p.getAuthentication())
					.filter(Authentication::isAuthenticated).map(Authentication::getDetails)
					.map(UserUniqueProps.class::cast).flatMap(c -> {
						Flux<String> fluxResult = loginService.invalidateUserSessions(c.getSessionId(), username);

						return ok().contentType(MediaType.TEXT_PLAIN).body(fluxResult, String.class)
								.switchIfEmpty(notFound().build());

					});

		});

	}

	public static UsrLoginLoc getUserRelativeLocation(@NotBlank String ipAddress, @NotBlank String userAgent) {

		try {
			RawDBDemoGeoIPLocationService locationService = new RawDBDemoGeoIPLocationService();
			GeoIP geoIP = locationService.getCityLocation(ipAddress);

			if (geoIP != null) {
				return UsrLoginLoc.builder().city(geoIP.getCity()).ipAddr(geoIP.getIpAddress())
						.latitude(Optional.ofNullable(Double.valueOf(geoIP.getLatitude())).orElse(null))
						.longitude(Optional.ofNullable(Double.valueOf(geoIP.getLongitude())).orElse(null))
						.countryCode(geoIP.getCountry()).capturedTime(LocalDateTime.now()).userAgent(userAgent).build();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	// get request headers
	/*
	 * private Map<String, String> getHeadersInfo() {
	 * 
	 * Map<String, String> map = new HashMap<String, String>();
	 * 
	 * Enumeration<String> headerNames = request.getHeaderNames(); while
	 * (headerNames.hasMoreElements()) { String key = (String)
	 * headerNames.nextElement(); String value = request.getHeader(key);
	 * map.put(key, value); }
	 * 
	 * return map; }
	 */
}
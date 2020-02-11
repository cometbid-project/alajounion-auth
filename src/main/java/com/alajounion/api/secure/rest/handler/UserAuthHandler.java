/**
 * 
 */
package com.alajounion.api.secure.rest.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import com.alajounion.api.secure.domain.vo.UserVO;
import com.alajounion.api.secure.services.ActivationTokenService;
import com.alajounion.api.secure.services.UserAuthService;
import com.alajounion.api.server.models.ExpireStatusUpdateRequest;
import com.alajounion.api.server.models.LockStatusUpdateRequest;
import com.alajounion.api.server.models.PasswordUpdateRequest;
import com.cometbid.project.common.exceptions.UserNotFoundException;
import com.cometbid.project.common.utils.ActivationToken;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 * 
 */
@Slf4j
@Component
public class UserAuthHandler {

	private final UserAuthService passwdService;
	private final ActivationTokenService tokenService;

	public UserAuthHandler(UserAuthService passwdService, ActivationTokenService tokenService) {
		this.passwdService = passwdService;
		this.tokenService = tokenService;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> getById(ServerRequest req) {
		String id = req.pathVariable("id");
		final Mono<UserVO> person = passwdService.findById(id);

		return person.flatMap(p -> ok().contentType(APPLICATION_JSON).body(person, UserVO.class))
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param id
	 * @param username
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> getByIdUsername(ServerRequest req) {

		String id = req.pathVariable("id");
		String username = req.pathVariable("username");

		final Mono<UserVO> person = passwdService.findByIdAndUsername(id, username);

		return person.flatMap(p -> ok().contentType(APPLICATION_JSON).body(person, UserVO.class))
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param person
	 * @return
	 */
	@PreAuthorize("hasRole('SECRETARY') or hasRole('ADMIN') or hasRole('MANAGER')")
	public Mono<ServerResponse> createUser(ServerRequest req) {

		final Mono<UserVO> userVo = req.bodyToMono(UserVO.class);
		log.info("Creating Auth User...:");

		return userVo.flatMap(passwdService::save)
				.flatMap(p -> tokenService.generateActivationToken(p.getId()))
				.flatMap(m -> created(UriComponentsBuilder.fromPath("/" + m.getUserId()).build().toUri())
						.contentType(APPLICATION_JSON).body(m, ActivationToken.class));
	}

	/**
	 * 
	 * @param id
	 * @param passwdUpd
	 * @return
	 */
	@PreAuthorize("hasRole('SECRETARY') or hasRole('ADMIN') or hasRole('MANAGER')")
	public Mono<ServerResponse> changePasswd(ServerRequest req) {

		String id = req.pathVariable("id");
		final Mono<PasswordUpdateRequest> passwdReq = req.bodyToMono(PasswordUpdateRequest.class);
		log.info("Changing User password...");

		return passwdReq.flatMap(p -> {
			return tokenService.validateActivationToken(p.getUserId(), p.getToken()).flatMap(valid -> {
				if (valid) {
					return passwdService.changePasswd(p).switchIfEmpty(Mono.error(new UserNotFoundException(id)))
							.flatMap(c -> ServerResponse.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN)
									.body(Mono.just("Password changed successfully"), String.class));
				} else {
					return ServerResponse.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.TEXT_PLAIN)
							.body(Mono.just("Activation token is invalid"), String.class);
				}
			});
		});
	}

	/**
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public Mono<ServerResponse> lockUserPasswordAcct(ServerRequest req) {

		String id = req.pathVariable("id");
		final Mono<LockStatusUpdateRequest> passwdReq = req.bodyToMono(LockStatusUpdateRequest.class);

		log.info("Lock/Unlock User password account...");

		return passwdReq.flatMap(p -> passwdService.lockUserStatus(id, p)
				.switchIfEmpty(Mono.error(new UserNotFoundException(id))).flatMap(c -> noContent().build()));
	}
	
	/**
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public Mono<ServerResponse> expireUserPasswordAcct(ServerRequest req) {

		String id = req.pathVariable("id");
		final Mono<ExpireStatusUpdateRequest> passwdReq = req.bodyToMono(ExpireStatusUpdateRequest.class);

		log.info("Activate/Deactivate User password account...");

		return passwdReq.flatMap(p -> passwdService.expireUserStatus(id, p)
				.switchIfEmpty(Mono.error(new UserNotFoundException(id))).flatMap(c -> noContent().build()));
	}
}

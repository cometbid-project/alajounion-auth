/**
 * 
 */
package com.alajounion.api.secure.rest.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alajounion.api.secure.services.LoginService;
import com.alajounion.api.server.models.SearchLoginRequest;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.exceptions.InvalidParameterException;
import com.cometbid.project.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Slf4j
@Component
public class LoginHandler {

	private final LoginService loginService;

	public LoginHandler(LoginService lgService) {
		this.loginService = lgService;
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> allSuccessfulLogins(ServerRequest req) {

		String statusParam = req.queryParam("status").orElse(null);
		int pageNo = Integer.valueOf(req.queryParam("pageNo").orElse("1"));
		int pageSize = Integer.valueOf(req.queryParam("pageSize").orElse("10"));

		Mono<Map<String, Object>> result;
		if (statusParam == null) {
			result = loginService.findAllSuccessLogin(pageNo, pageSize);
		} else {
			String status = getStatus(statusParam);
			if (status == null) {
				status = StatusType.VALID.name();
			}

			result = loginService.findAllSuccessLoginByStatus(status, pageNo, pageSize);
		}

		return result.flatMap(p -> ok().contentType(APPLICATION_JSON).body(result, Map.class))
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param statusParam
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> allFailedLogins(ServerRequest req) {

		String statusParam = req.queryParam("status").orElse(null);
		int pageNo = Integer.valueOf(req.queryParam("pageNo").orElse("1"));
		int pageSize = Integer.valueOf(req.queryParam("pageSize").orElse("10"));

		Mono<Map<String, Object>> result;
		if (statusParam == null) {
			result = loginService.findAllFailedLogin(pageNo, pageSize);
		} else {
			String status = getStatus(statusParam);
			if (status == null) {
				status = StatusType.VALID.name();
			}
			result = loginService.findAllFailedLoginByStatus(status, pageNo, pageSize);
		}

		return result.flatMap(p -> ok().contentType(APPLICATION_JSON).body(result, Map.class))
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param statusParam
	 * @param pageNo
	 * @param pageSize
	 * @param ipAddress
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> allSuccessfulLoginsByIPAddr(ServerRequest req) {

		String statusParam = req.queryParam("status").orElse(null);
		int pageNo = Integer.valueOf(req.queryParam("pageNo").orElse("1"));
		int pageSize = Integer.valueOf(req.queryParam("pageSize").orElse("10"));
		String ipAddress = req.pathVariable("ipAddr");

		String status = getStatus(statusParam);

		Mono<Map<String, Object>> result = loginService.findSuccessLoginByIP(ipAddress, status, pageNo, pageSize);

		return result.flatMap(p -> ok().contentType(APPLICATION_JSON).body(result, Map.class))
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param statusParam
	 * @param pageNo
	 * @param pageSize
	 * @param ipAddress
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> allFailedLoginsByIpAddr(ServerRequest req) {

		String statusParam = req.queryParam("status").orElse(null);
		int pageNo = Integer.valueOf(req.queryParam("pageNo").orElse("1"));
		int pageSize = Integer.valueOf(req.queryParam("pageSize").orElse("10"));
		String ipAddress = req.pathVariable("ipAddr");

		String status = getStatus(statusParam);

		Mono<Map<String, Object>> result = loginService.findFailedLoginByIP(ipAddress, status, pageNo, pageSize);

		return result.flatMap(p -> ok().contentType(APPLICATION_JSON).body(result, Map.class))
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param id
	 * @param username
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> successfulLoginByIPUsername(ServerRequest req) {

		String statusParam = req.queryParam("status").orElse(null);
		int pageNo = Integer.valueOf(req.queryParam("pageNo").orElse("1"));
		int pageSize = Integer.valueOf(req.queryParam("pageSize").orElse("10"));
		String ipAddress = req.pathVariable("ipAddr");
		String username = req.pathVariable("username");
		String op = req.pathVariable("op");

		String status = getStatus(statusParam);

		Mono<Map<String, Object>> result;
		if (op.equalsIgnoreCase("or")) {
			result = loginService.findSuccessLoginByIPOrUsername(ipAddress, username, status, pageNo, pageSize);
		} else {
			result = loginService.findSuccessLoginByIPAndUsername(ipAddress, username, status, pageNo, pageSize);
		}

		return result.flatMap(p -> ok().contentType(APPLICATION_JSON).body(result, Map.class))
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param id
	 * @param username
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> successfulLoginByIdUsername(ServerRequest req) {

		String statusParam = req.queryParam("status").orElse(null);
		int pageNo = Integer.valueOf(req.queryParam("pageNo").orElse("1"));
		int pageSize = Integer.valueOf(req.queryParam("pageSize").orElse("10"));
		String id = req.pathVariable("id");
		String username = req.pathVariable("username");
		String status = getStatus(statusParam);

		Mono<Map<String, Object>> result = loginService.findSuccessLoginByIdAndUsername(id, username, status, pageNo,
				pageSize);

		return result.flatMap(p -> ok().contentType(APPLICATION_JSON).body(result, Map.class))
				.switchIfEmpty(notFound().build());
	}

	/**
	 * 
	 * @param page
	 * @param size
	 * @param searchRequest
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ServerResponse> searchLoginByDate(ServerRequest req) {

		final int pageNo = Integer.valueOf(req.queryParam("pageNo").orElse("1"));
		final int pageSize = Integer.valueOf(req.queryParam("pageSize").orElse("10"));
		Mono<SearchLoginRequest> searchRequest = req.bodyToMono(SearchLoginRequest.class);

		Mono<Map<String, Object>> result = searchRequest.flatMap(sreq -> {

			LocalDate lStartDate = DateUtil.getStartDate(sreq.getStartDate());
			LocalDate lEndDate = DateUtil.getEndDate(sreq.getEndDate());

			if (lStartDate.isAfter(lEndDate)) {
				throw new InvalidParameterException("The Start date must be before the End date");
			}

			log.info("Business Logic Start Date: {}", lStartDate);
			log.info("Business Logic End Date: {}", lEndDate);
			/*
			 * Order order = sortDir.startsWith("asc") ? Order.asc(sort1) :
			 * Order.desc(sort1);
			 * 
			 * List<Order> sortOrders = new ArrayList<>(); sortOrders.add(order);
			 */

			if (sreq.isSuccess()) {
				// Order order2 = Order.desc("LOGIN_TIME");
				// sortOrders.add(order2);

				return loginService.findSuccessfulLoginsBetweenDate(pageNo, pageSize, lStartDate, lEndDate);
			} else {
				// Order order2 = Order.desc("LAST_TIME");
				// sortOrders.add(order2);

				return loginService.findFailedLoginsBetweenDate(pageNo, pageSize, lStartDate, lEndDate);
			}
		});

		return result.flatMap(p -> ok().contentType(APPLICATION_JSON).body(result, Map.class))
				.switchIfEmpty(notFound().build());
	}

	private String getStatus(String statusParam) {
		if (statusParam == null) {
			return null;
		}
		if (statusParam.equalsIgnoreCase("expired")) {
			return StatusType.EXPIRED.name();
		} else if (statusParam.equalsIgnoreCase("locked")) {
			return StatusType.LOCKED.name();
		} else if (statusParam.equalsIgnoreCase("active")) {
			return StatusType.VALID.name();
		}
		return null;
	}

}

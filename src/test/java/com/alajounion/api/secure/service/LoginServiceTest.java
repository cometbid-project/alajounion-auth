/**
 * 
 */
package com.alajounion.api.secure.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;
import com.alajounion.api.secure.config.AuthProfile;
import com.alajounion.api.secure.config.MongoConfig;
import com.alajounion.api.secure.config.SpringSecurityWebFluxConfig;
import com.alajounion.api.secure.domain.vo.SuccessLoginVO;
import com.alajounion.api.secure.repository.UserRepository;
import com.alajounion.api.secure.rest.handler.AuthHandler;
import com.alajounion.api.secure.services.impl.LoginServiceImpl;
import com.alajounion.api.secure.services.impl.UserAuthServiceImpl;
import com.cometbid.project.common.enums.StatusType;
import com.cometbid.project.common.validators.GlobalProgrammaticValidator;
import com.cometbid.project.security.handler.JWTReactiveAuthManager;
import com.cometbid.project.security.handler.SecurityContextRepository;
import com.cometbid.project.security.jwt.utils.JWTUtil;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Slf4j
@DataMongoTest
@ContextConfiguration(classes = { SpringSecurityWebFluxConfig.class, MongoConfig.class })
@Import({ UserAuthServiceImpl.class, LoginServiceImpl.class, JWTReactiveAuthManager.class,
		GlobalProgrammaticValidator.class, JWTUtil.class, SecurityContextRepository.class, AuthProfile.class })
public class LoginServiceTest {

	// @Autowired
	LoginServiceImpl loginService;

	// @Autowired
	UserRepository repository;

	String[] ipAddressArr = { "24.89.206.73", "116.134.151.61", "149.116.147.168", "170.40.138.110" };

	SuccessLoginVO[] defaultRecords = {
			SuccessLoginVO.builder().id(UUID.randomUUID().toString()).username("test1@cometbid.com")
					.ipAddr("24.89.206.73")
					.loginLocHis(AuthHandler.getUserRelativeLocation("24.89.206.73", "testing/Agent"))
					.status(StatusType.VALID.name()).build(),
			SuccessLoginVO.builder().id(UUID.randomUUID().toString()).username("test2@cometbid.com")
					.ipAddr("116.134.151.61")
					.loginLocHis(AuthHandler.getUserRelativeLocation("116.134.151.61", "testing/Agent"))
					.status(StatusType.EXPIRED.name()).build(),
			SuccessLoginVO.builder().id(UUID.randomUUID().toString()).username("test3@cometbid.com")
					.ipAddr("149.116.147.168")
					.loginLocHis(AuthHandler.getUserRelativeLocation("149.116.147.168", "testing/Agent"))
					.status(StatusType.VALID.name()).build(),
			SuccessLoginVO.builder().id(UUID.randomUUID().toString()).username("test4@cometbid.com")
					.ipAddr("170.40.138.110")
					.loginLocHis(AuthHandler.getUserRelativeLocation("170.40.138.110", "testing/Agent"))
					.status(StatusType.EXPIRED.name()).build() };

	public LoginServiceTest(@Autowired LoginServiceImpl service, @Autowired UserRepository repository) {
		this.loginService = service;
		this.repository = repository;
	}

	@Configuration
	static class Config {

		@Bean
		public ErrorResponseComposer errorResponseComposer() {
			List<AbstractExceptionHandler> handlers = new ArrayList<>();

			return new ErrorResponseComposer(handlers);
		}

		@Bean
		public ReactiveAuthenticationManager authManager() {

			return new JWTReactiveAuthManager();
		}

		@Bean
		public SecurityContextRepository securityContextRepository() {

			return new SecurityContextRepository();
		}

	}

	// @Test
	public void testFindAllSuccessLogin() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Mono<String> monoPublisher = Mono.just("test find all success login");

		monoPublisher.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.forEach(m -> loginService.recordSuccessLogin(m).log("Success login saved...").subscribe());

			return Mono.just("All records saved...");
		}).flatMap(l -> loginService.findAllSuccessLogin(1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == defaultRecords.length, "test failed, record found not equal record inserted...");

			if (count == defaultRecords.length) {
				log.info("test passed, No of record found equal record inserted...");
			} else {
				log.info("test failed, No of record found not equal record inserted...");
			}
			return count;
		})).subscribe();

	}

	// @Test
	public void testFindAllFailedLogin() {

		List<String> existingList = Arrays.asList(ipAddressArr);
		Flux<String> fluxPublisher = Flux.just("test find all failed login");

		fluxPublisher.flatMap(p -> {
			loginService.clearAllFailedRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.forEach(
					m -> loginService.incrementFailedLogins(m).log("Failed Login record saved...").subscribe());

			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findAllFailedLogin(1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == ipAddressArr.length, "test failed, record found not equal record inserted...");

			if (count == ipAddressArr.length) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		})).subscribe();

	}

	// @Test
	public void testFindAllSuccessLoginByStatus() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		// Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);
		Flux<String> fluxPublisher = Flux.just("test find all Success Login By Status");

		fluxPublisher.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.forEach(m -> loginService.recordSuccessLogin(m).log("Success login saved...").subscribe());

			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findAllSuccessLoginByStatus(StatusType.VALID.name(), 1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == 4, "test failed, record found not equal record inserted...");

			if (count == 4) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		})).flatMap(p -> loginService.findAllSuccessLoginByStatus(StatusType.EXPIRED.name(), 1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == 0, "test failed, record found not equal record inserted...");

			if (count == 0) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		})).subscribe();

	}

	// @Test
	public void testFindAllFailedLoginByStatus() {

		List<String> existingList = Arrays.asList(ipAddressArr);
		Flux<String> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			// loginService.clearAllFailedRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.forEach(p -> loginService.incrementFailedLogins(p).log("Failed login saved...").subscribe());

			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findAllFailedLoginByStatus(StatusType.VALID.name(), 1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == ipAddressArr.length, "test failed, record found not equal record inserted...");

			if (count == ipAddressArr.length) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		}));

		fluxCount.subscribe();

	}

	// @Test
	public void testFindSuccessLoginByIP() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.stream()
					.map(p -> loginService.recordSuccessLogin(p).log("Success login saved...").subscribe());
			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findSuccessLoginByIP("24.89.206.73", StatusType.VALID.name(), 1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

			if (count == 1) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		})).flatMap(
				p -> loginService.findSuccessLoginByIP("116.134.151.61", StatusType.EXPIRED.name(), 1, 10).map(c -> {
					Long count = (Long) c.get("recordCount");
					log.info("No of record {}", count);
					Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

					if (count == 1) {
						log.info("test passed, record found equal record inserted...");
					} else {
						log.info("test failed, record found not equal record inserted...");
					}
					return count;
				})).flatMap(p -> loginService.findSuccessLoginByIP("149.116.147.168", StatusType.VALID.name(), 1, 10)
						.map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}))
				.flatMap(p -> loginService.findSuccessLoginByIP("170.40.138.110", StatusType.EXPIRED.name(), 1, 10)
						.map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}));

		fluxCount.subscribe();
	}

	// @Test
	public void testFindFailedLoginByIP() {

		List<String> existingList = Arrays.asList(ipAddressArr);
		Flux<String> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllFailedRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.stream()
					.map(p -> loginService.incrementFailedLogins(p).log("Failed login saved...").subscribe());
			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findFailedLoginByIP("24.89.206.73", StatusType.VALID.name(), 1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == ipAddressArr.length, "test failed, record found not equal record inserted...");

			if (count == ipAddressArr.length) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		})).flatMap(p -> loginService.findFailedLoginByIP("116.134.151.61", StatusType.EXPIRED.name(), 1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == ipAddressArr.length, "test failed, record found not equal record inserted...");

			if (count == ipAddressArr.length) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		})).flatMap(p -> loginService.findFailedLoginByIP("149.116.147.168", StatusType.VALID.name(), 1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == ipAddressArr.length, "test failed, record found not equal record inserted...");

			if (count == ipAddressArr.length) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		})).flatMap(p -> loginService.findFailedLoginByIP("170.40.138.110", StatusType.EXPIRED.name(), 1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == ipAddressArr.length, "test failed, record found not equal record inserted...");

			if (count == ipAddressArr.length) {
				log.info("test passed, record found equal record inserted...");
			} else {
				log.info("test failed, record found not equal record inserted...");
			}
			return count;
		}));

		fluxCount.subscribe();

	}

	// @Test
	public void testFindSuccessLoginByIPAndUsername() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.stream()
					.map(p -> loginService.recordSuccessLogin(p).log("Success login saved...").subscribe());
			return Mono.just("New records saved...");
		}).flatMap(p -> loginService
				.findSuccessLoginByIPAndUsername("24.89.206.73", "test1@cometbid.com", StatusType.VALID.name(), 1, 10)
				.map(c -> {
					Long count = (Long) c.get("recordCount");
					log.info("No of record {}", count);
					Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

					if (count == 1) {
						log.info("test passed, record found equal record inserted...");
					} else {
						log.info("test failed, record found not equal record inserted...");
					}
					return count;
				})).flatMap(p -> loginService.findSuccessLoginByIPAndUsername("116.134.151.61", "test2@cometbid.com",
						StatusType.EXPIRED.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}))
				.flatMap(p -> loginService.findSuccessLoginByIPAndUsername("149.116.147.168", "test3@cometbid.com",
						StatusType.VALID.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}))
				.flatMap(p -> loginService.findSuccessLoginByIPAndUsername("170.40.138.110", "test4@cometbid.com",
						StatusType.EXPIRED.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}));

		fluxCount.subscribe();

	}

	// @Test
	public void testFindSuccessLoginByIPOrUsername() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.stream()
					.map(p -> loginService.recordSuccessLogin(p).log("Success login saved...").subscribe());
			return Mono.just("New records saved...");
		}).flatMap(p -> loginService
				.findSuccessLoginByIPOrUsername("24.89.206.73", "test1@cometbid.com", StatusType.VALID.name(), 1, 10)
				.map(c -> {
					Long count = (Long) c.get("recordCount");
					log.info("No of record {}", count);
					Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

					if (count == 1) {
						log.info("test passed, record found equal record inserted...");
					} else {
						log.info("test failed, record found not equal record inserted...");
					}
					return count;
				})).flatMap(p -> loginService.findSuccessLoginByIPOrUsername("116.134.151.61", "test2@cometbid.com",
						StatusType.EXPIRED.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}))
				.flatMap(p -> loginService.findSuccessLoginByIPOrUsername("149.116.147.168", "test3@cometbid.com",
						StatusType.VALID.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}))
				.flatMap(p -> loginService.findSuccessLoginByIPOrUsername("170.40.138.110", "test4@cometbid.com",
						StatusType.EXPIRED.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}));

		fluxCount.subscribe();

	}

	// @Test
	public void testFindSuccessLoginByIdAndUsername() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.stream()
					.map(p -> loginService.recordSuccessLogin(p).log("Success login saved...").subscribe());
			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findSuccessLoginByIdAndUsername(defaultRecords[0].getId(), "test1@cometbid.com",
				StatusType.VALID.name(), 1, 10).map(c -> {
					Long count = (Long) c.get("recordCount");
					log.info("No of record {}", count);
					Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

					if (count == 1) {
						log.info("test passed, record found equal record inserted...");
					} else {
						log.info("test failed, record found not equal record inserted...");
					}
					return count;
				})).flatMap(p -> loginService.findSuccessLoginByIdAndUsername(defaultRecords[1].getId(),
						"test2@cometbid.com", StatusType.EXPIRED.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}))
				.flatMap(p -> loginService.findSuccessLoginByIdAndUsername(defaultRecords[2].getId(),
						"test3@cometbid.com", StatusType.VALID.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}))
				.flatMap(p -> loginService.findSuccessLoginByIdAndUsername(defaultRecords[3].getId(),
						"test4@cometbid.com", StatusType.EXPIRED.name(), 1, 10).map(c -> {
							Long count = (Long) c.get("recordCount");
							log.info("No of record {}", count);
							Assert.isTrue(count == 1, "test failed, record found not equal record inserted...");

							if (count == 1) {
								log.info("test passed, record found equal record inserted...");
							} else {
								log.info("test failed, record found not equal record inserted...");
							}
							return count;
						}));

		fluxCount.subscribe();
	}

	// @Test
	public void invalidateUserSessions() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);

		Flux<Boolean> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.stream()
					.map(p -> loginService.recordSuccessLogin(p).log("Success login saved...").subscribe());
			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findAllSuccessLogin(1, 10).map(c -> {
			Flux<SuccessLoginVO> fluxResult = (Flux<SuccessLoginVO>) c.get("result");

			return fluxResult;

		})).flatMap(m -> m.collectList()
				.map(n -> n.stream().allMatch(p -> p.getStatus().equalsIgnoreCase(StatusType.EXPIRED.name()))));

		fluxCount.subscribe();
	}

	// @Test
	public void testRecordSuccessLogin() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.forEach(p -> loginService.recordSuccessLogin(p).log("Success login saved...").subscribe());

			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findAllSuccessLogin(1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == defaultRecords.length,
					"test failed, No of record found not equal record inserted...");

			if (count == defaultRecords.length) {
				log.info("test passed, No of record found equal record inserted...");
			} else {
				log.info("test failed, No of record found not equal record inserted...");
			}
			return count;
		}));
		fluxCount.subscribe();
	}

	// @Test
	public void testFindSuccessfulLoginsBetweenDate() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.forEach(p -> loginService.recordSuccessLogin(p).log("Success login saved...").subscribe());

			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findSuccessfulLoginsBetweenDate(1, 10, LocalDate.now(), LocalDate.now()).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == defaultRecords.length,
					"test failed, No of record found not equal record inserted...");

			if (count == defaultRecords.length) {
				log.info("test passed, No of record found equal record inserted...");
			} else {
				log.info("test failed, No of record found not equal record inserted...");
			}
			return count;
		}));

		fluxCount.subscribe();
	}

	// @Test
	public void testFindFailedLoginsBetweenDate() {

		List<SuccessLoginVO> existingList = Arrays.asList(defaultRecords);
		Flux<SuccessLoginVO> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllSuccessRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.forEach(p -> loginService.recordSuccessLogin(p).log("Success login saved...").subscribe());

			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findSuccessfulLoginsBetweenDate(1, 10, LocalDate.now(), LocalDate.now()).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == defaultRecords.length,
					"test failed, No of record found not equal record inserted...");

			if (count == defaultRecords.length) {
				log.info("test passed, No of record found equal record inserted...");
			} else {
				log.info("test failed, No of record found not equal record inserted...");
			}
			return count;
		}));

		fluxCount.subscribe();
	}

	// @Test
	public void testIncrementFailedLogins() {

		List<String> existingList = Arrays.asList(ipAddressArr);
		Flux<String> fluxRecord = Flux.fromIterable(existingList);

		Flux<Long> fluxCount = fluxRecord.flatMap(p -> {
			loginService.clearAllFailedRecords().log("Clear all records...").subscribe();
			return Mono.just("All record cleared...");
		}).flatMap(c -> {
			existingList.forEach(p -> loginService.incrementFailedLogins(p).log("Failed login saved...").subscribe());

			return Mono.just("New records saved...");
		}).flatMap(p -> loginService.findAllFailedLogin(1, 10).map(c -> {
			Long count = (Long) c.get("recordCount");
			log.info("No of record {}", count);
			Assert.isTrue(count == ipAddressArr.length, "test failed, No of record found not equal record inserted...");

			if (count == ipAddressArr.length) {
				log.info("test passed, No of record found equal record inserted...");
			} else {
				log.info("test failed, No of record found not equal record inserted...");
			}
			return count;
		}));

		fluxCount.subscribe();
	}

}

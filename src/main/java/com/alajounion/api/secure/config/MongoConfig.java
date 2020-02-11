/**
 * 
 */
package com.alajounion.api.secure.config;

/**
 * @author Gbenga
 *
 */
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.WriteConcernResolver;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.BasicMongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.core.userdetails.User;
import com.cometbid.project.security.audit.AuditAwareImpl;
import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableRetry
@EnableMongoAuditing(auditorAwareRef = "auditorProvider")
@EnableReactiveMongoRepositories(basePackages = "com.alajounion.api.secure.repository")
public class MongoConfig extends AbstractReactiveMongoConfiguration {

	private final List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();

	/*
	 * @Autowired private Environment environment;
	 */
	@Value("${spring.data.mongodb.database}")
	private String db;

	@Value("${spring.data.mongodb.uri}")
	private String mongoUri;

	@Bean
	public AuditorAware<User> auditorProvider() {
		return new AuditAwareImpl();
	}

	@Override
	public MongoClient reactiveMongoClient() {
		return MongoClients.create(mongoUri);
	}

	@Override
	protected String getDatabaseName() {
		// TODO Auto-generated method stub
		return db;
	}

	@Bean
	public WriteConcernResolver writeConcernResolver() {
		return action -> {
			System.out.println("Using Write Concern of Acknowledged");
			return WriteConcern.ACKNOWLEDGED;
		};
	}

	public ReactiveMongoDatabaseFactory mongoDbFactory() {
		return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
	}

	@Bean
	public ReactiveMongoTemplate reactiveMongoTemplate() {
		ReactiveMongoTemplate mongoTemplate = new ReactiveMongoTemplate(mongoDbFactory());
		mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);

		return mongoTemplate;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initIndicesAfterStartup() {

		log.info("Mongo InitIndicesAfterStartup init");
		long init = System.currentTimeMillis();

		final MongoMappingContext mappingContext = (MongoMappingContext) reactiveMongoTemplate().getConverter()
				.getMappingContext();

		if (mappingContext instanceof MongoMappingContext) {
			MongoMappingContext mongoMappingContext = (MongoMappingContext) mappingContext;

			for (BasicMongoPersistentEntity<?> persistentEntity : mongoMappingContext.getPersistentEntities()) {
				Class<?> clazz = persistentEntity.getType();
				if (clazz.isAnnotationPresent(Document.class)) {
					ReactiveIndexOperations indexOps = reactiveMongoTemplate().indexOps(clazz);
					IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
					resolver.resolveIndexFor(clazz).forEach(indexOps::ensureIndex);
				}
			}

		}

		log.info("Mongo InitIndicesAfterStartup take: {}", (System.currentTimeMillis() - init));
	}

}
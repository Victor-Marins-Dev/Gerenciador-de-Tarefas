package br.com.integrationtests.testcontainers;

import java.util.Map;
import java.util.stream.Stream;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;

@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
public class AbstractIntegrationTest {

	private static MySQLContainer<?> mysql = Initializer.mysql;
	
	public static MySQLContainer<?> getMysql() {
		return mysql;
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		
		static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.30");
		

		private static void startContainers() {
			Startables.deepStart(Stream.of(mysql)).join();
		}
		
		private static Map<String, String> createConnectionConfiguration(){
			return Map.of(
					"spring.datasource.url", mysql.getJdbcUrl(),
					"spring.datasource.username", mysql.getUsername(),
					"spring.datasource.password", mysql.getPassword(),
					"spring.flyway.enabled", "true",  
	                "spring.flyway.locations", "classpath:/db/migration" 
					);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			startContainers();
			ConfigurableEnvironment enviroment = applicationContext.getEnvironment();
			MapPropertySource testContainers = new MapPropertySource("testcontainers", (Map) createConnectionConfiguration());
			enviroment.getPropertySources().addFirst(testContainers);
		}
		
	}
}

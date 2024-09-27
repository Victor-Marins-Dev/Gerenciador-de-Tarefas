package br.com.integrationtests.swagger;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.configs.TestConfig;
import br.com.integrationtests.testcontainers.AbstractIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SwaggerIntegrationTest extends AbstractIntegrationTest{

	@Test
	void testShouldDisplaySwaggerUiPage() {
		
		String content = given().basePath("/swagger-ui/index.html")
			.port(TestConfig.SERVER_PORT)
			.when()
				.get()
			.then()
				.statusCode(200)
			.extract()
				.asString();
		
		assertTrue(content.contains("Swagger UI"));
	}

}

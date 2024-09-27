package br.com.utils;

import static io.restassured.RestAssured.given;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.configs.TestConfig;
import br.com.dtos.AuthenticationDto;
import br.com.dtos.RegisterDto;
import br.com.dtos.TokenDto;

public class AuthTestUtil {
	
	private ObjectMapper objectMapper;
	
	public AuthTestUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void createUser(String username) throws Exception {
        RegisterDto registerDto = new RegisterDto(username, "12345678aZ$");

        given()
            .basePath("/api/auth")
            .port(TestConfig.SERVER_PORT)
            .contentType(TestConfig.CONTENT_TYPE_JSON)
            .body(objectMapper.writeValueAsString(registerDto))
        .when()
            .post("/register")
        .then()
            .statusCode(201);
    }

    public String loggingUser(String username) throws JsonProcessingException {
        AuthenticationDto authDto = new AuthenticationDto(username, "12345678aZ$");

        TokenDto tokenDto = 
            given()
                .basePath("/api/auth")
                .port(TestConfig.SERVER_PORT)
                .contentType(TestConfig.CONTENT_TYPE_JSON)
                .body(objectMapper.writeValueAsString(authDto))
            .when()
                .post("/login")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenDto.class);

        return tokenDto.getToken();
    }
}

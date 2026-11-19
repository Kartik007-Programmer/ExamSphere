package com.examsphere.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@GetMapping("/PING")
	public String ping() {
		return "<h1 style=\"\n" +
				"  margin: 0;\n" +
				"  min-height: 100vh;\n" +
				"  display: flex;\n" +
				"  align-items: center;\n" +
				"  justify-content: center;\n" +
				"  text-align: center;\n" +
				"  padding: 24px;\n" +
				"  box-sizing: border-box;\n" +
				"  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
				"  font-size: clamp(2rem, 5vw, 3.5rem);\n" +
				"  font-weight: 800;\n" +
				"  letter-spacing: -0.02em;\n" +
				"  line-height: 1.2;\n" +
				"  color: #1e293b;\n" +
				"  background: radial-gradient(circle at top, #f3e8ff 0%, #f8fafc 60%, #e2e8f0 100%);\n" +
				"  text-shadow: 0 4px 18px rgba(126, 34, 206, 0.18);\n" +
				"\">\n" +
				"  \uD83C\uDF10 This is API Gateway Service...\n" +
				"</h1>";
	}

}

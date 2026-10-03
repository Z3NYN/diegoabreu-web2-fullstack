package br.ueg.trindade.diego_web2_fullstack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:contexttest", "spring.jpa.hibernate.ddl-auto=create-drop"})
class DiegoWeb2FullstackApplicationTests {

	@Test
	void contextLoads() {
	}

}

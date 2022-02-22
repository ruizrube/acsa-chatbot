package es.ja.csalud.sas.botcitas.botmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableScheduling

public class BotManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BotManagerApplication.class, args);
	}

}

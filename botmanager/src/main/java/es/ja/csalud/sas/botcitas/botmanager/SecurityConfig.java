package es.ja.csalud.sas.botcitas.botmanager;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//	@Bean
//	@Override
//	public UserDetailsService userDetailsService() {
//		UserDetails user =
//			 User.withDefaultPasswordEncoder()
//				.username("user")
//				.password("password")
//				.roles("USER")
//				.build();
//
//		return new InMemoryUserDetailsManager(user);
//	}

	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
	      .csrf().disable().authorizeRequests().antMatchers("/Appointments","/Users","/Clinics","/dialogflow").authenticated().anyRequest().permitAll().and()
				.httpBasic();

	}

	
}
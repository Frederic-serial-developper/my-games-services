package fdi.games.services;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = { "fdi.games.services" })
@PropertySource("classpath:my-games-services.properties")
@PropertySource(value = "classpath:my-games-services-local.properties", ignoreResourceNotFound = true)
public class AppConfig extends WebMvcConfigurerAdapter {
}

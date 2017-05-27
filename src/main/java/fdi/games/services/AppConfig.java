package fdi.games.services;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@PropertySource("classpath:my-games-services.properties")
@PropertySource(value = "classpath:my-games-services-local.properties", ignoreResourceNotFound = true)
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "fdi.games.services" })
public class AppConfig extends WebMvcConfigurerAdapter {
}

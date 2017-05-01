package fdi.games.services.mvc;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

	final static Logger logger = LoggerFactory.getLogger(HealthController.class);

	@RequestMapping(method = RequestMethod.GET)
	public String helloWorld() {
		logger.info("checking services are running");
		return LocalDateTime.now().toString();
	}
}

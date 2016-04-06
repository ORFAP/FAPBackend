package de.orfap.fap.backend.event;

import de.orfap.fap.backend.domain.Airline;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.repositories
 * Author(s): Rene Zarwel
 * Date: 06.04.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@Component
public class Airline_EventListener extends AbstractRepositoryEventListener<Airline> {

	// If you need access to the database you can autowire a Repository.
	//
	// @Autowired
	// <EntityName>Repository repo;



	//Override Methods here to add your custom logic
}

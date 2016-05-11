package de.orfap.fap.backend.repositories;

import de.orfap.fap.backend.domain.Airline;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.event
 * Author(s): Rene Zarwel
 * Date: 06.04.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@RepositoryRestResource
public interface AirlineRepository extends CrudRepository<Airline, String> {

  //FindBy Methods
  // http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods


}

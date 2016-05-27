package de.orfap.fap.backend.repositories;

import de.orfap.fap.backend.domain.Route;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
public interface RouteRepository extends CrudRepository<Route, UUID> {

  //FindBy Methods
  // http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods

  List<Route> findByDateBetweenAndAirline_IdInAndDestination_IdIn(
      @Param("start")Date start,
      @Param("end")Date end,
      @Param("airports") List<String> airports,
      @Param("destinations") List<String> destinations);

  List<Route> findByDateBetweenAndAirline_IdIn(
      @Param("start")Date start,
      @Param("end")Date end,
      @Param("airports") List<String> airports);

  List<Route> findByDateBetweenAndDestination_IdIn(
      @Param("start")Date start,
      @Param("end")Date end,
      @Param("destinations") List<String> destinations);

  List<Route> findByDateBetween(
      @Param("start")Date start,
      @Param("end")Date end);

  /**
   * Find routes within a given range and filtered by Market and Airline.
   * @param start of range (included)
   * @param end of range (excluded)
   * @param airports to filter. If empty return all.
   * @param destinations to filter. If empty return all.
   * @return return filtered routes.
   */
  default List<Route> findByDateBetweenAndFilteredByMarketAirline(
      Date start,
      Date end,
      List<String> airports,
      List<String> destinations){

    if (airports.isEmpty() && destinations.isEmpty())
      return findByDateBetween(start,end);
    else if (airports.isEmpty())
      return findByDateBetweenAndDestination_IdIn(start, end, destinations);
    else if ((destinations.isEmpty()))
      return findByDateBetweenAndAirline_IdIn(start, end, airports);
    else
      return findByDateBetweenAndAirline_IdInAndDestination_IdIn(
          start, end, airports, destinations
      );
  }
}

package de.orfap.fap.backend.controller;

import de.orfap.fap.backend.domain.QuantitiveValue;
import de.orfap.fap.backend.domain.Route;
import de.orfap.fap.backend.domain.Setting;
import de.orfap.fap.backend.domain.TimeSteps;
import de.orfap.fap.backend.repositories.RouteRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.controller
 * Author(s): Rene Zarwel
 * Date: 10.05.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@RestController
@ExposesResourceFor(Route.class)
@RequestMapping("/routes")
public class RouteController {


  @Autowired
  RouteRepository routeRepository;

  @RequestMapping("/filter")
  @Cacheable("filter")
  public Map<String, List<Integer>> filter(Setting setting) {

    checkSetting(setting);

    //Find filtered Data
    List<Route> routes = routeRepository.findByAirline_NameInOrDestination_NameInAndDateBetween(
        setting.getFilter().getAirlines(),
        setting.getFilter().getDestinations(),
        setting.getRangeFrom(),
        setting.getRangeTo()
    );

    //SetUp Date
    DateTimeFormatter timeFormat = getDateTimeFormatter(setting.getFilter().getTimestep());

    //Compute result
    switch (setting.getAxis().getX()) {
      case TIME:
        return mapByTime(
            timeFormat,
            setting.getAxis().getY(),
            routes);

      case DESTINATION:
        return mapToQuantitive(
            timeFormat,
            setting.getAxis().getY(),
            mapByDestination(routes));

      case AIRLINE:
        return mapToQuantitive(
            timeFormat,
            setting.getAxis().getY(),
            mapByAirline(routes));

      default:
        return null;
    }
  }

  private void checkSetting(@NonNull Setting setting) {
    if (setting == null)
      throw new AssertionError("Setting should not be null!");
    if (setting.getAxis() == null)
      throw new AssertionError("Axis should not be null!");
    if (setting.getFilter() == null)
      throw new AssertionError("Filter should not be null!");
  }

  private Map<String, List<Integer>> mapByTime(
      DateTimeFormatter timeFormat,
      QuantitiveValue quant, List<Route> routes) {

    //Sort by Date and sum values
    return routes
        .stream()
        .peek(route -> {
          //Normalize date of route to timeStep
          LocalDate reducedDate = LocalDate.from(timeFormat.parse(route.getDate().toString()));
          route.setDate(Date.from(reducedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        })
        .collect(Collectors.groupingBy(route -> route.getDate().toString(),
            Collectors.collectingAndThen(
                Collectors.summingInt(route -> getQuant(quant, route)),
                Collections::singletonList
            )
            )
        );
  }

  private Map<String, List<Route>> mapByAirline(List<Route> routes) {

    //Grouping Routes to Airlines
    return routes.stream()
        .collect(Collectors.groupingBy(route -> route.getAirline().getName(),
            Collectors.mapping(route -> route, Collectors.toList())));

  }

  private Map<String, List<Route>> mapByDestination(List<Route> routes) {

    //Grouping Routes to Destination
    return routes.stream()
        .collect(Collectors.groupingBy(route -> route.getDestination().getName(),
            Collectors.mapping(route -> route, Collectors.toList())));

  }

  private Map<String, List<Integer>> mapToQuantitive(
      DateTimeFormatter timeFormat, QuantitiveValue quant, Map<String, List<Route>> routeMap) {

    Map<String, List<Integer>> result = new HashMap<>();

    //Map to quantity for each key
    for (String key : routeMap.keySet()) {

      //Sort by Date
      Map<Date, List<Route>> dateMap = routeMap.get(key)
          .stream()
          .peek(route -> {
            //Normalize date of route to timeStep
            LocalDate reducedDate = LocalDate.from(timeFormat.parse(route.getDate().toString()));
            route.setDate(Date.from(reducedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
          })
          .collect(Collectors.groupingBy(Route::getDate,
              Collectors.mapping(route -> route, Collectors.toList())));

      //Strip to one value
      List<Integer> stripList = dateMap.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .map(e -> e.getValue().stream().mapToInt(route -> getQuant(quant, route)).sum())
          .collect(Collectors.toList());

      result.put(key, stripList);
    }

    return result;
  }

  private int getQuant(QuantitiveValue quant, Route route) {
    switch (quant) {

      case FLIGHTS:
        return route.getFlightCount();
      case PASSENGERS:
        return route.getPassengerCount();
      case DELAYFREQ:
        return route.getDelays();
      case CANCELLATIONS:
        return route.getCancelled();
      case AVGDELAY:
        return 0; //TODO
      default:
        return 0;
    }
  }

  private DateTimeFormatter getDateTimeFormatter(TimeSteps timeStep) {
    switch (timeStep) {

      case DAY_OF_WEEK:
        return DateTimeFormatter.ofPattern("dd.MM.uuuu");
      case MONTH:
        return DateTimeFormatter.ofPattern("MM.uuuu");
      case YEAR:
        return DateTimeFormatter.ofPattern("uuuu");
      default:
        return null;
    }

  }

}

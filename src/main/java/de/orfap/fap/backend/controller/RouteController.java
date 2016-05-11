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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

  @RequestMapping(value = "/filter", method = RequestMethod.POST)
  @Cacheable("filter")
  public Map<String, Integer> filter(@RequestBody Setting setting) {

    checkSetting(setting);

    //Find filtered Data
    List<Route> routes = routeRepository.findByDateBetweenAirportDestination(
        setting.getRangeFrom(),
        setting.getRangeTo(),
        setting.getFilter().getAirlines(),
        setting.getFilter().getDestinations()
    );

    //SetUp Date
    SimpleDateFormat timeFormat = getDateTimeFormatter(setting.getFilter().getTimestep());

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

  public Map<String, Integer> mapByTime(
      SimpleDateFormat timeFormat,
      QuantitiveValue quant, List<Route> routes) {

    //Sort by Date and sum values
    return routes
        .stream()
        .peek(route -> {
          //Normalize date of route to timeStep
          try {
            route.setDate(timeFormat.parse(timeFormat.format(route.getDate())));
          } catch (ParseException e){
            throw new AssertionError("Date Parse Error");
          }
        })
        .collect(Collectors.groupingBy(route -> timeFormat.format(route.getDate()),
            Collectors.summingInt(route -> getQuant(quant, route))
            )
        );
  }

  public Map<String, List<Route>> mapByAirline(List<Route> routes) {

    //Grouping Routes to Airlines
    return routes.stream()
        .collect(Collectors.groupingBy(route -> route.getAirline().getName(),
            Collectors.mapping(route -> route, Collectors.toList())));

  }

  public Map<String, List<Route>> mapByDestination(List<Route> routes) {

    //Grouping Routes to Destination
    return routes.stream()
        .collect(Collectors.groupingBy(route -> route.getDestination().getName(),
            Collectors.mapping(route -> route, Collectors.toList())));

  }

  public Map<String, Integer> mapToQuantitive(
      SimpleDateFormat timeFormat, QuantitiveValue quant, Map<String, List<Route>> routeMap) {

    Map<String, Integer> result = new HashMap<>();

    //Map to quantity for each key
    for (String key : routeMap.keySet()) {

      //Sort by Date
      Map<Date, List<Route>> dateMap = routeMap.get(key)
          .stream()
          .peek(route -> {
            //Normalize date of route to timeStep
            try {
              route.setDate(timeFormat.parse(timeFormat.format(route.getDate())));
            } catch (ParseException e){
              throw new AssertionError("Date Parse Error");
            }
          })
          .collect(Collectors.groupingBy(Route::getDate,
              Collectors.mapping(route -> route, Collectors.toList())));

      //Strip to one value
      Integer value = dateMap.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .mapToInt(e -> e.getValue().stream().mapToInt(route -> getQuant(quant, route)).sum())
          .sum();

      result.put(key, value);
    }

    return result;
  }

  public int getQuant(QuantitiveValue quant, Route route) {
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

  public SimpleDateFormat getDateTimeFormatter(TimeSteps timeStep) {
    switch (timeStep) {

      case DAY_OF_WEEK:
        return new SimpleDateFormat("EEEE", Locale.US);
      case MONTH:
        return new SimpleDateFormat("MMMM", Locale.US);
      case YEAR:
        return new SimpleDateFormat("yyyy", Locale.US);
      default:
        return null;
    }

  }

}

package de.orfap.fap.backend.controller;

import de.orfap.fap.backend.domain.QuantitiveValue;
import de.orfap.fap.backend.domain.Route;
import de.orfap.fap.backend.domain.Setting;
import de.orfap.fap.backend.domain.TimeSteps;
import de.orfap.fap.backend.repositories.RouteRepository;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
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

  public static final Logger LOG = LoggerFactory.getLogger(RouteController.class);


  @Autowired
  RouteRepository routeRepository;

  @RequestMapping(value = "/filter", method = RequestMethod.POST)
  @Cacheable("filter")
  public FilterResponse filter(@RequestBody Setting setting) {

    LOG.info("FILTER:" + setting.toString());

    checkSetting(setting);

    //Find filtered Data
    List<Route> routes = routeRepository.findByDateBetweenAirportDestination(
        setting.getRangeFrom(),
        setting.getRangeTo(),
        setting.getFilter().getAirlines(),
        setting.getFilter().getDestinations()
    );

    //SetUp Date
    DateNormalizer dateNormalizer = new DateNormalizer(setting.getFilter().getTimestep());

    //Set up keys if necessary
    Set<Date> keys = getDateRangeKeys(
        setting.getRangeFrom(),
        setting.getRangeTo(),
        setting.getFilter().getTimestep(),
        dateNormalizer
    );

    Map<String, List<Double>> data = null;

    //Compute result
    switch (setting.getAxis().getX()) {
      case TIME:
        data = mapByTime(
            dateNormalizer,
            setting.getAxis().getY(),
            keys,
            routes);
        keys = new HashSet<>();
        break;

      case DESTINATION:
        data = mapToQuantitive(
            dateNormalizer,
            setting.getAxis().getY(),
            keys,
            mapByDestination(routes));
        break;

      case AIRLINE:
        data = mapToQuantitive(
            dateNormalizer,
            setting.getAxis().getY(),
            keys,
            mapByAirline(routes));
        break;
    }

    return FilterResponse.builder()
        .data(data)
        .x(keys.stream().sorted().map(dateNormalizer::format).collect(Collectors.toList()))
        .y(setting.getAxis().getY())
        .z(setting.getAxis().getX())
        .build();
  }

  private void checkSetting(@NonNull Setting setting) {
    if (setting == null)
      throw new AssertionError("Setting should not be null!");
    if (setting.getAxis() == null)
      throw new AssertionError("Axis should not be null!");
    if (setting.getFilter() == null)
      throw new AssertionError("Filter should not be null!");
  }

  public Map<String, List<Double>> mapByTime(
      DateNormalizer dateNormalizer,
      QuantitiveValue quant, Set<Date> keys, List<Route> routes) {

    //Sort by Date and sum values
    Map<Date, List<Double>> dateMap = routes
        .stream()
        .peek(route -> route.setDate(dateNormalizer.normalizeDate(route.getDate())))
        .collect(Collectors.groupingBy(Route::getDate,
            Collectors.collectingAndThen(
                Collectors.summingDouble(route -> getQuant(quant, route)),
                Collections::singletonList
            )
            )
        );

    //Insert missing keys
    keys.forEach(keyValue -> dateMap.putIfAbsent(keyValue, new ArrayList<>()));

    return dateMap.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(entry -> dateNormalizer.format(entry.getKey()),
            Map.Entry::getValue,
            (key1, key2) -> key1,
            LinkedHashMap::new
        ));

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

  public Map<String, List<Double>> mapToQuantitive(
      DateNormalizer dateNormalizer, QuantitiveValue quant, Set<Date> keys, Map<String, List<Route>> routeMap) {

    Map<String, List<Double>> result = new TreeMap<>();

    //Map to quantity for each key
    for (String key : routeMap.keySet()) {

      //Sort by Date
      Map<Date, List<Route>> dateMap = routeMap.get(key)
          .stream()
          .peek(route -> route.setDate(dateNormalizer.normalizeDate(route.getDate())))
          .collect(Collectors.groupingBy(Route::getDate,
              Collectors.mapping(route -> route, Collectors.toList())));

      //Insert missing keys
      keys.forEach(keyValue -> dateMap.putIfAbsent(keyValue, new ArrayList<>()));

      //Strip to one value
      List<Double> values = dateMap.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .map(e -> e.getValue().stream().mapToDouble(route -> getQuant(quant, route)).sum())
          .collect(Collectors.toList());

      result.put(key, values);
    }

    return result;
  }

  public double getQuant(QuantitiveValue quant, Route route) {
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

  public Set<Date> getDateRangeKeys(Date rangeFrom, Date rangeTo, TimeSteps timestep, DateNormalizer dateNormalizer) {

    Set<Date> result = new LinkedHashSet<>();

    Calendar calendar = Calendar.getInstance(Locale.US);

    //Init Steps
    int calendarStep = 0;
    switch (timestep) {
      case DAY_OF_WEEK:
        calendarStep = Calendar.DAY_OF_WEEK;
        break;
      case MONTH:
        calendarStep = Calendar.MONTH;
        break;
      case YEAR:
        calendarStep = Calendar.YEAR;
        break;
    }

    //Init start
    calendar.setTime(rangeFrom);

    //Normalize start day for year and months
    if (timestep != TimeSteps.DAY_OF_WEEK)
      calendar.set(Calendar.DAY_OF_MONTH, 1);

    //FIX FOR rangeTo-Bug as Spring writes a day starting by 02:00:000
    calendar.set(Calendar.HOUR_OF_DAY, 2);

    //Save Keys into List
    while (calendar.getTime().before(rangeTo)) {

      result.add(dateNormalizer.normalizeDate(calendar.getTime()));

      calendar.add(calendarStep, 1);
    }

    return result;
  }

}

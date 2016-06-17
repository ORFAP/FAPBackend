package de.orfap.fap.backend.controller;

import de.orfap.fap.backend.domain.Airline;
import de.orfap.fap.backend.domain.Market;
import de.orfap.fap.backend.domain.QuantitiveValue;
import de.orfap.fap.backend.domain.Route;
import de.orfap.fap.backend.domain.Setting;
import de.orfap.fap.backend.domain.TimeSteps;
import de.orfap.fap.backend.repositories.AirlineRepository;
import de.orfap.fap.backend.repositories.MarketRepository;
import de.orfap.fap.backend.repositories.RouteRepository;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
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
 * <p>
 * Provides additional interfaces for the route entity.
 */
@RestController
@ExposesResourceFor(Route.class)
@RequestMapping("/routes")
public class RouteController {

  public static final Logger LOG = LoggerFactory.getLogger(RouteController.class);


  @Autowired
  RouteRepository routeRepository;

  @Autowired
  AirlineRepository airlineRepository;

  @Autowired
  MarketRepository marketRepository;

  @Autowired
  Validator validator;

  /**
   * Saves a list of routes at once.
   *
   * @param routes list of routes to save.
   * @return saved routes.
   */
  @RequestMapping(value = "saveAll", method = RequestMethod.POST)
  @CacheEvict(value = {"yearRoutes", "filter"}, allEntries = true)
  public ResponseEntity saveAll(@RequestBody List<RouteRequest> routes, BindingResult bindingResult) {

    List<Route> routeList = null;

    try {
      routeList = routes.stream().map(routeRequest -> {

        Airline airline = airlineRepository.findOne(routeRequest.getAirline());
        Market source = marketRepository.findOne(routeRequest.getSource());
        Market destination = marketRepository.findOne(routeRequest.getDestination());

        return new Route(
            routeRequest.getDate(),
            routeRequest.getDelays(),
            routeRequest.getCancelled(),
            routeRequest.getPassengerCount(),
            routeRequest.getFlightCount(),
            airline,
            source,
            destination
        );

      })
          .peek(route -> {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(route, bindingResult.getObjectName());
            validator.validate(route, errors);
            if (errors.hasErrors())
              bindingResult.addAllErrors(errors);
          })
          .collect(Collectors.toList());

    } catch (Exception exception){
      bindingResult.addError(new ObjectError(exception.getClass().getName(), exception.getMessage()));
    }

    if(bindingResult.hasErrors()){
      return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
    }

    routeRepository.save(routeList);
    return ResponseEntity.ok().build();
  }

  /**
   * Find routes by a given year.
   *
   * @param year to filter routes by
   * @return routes of given year
   */
  @RequestMapping(value = "/search/findByYear", method = RequestMethod.GET)
  @Cacheable("yearRoutes")
  public List<Route> findByYear(@RequestParam("year") Integer year) {

    if (year == null || year < 1970)
      throw new IllegalArgumentException("Year should not be null and greater than 1970!");

    //Format start and end of given year
    DateNormalizer dateNormalizer = new DateNormalizer(TimeSteps.YEAR);

    Date start;
    Date end;

    try {
      start = dateNormalizer.parse(year.toString());
      end = dateNormalizer.parse((Integer.toString(year + 1)));

    } catch (ParseException e) {
      throw new AssertionError("Year could not be parsed to date.");
    }

    return routeRepository.findByDateBetween(start, end);

  }

  /**
   * Test if there is are routes in the given month of year.
   *
   * @param date to find
   * @return true if there is are routes saved
   */
  @RequestMapping(value = "/search/isRouteInMonthOfYear", method = RequestMethod.GET)
  public Boolean isRouteInMonthOfYear(@RequestParam("date")@DateTimeFormat(pattern="yyyy-MM-dd") Date date) {

    if(date == null)
      throw new IllegalArgumentException("Date should not be null.");

    //Init Calendar to given date
    Calendar calendar = Calendar.getInstance(Locale.US);
    calendar.setTime(date);

    //set start day to one to get whole month
    calendar.set(Calendar.DAY_OF_MONTH, 1);

    //Get Start and End of month
    Date start = calendar.getTime();

    calendar.set(Calendar.MONTH, calendar.getMaximum(Calendar.MONTH));
    Date end = calendar.getTime();

    return !routeRepository.findByDateBetween(start, end).isEmpty();


  }

  /**
   * Provides an interface to format data with given settings.
   *
   * @param setting to format data.
   * @return formatted data
   */
  @RequestMapping(value = "/filter", method = RequestMethod.POST)
  @Cacheable("filter")
  public FilterResponse filter(@RequestBody Setting setting) {

    LOG.info("FILTER:" + setting.toString());

    checkSetting(setting);

    //Find filtered Data
    List<Route> routes = routeRepository.findByDateBetweenAndFilteredByMarketAirline(
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
      throw new IllegalArgumentException("Setting should not be null!");
    if (setting.getAxis() == null)
      throw new IllegalArgumentException("Axis should not be null!");
    if (setting.getFilter() == null)
      throw new IllegalArgumentException("Filter should not be null!");
  }

  /**
   * Maps given routes by time to a given quantitive value.
   *
   * @param dateNormalizer to normalize dates.
   * @param quant          Quantitive Value to map.
   * @param keys           all possible keys of map.
   * @param routes         data to map on.
   * @return mapped data.
   */
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
    keys.forEach(keyValue -> dateMap.putIfAbsent(keyValue, Collections.singletonList(0.0)));

    return dateMap.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(entry -> dateNormalizer.format(entry.getKey()),
            Map.Entry::getValue,
            (key1, key2) -> key1,
            LinkedHashMap::new
        ));

  }

  /**
   * Maps routes by Airline.
   *
   * @param routes to map.
   * @return mapped routes.
   */
  public Map<String, List<Route>> mapByAirline(List<Route> routes) {

    //Grouping Routes to Airlines
    return routes.stream()
        .collect(Collectors.groupingBy(route -> route.getAirline().getName(),
            Collectors.mapping(route -> route, Collectors.toList())));

  }

  /**
   * Maps routes by Destination.
   *
   * @param routes to map.
   * @return mapped routes.
   */
  public Map<String, List<Route>> mapByDestination(List<Route> routes) {

    //Grouping Routes to Destination
    return routes.stream()
        .collect(Collectors.groupingBy(route -> route.getDestination().getName(),
            Collectors.mapping(route -> route, Collectors.toList())));

  }

  /**
   * Maps a given route map by time to a given quantitive value.
   *
   * @param dateNormalizer to normalize dates.
   * @param quant          Quantitive Value to map.
   * @param keys           all possible keys of map.
   * @param routeMap       data to map on.
   * @return mapped data.
   */
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

  /**
   * Getter of quantitative value of a given route.
   *
   * @param quant quantitative value to return.
   * @param route source of value.
   * @return quantitative value of route.
   */
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

  /**
   * Calculates all possible date keys of a given range.
   *
   * @param rangeFrom      start of range (included)
   * @param rangeTo        end of range (excluded)
   * @param timestep       step to take
   * @param dateNormalizer normalizer to format dates.
   * @return Set of all possible date keys.
   */
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
      case WEEK_OF_YEAR:
        calendarStep = Calendar.WEEK_OF_YEAR;
    }

    //Init start
    calendar.setTime(rangeFrom);

    //Normalize start day for year and months
    if (timestep != TimeSteps.DAY_OF_WEEK && timestep != TimeSteps.WEEK_OF_YEAR)
      calendar.set(Calendar.DAY_OF_MONTH, 1);
    else if (timestep == TimeSteps.WEEK_OF_YEAR)
      calendar.set(Calendar.DAY_OF_WEEK, 1);

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

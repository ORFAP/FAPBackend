package de.orfap.fap.backend;

import de.orfap.fap.backend.controller.DateNormalizer;
import de.orfap.fap.backend.controller.RouteController;
import de.orfap.fap.backend.domain.Airline;
import de.orfap.fap.backend.domain.Market;
import de.orfap.fap.backend.domain.QuantitiveValue;
import de.orfap.fap.backend.domain.Route;
import de.orfap.fap.backend.domain.TimeSteps;
import de.orfap.fap.backend.repositories.AirlineRepository;
import de.orfap.fap.backend.repositories.MarketRepository;
import de.orfap.fap.backend.repositories.RouteRepository;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FapBackendApplication.class)
@WebAppConfiguration
@SuppressWarnings({"unchecked", "Duplicates"})
public class FapBackendApplicationTests {

  private List<Route> routes = new ArrayList<>();
  private SimpleDateFormat dateParser;

  @Autowired
  RouteRepository routeRepository;
  @Autowired
  MarketRepository cityRepository;
  @Autowired
  AirlineRepository airlineRepository;


  @Autowired
  RouteController routeController;

  @Test
  public void testFilterFindBy1() throws Exception {

    List<Route> result = routeRepository.findByDateBetweenAirportDestination(
        dateParser.parse("2014-01-01"),
        dateParser.parse("2014-01-03"),
        Collections.singletonList("LLL"),
        Collections.EMPTY_LIST
    );

    assertThat(result,
        IsIterableContainingInAnyOrder.containsInAnyOrder(Collections.singleton(routes.get(1)).toArray()));
  }

  @Test
  public void testFilterFindBy2() throws Exception {

    List<Route> result = routeRepository.findByDateBetweenAirportDestination(
        dateParser.parse("2014-01-01"),
        dateParser.parse("2014-01-03"),
        Collections.EMPTY_LIST,
        Collections.singletonList("NNN")
    );

    assertThat(result,
        IsIterableContainingInAnyOrder.containsInAnyOrder(Collections.singleton(routes.get(2)).toArray()));
  }

  @Test
  public void testFilterFindBy3() throws Exception {

    List<Route> result = routeRepository.findByDateBetweenAirportDestination(
        dateParser.parse("2014-01-01"),
        dateParser.parse("2014-01-03"),
        Collections.singletonList("AAA"),
        Collections.singletonList("DDD")
    );

    assertThat(result,
        IsIterableContainingInAnyOrder.containsInAnyOrder(Collections.singleton(routes.get(0)).toArray()));
  }

  @Test
  public void testFilterFindBy4() throws Exception {

    List<Route> result = routeRepository.findByDateBetweenAirportDestination(
        dateParser.parse("2014-01-01"),
        dateParser.parse("2014-01-03"),
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST
    );

    assertThat(result,
        IsIterableContainingInAnyOrder.containsInAnyOrder(routes.subList(0, 3).toArray()));
  }

  @Test
  public void mapByAirline() {
    Map<String, List<Route>> result = routeController.mapByAirline(routes.subList(0, 3));

    Map<String, List<Route>> check = new TreeMap<>();
    //Airberlin
    List<Route> airberlinRoutes = new ArrayList<>();
    airberlinRoutes.add(routes.get(0));
    airberlinRoutes.add(routes.get(2));
    check.put("AirBerlin", airberlinRoutes);
    //Lufthansa
    List<Route> lufthansaRoutes = new ArrayList<>();
    lufthansaRoutes.add(routes.get(1));
    check.put("Lufthansa", lufthansaRoutes);


    assertEquals(check, result);
  }

  @Test
  public void mapByDestination() {
    Map<String, List<Route>> result = routeController.mapByDestination(routes.subList(0, 3));

    Map<String, List<Route>> check = new TreeMap<>();
    //Detroit
    List<Route> detroitRoutes = new ArrayList<>();
    detroitRoutes.add(routes.get(0));
    check.put("Detroit", detroitRoutes);

    List<Route> sanFranRoutes = new ArrayList<>();
    sanFranRoutes.add(routes.get(1));
    check.put("SanFrancisco", sanFranRoutes);

    List<Route> newYorkRoutes = new ArrayList<>();
    newYorkRoutes.add(routes.get(2));
    check.put("NewYork", newYorkRoutes);


    assertEquals(check, result);
  }

  @Test
  public void mapByTimeDay() {
    Map<String, List<Double>> result = routeController.mapByTime(
        new DateNormalizer(TimeSteps.DAY_OF_WEEK),
        QuantitiveValue.FLIGHTS,
        routes.subList(0, 3));


    Map<String, List<Double>> check = new LinkedHashMap<>();
    check.put("Wednesday", Collections.singletonList(1.0));
    check.put("Thursday", Collections.singletonList(1.0));
    check.put("Friday", Collections.singletonList(1.0));



    assertEquals(check.toString(), result.toString());
  }

  @Test
  public void mapByTimeMonth() {
    Map<String, List<Double>> result = routeController.mapByTime(
        new DateNormalizer(TimeSteps.MONTH),
        QuantitiveValue.FLIGHTS,
        routes.subList(0, 5));


    Map<String, List<Double>> check = new LinkedHashMap<>();
    check.put("January", Collections.singletonList(3.0));
    check.put("February", Collections.singletonList(1.0));
    check.put("March", Collections.singletonList(1.0));

    assertEquals(check.toString(), result.toString());
  }

  @Test
  public void mapByTimeYear() {
    Map<String, List<Double>> result = routeController.mapByTime(
        new DateNormalizer(TimeSteps.YEAR),
        QuantitiveValue.FLIGHTS,
        routes.subList(0, 7));


    Map<String, List<Double>> check = new LinkedHashMap<>();
    check.put("2014", Collections.singletonList(5.0));
    check.put("2015", Collections.singletonList(1.0));
    check.put("2016", Collections.singletonList(1.0));

    assertEquals(check.toString(), result.toString());
  }

  @Test
  public void mapToQuantYear() {

    Map<String, List<Route>> routeMap = new HashMap<>();
    //Detroit
    List<Route> detroitRoutes = new ArrayList<>();
    detroitRoutes.add(routes.get(0));
    detroitRoutes.add(routes.get(4));
    routeMap.put("Detroit", detroitRoutes);

    List<Route> sanFranRoutes = new ArrayList<>();
    sanFranRoutes.add(routes.get(1));
    sanFranRoutes.add(routes.get(5));
    routeMap.put("SanFrancisco", sanFranRoutes);

    List<Route> newYorkRoutes = new ArrayList<>();
    newYorkRoutes.add(routes.get(2));
    newYorkRoutes.add(routes.get(3));
    newYorkRoutes.add(routes.get(6));
    routeMap.put("NewYork", newYorkRoutes);


    SimpleDateFormat formatter = new SimpleDateFormat("yyyy", Locale.US);
    Set<Date> keys = new HashSet<>();
    try {
      keys.add(formatter.parse("2014"));
      keys.add(formatter.parse("2015"));
      keys.add(formatter.parse("2016"));

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }


    Map<String, List<Double>> result = routeController.mapToQuantitive(
        new DateNormalizer(TimeSteps.YEAR),
        QuantitiveValue.FLIGHTS,
        keys,
        routeMap);


    Map<String, List<Double>> check = new TreeMap<>();
    check.put("Detroit", Arrays.asList(2.0, 0.0, 0.0));
    check.put("SanFrancisco", Arrays.asList(1.0, 1.0, 0.0));
    check.put("NewYork", Arrays.asList(2.0, 0.0, 1.0));

    assertEquals(check, result);


  }

  @Test
  public void mapToQuantMonth() {

    Map<String, List<Route>> routeMap = new HashMap<>();
    //Detroit
    List<Route> detroitRoutes = new ArrayList<>();
    detroitRoutes.add(routes.get(0));
    detroitRoutes.add(routes.get(4));
    routeMap.put("Detroit", detroitRoutes);

    List<Route> sanFranRoutes = new ArrayList<>();
    sanFranRoutes.add(routes.get(1));
    sanFranRoutes.add(routes.get(5));
    routeMap.put("SanFrancisco", sanFranRoutes);

    List<Route> newYorkRoutes = new ArrayList<>();
    newYorkRoutes.add(routes.get(2));
    newYorkRoutes.add(routes.get(3));
    newYorkRoutes.add(routes.get(6));
    routeMap.put("NewYork", newYorkRoutes);


    SimpleDateFormat formatter = new SimpleDateFormat("MMMM", Locale.US);
    Set<Date> keys = new HashSet<>();
    try {
      keys.add(formatter.parse("January"));
      keys.add(formatter.parse("February"));
      keys.add(formatter.parse("March"));
      keys.add(formatter.parse("April"));
      keys.add(formatter.parse("May"));
      keys.add(formatter.parse("June"));
      keys.add(formatter.parse("July"));
      keys.add(formatter.parse("August"));
      keys.add(formatter.parse("September"));
      keys.add(formatter.parse("October"));
      keys.add(formatter.parse("November"));
      keys.add(formatter.parse("December"));

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }


    Map<String, List<Double>> result = routeController.mapToQuantitive(
        new DateNormalizer(TimeSteps.MONTH),
        QuantitiveValue.FLIGHTS,
        keys,
        routeMap);


    Map<String, List<Double>> check = new TreeMap<>();
    check.put("Detroit", Arrays.asList(1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
    check.put("SanFrancisco", Arrays.asList(2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
    check.put("NewYork", Arrays.asList(2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

    assertEquals(check, result);


  }

  @Test
  public void mapToQuantDayOfWeek() {

    Map<String, List<Route>> routeMap = new HashMap<>();
    //Detroit
    List<Route> detroitRoutes = new ArrayList<>();
    detroitRoutes.add(routes.get(0));
    detroitRoutes.add(routes.get(4));
    routeMap.put("Detroit", detroitRoutes);

    List<Route> sanFranRoutes = new ArrayList<>();
    sanFranRoutes.add(routes.get(1));
    sanFranRoutes.add(routes.get(5));
    routeMap.put("SanFrancisco", sanFranRoutes);

    List<Route> newYorkRoutes = new ArrayList<>();
    newYorkRoutes.add(routes.get(2));
    newYorkRoutes.add(routes.get(3));
    newYorkRoutes.add(routes.get(6));
    routeMap.put("NewYork", newYorkRoutes);


    DateNormalizer dateNormalizer = new DateNormalizer(TimeSteps.DAY_OF_WEEK);
    Set<Date> keys = new HashSet<>();
    try {
      keys.add(dateNormalizer.parse("Monday"));
      keys.add(dateNormalizer.parse("Tuesday"));
      keys.add(dateNormalizer.parse("Wednesday"));
      keys.add(dateNormalizer.parse("Thursday"));
      keys.add(dateNormalizer.parse("Friday"));
      keys.add(dateNormalizer.parse("Saturday"));
      keys.add(dateNormalizer.parse("Sunday"));

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }


    Map<String, List<Double>> result = routeController.mapToQuantitive(
        dateNormalizer,
        QuantitiveValue.FLIGHTS,
        keys,
        routeMap);


    Map<String, List<Double>> check = new TreeMap<>();
    check.put("Detroit", Arrays.asList(0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0));
    check.put("SanFrancisco", Arrays.asList(0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0));
    check.put("NewYork", Arrays.asList(0.0, 0.0, 0.0, 0.0, 2.0, 1.0, 0.0));

    assertEquals(check, result);


  }

  @Test
  public void dateRangeKeysYear() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    DateNormalizer dateNormalizer = new DateNormalizer(TimeSteps.YEAR);
    Date from;
    Date to;
    try {
      from = formatter.parse("2014-01-05");
      to = formatter.parse("2016-01-02");

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }

    Set<Date> result = routeController.getDateRangeKeys(
        from, to, TimeSteps.YEAR, dateNormalizer);


    Set<Date> check = new HashSet<>();
    try {
      check.add(dateNormalizer.parse("2014"));
      check.add(dateNormalizer.parse("2015"));
      check.add(dateNormalizer.parse("2016"));

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }

    assertEquals(check, result);

  }

  @Test
  public void dateRangeKeysMonth() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    DateNormalizer dateNormalizer = new DateNormalizer(TimeSteps.MONTH);
    Date from;
    Date to;
    try {
      from = formatter.parse("2014-01-05");
      to = formatter.parse("2014-03-02");

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }

    Set<Date> result = routeController.getDateRangeKeys(
        from, to, TimeSteps.MONTH, dateNormalizer);


    Set<Date> check = new HashSet<>();
    try {
      check.add(dateNormalizer.parse("January"));
      check.add(dateNormalizer.parse("February"));
      check.add(dateNormalizer.parse("March"));

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }

    assertEquals(check, result);

  }

  @Test
  public void dateRangeKeysDaysOfWeek() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    DateNormalizer dateNormalizer = new DateNormalizer(TimeSteps.DAY_OF_WEEK);
    Date from;
    Date to;
    try {
      from = formatter.parse("2014-01-05");
      to = formatter.parse("2014-01-11");

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }

    Set<Date> result = routeController.getDateRangeKeys(
        from, to, TimeSteps.DAY_OF_WEEK, dateNormalizer);


    Set<Date> check = new HashSet<>();
    try {
      check.add(dateNormalizer.parse("Monday"));
      check.add(dateNormalizer.parse("Tuesday"));
      check.add(dateNormalizer.parse("Wednesday"));
      check.add(dateNormalizer.parse("Thursday"));
      check.add(dateNormalizer.parse("Friday"));
      check.add(dateNormalizer.parse("Sunday"));

    } catch (ParseException e) {
      throw new AssertionError("Date parse Error");
    }

    assertEquals(check, result);

  }


  @Before
  public void setUp() {
    routeRepository.deleteAll();
    cityRepository.deleteAll();
    airlineRepository.deleteAll();

    Airline airberlin = new Airline("AirBerlin", "AAA");
    Airline lufthansa = new Airline("Lufthansa", "LLL");

    airlineRepository.save(airberlin);
    airlineRepository.save(lufthansa);

    Market newYork = new Market("NewYork", "NNN");
    Market detroit = new Market("Detroit", "DDD");
    Market sanFran = new Market("SanFrancisco", "SSS");

    cityRepository.save(newYork);
    cityRepository.save(detroit);
    cityRepository.save(sanFran);

    dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    Date firstJan14;
    Date secondJan14;
    Date thirdJan14;
    Date firstFeb14;
    Date firstMar14;
    Date firstJan15;
    Date firstJan16;
    try {

      firstJan14 = dateParser.parse("2014-01-01");
      secondJan14 = dateParser.parse("2014-01-02");
      thirdJan14 = dateParser.parse("2014-01-03");
      firstFeb14 = dateParser.parse("2014-02-01");
      firstMar14 = dateParser.parse("2014-03-01");
      firstJan15 = dateParser.parse("2015-01-01");
      firstJan16 = dateParser.parse("2016-01-01");

    } catch (ParseException e) {
      throw new AssertionError("Error on Date parsing");
    }

    //0
    routes.add(Route.builder()
        .airline(airberlin)
        .source(newYork)
        .destination(detroit)
        .cancelled(1)
        .delays(1)
        .flightCount(1)
        .passengerCount(1)
        .date(firstJan14)
        .build()
    );
    //1
    routes.add(Route.builder()
        .airline(lufthansa)
        .source(newYork)
        .destination(sanFran)
        .cancelled(1)
        .delays(1)
        .flightCount(1)
        .passengerCount(1)
        .date(secondJan14)
        .build()
    );
    //2
    routes.add(Route.builder()
        .airline(airberlin)
        .source(detroit)
        .destination(newYork)
        .cancelled(1)
        .delays(1)
        .flightCount(1)
        .passengerCount(1)
        .date(thirdJan14)
        .build()
    );
    //3
    routes.add(Route.builder()
        .airline(lufthansa)
        .source(sanFran)
        .destination(newYork)
        .cancelled(1)
        .delays(1)
        .flightCount(1)
        .passengerCount(1)
        .date(firstFeb14)
        .build()
    );
    //4
    routes.add(Route.builder()
        .airline(airberlin)
        .source(newYork)
        .destination(detroit)
        .cancelled(1)
        .delays(1)
        .flightCount(1)
        .passengerCount(1)
        .date(firstMar14)
        .build()
    );
    //5
    routes.add(Route.builder()
        .airline(lufthansa)
        .source(newYork)
        .destination(sanFran)
        .cancelled(1)
        .delays(1)
        .flightCount(1)
        .passengerCount(1)
        .date(firstJan15)
        .build()
    );
    //6
    routes.add(Route.builder()
        .airline(airberlin)
        .source(sanFran)
        .destination(newYork)
        .cancelled(1)
        .delays(1)
        .flightCount(1)
        .passengerCount(1)
        .date(firstJan16)
        .build()
    );

    routeRepository.save(routes);
  }

  @After
  public void tearDown() {
    routes.clear();
  }

}

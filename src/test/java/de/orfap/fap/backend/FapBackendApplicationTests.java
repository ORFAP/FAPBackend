package de.orfap.fap.backend;

import de.orfap.fap.backend.controller.RouteController;
import de.orfap.fap.backend.domain.Airline;
import de.orfap.fap.backend.domain.City;
import de.orfap.fap.backend.domain.QuantitiveValue;
import de.orfap.fap.backend.domain.Route;
import de.orfap.fap.backend.domain.TimeSteps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FapBackendApplication.class)
@WebAppConfiguration
public class FapBackendApplicationTests {

  private List<Route> routes = new ArrayList<>();

  RouteController routeController = new RouteController();


  @Test
  public void mapByAirline() {
    Map<String, List<Route>> result = routeController.mapByAirline(routes.subList(0, 3));

    HashMap<String, List<Route>> check = new HashMap<>();
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

    HashMap<String, List<Route>> check = new HashMap<>();
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
    Map<String, Integer> result = routeController.mapByTime(
        routeController.getDateTimeFormatter(TimeSteps.DAY_OF_WEEK),
        QuantitiveValue.FLIGHTS,
        routes.subList(0, 3));


    Map<String, Integer> check = new HashMap<>();
    check.put("Wednesday", 1);
    check.put("Thursday", 1);
    check.put("Friday", 1);

    assertEquals(check, result);
  }

  @Test
  public void mapByTimeMonth() {
    Map<String, Integer> result = routeController.mapByTime(
        routeController.getDateTimeFormatter(TimeSteps.MONTH),
        QuantitiveValue.FLIGHTS,
        routes.subList(0, 5));


    Map<String, Integer> check = new HashMap<>();
    check.put("January", 3);
    check.put("February", 1);
    check.put("March", 1);

    assertEquals(check, result);
  }

  @Test
  public void mapByTimeYear() {
    Map<String, Integer> result = routeController.mapByTime(
        routeController.getDateTimeFormatter(TimeSteps.YEAR),
        QuantitiveValue.FLIGHTS,
        routes.subList(0, 7));


    Map<String, Integer> check = new HashMap<>();
    check.put("2014", 5);
    check.put("2015", 1);
    check.put("2016", 1);

    assertEquals(check, result);
  }

  @Test
  public void mapToQuant(){

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


    Map<String, Integer> result = routeController.mapToQuantitive(
        routeController.getDateTimeFormatter(TimeSteps.MONTH),
        QuantitiveValue.FLIGHTS,
        routeMap);


    Map<String, Integer> check = new HashMap<>();
    check.put("Detroit", 2);
    check.put("SanFrancisco", 2);
    check.put("NewYork", 3);

    assertEquals(check, result);


  }


  @Before
  public void setUp() {

    Airline airberlin = new Airline("AirBerlin", "XXX");
    Airline lufthansa = new Airline("Lufthansa", "YYY");

    City newYork = new City("NewYork", "NNN");
    City detroit = new City("Detroit", "DDD");
    City sanFran = new City("SanFrancisco", "SSS");

    SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");

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
  }

  @After
  public void tearDown() {
    routes.clear();
  }

}

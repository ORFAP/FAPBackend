package de.orfap.fap.backend;

import com.jayway.restassured.RestAssured;
import de.orfap.fap.backend.domain.Airline;
import de.orfap.fap.backend.repositories.AirlineRepository;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.jayway.restassured.RestAssured.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FapBackendApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class FapBackendApplicationTests {

  public static final String BASEPATH = "/airlines";

  @Value("${local.server.port}")
  private int serverPort;

  @Autowired
  AirlineRepository airlineRepository;

  private Airline first;
  private Airline second;

  @Before
  public void setUp() {
    airlineRepository.deleteAll();

    Airline airline0 = new Airline();
    airline0.setName("AIR");
    airline0.setId("X-X");

    first = airlineRepository.save(airline0);

    Airline airline1 = new Airline();
    airline1.setName("Luft");
    airline1.setId("Y-X");

    second = airlineRepository.save(airline1);

    RestAssured.port = serverPort;
  }

  @Test
  public void canFetchFirst() {

    String firstID = first.getId();

    when().
        get(BASEPATH + "/{id}", firstID).
    then().
        statusCode(HttpStatus.SC_OK).
        body("name", Matchers.is("AIR"));

  }

}

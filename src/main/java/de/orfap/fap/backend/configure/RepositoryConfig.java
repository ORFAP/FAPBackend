package de.orfap.fap.backend.configure;

import de.orfap.fap.backend.domain.Airline;
import de.orfap.fap.backend.domain.Market;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

/**
 * Organization: HM FK07.
 * Project: BikeBattleBackend, edu.hm.cs.bikebattle.config
 * Author(s): Rene Zarwel
 * Date: 26.04.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@Configuration
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {

  /**
   * Expose IDs of some entities on REST Interface.
   * @param config repo Config
   */
  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    config.exposeIdsFor(Airline.class);
    config.exposeIdsFor(Market.class);
  }
}

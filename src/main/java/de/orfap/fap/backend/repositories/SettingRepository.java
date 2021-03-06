package de.orfap.fap.backend.repositories;

import de.orfap.fap.backend.domain.Setting;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

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
public interface SettingRepository extends CrudRepository<Setting, UUID> {

  //FindBy Methods
  // http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods

  List<Setting> findByNameContainingIgnoreCaseOrCreatorContainingIgnoreCase(
      @Param("name") String name,
      @Param("name") String creator);

  List<Setting> findByCreatorContainingIgnoreCaseOrShareableTrue(
      @Param("name") String creator);


}

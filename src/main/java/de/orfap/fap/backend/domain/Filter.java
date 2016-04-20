package de.orfap.fap.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.validation.constraints.Past;
import java.util.Date;
import java.util.List;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.domain
 * Author(s): Rene Zarwel
 * Date: 06.04.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Filter {

  @ElementCollection
  List<String> originFilter;

  @ElementCollection
  List<String> destinationFilter;

  @ElementCollection
  List<String> arlineFilter;

  @Past
  Date rangeFrom;

  @Past
  Date rangeTo;

}

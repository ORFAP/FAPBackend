package de.orfap.fap.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.domain
 * Author(s): Rene Zarwel
 * Date: 06.04.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Route extends BaseEntity {

  @NotNull
  @NonNull
  @Past
  Date date;

  @Min(0)
  double delays;

  @Min(0)
  double cancelled;

  @Min(0)
  double passengerCount;

  @Min(0)
  double flightCount;

  @ManyToOne
  @NotNull
  @NonNull
  Airline airline;

  @ManyToOne
  @NotNull
  @NonNull
  Market source;

  @ManyToOne
  @NotNull
  @NonNull
  Market destination;

}

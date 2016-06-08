package de.orfap.fap.backend.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Date;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.controller
 * Author(s): Rene Zarwel
 * Date: 08.06.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class RouteRequest {

  @NonNull
  private Date date;

  private double delays;

  private double cancelled;

  private double passengerCount;

  private double flightCount;


  @NonNull
  private String airline;

  @NonNull
  private String source;

  @NonNull
  private String destination;
}

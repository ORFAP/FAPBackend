package de.orfap.fap.backend.controller;

import de.orfap.fap.backend.domain.QualitiativeValue;
import de.orfap.fap.backend.domain.QuantitiveValue;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.controller
 * Author(s): Rene Zarwel
 * Date: 16.05.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 *
 * Response of the Filter method for routes.
 */
@Data
@Builder
public class FilterResponse {

  QuantitiveValue y;

  QualitiativeValue z;

  List<String> x;

  Map<String, List<Double>> data;
}

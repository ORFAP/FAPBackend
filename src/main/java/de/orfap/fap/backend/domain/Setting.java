package de.orfap.fap.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.domain
 * Author(s): Rene Zarwel
 * Date: 18.04.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Setting extends BaseEntity{

  @NotNull
  @NonNull
  @Size(min = 3)
  String name;

  @NotNull
  @NonNull
  @Size(min = 3)
  String creator;

  boolean shareable;

  @Embedded
  Filter filter;

  @Embedded
  Axis axis;

}

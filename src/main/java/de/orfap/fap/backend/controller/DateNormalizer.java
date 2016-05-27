package de.orfap.fap.backend.controller;

import de.orfap.fap.backend.domain.TimeSteps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Organization: HM FK07.
 * Project: FAPBackend, de.orfap.fap.backend.controller
 * Author(s): Rene Zarwel
 * Date: 16.05.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 *
 *
 * Normalizes given dates to a given timestep, so they compared to each other.
 */
public class DateNormalizer {

  private final TimeSteps steps;

  private SimpleDateFormat formatter;

  private static final Date MONDAY;
  private static final Date TUESDAY;
  private static final Date WEDNESDAY;
  private static final Date THURSDAY;
  private static final Date FRIDAY;
  private static final Date SATURDAY;
  private static final Date SUNDAY;

  /**
   * Init the Day-Of-Week data.
   *
   * Due to the fakt that the beginning of
   * the Date.class, which is the 01.01.1970,
   * is not a Monday and so a sorting of normalized dates
   * will be fail, if the dates are not shifted to the 05.01.1970.
   */
  static {
    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    try {
      MONDAY = format.parse("05.01.1970");
      TUESDAY = format.parse("06.01.1970");
      WEDNESDAY = format.parse("07.01.1970");
      THURSDAY = format.parse("08.01.1970");
      FRIDAY = format.parse("09.01.1970");
      SATURDAY = format.parse("10.01.1970");
      SUNDAY = format.parse("11.01.1970");

    }catch (ParseException e){
      throw new AssertionError("Date parse error");
    }
  }

  public DateNormalizer(TimeSteps steps) {
    this.steps = steps;

    formatter = getDateTimeFormatter(steps);
  }

  /**
   * Normalize the given date to the timesteps.
   * @param date to normalize
   * @return normalized date
   */
  public Date normalizeDate(Date date) {

    if(steps != TimeSteps.DAY_OF_WEEK) {
      try {
        return formatter.parse(formatter.format(date));
      } catch (ParseException e) {
        throw new AssertionError("Date Parse Error");
      }
    } else {

      switch (formatter.format(date)) {
        case "Monday": return MONDAY;
        case "Tuesday": return TUESDAY;
        case "Wednesday": return WEDNESDAY;
        case "Thursday": return THURSDAY;
        case "Friday": return FRIDAY;
        case "Saturday": return SATURDAY;
        case "Sunday": return SUNDAY;

        default: return null; //Not gonna happen
      }

    }
  }

  /**
   * Returns the normalized string representation of the date.
   * @param date to represent
   * @return normalized string
   */
  public String format(Date date){
    return formatter.format(date);
  }

  public Date parse (String date) throws ParseException{
    return normalizeDate(formatter.parse(date));
  }

  private SimpleDateFormat getDateTimeFormatter(TimeSteps timeStep) {
    switch (timeStep) {

      case DAY_OF_WEEK:
        return new SimpleDateFormat("EEEE", Locale.US);
      case MONTH:
        return new SimpleDateFormat("MMMM", Locale.US);
      case YEAR:
        return new SimpleDateFormat("yyyy", Locale.US);
      default:
        return null;
    }

  }
}

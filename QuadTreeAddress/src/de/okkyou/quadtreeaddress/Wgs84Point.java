package de.okkyou.quadtreeaddress;

import java.util.Objects;

public class Wgs84Point {

  private static final double FACTOR_DEGREE_TO_TENTH_MICRO_DEGREE = 10000000.0;

  public static final int MAX_LATITUDE_TENTH_MICRO_DEGREE = 900000000;
  public static final int MIN_LATITUDE_TENTH_MICRO_DEGREE = -1 * MAX_LATITUDE_TENTH_MICRO_DEGREE;
  public static final int MAX_LONGITUDE_TENTH_MICRO_DEGREE = 1800000000;
  public static final int MIN_LONGITUDE_TENTH_MICRO_DEGREE = -1 * MAX_LONGITUDE_TENTH_MICRO_DEGREE;
  public static final double MAX_LATITUDE_DEGREE = 90.0;
  public static final double MIN_LATITUDE_DEGREE = -1.0 * MAX_LATITUDE_DEGREE;
  public static final double MAX_LONGITUDE_DEGREE = 180.0;
  public static final double MIN_LONGITUDE_DEGREE = -1 * MAX_LONGITUDE_DEGREE;

  private final int latitudeInTenthMicroDegree;
  private final int longitudeInTenthMicroDegree;

  public Wgs84Point(final double latitudeInDegree, final double longitudeInDegree) {
    this((int) (latitudeInDegree * FACTOR_DEGREE_TO_TENTH_MICRO_DEGREE),
        (int) (longitudeInDegree * FACTOR_DEGREE_TO_TENTH_MICRO_DEGREE));
  }

  public Wgs84Point(final int latitudeInTenthMicroDegree, final int longitudeInTenthMicroDegree) {
    if (latitudeInTenthMicroDegree > MAX_LATITUDE_TENTH_MICRO_DEGREE
        || latitudeInTenthMicroDegree < MIN_LATITUDE_TENTH_MICRO_DEGREE) {
      throw new IllegalArgumentException(
          "Latitude " + latitudeInTenthMicroDegree + " is not withing allowed range");
    }
    if (longitudeInTenthMicroDegree > MAX_LONGITUDE_TENTH_MICRO_DEGREE
        || longitudeInTenthMicroDegree < MIN_LONGITUDE_TENTH_MICRO_DEGREE) {
      throw new IllegalArgumentException("Longitude is not within allowed range");
    }
    this.latitudeInTenthMicroDegree = latitudeInTenthMicroDegree;
    this.longitudeInTenthMicroDegree = longitudeInTenthMicroDegree;
  }

  /**
   * @return the latitudeInTenthMicroDegree
   */
  public final int getLatitudeInTenthMicroDegree() {
    return latitudeInTenthMicroDegree;
  }

  /**
   * @return the longitudeInTenthMicroDegree
   */
  public final int getLongitudeInTenthMicroDegree() {
    return longitudeInTenthMicroDegree;
  }

  /**
   * @return the latitudeInTenthMicroDegree
   */
  public final double getLatitudeInDegree() {
    return latitudeInTenthMicroDegree / FACTOR_DEGREE_TO_TENTH_MICRO_DEGREE;
  }

  /**
   * @return the longitudeInTenthMicroDegree
   */
  public final double getLongitudeInDegree() {
    return longitudeInTenthMicroDegree / FACTOR_DEGREE_TO_TENTH_MICRO_DEGREE;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitudeInTenthMicroDegree, longitudeInTenthMicroDegree);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Wgs84Point other = (Wgs84Point) obj;
    return latitudeInTenthMicroDegree == other.latitudeInTenthMicroDegree
        && longitudeInTenthMicroDegree == other.longitudeInTenthMicroDegree;
  }

  @Override
  public String toString() {
    return "Wgs84Point [latitudeInTenthMicroDegree=" + latitudeInTenthMicroDegree
        + ", longitudeInTenthMicroDegree=" + longitudeInTenthMicroDegree + "]";
  }

}

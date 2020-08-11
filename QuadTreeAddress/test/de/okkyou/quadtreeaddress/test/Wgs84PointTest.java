package de.okkyou.quadtreeaddress.test;

import de.okkyou.quadtreeaddress.Wgs84Point;
import org.junit.Assert;
import org.junit.Test;

public class Wgs84PointTest {

  private static final Wgs84Point VALID_POINT = new Wgs84Point(49.315576, 6.750524);

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_latitudeTooLow() {
    new Wgs84Point(-90.0000001, VALID_POINT.getLongitudeInDegree());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_latitudeTooHigh() {
    new Wgs84Point(90.0000001, VALID_POINT.getLongitudeInDegree());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_longitudeTooLow() {
    new Wgs84Point(VALID_POINT.getLatitudeInDegree(), -180.0000001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_longitudeTooHigh() {
    new Wgs84Point(VALID_POINT.getLatitudeInDegree(), 180.0000001);
  }


  @Test
  public void test_getLatitudeInDegree_validAddress() {
    Assert.assertEquals(49.315576, VALID_POINT.getLatitudeInDegree(), 0.00000001);
  }

  @Test
  public void test_getLongitudeInDegree_validAddress() {
    Assert.assertEquals(6.750524, VALID_POINT.getLongitudeInDegree(), 0.00000001);
  }



}

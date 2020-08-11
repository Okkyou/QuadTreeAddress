package de.okkyou.quadtreeaddress.test;

import de.okkyou.quadtreeaddress.QuadTreeAddress;
import de.okkyou.quadtreeaddress.Wgs84Point;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class QuadTreeAddressTest {

  private static final int VALID_DEPTH = 3;
  private static final Wgs84Point VALID_POINT1 = new Wgs84Point(49.721, 9.124);
  private static final Wgs84Point VALID_POINT2 = new Wgs84Point(-49.721, -9.124);
  private static final String INVALID_QUADTREE_1 = "ACABACAB";
  private static final String INVALID_QUADTREE_2 = "+BAEA";
  private static final String VALID_QUADTREE = "+ACABD";

  private static final QuadTreeAddress ACAB_QUAD_TREE =
      QuadTreeAddress.createFromQuadTreeString("+ACAB");
  private static final QuadTreeAddress AAAAA_QUAD_TREE =
      QuadTreeAddress.createFromQuadTreeString("+AAAAA");

  @Test(expected = NullPointerException.class)
  public void test_toQuadTreeString_null1() {
    QuadTreeAddress.createFromPoint(null, QuadTreeAddressTest.VALID_DEPTH);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_toQuadTreeString_invalidDepth1() {
    QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_toQuadTreeString_invalidDepth2() {
    QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 27);
  }

  /**
   * Length of string shall be depth + 1 because of the start char.
   */
  @Test
  public void test_toQuadTreeString_depthToStringLength() {
    final int depth = QuadTreeAddress.MAX_DEPTH;
    final QuadTreeAddress quadTreeAddress = QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, depth);
    Assert.assertEquals(depth, quadTreeAddress.getDepth());
  }

  @Test(expected = NullPointerException.class)
  public void test_getPolygonOfQuadTree_null() {
    QuadTreeAddress.createFromQuadTreeString(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getPolygonOfQuadTree_invalidQuadTree1() {
    QuadTreeAddress.createFromQuadTreeString(QuadTreeAddressTest.INVALID_QUADTREE_1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_getPolygonOfQuadTree_invalidQuadTree2() {
    QuadTreeAddress.createFromQuadTreeString(QuadTreeAddressTest.INVALID_QUADTREE_2);
  }

  public void test_getPolygonOfQuadTree_validQuadTree() {
    Assert.assertEquals(QuadTreeAddressTest.VALID_QUADTREE,
        QuadTreeAddress.createFromQuadTreeString(QuadTreeAddressTest.VALID_QUADTREE).getQuadTree());
  }

  @Test
  public void test_isValid_invalid1() {
    Assert.assertFalse(QuadTreeAddress.isValid(QuadTreeAddressTest.INVALID_QUADTREE_1));
  }

  @Test
  public void test_isValid_invalid2() {
    Assert.assertFalse(QuadTreeAddress.isValid(QuadTreeAddressTest.INVALID_QUADTREE_2));
  }

  @Test
  public void test_isValid_valid() {
    Assert.assertTrue(QuadTreeAddress.isValid(QuadTreeAddressTest.VALID_QUADTREE));
  }

  @Test
  public void test_containsPoint_true1() {
    Assert.assertTrue(QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15)
        .contains(QuadTreeAddressTest.VALID_POINT1));
  }

  @Test
  public void test_containsPoint_true2() {
    Assert.assertTrue(
        QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, QuadTreeAddress.MAX_DEPTH)
            .contains(QuadTreeAddressTest.VALID_POINT1));
  }

  @Test
  public void test_containsPoint_false_pointNotWithin() {
    Assert.assertFalse(QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15)
        .contains(QuadTreeAddressTest.VALID_POINT2));
  }

  @Test
  public void test_containsQuadTree_true_differentDepth() {
    Assert.assertTrue(QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15).contains(
        QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, QuadTreeAddress.MAX_DEPTH)));
  }

  @Test
  public void test_containsQuadTree_true_sameDepth() {
    Assert.assertTrue(QuadTreeAddress.contains(
        QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15).getQuadTree(),
        QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15).getQuadTree()));
  }

  @Test
  public void test_containsQuadTree_false_lowerDepth() {
    Assert.assertFalse(QuadTreeAddress.contains(
        QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15).getQuadTree(),
        QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 14).getQuadTree()));
  }

  @Test
  public void test_containsQuadTree_false_otherArea() {
    Assert.assertFalse(QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15)
        .contains(QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT2, 15)));
  }

  @Test
  public void test_getDepth1() {
    Assert.assertEquals(5, QuadTreeAddress.getDepth(QuadTreeAddressTest.VALID_QUADTREE));
  }

  @Test
  public void test_getDepth2() {
    Assert.assertEquals(15, QuadTreeAddress
        .getDepth(QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15).getQuadTree()));
  }

  @Test
  public void test_shortenTo() {
    Assert.assertEquals(5,
        QuadTreeAddress.getDepth(QuadTreeAddress
            .shortenToDepth(
                QuadTreeAddress.createFromPoint(QuadTreeAddressTest.VALID_POINT1, 15).getQuadTree(), 5)
            .getQuadTree()));
  }

  @Test
  public void test_northernNeighbor() {
    Assert.assertEquals("+AACD",
        QuadTreeAddress.getNorthernNeighbor(QuadTreeAddressTest.ACAB_QUAD_TREE.getQuadTree()));
  }

  @Test
  public void test_northernNeighbor2() {
    Assert.assertEquals("+CCCCC",
        QuadTreeAddress.getNorthernNeighbor(QuadTreeAddressTest.AAAAA_QUAD_TREE.getQuadTree()));
  }

  @Test
  public void test_southernNeighbor() {
    Assert.assertEquals("+ACAD",
        QuadTreeAddress.getSouthernNeighbor(QuadTreeAddressTest.ACAB_QUAD_TREE.getQuadTree()));
  }

  @Test
  public void test_easternNeighbor() {
    Assert.assertEquals("+ACAA",
        QuadTreeAddress.getEasternNeighbor(QuadTreeAddressTest.ACAB_QUAD_TREE.getQuadTree()));
  }

  @Test
  public void test_easternNeighbor2() {
    Assert.assertEquals("+BBBBB",
        QuadTreeAddress.getEasternNeighbor(QuadTreeAddressTest.AAAAA_QUAD_TREE.getQuadTree()));
  }

  @Test
  public void test_westernNeighbor() {
    Assert.assertEquals("+ACBA",
        QuadTreeAddress.getWesternNeighbor(QuadTreeAddressTest.ACAB_QUAD_TREE.getQuadTree()));
  }

  @Test
  public void test_allNeighbors() {
    final Set<String> neighbors = (Set<String>) QuadTreeAddressTest.ACAB_QUAD_TREE.getNeighbors();
    Assert.assertEquals(true,
        neighbors.contains("+AACD") && neighbors.contains("+ACAD") && neighbors.contains("+ACAA")
            && neighbors.contains("+ACBA") && neighbors.contains("+AACC")
            && neighbors.contains("+AADC") && neighbors.contains("+ACAC")
            && neighbors.contains("+ACBC"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_allNeighbors_Depth1() {
    QuadTreeAddress.getNeighbors("+A");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_allNeighbors_invalidQuadTree() {
    QuadTreeAddress.getNeighbors("+EEEF");
  }

  @Test
  public void test_maxDepth() {
    Assert.assertEquals("+DDDDDDDDDDDDDDDDDDDDDDDDDD",
        QuadTreeAddress.getNorthernNeighbor("+BBBBBBBBBBBBBBBBBBBBBBBBBB"));
  }

  @Test(expected = NullPointerException.class)
  public void test_toNumberRepresentation_null() {
    final QuadTreeAddress quadTreeAddress = null;
    QuadTreeAddress.toNumberRepresentation(quadTreeAddress);
  }

  @Test(expected = NullPointerException.class)
  public void test_toNumberRepresentation_null2() {
    final String quadTree = null;
    QuadTreeAddress.toNumberRepresentation(quadTree);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_toNumberRepresentation_invalidQuadTree() {
    QuadTreeAddress.toNumberRepresentation("ABA");
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_toNumberRepresentation_tooLongQuadTree() {
    QuadTreeAddress.toNumberRepresentation("+AAAAAAAAAAAAAAAAAAAAAAAAAAA");
  }

  @Test
  public void test_toNumberRepresentation_valid() {
    Assert.assertEquals(6629359005329988536L,
        QuadTreeAddress.toNumberRepresentation("+ACDCDCBADCDABCDAABCDCBD"));
  }

  @Test
  public void test_toLetterRepresentation_valid() {
    Assert.assertEquals("+ACDCDCBADCDABCDAABCDCBD",
        QuadTreeAddress.toLetterRepresentation(6629359005329988536L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_toLetterRepresentation_negative() {
    QuadTreeAddress.toLetterRepresentation(-12);
  }

  @SuppressWarnings("null")
  @Test(expected = NullPointerException.class)
  public void test_toLetterRepresentation_null() {
    final Long l = null;
    QuadTreeAddress.toLetterRepresentation(l);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_toLetterRepresentation_illegalDepth() {
    final Long l = 0b0111110000001111111111111111111111111111111111111111111111111111L;
    QuadTreeAddress.toLetterRepresentation(l);
  }


}

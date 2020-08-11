package de.okkyou.quadtreeaddress;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * <p>
 * QuadTree is a form of a geographical area description. This QuadTree is used to slice the WGS84
 * defined world map in 4 tiles per depth. The deeper the quad tree is defined, a more detailed area
 * is addressed. The chars {@value QuadTreeAddress#I}, {@value QuadTreeAddress#II},
 * {@value QuadTreeAddress#III} and {@value QuadTreeAddress#IV} are used to identify the quarter of
 * each depth where {@value QuadTreeAddress#I} is the tile in the upper left,
 * {@value QuadTreeAddress#II} in the upper right, {@value QuadTreeAddress#III} in the lower left
 * and {@value QuadTreeAddress#IV} in the lower right. The start of the QuadTree is defined by a
 * {@value QuadTreeAddress#START_CHAR}.
 * </p>
 *
 *
 * @author Andreas Otte
 *
 */
public final class QuadTreeAddress {

  private static final int MAX_LAT = 900000000;
  private static final int MAX_LON = 1800000000;
  private static final int MIN_LAT = -1 * QuadTreeAddress.MAX_LAT;
  private static final int MIN_LON = -1 * QuadTreeAddress.MAX_LON;

  private static final byte I_SECTOR = 0b000;
  private static final byte II_SECTOR = 0b001;
  private static final byte III_SECTOR = 0b010;
  private static final byte IV_SECTOR = 0b011;

  private static final char I = 'A';
  private static final char II = 'B';
  private static final char III = 'C';
  private static final char IV = 'D';

  private static final long MINIMIZED_DEPTH_OFFSET = 288230376151711744L;
  private static final long MINIMIZED_SECTOR_MASK = 0b011L;
  private static final long MINIMIZED_PRE_MASK = 8939645260330434559L;

  private static final String VALID_PATTERN = "^\\+[ABCD]*$";

  public static final byte MAX_DEPTH = 26;
  public static final char START_CHAR = '+';



  private final String quadTree;
  private final Wgs84Point upperLeftPoint;
  private final Wgs84Point upperRightPoint;
  private final Wgs84Point lowerLeftPoint;
  private final Wgs84Point lowerRightPoint;
  private final Wgs84Point centerPoint;

  private QuadTreeAddress(final String quadTree, final Wgs84Point point1, final Wgs84Point point2,
      final Wgs84Point point3, final Wgs84Point point4, final Wgs84Point center) {
    this.quadTree = Objects.requireNonNull(quadTree);
    this.upperLeftPoint = Objects.requireNonNull(point1);
    this.upperRightPoint = Objects.requireNonNull(point2);
    this.lowerLeftPoint = Objects.requireNonNull(point3);
    this.lowerRightPoint = Objects.requireNonNull(point4);
    this.centerPoint = Objects.requireNonNull(center);
  }

  /**
   * Returns the QuadTree area of a given {@link Wgs84Point} with given depth.
   *
   * @param point is the given position. Must not be null or invalid
   * @param depth contains the depth of the QuadTree. The larger the depth, the smaller the area.
   *        Must be within [1, {@value #MAX_DEPTH} ]
   * @return the quad tree string that starts with its start char
   *         {@value QuadTreeAddress#START_CHAR}
   * @throws IllegalArgumentException if given point is invalid or depth is below 1
   * @throws NullPointerException if given point is null
   */
  public static QuadTreeAddress createFromPoint(final Wgs84Point point, final int depth) {

    if ((depth < 1) || (depth > QuadTreeAddress.MAX_DEPTH)) {
      throw new IllegalArgumentException("Invalid depth: " + depth);
    }

    String quadTreeString = String.valueOf(QuadTreeAddress.START_CHAR);

    int latUpperThreshold = QuadTreeAddress.MAX_LAT;
    int latLowerThreshold = QuadTreeAddress.MIN_LAT;
    int lonUpperThreshold = QuadTreeAddress.MAX_LON;
    int lonLowerThreshold = QuadTreeAddress.MIN_LON;

    int latThreshold = 0;
    int lonThreshold = 0;

    byte latCurrentSector = 0;
    byte lonCurrentSector = 0;

    for (int i = 0; i < depth; i++) {

      latThreshold = (latUpperThreshold + latLowerThreshold) / 2;
      lonThreshold = (lonUpperThreshold + lonLowerThreshold) / 2;

      if (point.getLatitudeInTenthMicroDegree() >= latThreshold) {
        latLowerThreshold = latThreshold;
        latCurrentSector = 0b000;
      } else {
        latUpperThreshold = latThreshold;
        latCurrentSector = 0b010;
      }

      if (point.getLongitudeInTenthMicroDegree() >= lonThreshold) {
        lonLowerThreshold = lonThreshold;
        lonCurrentSector = 0b001;
      } else {
        lonUpperThreshold = lonThreshold;
        lonCurrentSector = 0b000;
      }

      final int sector = latCurrentSector | lonCurrentSector;
      if (sector == QuadTreeAddress.I_SECTOR) {
        quadTreeString += QuadTreeAddress.I;
      } else if (sector == QuadTreeAddress.II_SECTOR) {
        quadTreeString += QuadTreeAddress.II;
      } else if (sector == QuadTreeAddress.III_SECTOR) {
        quadTreeString += QuadTreeAddress.III;
      } else if (sector == QuadTreeAddress.IV_SECTOR) {
        quadTreeString += QuadTreeAddress.IV;
      }

    }

    return new QuadTreeAddress(quadTreeString, new Wgs84Point(latUpperThreshold, lonLowerThreshold),
        new Wgs84Point(latUpperThreshold, lonUpperThreshold),
        new Wgs84Point(latLowerThreshold, lonLowerThreshold),
        new Wgs84Point(latLowerThreshold, lonUpperThreshold),
        new Wgs84Point((latUpperThreshold + latLowerThreshold) / 2,
            (lonUpperThreshold + lonLowerThreshold) / 2));
  }


  /**
   * Creates the QuadTree from given QuadTree String.
   *
   * @param quadTree begins with {@value QuadTreeAddress#START_CHAR} and contains only
   *        {@value QuadTreeAddress#I}, {@value QuadTreeAddress#II}, {@value QuadTreeAddress#III} or
   *        {@value QuadTreeAddress#IV}
   * @return the {@link QuadTreeAddress}
   * @throws NullPointerException if given quadTree was null
   * @throws IllegalArgumentException if quadTree string contains other chars than
   *         {@value QuadTreeAddress#START_CHAR}, {@value QuadTreeAddress#I},
   *         {@value QuadTreeAddress#II}, {@value QuadTreeAddress#III} and
   *         {@value QuadTreeAddress#IV}
   */
  public static QuadTreeAddress createFromQuadTreeString(final String quadTree) {

    if (!QuadTreeAddress.isValid(quadTree)) {
      throw new IllegalArgumentException("QuadTree is invalid");
    }

    int latUpperThreshold = QuadTreeAddress.MAX_LAT;
    int latLowerThreshold = QuadTreeAddress.MIN_LAT;
    int lonUpperThreshold = QuadTreeAddress.MAX_LON;
    int lonLowerThreshold = QuadTreeAddress.MIN_LON;

    int centerLat = 0;
    int centerLon = 0;

    for (int i = 1; i < quadTree.length(); i++) {

      centerLat = (latUpperThreshold + latLowerThreshold) / 2;
      centerLon = (lonUpperThreshold + lonLowerThreshold) / 2;

      final char quadTreeSector = quadTree.charAt(i);
      if (quadTreeSector == QuadTreeAddress.I) {
        latLowerThreshold = centerLat;
        lonUpperThreshold = centerLon;
      } else if (quadTreeSector == QuadTreeAddress.II) {
        latLowerThreshold = centerLat;
        lonLowerThreshold = centerLon;
      } else if (quadTreeSector == QuadTreeAddress.III) {
        latUpperThreshold = centerLat;
        lonUpperThreshold = centerLon;
      } else if (quadTreeSector == QuadTreeAddress.IV) {
        latUpperThreshold = centerLat;
        lonLowerThreshold = centerLon;
      } else {
        throw new IllegalArgumentException("Invalid char in quadTree string: " + quadTreeSector);
      }
    }

    return new QuadTreeAddress(quadTree, new Wgs84Point(latUpperThreshold, lonLowerThreshold),
        new Wgs84Point(latUpperThreshold, lonUpperThreshold),
        new Wgs84Point(latLowerThreshold, lonLowerThreshold),
        new Wgs84Point(latLowerThreshold, lonUpperThreshold),
        new Wgs84Point((latUpperThreshold + latLowerThreshold) / 2,
            (lonUpperThreshold + lonLowerThreshold) / 2));

  }

  /**
   * Checks if given QuadTree is valid.
   *
   * @param quadTree is the QuadTree to check. Must not be null
   * @return false if the quadTree has not the expected form
   * @throws NullPointerException if quadTree is null
   */
  public static boolean isValid(final String quadTree) {
    Objects.requireNonNull(quadTree);
    // Check if QuadTree is deeper than allowed
    if ((quadTree.length() - 1) > QuadTreeAddress.MAX_DEPTH) {
      return false;
    }
    return quadTree.matches(QuadTreeAddress.VALID_PATTERN);
  }

  /**
   * Checks if the given point is in the given quadTree.
   *
   * @param point the point to check. Must not be null.
   * @return false if the point is not within the QuadTree
   * @throws NullPointerException point is null
   * @throws IllegalArgumentException if point is invalid
   */
  public boolean contains(final Wgs84Point point) {
    return QuadTreeAddress.contains(this.quadTree, point);
  }

  /**
   * Checks if the given point is in the given quadTree.
   *
   * @param quadTree the quadTree. Must not be null
   * @param point the point to check. Must not be null.
   * @return false if the point is not within the QuadTree
   * @throws NullPointerException if quadTree or point is null
   * @throws IllegalArgumentException if quadTree or point is invalid
   */
  public static boolean contains(final String quadTree, final Wgs84Point point) {
    Objects.requireNonNull(quadTree);
    Objects.requireNonNull(point);
    if (!QuadTreeAddress.isValid(quadTree)) {
      throw new IllegalArgumentException("Invalid point or quadTree");
    }
    final String quadTreeOfPoint =
        QuadTreeAddress.createFromPoint(point, QuadTreeAddress.MAX_DEPTH).quadTree;
    if (quadTreeOfPoint.contains(quadTree)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the given quadTree contains the otherQuadTree.
   *
   * @param quadTree is tested to contain otherQuadTree. Must not be null
   * @param otherQuadTree is tested to be within quadTree. Must not be null
   * @return true if otherQuadTree is within quadTree
   * @throws NullPointerException if quadTree or otherQuadTree is null
   * @throws IllegalArgumentException if quadTree or otherQuadTree is invalid
   */
  public static boolean contains(final String quadTree, final String otherQuadTree) {
    if (!QuadTreeAddress.isValid(quadTree) || !QuadTreeAddress.isValid(otherQuadTree)) {
      throw new IllegalArgumentException("At least one QuadTree is not valid");
    }
    if (otherQuadTree.contains(quadTree)) {
      return true;
    }
    return false;
  }

  /**
   * Checks if this QuadTree contains the given otherQuadTree.
   *
   * @param otherQuadTree is tested to be within quadTree. Must not be null
   * @return true if otherQuadTree is within quadTree
   * @throws NullPointerException otherQuadTree is null
   * @throws IllegalArgumentException if otherQuadTree is invalid
   */
  public boolean contains(final String otherQuadTree) {
    return QuadTreeAddress.contains(this.quadTree, otherQuadTree);
  }

  /**
   * Checks if this QuadTree contains the given otherQuadTree.
   *
   * @param otherQuadTree is tested to be within quadTree. Must not be null
   * @return true if otherQuadTree is within quadTree
   * @throws NullPointerException otherQuadTree is null
   * @throws IllegalArgumentException if otherQuadTree is invalid
   */
  public boolean contains(final QuadTreeAddress otherQuadTree) {
    return this.contains(otherQuadTree.quadTree);
  }

  /**
   * Shortens a given QuadTree to given depth. If the quadTree is already shorter, the quadTree will
   * be returned unchanged.
   *
   * @param quadTree the quadTree to be shortened
   * @param shortenToDepth the depth of the shortened QuadTree
   * @return the shortened QuadTree or the already shorter original QuadTree
   */
  public static QuadTreeAddress shortenToDepth(final String quadTree, final int shortenToDepth) {
    if (!QuadTreeAddress.isValid(quadTree)) {
      throw new IllegalArgumentException("QuadTree is invalid");
    }
    if (shortenToDepth > QuadTreeAddress.getDepth(quadTree)) {
      return QuadTreeAddress.createFromQuadTreeString(quadTree);
    }
    return QuadTreeAddress.createFromQuadTreeString(quadTree.substring(0, shortenToDepth + 1));
  }

  public QuadTreeAddress shortenTo(final int shortenToDepth) {
    return QuadTreeAddress.shortenToDepth(this.quadTree, shortenToDepth);
  }

  /**
   * Returns the depth of the QuadTree.
   *
   * @return the depth of the quadTree.
   */
  public int getDepth() {
    return QuadTreeAddress.getDepth(this.quadTree);
  }

  /**
   * Returns the depth of the given QuadTree.
   *
   * @param quadTree the quadTree. Must not be null or invalid
   * @return the depth of the quadTree.
   */
  public static int getDepth(final String quadTree) {
    if (!QuadTreeAddress.isValid(quadTree)) {
      throw new IllegalArgumentException("QuadTree is invalid");
    }
    return quadTree.length() - 1;
  }


  /**
   * Returns the GeoJson feature that contains the points of the polygon without the center point.
   *
   * @return a string that contains the geojson representation of the Polygon.
   * @throws NullPointerException if one of the points equals null.
   */
  public String toGeoJson() {

    final StringBuilder sb = new StringBuilder();
    sb.append("{ \"type\": \"Feature\", \"properties\": {\"quadTree\": \"" + this.quadTree
        + "\"}, \"geometry\": " + "{ \"type\": \"Polygon\", \"coordinates\": [ [ ");
    sb.append("[" + this.upperLeftPoint.getLongitudeInDegree() + ", "
        + this.upperLeftPoint.getLatitudeInDegree() + "], ");
    sb.append("[" + this.upperRightPoint.getLongitudeInDegree() + ", "
        + this.upperRightPoint.getLatitudeInDegree() + "], ");
    sb.append("[" + this.lowerRightPoint.getLongitudeInDegree() + ", "
        + this.lowerRightPoint.getLatitudeInDegree() + "], ");
    sb.append("[" + this.lowerLeftPoint.getLongitudeInDegree() + ", "
        + this.lowerLeftPoint.getLatitudeInDegree() + "], ");
    sb.append("[" + this.upperLeftPoint.getLongitudeInDegree() + ", "
        + this.upperLeftPoint.getLatitudeInDegree() + "]");
    sb.append("] ] } }");

    return sb.toString();
  }


  /**
   * Returns a collection of all 8 neighbors of the QuadTree.
   *
   * @throws IllegalArgumentException if depth of quadTree is below 2
   */
  public Collection<String> getNeighbors() {
    return QuadTreeAddress.getNeighbors(this);
  }

  /**
   * Returns a collection of all 8 neighbors of the QuadTree.
   *
   * @throws IllegalArgumentException if depth of quadTree is below 2
   */
  public static Collection<String> getNeighbors(final QuadTreeAddress quadTree)
      throws IllegalArgumentException {
    return QuadTreeAddress.getNeighbors(quadTree.getQuadTree());
  }

  /**
   * Returns a collection of all 8 neighbors of the QuadTree.
   *
   * @throws IllegalArgumentException if depth of quadTree is below 2
   */
  public static Collection<String> getNeighbors(final String quadTree)
      throws IllegalArgumentException {
    if (!QuadTreeAddress.isValid(quadTree) || (QuadTreeAddress.getDepth(quadTree) < 2)) {
      throw new IllegalArgumentException(
          "QuadTree must have a depth of at least 2 to have 8 neighbors");
    }
    final var returnSet = new HashSet<String>();

    // North
    final String northernNeighbor = QuadTreeAddress.getNorthernNeighbor(quadTree);
    returnSet.add(northernNeighbor);
    // NorthEast
    returnSet.add(QuadTreeAddress.getEasternNeighbor(northernNeighbor));
    // NorthWest
    returnSet.add(QuadTreeAddress.getWesternNeighbor(northernNeighbor));
    // South
    final String southernNeighbor = QuadTreeAddress.getSouthernNeighbor(quadTree);
    returnSet.add(southernNeighbor);
    // SouthEast
    returnSet.add(QuadTreeAddress.getEasternNeighbor(southernNeighbor));
    // SouthWest
    returnSet.add(QuadTreeAddress.getWesternNeighbor(southernNeighbor));
    // East
    returnSet.add(QuadTreeAddress.getEasternNeighbor(quadTree));
    // West
    returnSet.add(QuadTreeAddress.getWesternNeighbor(quadTree));

    return returnSet;
  }

  /**
   * Returns the northern neighbor of the given QuadTree String.
   *
   * @throws IllegalArgumentException if QuadTree String is not valid
   */
  public static String getNorthernNeighbor(final String quadTree) throws IllegalArgumentException {
    return QuadTreeAddress.shiftVertically(quadTree, false);
  }

  /**
   * Returns the southern neighbor of the given QuadTree String.
   *
   * @throws IllegalArgumentException if QuadTree String is not valid
   */
  public static String getSouthernNeighbor(final String quadTree) {
    return QuadTreeAddress.shiftVertically(quadTree, true);
  }

  /**
   * Returns the eastern neighbor of the given QuadTree String.
   *
   * @throws IllegalArgumentException if QuadTree String is not valid
   */
  public static String getEasternNeighbor(final String quadTree) {
    return QuadTreeAddress.shiftHorizontal(quadTree, true);
  }

  /**
   * Returns the western neighbor of the given QuadTree String.
   *
   * @throws IllegalArgumentException if QuadTree String is not valid
   */
  public static String getWesternNeighbor(final String quadTree) {
    return QuadTreeAddress.shiftHorizontal(quadTree, false);
  }

  private static String shiftHorizontal(final String quadTree, final boolean left) {
    if (!QuadTreeAddress.isValid(quadTree) || (QuadTreeAddress.getDepth(quadTree) < 1)) {
      throw new IllegalArgumentException("Not a valid QuadTree or QuadTree is not deep enough");
    }
    final char[] northernNeighbor = quadTree.toCharArray();
    boolean shift = false;
    for (int index = quadTree.length() - 1; index > 0; index--) {
      final char coordinate = quadTree.charAt(index);
      if (coordinate == QuadTreeAddress.I) {
        northernNeighbor[index] = QuadTreeAddress.II;
        shift = (true == left);
      } else if (coordinate == QuadTreeAddress.II) {
        northernNeighbor[index] = QuadTreeAddress.I;
        shift = (false == left);
      } else if (coordinate == QuadTreeAddress.III) {
        northernNeighbor[index] = QuadTreeAddress.IV;
        shift = (true == left);
      } else if (coordinate == QuadTreeAddress.IV) {
        northernNeighbor[index] = QuadTreeAddress.III;
        shift = (false == left);
      }

      if (!shift || (index == 1)) {
        break;
      }
    }
    return String.valueOf(northernNeighbor);
  }

  private static String shiftVertically(final String quadTree, final boolean down) {
    if (!QuadTreeAddress.isValid(quadTree) || (QuadTreeAddress.getDepth(quadTree) < 1)) {
      throw new IllegalArgumentException("Not a valid QuadTree or QuadTree is not deep enough");
    }
    final char[] northernNeighbor = quadTree.toCharArray();
    boolean shift = false;
    for (int index = quadTree.length() - 1; index > 0; index--) {
      final char coordinate = quadTree.charAt(index);
      if (coordinate == QuadTreeAddress.I) {
        northernNeighbor[index] = QuadTreeAddress.III;
        shift = (false == down);
      } else if (coordinate == QuadTreeAddress.II) {
        northernNeighbor[index] = QuadTreeAddress.IV;
        shift = (false == down);
      } else if (coordinate == QuadTreeAddress.III) {
        northernNeighbor[index] = QuadTreeAddress.I;
        shift = (true == down);
      } else if (coordinate == QuadTreeAddress.IV) {
        northernNeighbor[index] = QuadTreeAddress.II;
        shift = (true == down);
      }

      if (!shift || (index == 1)) {
        break;
      }
    }
    return String.valueOf(northernNeighbor);
  }

  /**
   * Returns a 64 bit long value that represents the quad tree. It is interpreted as follows:
   *
   * <table border="1">
   * <tr>
   * <td>63</td>
   * <td>62 - 58</td>
   * <td>57 - 52</td>
   * <td>51 - 50</td>
   * <td>49 - 48</td>
   * <td>...</td>
   * <td>3 - 2</td>
   * <td>1 - 0</td>
   * </tr>
   * <tr>
   * <td>unused</td>
   * <td>depth</td>
   * <td>reserved</td>
   * <td>Sector of 26th depth</td>
   * <td>Sector of 25th depth</td>
   * <td>...</td>
   * <td>Sector of 2nd depth</td>
   * <td>Sector of 1st depth</td>
   * </tr>
   * </table>
   * 2 Bit describes a sector. Where '00' means {@value QuadTreeAddress#I}, '01' means
   * {@value QuadTreeAddress#II}, '10' means {@value #III} and '11' means {@value #IV};
   *
   * @return a long representing the quad tree area
   */
  public long toNumberRepresentation() {
    return QuadTreeAddress.toNumberRepresentation(this);
  }

  /**
   * Returns a 64 bit long value that represents the quad tree. It is interpreted as follows:
   *
   * <table border="1">
   * <tr>
   * <td>63</td>
   * <td>62 - 58</td>
   * <td>57 - 52</td>
   * <td>51 - 50</td>
   * <td>49 - 48</td>
   * <td>...</td>
   * <td>3 - 2</td>
   * <td>1 - 0</td>
   * </tr>
   * <tr>
   * <td>unused</td>
   * <td>depth</td>
   * <td>reserved</td>
   * <td>Sector of 26th depth</td>
   * <td>Sector of 25th depth</td>
   * <td>...</td>
   * <td>Sector of 2nd depth</td>
   * <td>Sector of 1st depth</td>
   * </tr>
   * </table>
   * 2 Bit describes a sector. Where '00' means {@value QuadTreeAddress#I}, '01' means
   * {@value QuadTreeAddress#II}, '10' means {@value #III} and '11' means {@value #IV};
   *
   * @param quadTreeString the quad tree to minimize
   * @return a long representing the quad tree area
   * @see #toNumberRepresentation(QuadTreeAddress)
   */
  public static long toNumberRepresentation(final String quadTreeString) {
    final QuadTreeAddress quadTree = QuadTreeAddress.createFromQuadTreeString(quadTreeString);
    return QuadTreeAddress.toNumberRepresentation(quadTree);
  }

  /**
   * Returns a 64 bit long value that represents the quad tree. It is interpreted as follows:
   *
   * <table border="1">
   * <tr>
   * <td>63</td>
   * <td>62 - 58</td>
   * <td>57 - 52</td>
   * <td>51 - 50</td>
   * <td>49 - 48</td>
   * <td>...</td>
   * <td>3 - 2</td>
   * <td>1 - 0</td>
   * </tr>
   * <tr>
   * <td>unused</td>
   * <td>depth</td>
   * <td>reserved</td>
   * <td>Sector of 26th depth</td>
   * <td>Sector of 25th depth</td>
   * <td>...</td>
   * <td>Sector of 2nd depth</td>
   * <td>Sector of 1st depth</td>
   * </tr>
   * </table>
   * 2 Bit describes a sector. Where '00' means {@value QuadTreeAddress#I}, '01' means
   * {@value QuadTreeAddress#II}, '10' means {@value #III} and '11' means {@value #IV};
   *
   * @param quadTree the quad tree to minimize
   * @return a long representing the quad tree area
   */
  public static long toNumberRepresentation(final QuadTreeAddress quadTree) {
    long sectors = 0;
    final long depth = quadTree.getDepth() * QuadTreeAddress.MINIMIZED_DEPTH_OFFSET;
    for (int i = quadTree.getDepth(); i > 0; i--) {
      final char sector = quadTree.getQuadTree().charAt(i);
      if (sector == QuadTreeAddress.I) {
        sectors += QuadTreeAddress.I_SECTOR;
      } else if (sector == QuadTreeAddress.II) {
        sectors += QuadTreeAddress.II_SECTOR;
      } else if (sector == QuadTreeAddress.III) {
        sectors += QuadTreeAddress.III_SECTOR;
      } else if (sector == QuadTreeAddress.IV) {
        sectors += QuadTreeAddress.IV_SECTOR;
      } else {
        throw new IllegalStateException("Unknown sector, check implementation");
      }
      if ((i - 1) != 0) {
        sectors <<= 2;
      }

    }
    return depth + sectors;
  }

  /**
   * The reverse operation of {@link #toNumberRepresentation(String)}. It receives a long and
   * creates the corresponding QuadTree String.
   *
   * @param minimizedQuadTree a minimized QuadTree. Must be
   * @return
   */
  public static String toLetterRepresentation(final long minimizedQuadTree) {
    if (minimizedQuadTree < 0) {
      throw new IllegalArgumentException("Invalid minimized QuadTree");
    }
    final long preMaskedQuadTree = minimizedQuadTree & QuadTreeAddress.MINIMIZED_PRE_MASK;


    final int depth = (int) (preMaskedQuadTree / QuadTreeAddress.MINIMIZED_DEPTH_OFFSET);

    if (depth > QuadTreeAddress.MAX_DEPTH) {
      throw new IllegalArgumentException("Invalid minimized QuadTree");
    }

    final char[] quadTreeChars = new char[depth];
    for (int i = 0; i < depth; i++) {
      final byte sector =
          (byte) ((preMaskedQuadTree >> (2 * i)) & QuadTreeAddress.MINIMIZED_SECTOR_MASK);
      if (sector == QuadTreeAddress.I_SECTOR) {
        quadTreeChars[i] = QuadTreeAddress.I;
      } else if (sector == QuadTreeAddress.II_SECTOR) {
        quadTreeChars[i] = QuadTreeAddress.II;
      } else if (sector == QuadTreeAddress.III_SECTOR) {
        quadTreeChars[i] = QuadTreeAddress.III;
      } else if (sector == QuadTreeAddress.IV_SECTOR) {
        quadTreeChars[i] = QuadTreeAddress.IV;
      } else {
        throw new IllegalStateException("Unknown sector, check implementation");
      }
    }

    return new String(new byte[] {QuadTreeAddress.START_CHAR}) + new String(quadTreeChars);
  }

  public String getQuadTree() {
    return this.quadTree;
  }

  public Wgs84Point getUpperLeftPoint() {
    return this.upperLeftPoint;
  }

  public Wgs84Point getUpperRightPoint() {
    return this.upperRightPoint;
  }

  public Wgs84Point getLowerLeftPoint() {
    return this.lowerLeftPoint;
  }

  public Wgs84Point getLowerRightPoint() {
    return this.lowerRightPoint;
  }

  public Wgs84Point getCenterPoint() {
    return this.centerPoint;
  }

  @Override
  public String toString() {
    return "QuadTree [quadTree=" + this.quadTree + ", upperLeftPoint=" + this.upperLeftPoint
        + ", upperRightPoint=" + this.upperRightPoint + ", lowerLeftPoint=" + this.lowerLeftPoint
        + ", lowerRightPoint=" + this.lowerRightPoint + ", centerPoint=" + this.centerPoint + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.quadTree);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final QuadTreeAddress other = (QuadTreeAddress) obj;
    return Objects.equals(this.quadTree, other.quadTree);
  }



}

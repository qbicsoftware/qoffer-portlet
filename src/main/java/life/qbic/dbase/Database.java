/*******************************************************************************
 * QBiC Offer Generator provides an infrastructure for creating offers using QBiC portal and
 * infrastructure. Copyright (C) 2017 Aydın Can Polatkan, 2018 Benjamin Sailer
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package life.qbic.dbase;

import life.qbic.model.packageBean;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public enum Database {

  Instance;
  private String password;
  private String user;
  private String host;
  Connection conn = null;

  public void init(String user, String password, String host) {
    // check if com.mysql.jdbc.Driver exists. If not try to add it
    String mysqlDriverName = "com.mysql.jdbc.Driver";
    Enumeration<Driver> tmp = DriverManager.getDrivers();
    boolean existsDriver = false;
    while (tmp.hasMoreElements()) {
      Driver d = tmp.nextElement();
      if (d.toString().equals(mysqlDriverName)) {
        existsDriver = true;
        break;
      }
    }
    if (!existsDriver) {
      // Register JDBC driver
      // According http://docs.oracle.com/javase/6/docs/api/java/sql/DriverManager.html
      // this should not be needed anymore. But without it I get the following error:
      // java.sql.SQLException: No suitable driver found for
      // jdbc:mysql://localhost:3306/facs_facility
      // Does not work for servlets, just for portlets :(
      try {
        Class.forName(mysqlDriverName);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    this.setPassword(password);
    this.setUser(user);
    this.setHost(host);
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host the host to set
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * 
   * Undoes all changes made in the current transaction. Does not undo, if conn IS in auto commit
   * mode
   * 
   * @param conn:
   * @param closeConnection:
   */
  @SuppressWarnings("unused")
  private void rollback(Connection conn, boolean closeConnection) {

    try {
      if (!conn.getAutoCommit()) {
        conn.rollback();
      }
      if (closeConnection) {
        logout(conn);
      }
    } catch (SQLException e) {
      // TODO log everything
      e.printStackTrace();
    }
  }

  /**
   * logs into database with the parameters given in init()
   * 
   * @return Connection, otherwise null if connecting to the database fails
   * @exception SQLException if a database access error occurs or the url is
   * {@code null}
   */
  private Connection login() throws SQLException {
    return DriverManager.getConnection(host, user, password);
  }

  /**
   * tries to close the given connection and release it
   * 
   * From java documentation: It is strongly recommended that an application explicitly commits or
   * rolls back an active transaction prior to calling the close method. If the close method is
   * called and there is an active transaction, the results are implementation-defined.
   *
   * @param conn
   */
  private void logout(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      // TODO log logout failure
      e.printStackTrace();
    }
  }

  public List<packageBean> getPackages() {
    String sql = "SELECT * FROM packages";
    List<packageBean> pbean = new ArrayList<>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        packageBean p = new packageBean();
        p.setpackage_id(rs.getInt("package_id"));
        p.setpackage_name(rs.getString("package_name"));
        p.setpackage_facility(rs.getString("package_facility"));
        p.setpackage_description(rs.getString("package_description"));
        p.setpackage_group(rs.getString("package_group"));
        p.setpackage_price(rs.getInt("package_price"));
        p.setPackage_price_external(rs.getInt("package_price_external"));
        p.setPackage_unit_type(rs.getString("package_unit_type"));
        p.setpackage_date(rs.getTimestamp("package_date"));
        pbean.add(p);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return pbean;
  }

  public ArrayList<String> getPackageGroups() {
    ArrayList<String> list = new ArrayList<>();
    String sql = "SELECT DISTINCT package_group FROM packages";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("package_group"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return list;
  }

  public ArrayList<String> getUsernames() {
    ArrayList<String> list = new ArrayList<>();
    String sql = "SELECT user_name FROM user";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("user_name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return list;
  }

  public ArrayList<String> getProjects() {
    ArrayList<String> list = new ArrayList<>();
    String sql = "SELECT openbis_project_identifier FROM projects";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("openbis_project_identifier").replaceAll("/.*?/", ""));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return list;
  }

  public ArrayList<String> getPackageNames() {

    ArrayList<String> list = new ArrayList<>();
    String sql = "SELECT package_name FROM packages ORDER BY package_name";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("package_name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return list;
  }

  /**
   * returns the package ids and names based on the package_group
   * @param package_group: one of ["Project Management", "Bioinformatics", "Sequencing", ""]
   * @return ArrayList of strings where each string consists of: packageId + ": " + packageName
   */
  public ArrayList<String> getPackageIdsAndNames(String package_group) {

    ArrayList<String> list = new ArrayList<>();
    String sql = "SELECT package_id, package_name FROM packages WHERE package_group = ? ORDER BY package_name";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_group);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        String packageId = rs.getString("package_id");
        String packageName = rs.getString("package_name");
        list.add(packageId + ": " + packageName);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return list;
  }

  public String getPackageNameFromPackageId(String packageId) {
    String sql = "SELECT package_name FROM packages WHERE package_id = ?";

    String packageName = "Error: package not found";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, packageId);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        packageName = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return packageName;
  }

  public ArrayList<String> getPackageIdsAndNames() {

    ArrayList<String> list = new ArrayList<>();
    String sql = "SELECT package_id, package_name FROM packages ORDER BY package_name";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        String packageId = rs.getString("package_id");
        String packageName = rs.getString("package_name");
        list.add(packageId + ": " + packageName);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return list;
  }


  public int getOrganizationIdForPersonId(int person_id) {

    int organizationId = -1;
    String sql = "SELECT organization_id FROM persons_organizations WHERE person_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, person_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        organizationId = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return organizationId;
  }

  public String[] getAddressForOrganizationId(int organizationId) {

    String[] address = new String[7];
    String sql = "SELECT group_acronym, institute, umbrella_organization, street, zip_code, city, country " +
        "FROM organizations WHERE id = ?";

    String group_acronym = "";
    String institute = "";
    String umbrella_organization = "";
    String street = "";
    String zip_code = "";
    String city = "";
    String country = "";

    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, organizationId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        group_acronym = rs.getString(1);
        institute = rs.getString(2);
        umbrella_organization = rs.getString(3);
        street = rs.getString(4);
        zip_code = rs.getString(5);
        city = rs.getString(6);
        country = rs.getString(7);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    address[0] = group_acronym;
    address[1] = institute;
    address[2] = umbrella_organization;
    address[3] = street;
    address[4] = zip_code;
    address[5] = city;
    address[6] = country;

    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return address;
  }

  public int getPersonIdForPersonName(String title, String firstName, String familyName) {

    int personId = -1;
    String sql = "SELECT id FROM persons WHERE title = ? AND first_name = ? AND family_name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, title);
      statement.setString(2, firstName);
      statement.setString(3, familyName);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        personId = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }

    return personId;
  }

  public String[] getAddressForPerson(String personFullName) {

    // TODO: deal with multiple first names
    String[] clientNameArray = personFullName.split(" ");
    String title = clientNameArray[0];
    String firstName = clientNameArray[1];
    String familyName = clientNameArray[2];
    int personId = getPersonIdForPersonName(title, firstName, familyName);

    // the person could not be found, so we return the notification message
    if (personId == -1)
      return new String[]{"There is no entry in the persons table for the person " + personFullName + ". The address " +
          "fields in the generated .docx file will thus be placeholders. Please consider creating the user before " +
          "creating the offer."};

    int organizationId = getOrganizationIdForPersonId(personId);

    // the organization could not be found, so we return the notification message
    if (organizationId == -1)
      return new String[]{"There is no entry in the persons_organizations table for the person with id " +
          Integer.toString(personId) + ". The address fields in the generated .docx file will thus be placeholders. " +
          "Please consider linking the user to his organization before creating the offer."};

    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }

    return getAddressForOrganizationId(organizationId);
  }

  public String getUserEmail(String username) {
    String userEmail = "oops! no email address is available in the database.";
    String sql = "SELECT email FROM persons WHERE username = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, username);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        userEmail = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }

    return userEmail;
  }

  public String getPackageDescriptionFromPackageId(int package_id) {
    String package_description = "N/A";
    String sql = "SELECT package_description FROM packages WHERE package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_description = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return package_description;
  }

  public int getPackageIDFromPackageName(String package_name) {
    int package_id = 0;
    String sql = "SELECT package_id FROM packages WHERE package_name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_name);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_id = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return package_id;
  }

  public ArrayList<String> getOfferIdsForPackage(int package_id) {
    String sqlCheck = "SELECT offer_id FROM offers_packages WHERE package_id = ?";
    ArrayList<String> offerIds = new ArrayList<>();

    try (Connection connCheck = login();
         PreparedStatement statementCheck =
             connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        offerIds.add(resultCheck.getString(1));
      }
      // System.out.println("resultCheck: " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }

    return offerIds;
  }

  public void deleteOffer(int offerId) {

    String sql = "DELETE FROM offers_packages WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, offerId);
      int rs = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }

    sql = "DELETE FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, offerId);
      int rs = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
  }

  public void deletePackage(int packageId) {

    String sql = "DELETE FROM packages WHERE package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, packageId);
      int rs = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
  }

  public void removePackageFromOffer(int packageId, int selectedOfferId) {
    String sql = "DELETE FROM offers_packages WHERE package_id = ? AND offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, packageId);
      statement.setInt(2, selectedOfferId);
      int rs = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
  }

  public void printDatabaseTable(String tableName) {

    String sql = "SELECT * FROM " + tableName;
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData resultSetMetaData = rs.getMetaData();
      int columnCount = resultSetMetaData.getColumnCount();

      System.out.println("#############################################");
      System.out.println("Displaying " + tableName);

      // The column count starts from 1
      for (int i = 1; i <= columnCount; i++) {
        try {
          String name = resultSetMetaData.getColumnName(i);
          System.out.print(name + "\t");
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      System.out.println();

      while (rs.next()) {
        //Print one row
        for (int i = 1; i <= columnCount; i++) {
          try {
            System.out.print(rs.getString(i) + "\t"); //Print one element of a row
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
        System.out.println();//Move to the next line to print the next row.
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
  }

  public boolean internalOfferCheck(String offer_id) {
    boolean internal = true;
    String sql = "SELECT internal FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        internal = rs.getBoolean(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return internal;
  }

  public void updateTotalOfferPrice(String offer_id, float offer_total) {
    String sql = "UPDATE offers SET offer_total = ? WHERE offer_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      String updatedPriceFormatted = String.format("%.02f", offer_total);
      updatedPriceFormatted = updatedPriceFormatted.replaceAll(",", ".");
      statement.setString(1, updatedPriceFormatted);
      statement.setString(2, offer_id);
      int result = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
  }

  public boolean updateDiscount(String discount, String offer_id, float percentage) {
    boolean success = false;
    float updatedPrice = 0;

    String sqlS = "SELECT offer_price FROM offers WHERE offer_id = ? ";
    try (Connection conn = login(); PreparedStatement statementS = conn.prepareStatement(sqlS)) {
      statementS.setString(1, offer_id);
      ResultSet rs = statementS.executeQuery();
      if (rs.next())
        updatedPrice = rs.getFloat(1);
      // TODO:
      updatedPrice = updatedPrice * ((100 - percentage) / 100);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }

    String sql = "UPDATE offers SET discount = ?, offer_total = ? WHERE offer_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      String updatedPriceFormatted = String.format("%.02f", updatedPrice);
      updatedPriceFormatted = updatedPriceFormatted.replaceAll(",", ".");
      statement.setString(1, discount);
      statement.setString(2, updatedPriceFormatted);
      statement.setString(3, offer_id);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return success;
  }

  public void updatePackageQuantityAndRecalcalculatePrice(String package_count, String offer_id, String package_id,
                                                             String packagePriceType, float packageDiscount) {



    float updatedPackageAddOnPrice = 0;
    float offerPrice = 0;
    float offerDiscount = 0;
    String sqlS;

    switch (packagePriceType) {
      case "internal":
        sqlS = "SELECT package_price_internal FROM packages WHERE package_id = ? ";
        break;
      case "external_academic":
        sqlS = "SELECT package_price_external_academic FROM packages WHERE package_id = ? ";
        break;
      case "external_commercial":
        sqlS = "SELECT package_price_external_commercial FROM packages WHERE package_id = ? ";
        break;
      default:
        sqlS = "SELECT package_price_internal FROM packages WHERE package_id = ? ";
        break;
    }

    try (Connection conn = login(); PreparedStatement statementS = conn.prepareStatement(sqlS)) {
      statementS.setString(1, package_id);
      ResultSet rs = statementS.executeQuery();
      if (rs.next())
        updatedPackageAddOnPrice = rs.getFloat(1);
      updatedPackageAddOnPrice = updatedPackageAddOnPrice * Integer.parseInt(package_count) * packageDiscount;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // format the package discount
    DecimalFormat df = new DecimalFormat("#");
    String packageDiscountFormatted = df.format((1 - packageDiscount) * 100) + "%";

    // update the package_count, the package_addon_price and the package_disocunt in the offers_packages table
    String sql =
        "UPDATE offers_packages SET package_count = ?, package_addon_price = ?, package_discount = ? " +
            "WHERE offer_id = ? AND package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_count);
      statement.setFloat(2, updatedPackageAddOnPrice);
      statement.setString(3, packageDiscountFormatted);
      statement.setString(4, offer_id);
      statement.setString(5, package_id);
      int result = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // get the sum of all total package prices as offer price
    String sqlL = "SELECT SUM(package_addon_price) FROM offers_packages WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statementL = conn.prepareStatement(sqlL)) {
      statementL.setString(1, offer_id);
      ResultSet rs = statementL.executeQuery();
      if (rs.next())
        offerPrice = rs.getFloat(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // update the offer price in the offers database
    String sqlF = "UPDATE offers SET offer_price = ? WHERE offer_id = ?";
    try (Connection conn = login(); PreparedStatement statementF = conn.prepareStatement(sqlF)) {
      String offerPriceFormatted = String.format("%.02f", offerPrice);
      offerPriceFormatted = offerPriceFormatted.replaceAll(",", ".");
      statementF.setString(1, offerPriceFormatted);
      statementF.setString(2, offer_id);
      int result = statementF.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // get the offer discount
    String sqlD = "SELECT discount FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statementD = conn.prepareStatement(sqlD)) {
      statementD.setString(1, offer_id);
      ResultSet rs = statementD.executeQuery();
      if (rs.next())
        offerDiscount = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // apply the discount + recalculate the total offer price
    float offerTotalPrice = offerPrice * ((100 - offerDiscount) / 100);

    // update the total offer price
    String sqlT = "UPDATE offers SET offer_total = ? WHERE offer_id = ?";
    try (Connection conn = login(); PreparedStatement statementT = conn.prepareStatement(sqlT)) {
      String offerTotalPriceFormatted = String.format("%.02f", offerTotalPrice);
      offerTotalPriceFormatted = offerTotalPriceFormatted.replaceAll(",", ".");
      statementT.setString(1, offerTotalPriceFormatted);
      statementT.setString(2, offer_id);
      int result = statementT.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // TODO: probably not needed
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
  }

  public boolean updateQuantityDiscountQuery(String package_discount, String offer_id,
      String package_id) {
    boolean success = false;
    String sql =
        "UPDATE offers_packages SET package_discount = ? WHERE offer_id = ? AND package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_discount);
      statement.setString(2, offer_id);
      statement.setString(3, package_id);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return success;
  }

  public void updatePackageGroupForPackage(String selectedPackageGroup, String packageId) {

    String sql = "UPDATE packages SET package_group = ? WHERE package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, selectedPackageGroup);
      statement.setString(2, packageId);
      int result = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
  }


  public boolean updateStatus(String offer_status, String offer_id) {
    boolean success = false;
    String sql = "UPDATE offers SET offer_status = ? WHERE offer_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_status);
      statement.setString(2, offer_id);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return success;
  }

  public String getOfferDiscount(String offer_id) {
    String discount = "0%";
    String sql = "SELECT discount FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        discount = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return discount;
  }

  public int getPackageCount(String offer_id, String package_id) {

    int count = -1;
    String sql = "SELECT package_count FROM offers_packages WHERE offer_id = ? AND package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      statement.setString(2, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        count = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return count;
  }

  public String getPackageDiscount(String offer_id, String package_id) {
    String count = null;
    String sql =
        "SELECT package_discount FROM offers_packages WHERE offer_id = ? AND package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      statement.setString(2, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        count = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return count;
  }

  public String getOfferStatus(String offer_id) {
    String status = "In Progress";
    String sql = "SELECT offer_status FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        status = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return status;
  }

  public float getPriceFromPackageName(String package_name, boolean externalSelected) {
    float package_price = 0;
    String sql;
    if (externalSelected) {
      sql = "SELECT package_price_internal FROM packages WHERE package_name = ?";
    } else {
      sql = "SELECT package_price_external FROM packages WHERE package_name = ?";
    }
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_name);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_price = rs.getFloat(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return package_price;
  }

  public String getPriceFromPackageId(int package_id, String packagePriceType) {
    // we use String for the price, since ResultSet.getFloat returns 0.0 if the value in the database is null
    String package_price = "-1";
    String sql;

    switch (packagePriceType) {
      case "internal":
        sql = "SELECT package_price_internal FROM packages WHERE package_id = ?";
        break;
      case "external_academic":
        sql = "SELECT package_price_external_academic FROM packages WHERE package_id = ?";
        break;
      case "external_commercial":
        sql = "SELECT package_price_external_commercial FROM packages WHERE package_id = ?";
        break;
      default:
        sql = "SELECT package_price_internal FROM packages WHERE package_id = ?";
        break;
    }

    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_price = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return package_price;
  }

  public void addNewPackage(String name) {
    String sql = "INSERT INTO packages (package_name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
  }

  public String getShortTitleFromProjectRef(String openbis_project_identifier) {
    String short_title = "N/A";
    String sql = "SELECT short_title FROM projects WHERE openbis_project_identifier LIKE ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, "%" + openbis_project_identifier + "%");
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        short_title = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return short_title;
  }

  public String getLongDescFromProjectRef(String openbis_project_identifier) {
    String long_description = "N/A";
    String sql = "SELECT long_description FROM projects WHERE openbis_project_identifier LIKE ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, "%" + openbis_project_identifier + "%");
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        long_description = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return long_description;
  }

  public String getClientEmailFromProjectRef(String openbis_project_identifier) {

    String clientEmail = "";

    String sql =
        "SELECT DISTINCT persons.email FROM projects INNER JOIN " +
            "projects_persons ON projects.`id` = projects_persons.`project_id` INNER JOIN persons ON persons.`id` " +
            "= projects_persons.`person_id` WHERE projects_persons.`project_role` = 'PI' AND " +
            "`projects`.`openbis_project_identifier` LIKE ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, "%" + openbis_project_identifier + "%");
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        clientEmail = rs.getString(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return clientEmail;
  }

  public String getPIFromProjectRef(String openbis_project_identifier) {
    String pi_title = "", pi_name = "", pi_surname = "", pi_fullname = "";
    String sql =
        "SELECT DISTINCT persons.title, persons.first_name, persons.family_name FROM projects INNER JOIN " +
            "projects_persons ON projects.`id` = projects_persons.`project_id` INNER JOIN persons ON persons.`id` " +
            "= projects_persons.`person_id` WHERE projects_persons.`project_role` = 'PI' AND " +
            "`projects`.`openbis_project_identifier` LIKE ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, "%" + openbis_project_identifier + "%");
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        pi_title = rs.getString(1);
        pi_name = rs.getString(2);
        pi_surname = rs.getString(3);
      }
      pi_fullname = pi_title + " " + pi_name + " " + pi_surname;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return pi_fullname;
  }


  public int registerNewOffer(String offer_number, String offer_project_reference,
      String offer_facility, String offer_name, String offer_description, float offer_price,
      Date offer_date, String added_by, boolean internal) {

    Timestamp sql_offer_date = new Timestamp(offer_date.getTime());

    int offer_id = 0;

    String sql =
        "INSERT INTO offers (offer_number, offer_project_reference, offer_facility, offer_name, offer_description," +
            " offer_price, offer_total, offer_date, added_by, offer_status, discount, internal) " +
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, offer_number);
      statement.setString(2, offer_project_reference);
      statement.setString(3, offer_facility);
      statement.setString(4, offer_name);
      statement.setString(5, offer_description);
      statement.setFloat(6, offer_price);
      statement.setFloat(7, offer_price);
      statement.setTimestamp(8, sql_offer_date);
      statement.setString(9, added_by);
      statement.setString(10, "In Progress");
      statement.setString(11, "0%");
      statement.setBoolean(12, internal);
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // query the database for the current offer to get the offer id (there is probably a better way..)
    String sql2 = "SELECT offer_id FROM offers WHERE offer_project_reference = ? AND offer_price = ?";
    try (Connection conn = login();
        PreparedStatement statement2 =
            conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
      statement2.setString(1, offer_project_reference);
      statement2.setFloat(2, offer_price);
      ResultSet rs = statement2.executeQuery();
      if (rs.next())
        offer_id = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // update the offer_number by appending the offer_id to it
    offer_number = offer_number.concat("_").concat(String.valueOf(offer_id));
    String sql3 = "UPDATE offers SET offer_number = ? WHERE offer_id = ?";
    try (Connection conn = login();
         PreparedStatement statement3 =
             conn.prepareStatement(sql3, Statement.RETURN_GENERATED_KEYS)) {
      statement3.setString(1, offer_number);
      statement3.setInt(2, offer_id);
      int rs = statement3.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return offer_id;
  }

  public int getFirstOfferIdForPackageId(int package_id) {
    String sqlCheck = "SELECT offer_id FROM offers_packages WHERE package_id = ?";
    int offerID = -1;
    try (Connection connCheck = login();
         PreparedStatement statementCheck =
             connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      if (resultCheck.next()) {
        offerID = resultCheck.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return offerID;
  }

  public boolean isPackageSelectedForAnyOffer(int package_id) {
    String sqlCheck = "SELECT * FROM offers_packages WHERE package_id = ?";
    try (Connection connCheck = login();
         PreparedStatement statementCheck =
             connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      if (resultCheck.next()) {
        return true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }

    return false;
  }

  public boolean checkForPackageInOffer(int offer_id, int package_id) {

    int count = 0;
    String sqlCheck = "SELECT COUNT(*) FROM offers_packages WHERE offer_id = ? AND package_id = ?";

    try (Connection connCheck = login();
         PreparedStatement statementCheck =
             connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, offer_id);
      statementCheck.setInt(2, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return count > 0;
  }

  public boolean insertOrUpdateOffersPackages(int offer_id, int package_id, float package_unit_price) {
    int count = 0;
    boolean success = false;

    // check if we need to update or insert the package
    String sqlCheck = "SELECT COUNT(*) FROM offers_packages WHERE offer_id = ? AND package_id = ?";
    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, offer_id);
      statementCheck.setInt(2, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (count > 0) {
      String sqlUpdate =
          "UPDATE offers_packages SET package_id = ?, package_addon_price = ? WHERE offer_id = ?";
      try (Connection connUpdate = login();
          PreparedStatement statementUpdate = connUpdate.prepareStatement(sqlUpdate)) {
        statementUpdate.setInt(1, package_id);
        statementUpdate.setFloat(2, package_unit_price);
        statementUpdate.setInt(3, offer_id);
        int result = statementUpdate.executeUpdate();
        // System.out.println("Update: " + statementUpdate);
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      String sqlInsert =
          "INSERT INTO offers_packages (offer_id, package_id, package_addon_price, package_count, package_discount) " +
              "VALUES (?,?,?,?,?)";
      try (Connection connInsert = login();
          PreparedStatement statementInsert = connInsert.prepareStatement(sqlInsert)) {
        statementInsert.setInt(1, offer_id);
        statementInsert.setInt(2, package_id);
        statementInsert.setFloat(3, package_unit_price);
        statementInsert.setInt(4, 1);
        statementInsert.setString(5, "0%");
        int result = statementInsert.executeUpdate();
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return success;
  }

  /**
   * returns all package ids for the offer which have no package group associated to itself
   * @param offer_id: id of the offer to check
   * @return ArrayList of strings holding the package ids
   */
  public ArrayList<String> getPackageIdsWithoutPackageGroup(String offer_id) {

    ArrayList<String> packageIdsArray = new ArrayList<>();

    // returns the package_id from the packages of the current offer where package_group is null
    String sql = "SELECT packages.`package_id` " + "FROM packages " +
        "INNER JOIN offers_packages ON packages.`package_id` = offers_packages.`package_id` " +
        "WHERE offers_packages.`offer_id` = " + offer_id + " AND packages.`package_group` IS NULL";

    try (Connection connCheck = login();
         PreparedStatement statementCheck =
             connCheck.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ResultSet resultCheck = statementCheck.executeQuery();
      while (resultCheck.next()) {
        packageIdsArray.add(resultCheck.getString(1));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      conn.close();
    } catch (Exception e) { /* ignored */
    }
    return packageIdsArray;
  }

  public void updatePackagePriceTypeForPackage(String offerId, String packageId, String packagePriceType) {

    String sqlUpdate =
        "UPDATE offers_packages SET package_price_type = ? WHERE offer_id = ? AND package_id = ?";
    try (Connection connUpdate = login();
         PreparedStatement statementUpdate = connUpdate.prepareStatement(sqlUpdate)) {
      statementUpdate.setString(1, packagePriceType);
      statementUpdate.setString(2, offerId);
      statementUpdate.setString(3, packageId);
      int result = statementUpdate.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
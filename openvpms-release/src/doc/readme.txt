OpenVPMS ${pom.version} Release
==========================

1. Installation Notes

1.1 Requirements
- OpenVPMS requires the following to be installed:

  - Java 1.6.x or higher
    See http://www.oracle.com/technetwork/java/javase/downloads/index.html

  - MySQL 5.0.24a or higher
    See http://www.mysql.org/downloads/mysql/5.0.html

  - MySQL Connector/J JDBC driver 
    See http://dev.mysql.com/downloads/connector/j/5.1.html

  - Tomcat 6.0 or higher
    See http://tomcat.apache.org/download-60.cgi

  - OpenOffice 2.2.1 or higher
    See http://download.openoffice.org/

- MySQL:
  - should be on the same host as Tomcat
  - should accept connections on port 3306
  - include the following lines in my.ini
    max_allowed_packet=16M
    innodb_file_per_table

1.2 Directory structure

 The OpenVPMS installation has a single top-level directory named
 openvpms-release-${pom.version}
 This will be referred to as <OPENVPMS_HOME> in the remainder of this document.
 This directory has the following sub-directories:

 - bin     contains a number of tool scripts used to load data into OpenVPMS
 - conf    contains configuration files for the tools in bin/
 - db      contains MySQL SQL scripts to create the initial database
 - import  contains data to import into OpenVPMS
 - lib     contains jars used by the tools in bin/
 - reports contains document templates for reporting
 - update  contains data and scripts to migrate from earlier versions of
           OpenVPMS
 - webapps contains the OpenVPMS web applications

1.3 JDBC driver installation
  The MySQL Connector/J JDBC driver needs to be downloaded from:
    http://dev.mysql.com/downloads/connector/j/5.1.html
   
  It is typically named mysql-connector-java-5.1.<x>.zip or 
  mysql-connector-java-5.1.<x>.tar.gz where <x> represents the minor version 
  number.

  The JDBC driver in the archive is named:
    mysql-connector-java-5.1.<x>-bin.jar.

  This needs to be copied to:

    * the Apache Tomcat library directory: <TOMCAT_HOME>/lib
    * the OpenVPMS library directory:  <OPENVPMS_HOME>/lib

  In the above, <TOMCAT_HOME> refers to the directory where Apache Tomcat is installed.
  On Windows, this will be something like: C:\Program Files\Apache Software Foundation\Tomcat 6.0

1.4 Database setup

  To create the OpenVPMS MySQL database, run the following in a shell prompt
  > cd <OPENVPMS_HOME>/db
  > mysql -u admin -p < createdb.sql
  > mysql -u admin -p openvpms < db.sql

  NOTE: replace 'admin' with a user that has administrator priviledges in MySQL

  Next, run the 'dataload' script. This provides two options, 'base' and
  'setup'. The former loads a base database setup in preparation for data
  migration. The latter contains a default setup suitable for a new 
  installation.

  E.g:
  > cd <OPENVPMS_HOME>/bin
  > dataload setup

1.5. Web application installation

  If you are upgrading OpenVPMS, the existing web application should be
  removed before installing the new version.
  To do this:
  1. Shut down Apache Tomcat if it is already running.
  2. Delete the directory: <TOMCAT_HOME>/webapps/openvpms
  3. Delete the file:      <TOMCAT_HOME>/webapps/openvpms.war

  To install the OpenVPMS web application:

  1. Copy <OPENVPMS_HOME>/webapps/openvpms.war to the <TOMCAT_HOME>/webapps
     directory.
  2. Start Apache Tomcat if it is not running


2. Testing the installation

  To test the installation, open up your Internet Browser and enter the 
  address:

      http://localhost:8080/openvpms/app

  Login to OpenVPMS using user admin and password admin

3. Subscription

 OpenVPMS relies on user subscriptions to fund development. Your subscription
 status is displayed on the OpenVPMS login screen. If you have not paid, a
 link to a payment page is displayed. On payment, a subscription key will be
 mailed to you.
 If you have a current subscription, you can request a subscription key be
 emailing a copy of your receipt to subscription@openvpms.org.

 To update your subscription status, edit the Practice in the
 Administration -> Organisation workspace and upload the new subscription key.

4. Data Migration

 OpenVPMS doesn't directly support data migration from other veterinary
 practice systems.
 It does however provide a plugin for Pentaho Data Integration
 (PDI, aka Kettle) that can be used to get data into OpenVPMS.

 At this stage, only PDI 3.2 is supported. This can be obtained from:
   http://sourceforge.net/projects/pentaho/files/Data%20Integration/3.2.0-stable/pdi-ce-3.2.0-stable.zip/download

 Extract this zip file to a directory. This will be referred to as <PDI_HOME>.

 To install the OpenVPMSLoader plugin:

 1. Extract <OPENVPMS_HOME>/import/plugin/OpenVPMSLoader.zip to <PDI_HOME>/plugins/steps/OpenVPMSLoader
 2. Remove <PDI_HOME>/libext/spring/spring-core.jar
 3. Copy the OpenVPMS jars to libext/spring i.e.
    copy plugins/steps/OpenVPMSLoader/*.jar libext/spring/


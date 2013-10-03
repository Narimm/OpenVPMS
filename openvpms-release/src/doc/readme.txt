OpenVPMS ${pom.version} Release
==========================

1. Installation Notes

See section 2 if you are upgrading an existing system.

Note that in this text the directory or folder separator character is shown as /
following the unix usage. If you are using Windows, then you will be using \ so
that paths shown as say <OPENVPMS_HOME>/lib will in fact be <OPENVPMS_HOME>\lib.

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

  To install the OpenVPMS web application:

  1. Copy <OPENVPMS_HOME>/webapps/openvpms.war to the <TOMCAT_HOME>/webapps
     directory.
  2. Start Apache Tomcat if it is not running

1.6 Testing the installation

  To test the installation, open up your Internet Browser and enter the 
  address:

      http://localhost:8080/openvpms/app

  Login to OpenVPMS using user admin and password admin
  
2. Upgrading
 This section details the upgrade procedure.

 These instructions assume that:
 1. The previous OpenVPMS installation is available in <OPENVPMS_PREV>.
     E.g. on Windows:
        c:\OpenVPMS\openvpms-release-1.6
 2. The new installation will be located in <OPENVPMS_HOME>.
     E.g. on Windows:
        c:\OpenVPMS\openvpms-release-1.7

 NOTE: the OpenVPMS version can be excluded from the path name. This can simplify upgrades by removing the need
       to change custom scripts that contain the installation path.

 The previous installation should be retained until:
 . settings have been migrated to the new installation
 . the migration has been verified to be successful

2.1 Preparation

  Back up your database prior to performing the upgrade.
  This can be done using the mysqldump tool. E.g.:

        mysqldump -u openvpms -p openvpms > openvpms.sql

  See http://dev.mysql.com/doc/refman/5.1/en/mysqldump.html for more
  details on backing up and restoring MySQL databases

2.2 Release compatibility
 The following table shows the upgrade path to the current release:

 From version         Remarks
 1.7 (earlier build)  As per steps 2.3 and following
 1.6                  As per steps 2.3 and following
 1.5                  As per steps 2.3 and following
 1.4 and earlier      See 2.8

2.3 MySQL connector
  Copy the MySQL JDBC driver mysql-connector-java-5.1.<x>-bin.jar from <OPENVPMS_PREV>/lib
  to <OPENVPMS_HOME>/lib

2.4 Load archetypes
   Load the latest archetypes by running the appropriate archload script for your platform.
   Windows:
   > cd <OPENVPMS_HOME>\bin
   > archload

   Unix:
   > cd <OPENVPMS_HOME>/bin
   > archload.sh

2.5 Web application
  The existing web application should be removed before installing the new
  version.
  To do this:
  1. Shut down Apache Tomcat if it is already running.
  2. Delete or move directory: <TOMCAT_HOME>/webapps/openvpms
     Do not move it to another directory under <TOMCAT_HOME>/webapps/ as
     Tomcat will continue to launch it.
  3. Delete the file:      <TOMCAT_HOME>/webapps/openvpms.war
  4. Copy <OPENVPMS_HOME>/webapps/openvpms.war to the directory <TOMCAT_HOME>/webapps
  5. Start Apache Tomcat - this will extract <TOMCAT_HOME>/webapps/openvpms.war
     and build <TOMCAT_HOME>/webapps/openvpms

2.6 Customisation
  If you have customised version of propercase.properties, help.properties, or
  messages.properties you need to install these in
  <TOMCAT_HOME>/webapps/openvpms/WEB-INF/classes/localisation
  You can simply overwrite the default propercase.properties with your own version.
  However, help.properties and messages.properties will need to be edited to
  bring your adjustments into the current versions.
  
  If you have a customised default.stylesheet, then the version in
  <TOMCAT_HOME>/webapps/openvpms/WEB-INF/classes/style will need to be edited
  to incorporate your changes.
  
  Now restart Apache Tomcat so the above customisations get picked up and login and see
  that things are as they should be.
  
2.7 Kettle
  If you use Pentaho Data Integration (see 4 below) then you need perform its
  steps 1,2 and 3 to upgrade the OpenVPMS components.

2.8 Upgrading from older releases of OpenVPMS

  Upgrading from OpenVPMS 1.4 or earlier requires data migration scripts to be run.
  These are located in the <OPENVPMS_HOME>/update/db directory.
  See the following sections to migrate data from a particular release.
  Once complete, continue with step 2.3.

  1. Upgrading from OpenVPMS 1.4
     Run:
     > mysql -u openvpms -p openvpms < migrate-1.4-to-1.5.sql

  2. Upgrading from OpenVPMS 1.3
     Run:
     > mysql -u openvpms -p openvpms < migrate-1.3-to-1.4.sql
     > mysql -u openvpms -p openvpms < migrate-1.4-to-1.5.sql

  3. Upgrading from OpenVPMS 1.2
     Run:
     > mysql -u openvpms -p openvpms < migrate-1.2-to-1.3.sql
     > mysql -u openvpms -p openvpms < migrate-1.3-to-1.4.sql
     > mysql -u openvpms -p openvpms < migrate-1.4-to-1.5.sql

  4. Upgrading from OpenVPMS 1.1
     Run:
     > mysql -u openvpms -p openvpms < migrate-1.1-to-1.2.sql
     > mysql -u openvpms -p openvpms < migrate-1.2-to-1.3.sql
     > mysql -u openvpms -p openvpms < migrate-1.3-to-1.4.sql
     > mysql -u openvpms -p openvpms < migrate-1.4-to-1.5.sql

  5. Upgrading from OpenVPMS 1.0
     Run:
     > mysql -u openvpms -p openvpms < migrate-1.0-to-1.1.sql
     > mysql -u openvpms -p openvpms < migrate-1.1-to-1.2.sql
     > mysql -u openvpms -p openvpms < migrate-1.2-to-1.3.sql
     > mysql -u openvpms -p openvpms < migrate-1.3-to-1.4.sql
     > mysql -u openvpms -p openvpms < migrate-1.4-to-1.5.sql

3. Subscription

 OpenVPMS relies on user subscriptions to fund development. Your subscription
 status is displayed on the OpenVPMS login screen. If you have not paid, a
 link to a payment page is displayed. On payment, a subscription key will be
 mailed to you.
 If you have a current subscription, you can request a subscription key by
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
    
5. Browser Compatibility
 OpenVPMS is designed to be used with Firefox, Chrome, or Safari. The Context Sensitive Help
 facility provides help when you press Alt-F1 on almost all screens. By default, the help
 is displayed in a separate browser window. If you want it displayed in a tab rather than a
 new window try the following:
  - Firefox: install the 'Open Link in New Tab' add-on
  - Chrome: install the 'One Window' extension
  - Safari: select Preferences|Tabs|Open pages in tabs instead of windows: Always

OpenVPMS ${pom.version} (${buildNumber}) Release
===============================================================================

1. Installation Notes

See section 2 if you are upgrading an existing system.

Note that in this text, the directory or folder separator character is shown
as /, following unix conventions. On Windows, replace / with \. E.g. given:
  <OPENVPMS_HOME>/lib
change to:
  <OPENVPMS_HOME>\lib.

1.1 Requirements
- OpenVPMS requires the following to be installed:

  - Java Platform, Standard Edition 7.
    The Java Runtime Environment (JRE) is the minimum requirement.
    See http://www.oracle.com/technetwork/java/javase/downloads/index.html

    NOTE: Java 8 is not yet supported.

  - MySQL 5.1.x or 5.5.x
    Version 5.1.53 is the minimum requirement.

    See http://dev.mysql.com/downloads/mysql/5.1.html

    Download the MySQL Community Server 'Generally Available (GA) Release'

    On Windows, the MySQL installer may require that the Microsoft .Net
    Framework 4 be installed. This is available from:
        http://www.microsoft.com/en-au/download/details.aspx?id=17851

  - MySQL Connector/J JDBC driver 
    See http://dev.mysql.com/downloads/connector/j/5.1.html

    This may be included in the MySQL server installation.

  - Tomcat 6.x or Tomcat 7.x
    See http://tomcat.apache.org/download-60.cgi

    On Windows, select the 32-bit/64-bit Windows Service Installer

  - OpenOffice 4.0.x
    See http://www.openoffice.org/download/

  - iReport Designer 5.0.4 or higher
    Only required to customise document templates.
    See http://community.jaspersoft.com/project/ireport-designer

- MySQL:
  - should be on the same host as Tomcat
  - should accept connections on port 3306
  - include the following lines in my.ini
    max_allowed_packet=16M
    innodb_file_per_table

1.2 Directory structure

 The OpenVPMS installation has a single top-level directory named:
   openvpms-release-${pom.version}

 This will be referred to as <OPENVPMS_HOME> in the remainder of this document.
 This directory has the following sub-directories:

 - bin     contains a number of tools used to load data into OpenVPMS
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

  In the above, <TOMCAT_HOME> refers to the directory where Apache Tomcat is
  installed.
  On Windows, this will be something like:
    C:\Program Files\Apache Software Foundation\Tomcat 6.0

1.4 Database setup

  To create the OpenVPMS MySQL database, run the following in a shell prompt
  > cd <OPENVPMS_HOME>/db
  > mysql -u admin -p < createdb.sql
  > mysql -u admin -p openvpms < db.sql

  NOTES:
  * replace 'admin' with a user that has administrator privileges in MySQL.
  * the createdb.sql script creates an 'openvpms' user that only has
    privileges to connect from localhost.
    If the MySQL database is a different host to that running Tomcat, change
    the createdb.sql script to uncomment:

    # GRANT ALL PRIVILEGES ON openvpms.* TO 'openvpms'@'%'
    #     IDENTIFIED BY 'openvpms' WITH GRANT OPTION;

    To improve security, the '%' can be limited to the host that Tomcat will
    connect from.

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

1.6 OpenOffice installation

  OpenVPMS uses OpenOffice to perform reporting, printing and document conversion.
  Install it as per your platform's requirements and then:

  1. Add it to the PATH of the user that runs Apache Tomcat.
     Windows users can find instructions for this at:
        http://www.openvpms.org/documentation/installing-openvpms-windows-7-computer#openvpms_win7_10
  2. Verify it can be run as that user.
     From a command prompt, enter:
        soffice
     This should start OpenOffice.

1.7 Document Templates

  Document templates used for invoices, payments etc., are located in:
      <OPENVPMS_HOME>/reports

  These need to be loaded prior to use.
  This can be done using the 'templateload' script. E.g:

  > cd <OPENVPMS_HOME>/bin
  > templateload ../reports/templates-X.xml

  Where X is:
       A4 to load the A4 template set
       A5 to load the A5 template set
       Letter to load the Letter (ie US) template

  NOTES:
  * If a template with the same name has been already loaded, it will be replaced
  * Not all templates are available in all formats, however a complete set
    is loaded. Thus if you load the A5 set you will find that some templates
    are in A4 format.
    To clarify the situation, the description field indicates the paper size
    if it is not A4.  Eg " Medical Records (A5)"
  * Three drug label formats are available, Dymo, Epson, and 1.8x3.1.  If
    you load the Letter set you will get the latter, if you load A4 or A5 you
    will get Dymo.
  * Not everything is loaded. For example the Samples and the Patient
    Reminder Post Cards are not.

  After installation, templates can be updated using via
  Administration|Templates.

  The templates need to be customised to add practice logos etc. Templates
  with a:
  - .doc or .odt extension can be customised in OpenOffice Writer or
     Microsoft Word
  - .jrxml extension can be customised in iReport Designer

  See also:
  - http://www.openvpms.org/documentation/csh/1.8/introduction/reporting
  - http://www.openvpms.org/documentation/csh/1.8/reference/reportsForms
  - http://www.openvpms.org/documentation/csh/1.8/admin/template

1.8 Testing the installation

  To test the installation, open up your Internet Browser and enter the
  address:

      http://localhost:8080/openvpms/app

  Login to OpenVPMS using user admin and password admin

2. Upgrading
  This section details the upgrade procedure.

  These instructions assume that:
  1. The previous OpenVPMS installation is available in <OPENVPMS_PREV>.
     E.g. on Windows:
        c:\OpenVPMS\openvpms-release-1.7
  2. The new installation will be located in <OPENVPMS_HOME>.
     E.g. on Windows:
        c:\OpenVPMS\openvpms-release-1.7

  NOTE: the OpenVPMS version can be excluded from the path name. This can
  simplify upgrades by removing the need to change custom scripts that contain
  the installation path.

  The previous installation should be retained until:
  . settings have been migrated to the new installation
  . the migration has been verified to be successful

2.1 Preparation

  Back up your database prior to performing the upgrade.
  This can be done using the mysqldump tool. E.g.:

        mysqldump -u openvpms -p openvpms > openvpms.sql

  NOTE: It is good practice to ensure that the backup can be restored to a
        different server, prior to performing any upgrade.

  For more information on backing up and restoring MySQL databases, see:
  . http://dev.mysql.com/doc/refman/5.1/en/mysqldump.html and
  . http://dev.mysql.com/doc/refman/5.1/en/innodb-backup.html
  . http://dev.mysql.com/doc/refman/5.1/en/mysql.html

2.2 Release compatibility
 The following table shows the upgrade path to the current release:

 From version         Remarks
 1.7 (earlier build)  As per steps 2.3 and following
 1.6 and earlier      See 2.8

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
  If you use customised versions of the standard archetypes, or have added
  archetypes, these will need to be loaded.
  For modified versions of the standard archetypes, be sure to incorporate
  any changes that have been made.
  You should then use archload to load these archetypes - or if you have
  only a few, use Administration|Archetypes|Import.

  If you have customised versions of propercase.properties, help.properties, or
  messages.properties you need to install these in
  <TOMCAT_HOME>/webapps/openvpms/WEB-INF/classes/localisation
  You can simply overwrite the default propercase.properties with your own
  version.
  However, help.properties and messages.properties will need to be edited to
  bring your adjustments into the current versions.
  
  If you have a customised default.stylesheet, then the version in
  <TOMCAT_HOME>/webapps/openvpms/WEB-INF/classes/style will need to be edited
  to incorporate your changes.
  
  Now restart Apache Tomcat so the above customisations get picked up and
  login and see that things are as they should be.
  
2.7 Kettle
  If you use Pentaho Data Integration (see 4 below) then you need perform its
  steps 1,2 and 3 to upgrade the OpenVPMS components.

2.8 Upgrading from older releases of OpenVPMS

  Upgrading from OpenVPMS 1.7 or earlier requires data migration scripts to be
  run. Once this has been done, continue with step 2.3.

  The scripts are located in the <OPENVPMS_HOME>/update/db directory.
  With the exception of the 1.5 to 1.6 release (where there was no change to the
  database structure), there is one sql script per release.

  The sql scripts are:
     migrate-1.0-to-1.1.sql
     migrate-1.1-to-1.2.sql
     migrate-1.2-to-1.3.sql
     migrate-1.3-to-1.4.sql
     migrate-1.4-to-1.5.sql
     migrate-1.6-to-1.7.sql
     migrate-1.7-to-1.8.sql

  You need to run each relevant one in turn using the mysql utility.

  Hence if you are upgrading from OpenVPMS 1.7, run:
     > mysql -u openvpms -p openvpms < migrate-1.7-to-1.8.sql

  If you are upgrading from OpenVPMS 1.5 or 1.6, run:
     > mysql -u openvpms -p openvpms < migrate-1.6-to-1.7.sql
     > mysql -u openvpms -p openvpms < migrate-1.7-to-1.8.sql

  If you are upgrading from OpenVPMS 1.0 - you need the lot, so run:
     > mysql -u openvpms -p openvpms < migrate-1.0-to-1.1.sql
     > mysql -u openvpms -p openvpms < migrate-1.1-to-1.2.sql
     > mysql -u openvpms -p openvpms < migrate-1.2-to-1.3.sql
     > mysql -u openvpms -p openvpms < migrate-1.3-to-1.4.sql
     > mysql -u openvpms -p openvpms < migrate-1.4-to-1.5.sql
     > mysql -u openvpms -p openvpms < migrate-1.6-to-1.7.sql
     > mysql -u openvpms -p openvpms < migrate-1.7-to-1.8.sql

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

 1. Extract <OPENVPMS_HOME>/import/plugin/OpenVPMSLoader.zip to
    <PDI_HOME>/plugins/steps/OpenVPMSLoader
 2. Remove <PDI_HOME>/libext/spring/spring-core.jar
 3. Copy the OpenVPMS jars to libext/spring i.e.
    copy plugins/steps/OpenVPMSLoader/*.jar libext/spring/
    
5. Browser Compatibility

  OpenVPMS is designed to be used with Firefox, Chrome, or Safari. The Context
  Sensitive Help facility provides help when you press Alt-F1 on most screens.
  By default, the help is displayed in a separate browser window. If you want
  it displayed in a tab rather than a new window try the following:
  - Firefox: install the 'Open Link in New Tab' add-on
  - Chrome: install the 'One Window' extension
  - Safari:
      select Preferences|Tabs|Open pages in tabs instead of windows: Always

  Note that both Context Sensitive Help and Print Previews are treated as
  pop-ups by browsers and may be blocked.
  You will need to enable pop-ups for your OpenVPMS site.

6. Security

6.1 Database passwords
  The default installation creates a MySQL database user named 'openvpms'
  with password 'openvpms'.

  This password should be changed. E.g, using the mysql tool:
    set password for 'openvpms'@'localhost' = password('3f6iNF7m46w9vjc');

  This should be done for each host that the openvpms user has been granted
  access from.

  For more information see:
  . https://dev.mysql.com/doc/refman/5.5/en/set-password.html

  The password is stored in configuration files that will also need to be
  updated:
  . <OPENVPMS_HOME>/conf/hibernate.properties
  . <TOMCAT_HOME>/webapps/openvpms/WEB-INF/classes/hibernate.properties

6.2 Administrator password
  The default installation creates an OpenVPMS user named 'admin', with
  password 'admin'.
  This should be changed when installation is complete, via
  Administration|Users.

6.3 User passwords
  User passwords are configured via Administration|Users. There is little
  restriction on what passwords may be entered, but it is recommended that
  strong passwords are used.

6.4 File permissions
  The OpenVPMS and Tomcat installation directories should only be accessible
  to a single user with a strong password.
  These directories contain files that could enable an attacker to gain access
  to the OpenVPMS web application, or the MySQL database.

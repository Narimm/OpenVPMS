OpenVPMS 1.2 Release
====================

1. Installation Notes

1.1 Requirements
- OpenVPMS requires the following to be installed:

  - Java 1.5.x or higher
    See: http://java.sun.com/javase/downloads/index_jdk5.jsp

  - MySQL 5.0.24a or higher
    See http://www.mysql.org/downloads/mysql/5.0.html

  - Tomcat 5.5.12 or higher
    See http://tomcat.apache.org/download-55.cgi

  - OpenOffice 2.2.1 or higher
    See: http://download.openoffice.org/

- MySQL:
  - should be on the same host as Tomcat
  - should accept connections on port 3306
  - include the following lines in my.ini
    max_allowed_packet=16M
    innodb_file_per_table

1.2 Directory structure

 The OpenVPMS installation has a single top-level directory named
 openvpms-release-1.1 (this will be referred to as <OPENVPMS_HOME> in the
 reminder of this document). This has the following sub-directories:

 - bin     contains a number of tool scripts used to load data into OpenVPMS
 - conf    contains configuration files for the tools in bin/
 - db      contains MySQL SQL scripts to create the initial database
 - import  contains data to import into OpenVPMS
 - lib     contains jars used by the tools in bin/
 - reports contains document templates for reporting
 - update  contains data and scripts to migrate from earlier versions of
           OpenVPMS
 - webapps contains the OpenVPMS web applications

1.3 Database setup

  To create the OpenVPMS MySQL database, run the following in a shell prompt
  > cd <OPENVPMS_HOME>/db
  > mysql -u admin -p < createdb.sql
  > mysql -u admin -p < db.sql

  NOTE: replace 'admin' with a user that has administrator priviledges in MySQL

  Next, run the 'dataload' script. This provides two options, 'base' and
  'setup'. The former loads a base database setup in preparation for data
  migration. The latter contains a default setup suitable for a new installation.

  E.g:
  > cd <OPENVPMS_HOME>/bin
  > dataload setup

1.4. Web application installation

  To install the OpenVPMS web application,
  copy <OPENVPMS_HOME>/webapps/openvpms.war
  to the <TOMCAT_HOME>/webapps directory.


2. Testing the installation

  To test the installation, open up your Internet Browser and enter the address:

      http://localhost:8080/openvpms/app

  Login to OpenVPMS using user admin and password admin

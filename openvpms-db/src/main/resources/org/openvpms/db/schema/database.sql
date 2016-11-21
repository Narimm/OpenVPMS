#
# Script to create the openvpms database, and add a single user 'openvpms',
# with all privileges
#

CREATE DATABASE `${db.name}` /*!40100 DEFAULT CHARACTER SET utf8 */;

GRANT ALL PRIVILEGES ON `${db.name}`.* TO 'openvpms'@'localhost'
IDENTIFIED BY 'openvpms'
WITH GRANT OPTION;

#
# Uncomment the following if MySQL is on a different host to Tomcat.
# The '%' can be changed to the Tomcat host to improve security.
#
# GRANT ALL PRIVILEGES ON `${db.name}`.* TO 'openvpms'@'%'
#     IDENTIFIED BY 'openvpms' WITH GRANT OPTION;

COMMIT;

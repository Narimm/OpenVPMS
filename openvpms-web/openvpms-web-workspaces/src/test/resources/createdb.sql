#
# Script to create the openvpms database, and add a single user 'openvpms',
# with all privileges
#

CREATE DATABASE IF NOT EXISTS `openvpms-1_8` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `openvpms-1_8`;

GRANT ALL PRIVILEGES ON `openvpms-1_8`.* TO 'openvpms'@'localhost'
    IDENTIFIED BY 'openvpms' WITH GRANT OPTION;

COMMIT;
opevnpms-web/openvpms-macro/pom.xml                             |    5 +-
opevnpms-web/openvpms-web-component/pom.xml                     |    7 +-
.../web/component/print/AbstractPrinter.java       |  404 +++---
.../openvpms/web/component/print/PrintDialog.java  |   11 +-
.../web/resource/image/buttons/estimate.png        |  Bin 0 -> 618 bytes
.../web/resource/localisation/messages.properties  |    3 +
opevnpms-web/openvpms-web-echo/pom.xml                          |    5 +-
opevnpms-web/openvpms-web-jobs/pom.xml                          |    5 +-
opevnpms-web/openvpms-web-resource/pom.xml                      |   22 +-
opevnpms-web/openvpms-web-workspaces/pom.xml                    |    5 +-
.../web/workspace/customer/CustomerSummary.java    |  494 ++++----
.../customer/estimate/CustomerEstimateQuery.java   |  129 ++
.../customer/estimate/EstimateViewer.java          |  326 ++---
.../customer/info/CustomerViewLayoutStrategy.java  |  272 ++---
.../workspace/patient/summary/PatientSummary.java  | 1280 ++++++++++----------
.../web/workspace/summary/PartySummary.java        |  353 +++---
opevnpms-web/pom.xml                                            |    9 +-
18 files changed, 1746 insertions(+), 1587 deletions(-)


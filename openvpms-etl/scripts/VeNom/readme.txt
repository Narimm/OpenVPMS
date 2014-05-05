This directory provides scripts to extract data from VeNom CSV files and generate SQL and dataload files.
The scripts were developed based on the format of the 'VeNom v20 04DEC2012 one sheet.xls' file.

Note that the original spreadsheets cannot be committed to svn due to licensing restrictions.

This was exported to CSV using OpenOffice with the following settings:
. Characer set: Unicode (UTF-8)
. Field delimeter: ,
. Text delimeter: "
. Save cell contents as shown

The scripts are:
. diagnosis-sql.pl            - generates SQL to load lookup.diagnosisVeNom lookups from VeNom Diagnosis codes.

. presenting-complaint-sql.pl - generates SQL to load lookup.presentingComplaintVeNom lookups from VeNom Presenting
                                complaint codes.
                                This excludes 'Active Non-referral' presenting complaints which essentially duplicate
                                the 'Active referral' ones.

Note that the perl scripts require the Text::CSV module. If this is not installed, run the following:

$ perl -MCPAN -e shell

  cpan shell -- CPAN exploration and modules installation (v1.960001)
  Enter 'h' for help.

  cpan[1]> install Text::CSV
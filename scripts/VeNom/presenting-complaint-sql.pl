#!/usr/bin/perl

use strict;
use warnings;
use Text::CSV;
use open ':std', ':encoding(UTF-8)';
 
my $file = $ARGV[0] or die "Usage presenting-complaint-sql.pl <csv file>\n";
 
my $csv = Text::CSV->new ({binary => 1, auto_diag => 1, sep_char => ','});

open(my $data, $file) or die "Could not open '$file' $!\n";
print "drop table if exists venom_presenting_complaint;\n";
print "create table venom_presenting_complaint (
    code varchar(255) primary key,
    dictionaryId varchar(255),
    term varchar(255),
    modelling varchar(255));\n";
print "insert into venom_presenting_complaint (code, dictionaryId, term, modelling) values\n";

my $count = 0;
while (my $fields = $csv->getline( $data )) {
    my $status = $fields->[0];
    my $label = $fields->[4];
    my $id = $fields->[5];
    my $term = $fields->[6];
    my $modelling = $fields->[7];
    if ($status ne "Active Non-referral" && $label eq "Presenting complaint") {
        $term =~ s/\'/\'\'/g;
        $term =~ s/\226/-/g;       # handle special hyphens
        $modelling =~ s/\'/\'\'/g;
        $modelling =~ s/\226/-/g;
        if ($count++ > 0) {
            print(",\n");
        }
        print "('$id-VENOM', '$id', '$term', '$modelling')"
    }
}
print (";\n");

print "insert into lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
select 0, UUID(), 'lookup.presentingComplaintVeNom', 1, '1.0', d.code, d.term, d.modelling, 0
from venom_presenting_complaint d
where not exists (select * from lookups e where e.arch_short_name = 'lookup.presentingComplaintVeNom' and e.code = d.code);\n";

print "insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, 'dataDictionaryId', 'string', d.dictionaryId
from lookups l, venom_presenting_complaint d
where l.arch_short_name = 'lookup.presentingComplaintVeNom' and l.code = d.code
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name = 'dataDictionaryId');\n";

print "drop table venom_presenting_complaint;\n";

if (not $csv->eof) {
  $csv->error_diag();
}
close $data;

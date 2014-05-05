#!/usr/bin/perl

use strict;
use warnings;
 
use Text::CSV;
 
my $file = $ARGV[0] or die "Usage presenting-complaint-sql.pl <csv file>\n";
 
my $csv = Text::CSV->new ({binary => 1, auto_diag => 1, sep_char => ','});
 
my $sum = 0;
open(my $data, $file) or die "Could not open '$file' $!\n";
while (my $fields = $csv->getline( $data )) {
    my $status = $fields->[0];
    my $label = $fields->[4];
    if ($status ne "Active Non-referral" && $label eq "Presenting complaint") {
      my $id = $fields->[5];
      my $term = $fields->[6];
      my $modelling = $fields->[7];
      $term =~ s/\'/\'\'/g;
      $term =~ s/\226/-/g;       # handle special hyphens
      $modelling =~ s/\'/\'\'/g;
      $modelling =~ s/\226/-/g;
      print "insert into lookups (version, linkId, arch_short_name, active, arch_version, code, name, description, default_lookup)
select 0, UUID(), 'lookup.presentingComplaintVeNom', 1, '1.0', 'VENOM-$id', '$term', '$modelling', 0
from dual
where not exists (select * from lookups e where e.arch_short_name = 'lookup.presentingComplaintVeNom' and e.code = 'VENOM-$id');\n";

     print "insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, 'dataDictionaryId', 'string', '$id'
from lookups l
where l.arch_short_name = 'lookup.presentingComplaintVeNom' and l.code = 'VENOM-$id'
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name = 'dataDictionaryId');\n";
    }
}

if (not $csv->eof) {
  $csv->error_diag();
}
close $data;

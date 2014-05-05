#!/usr/bin/perl

use strict;
use warnings;

use Text::CSV;
use XML::LibXML;

my $file = $ARGV[0] or die "Usage lookups.pl <csv file>\n";

my $csv = Text::CSV->new ({binary => 1, auto_diag => 1, sep_char => ','});

use open ':std', ':encoding(UTF-8)';
open(my $data, $file) or die "Could not open '$file' $!\n";

my $doc = XML::LibXML::Document->new('1.0', 'UTF-8');
print "<archetype>\n";
while (my $fields = $csv->getline( $data )) {
    my $status = $fields->[0];
    my $label = $fields->[4];
    if (($status ne "Active Non-referral" && $label eq "Presenting complaint") || $label eq "Diagnosis") {
      my $id = $fields->[5];
      my $term = $fields->[6];
      my $modelling = $fields->[7];
      $term = $doc->createTextNode($term)->toString();
      $modelling = $doc->createTextNode($modelling)->toString();

      $term =~ s/\'/&apos;/g;
      $modelling =~ s/\'/&apos;/g;
      $term =~ s/\226/-/g;       # handle special hyphens
      $modelling =~ s/\226/-/g;
      $term =~ s/\xb5/&\#181;/g;    # handle micro

      my $code = "VENOM_$id";
      my $archetype = $label eq "Diagnosis" ? "lookup.diagnosisVeNom" : "lookup.presentingComplaintVeNom";
      print "<data id='$code' archetype='$archetype' code='$code' name='$term' description='$modelling' dataDictionaryId='$id'/>\n";
    }
}
print "</archetype>\n";

if (not $csv->eof) {
  $csv->error_diag();
}
close $data;

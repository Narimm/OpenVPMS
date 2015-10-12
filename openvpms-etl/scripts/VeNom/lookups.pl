#!/usr/bin/perl

use strict;
use warnings;

use Text::CSV;
use XML::LibXML;

my $file = shift or die "Usage lookups.pl <csv file> [-all|-diagnosis|-presentingComplaint|-visitReason]\n";
my $lookups = shift || '-all';
if ($lookups ne '-all' && $lookups ne "-diagnosis" && $lookups ne "-presentingComplaint"
   && $lookups ne "-visitReason" ) {
   die "Usage lookups.pl <csv file> [-all|-diagnosis|-presentingComplaint|-visitReason]\n";
}

my $csv = Text::CSV->new ({binary => 1, auto_diag => 1, sep_char => ','});

use open ':std', ':encoding(UTF-8)';
open(my $data, $file) or die "Could not open '$file' $!\n";

my $doc = XML::LibXML::Document->new('1.0', 'UTF-8');
my $all = $lookups eq "-all";
my $complaint = $all || $lookups eq "-presentingComplaint";
my $diagnosis = $all || $lookups eq "-diagnosis";
my $reason = $all || $lookups eq "-visitReason";

print "<archetype>\n";
while (my $fields = $csv->getline( $data )) {
    my $status = $fields->[0];
    my $label = $fields->[4];
    if (($status ne "Active Non-referral" && $label eq "Presenting complaint" && $complaint)
        || ($label eq "Diagnosis" && $diagnosis)
        || ($label eq "Reason for visit" && $reason)) {
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
      my $archetype = "lookup.diagnosisVeNom";
      if ($label eq "Presenting complaint") {
        $archetype = "lookup.presentingComplaintVeNom";
      } elsif ($label eq "Reason for visit") {
        $archetype = "lookup.visitReasonVeNom";
      }

      print "<data id='$code' archetype='$archetype' code='$code' name='$term' description='$modelling' dataDictionaryId='$id'/>\n";
    }
}
print "</archetype>\n";

if (not $csv->eof) {
  $csv->error_diag();
}
close $data;

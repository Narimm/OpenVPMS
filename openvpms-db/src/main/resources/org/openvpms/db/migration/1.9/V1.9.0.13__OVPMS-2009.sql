#
# OVPMS-2009 Typo in base.xml allergy lookup.patientAlertType
#

UPDATE lookups l
SET code = 'ALLERGY'
WHERE l.arch_short_name = 'lookup.patientAlertType'
      AND l.code = 'ALLLERGY';

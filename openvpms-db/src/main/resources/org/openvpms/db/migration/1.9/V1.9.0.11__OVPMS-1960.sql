#
# OVPMS-1960 Patient Insurance
#

#
# This:
# . adds authorities for:
#   . actIdentity.*
#   . act.patientInsurancePolicy
#   . act.patientInsuranceClaim*
#   . act.patientInvestigation*
#   . act.patientPrescription
#   . act.patientWeight
# . adds actIdentity.* to Base Role
# . adds patient act authorities to Base Role, with the exception of act.patientInsuranceClaim*
# . adds act.patientInsuranceClaim authorities to new Insurance Claims role
#
DROP TABLE IF EXISTS new_authorities;
CREATE TEMPORARY TABLE new_authorities (
  name        VARCHAR(255) PRIMARY KEY,
  description VARCHAR(255),
  method      VARCHAR(255),
  archetype   VARCHAR(255)
);

INSERT INTO new_authorities (name, description, method, archetype)
VALUES ('Patient Insurance Policy Create', 'Authority to Create Patient Insurance Policy', 'create',
        'act.patientInsurancePolicy'),
  ('Patient Insurance Policy Save', 'Authority to Save Patient Insurance Policy', 'save', 'act.patientInsurancePolicy'),
  ('Patient Insurance Policy Remove', 'Authority to Remove Patient Insurance Policy', 'remove',
   'act.patientInsurancePolicy'),

  ('Patient Insurance Claim Create', 'Authority to Create Patient Insurance Claim', 'create',
   'act.patientInsuranceClaim'),
  ('Patient Insurance Claim Save', 'Authority to Save Patient Insurance Claim', 'save', 'act.patientInsuranceClaim'),
  ('Patient Insurance Claim Remove', 'Authority to Remove Patient Insurance Claim', 'remove',
   'act.patientInsuranceClaim'),

  ('Patient Insurance Claim Condition Create', 'Authority to Create Patient Insurance Claim Condition', 'create',
   'act.patientInsuranceClaimItem'),
  ('Patient Insurance Claim Condition Save', 'Authority to Save Patient Insurance Claim Condition', 'save',
   'act.patientInsuranceClaimItem'),
  ('Patient Insurance Claim Condition Remove', 'Authority to Remove Patient Insurance Claim Condition', 'remove',
   'act.patientInsuranceClaimItem'),

  ('Patient Insurance Claim Attachment Create', 'Authority to Create Patient Insurance Claim Attachment', 'create',
   'act.patientInsuranceClaimAttachment'),
  ('Patient Insurance Claim Attachment Save', 'Authority to Save Patient Insurance Claim Attachment', 'save',
   'act.patientInsuranceClaimAttachment'),
  ('Patient Insurance Claim Attachment Remove', 'Authority to Remove Patient Insurance Claim Attachment', 'remove',
   'act.patientInsuranceClaimAttachment'),

  ('Act Identity Create', 'Authority to Create Act Identity', 'create', 'actIdentity.*'),
  ('Act Identity Save', 'Authority to Save Act Identity', 'save', 'actIdentity.*'),
  ('Act Identity Remove', 'Authority to Remove Act Identity', 'remove', 'actIdentity.*'),

  ('Patient Investigation Create', 'Authority to Create Investigation', 'create', 'act.patientInvestigation*'),
  ('Patient Investigation Save', 'Authority to Save Investigation', 'save', 'act.patientInvestigation*'),
  ('Patient Investigation Remove', 'Authority to Remove Investigation', 'remove', 'act.patientInvestigation*'),

  ('Patient Prescription Create', 'Authority to Create Prescription', 'create', 'act.patientPrescription'),
  ('Patient Prescription Save', 'Authority to Save Prescription', 'save', 'act.patientPrescription'),
  ('Patient Prescription Remove', 'Authority to Remove Prescription', 'remove', 'act.patientPrescription'),

  ('Patient Weight Create', 'Authority to Create Weight', 'create', 'act.patientWeight'),
  ('Patient Weight Save', 'Authority to Save Weight', 'save', 'act.patientWeight'),
  ('Patient Weight Remove', 'Authority to Remove Weight', 'remove', 'act.patientWeight');

#
# Create authorities, if they don't already exist.
#
INSERT INTO granted_authorities (version, linkId, arch_short_name, arch_version, name, description, active,
                                 service_name, method, archetype)
  SELECT
    0,
    UUID(),
    'security.archetypeAuthority',
    '1.0',
    a.name,
    a.description,
    1,
    'archetypeService',
    a.method,
    a.archetype
  FROM new_authorities a
  WHERE NOT exists(
      SELECT *
      FROM granted_authorities g
      WHERE g.method = a.method
            AND g.archetype = a.archetype);

#
# Remove act.patient* authorities from Base Role, as it should not be able to make insurance claims.
#
DELETE ra.*
FROM roles_authorities ra
  JOIN security_roles r
    ON r.security_role_id = ra.security_role_id
       AND r.name = 'Base Role'
  JOIN granted_authorities ga
    ON ra.authority_id = ga.granted_authority_id
       AND ga.archetype = 'act.patient*';

#
# Add act authorities, minus the authorities to make insurance claims, to Base Role
#
INSERT INTO roles_authorities (security_role_id, authority_id)
  SELECT
    r.security_role_id,
    g.granted_authority_id
  FROM security_roles r
    JOIN granted_authorities g
  WHERE r.name = 'Base Role'
        AND g.archetype IN ('actIdentity.*', 'act.patientAlert', 'act.patientClinical*', 'act.patientCustomerNote',
                            'act.patientDocument*', 'act.patientInsurancePolicy', 'act.patientInvestigation*',
                            'act.patientMedication', 'act.patientPrescription', 'act.patientReminder',
                            'act.patientReminderItem*', 'act.patientWeight')
        AND NOT exists(SELECT *
                       FROM roles_authorities x
                       WHERE x.security_role_id = r.security_role_id
                             AND x.authority_id = g.granted_authority_id);

#
# Create the Insurance Claims role
#
INSERT INTO security_roles (version, linkId, arch_short_name, arch_version, name, description, active)
  SELECT
    0,
    UUID(),
    'security.role',
    '1.0',
    'Insurance Claims',
    NULL,
    1
  FROM dual
  WHERE NOT exists(SELECT *
                   FROM security_roles
                   WHERE name = 'Insurance Claims');

#
# Add authorities for the Insurance Claims role.
#
INSERT INTO roles_authorities (security_role_id, authority_id)
  SELECT
    r.security_role_id,
    g.granted_authority_id
  FROM security_roles r
    JOIN granted_authorities g
  WHERE r.name = 'Insurance Claims'
        AND g.archetype IN ('act.patientInsuranceClaim', 'act.patientInsuranceClaimItem',
                            'act.patientInsuranceClaimAttachment')
        AND NOT exists(SELECT *
                       FROM roles_authorities x
                       WHERE x.security_role_id = r.security_role_id
                             AND x.authority_id = g.granted_authority_id);

DROP TABLE new_authorities;


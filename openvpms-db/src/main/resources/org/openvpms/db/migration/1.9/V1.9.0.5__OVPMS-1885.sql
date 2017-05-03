INSERT INTO entities (version, linkId, arch_short_name, arch_version, name, active)
  SELECT
    1,
    l.linkId,
    'entity.patientAlertType',
    '1.0',
    l.name,
    l.active
  FROM lookups l
  where l.arch_short_name = 'lookup.patientAlertType'
	and not exists (select *
                    from entities e
					where e.linkId = l.linkId and e.arch_short_name = 'entity.patientAlertType');


#
# Fix schedule start and end times, for OVPMS-1372
#

UPDATE entity_details d, entities e
SET d.value = concat("1970-01-01 ", substring(d.value, 12, 5), ":00"),
  d.type = "sql-timestamp"
WHERE substring(d.value, 1, 10) NOT IN ("1970-01-01", "1970-01-02")
      AND e.arch_short_name = "party.organisationSchedule"
      AND e.entity_id = d.entity_id
      AND d.name IN ("startTime", "endTime");
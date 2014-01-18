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

#
# Migrate party.organisationSchedule, party.organisationWorkList, for OVPMS-1424
#

insert into entity_details (entity_id, name, type, value)
select e.entity_id, "useAllWorkLists", "boolean", "true"
from entities e
where e.arch_short_name = "party.organisationSchedule"
      and not exists (select * from entity_details d where d.entity_id = e.entity_id and d.name ="useAllWorkLists");

insert into entity_details (entity_id, name, type, value)
select e.entity_id, "inputWeight", "boolean", "true"
from entities e
where e.arch_short_name = "party.organisationSchedule"
      and not exists (select * from entity_details d where d.entity_id = e.entity_id and d.name ="inputWeight");

insert into entity_details (entity_id, name, type, value)
select e.entity_id, "useAllTemplates", "boolean", "true"
from entities e
where e.arch_short_name = "party.organisationSchedule"
      and not exists (select * from entity_details d where d.entity_id = e.entity_id and d.name ="useAllTemplates");

insert into entity_details (entity_id, name, type, value)
select e.entity_id, "inputWeight", "boolean", "true"
from entities e
where e.arch_short_name = "party.organisationWorkList"
      and not exists (select * from entity_details d where d.entity_id = e.entity_id and d.name ="inputWeight");

insert into entity_details (entity_id, name, type, value)
select e.entity_id, "useAllTemplates", "boolean", "true"
from entities e
where e.arch_short_name = "party.organisationWorkList"
      and not exists (select * from entity_details d where d.entity_id = e.entity_id and d.name ="useAllTemplates");



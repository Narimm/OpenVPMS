# 
# Create customer alerts for ARCH-34
#

# create lookup.customerAlertType instances for each 
# lookup.customerNoteCategory that is associated with a note with alert="true"
# Default the priority to "HIGH" and the colour to "0xff0000" (red)

insert into lookups (version, linkId, arch_short_name, active, arch_version,
    code, name, description, default_lookup)
select distinct 0, UUID(), "lookup.customerAlertType", l.active, "1.0", l.code, l.name, l.description, l.default_lookup
from acts a
join act_details d on a.act_id = d.act_id and d.name="alert" and d.value="true"
join act_details c on a.act_id = c.act_id and c.name = "category"
join lookups l on l.arch_short_name = "lookup.customerNoteCategory"
where a.arch_short_name = "act.customerNote";

insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "priority", "string", "HIGH"
from lookups l
where l.arch_short_name = "lookup.customerAlertType"
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name = "priority");

insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "colour", "string", "0xff0000"
from lookups l
where l.arch_short_name = "lookup.customerAlertType"
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name = "colour");

# migrate all act.customerNote with alert="true" to act.customerAlert

update acts a, act_details d
set a.arch_short_name = "act.customerAlert"
where a.arch_short_name = "act.customerNote"
      and a.act_id = d.act_id
      and d.name = "alert" and d.value = "true";

update acts a, act_details d
set d.name = "alertType" 
where a.arch_short_name = "act.customerAlert"
      and a.act_id = d.act_id
      and d.name = "category";

update acts a, act_details d
set d.name = "notes"
where a.arch_short_name = "act.customerAlert"
      and a.act_id = d.act_id
      and d.name = "note";

# duplicate a portion of the notes to the reason field, truncating long notes
update acts a,act_details d
set a.reason = if (length(d.value) > 80, concat(left(d.value, 80), "..."), d.value)
where a.act_id = d.act_id
     and a.arch_short_name = "act.customerAlert"
     and d.name = "notes";

update acts a, participations p
set p.act_arch_short_name = "act.customerAlert"
where a.act_id = p.act_id and a.arch_short_name = "act.customerAlert" 
      and p.act_arch_short_name = "act.customerNote";

# set the act status to IN_PROGRESS.
update acts a
set a.status = "IN_PROGRESS"
where a.arch_short_name = "act.customerAlert";

# delete redundant alert flag
delete d
from acts a, act_details d
where a.arch_short_name in ("act.customerAlert", "act.customerNote")
      and a.act_id = d.act_id 
      and d.name = "alert";

# Rename lookup.alertType to lookup.patientAlertType and 
# default the priority to "HIGH" and the colour to "0xff0000" (red)

update lookups l 
set l.arch_short_name = "lookup.patientAlertType" 
where l.arch_short_name = "lookup.alertType";

insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "priority", "string", "HIGH"
from lookups l
where l.arch_short_name = "lookup.patientAlertType"
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name = "priority");

insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "colour", "string", "0xff0000"
from lookups l
where l.arch_short_name = "lookup.patientAlertType"
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name = "colour");

# remove lookup.alertType archetype

delete d
from assertion_descriptors d, node_descriptors n, archetype_descriptors a
where d.node_desc_id = n.node_desc_id 
      and n.archetype_desc_id = a.archetype_desc_id
      and a.name = "lookup.alertType.1.0";

delete a, n
from node_descriptors n, archetype_descriptors a
where n.archetype_desc_id = a.archetype_desc_id 
      and a.name = "lookup.alertType.1.0";


#
# Link customer account types to customer alerts for ARCH-35
#

# create a lookup.customerAlertType for each lookup.customerAccountType that has showAlert="true"
# ensuring that a lookup doesn't already exist

insert into lookups (version, linkId, arch_short_name, active, arch_version,
    code, name, description, default_lookup)
select 0, UUID(), "lookup.customerAlertType", l.active, "1.0", l.code, l.name, l.description, l.default_lookup
from lookups l
left join lookups e on e.arch_short_name = "lookup.customerAlertType" and e.code = l.code
join lookup_details d on l.lookup_id = d.lookup_id
where l.arch_short_name = "lookup.customerAccountType"
      and d.name = "showAlert"
      and d.value = "true"
      and e.code is null;

insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "priority", "string", "HIGH"
from lookups l
where l.arch_short_name = "lookup.customerAlertType"
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name ="priority");

insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "colour", "string", "0xff0000"
from lookups l
where l.arch_short_name = "lookup.customerAlertType"
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name ="colour");

# create a relationship between the account type and alert, if it doesn't already exist

insert into lookup_relationships (version, linkId, arch_short_name, arch_version, active, source_id, target_id)
select 0, UUID(), "lookupRelationship.customerAccountAlert", "1.0", 1, l.lookup_id, alertType.lookup_id
from lookups l
join lookup_details d on l.arch_short_name = "lookup.customerAccountType"
                      and l.lookup_id = d.lookup_id
                      and d.name = "showAlert"
                      and d.value = "true"
join lookups alertType on alertType.arch_short_name = "lookup.customerAlertType" and alertType.code = l.code
where not exists
  (select *
   from lookup_relationships r
   where r.source_id = l.lookup_id and r.target_id = alertType.lookup_id);

# delete redundant showAlert flag
delete d
from lookups l
join lookup_details d on l.arch_short_name = "lookup.customerAccountType" and d.name = "showAlert";


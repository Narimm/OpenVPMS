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

#
# Update lookup.uom for ESCI-1
#
drop table if exists unit_codes;
create table unit_codes (
    lookup_code varchar(10) not null,
    unit_code varchar(10) not null);

insert into unit_codes values
    ("UNIT", "EA"),
  	("BOTTLE", "BO"),
    ("TABLET", "U2"),
	("PACKET", "PA"),
	("VIAL","VI"),
	("BOX", "BX"),
	("GRAMS", "GRM"),
	("LITRES","LTR"),
	("AMPOULE", "AM"),
	("MLS", "MLT"),
	("TUBE","TU"),
	("KILOGRAM", "KGM"),
  ("POUNDS", "LBR");

insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "unitCode", "string", u.unit_code
from lookups l, unit_codes u
where l.code = u.lookup_code and l.arch_short_name = "lookup.uom"
      and not exists (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name ="unitCode");

drop table unit_codes;

#
# Update lookup.taxType for ESCI-1
#
insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "taxScheme", "string", "GST"
from lookups l
where l.arch_short_name = "lookup.taxType" and l.code= "GST" and not exists
      (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name = "taxScheme");

insert into lookup_details (lookup_id, name, type, value)
select l.lookup_id, "taxCategory", "string", "S"
from lookups l
where l.arch_short_name = "lookup.taxType" and l.code= "GST" and not exists
      (select * from lookup_details d where d.lookup_id = l.lookup_id and d.name = "taxCategory");

#
# Update of entity.documentTemplate for OVPMS-900
#
insert into entity_details (entity_id, name, type, value)
select e.entity_id, "printMode", "string", "CHECK_OUT"
from entities e
where e.arch_short_name = "entity.documentTemplate"
      and not exists (select * from entity_details d where d.entity_id = e.entity_id and d.name ="printMode");

#
# Update lookup.customerAccountType accountFeeAmount percentages for OVPMS-1028
#
update lookups l, lookup_details t, lookup_details p
set p.value = p.value * 100
where l.lookup_id = t.lookup_id and t.name ="accountFee" and t.value = "PERCENTAGE"
      and l.lookup_id = p.lookup_id and p.name = "accountFeeAmount" and p.value < 1
      and l.arch_short_name = "lookup.customerAccountType";

#
# Update act.customerNote for OVPMS-1108
#
update acts a
set a.status = "IN_PROGRESS"
where a.arch_short_name = "act.customerNote" and a.status = "POSTED";

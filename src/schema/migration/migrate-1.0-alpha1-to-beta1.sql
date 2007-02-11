#
# Script to modify the database schema and data from 1.0-alpha-1 to
# 1.0-beta-1
#
# NOTE: species classifications are removed as part of the migration
#       as these duplicate species lookups. Any associations between products
#       and species classifications are not migrated.

# remove mood, repeat_number columns from acts
alter table acts
drop column mood;

alter table acts
drop column repeat_number;


# remove code column from entities
alter table entities
drop column code;


# drop foreign key constraints referencing classifications table
alter table entity_classifications
drop foreign key FK76B55CF17751F7FA;

alter table contact_classifications
drop foreign key FK5AC8832E7751F7FA;

alter table product_price_classifications
drop foreign key FK9EC6BF077751F7FA;


# remove species classifications. These duplicate those in lookups
delete from classifications
where arch_namespace='openvpms' and arch_rm_name='common'
      and arch_entity_name='classification' and arch_concept_name='species';

# copy existing classifications into lookups table
insert into lookups (version, linkId, active, arch_namespace, arch_rm_name,
                     arch_concept_name, arch_entity_name, arch_version, code,
                     name, description, default_lookup, details)
    select version, linkId, active, arch_namespace, 'lookup' as arch_rm_name,
           arch_concept_name, 'lookup' as arch_entity_name, arch_version,
           replace(upper(name), ' ', '_') as code, name, description,
           false as default_lookup, details
    from classifications;


# add lookup_id columns
alter table entity_classifications
add column lookup_id bigint not null;

alter table contact_classifications
add column lookup_id bigint not null;

alter table product_price_classifications
add column lookup_id bigint not null;


# link entities to the their corresponding lookups
update entity_classifications ec
set lookup_id = (select l.lookup_id
                 from lookups l, classifications c
                 where ec.classification_id = c.classification_id
                       and c.linkId = l.linkId);


# link contacts to their corresponding lookups
update contact_classifications cc
set lookup_id = (select l.lookup_id
                 from lookups l, classifications c
                 where cc.classification_id = c.classification_id
                       and c.linkId = l.linkId);


# link product prices to their corresponding lookups
update product_price_classifications pp
set lookup_id = (select l.lookup_id
                 from lookups l, classifications c
                 where pp.classification_id = c.classification_id
                       and c.linkId = l.linkId);


# add foreign key constraints referencing the lookups table
alter table entity_classifications
add index FK76B55CF1CEABD10B (lookup_id),
add constraint FK76B55CF1CEABD10B
    foreign key (lookup_id) references lookups (lookup_id);

alter table contact_classifications
add index FK5AC8832ECEABD10B (lookup_id),
add constraint FK5AC8832ECEABD10B
    foreign key (lookup_id) references lookups (lookup_id);

alter table product_price_classifications
add index FK9EC6BF07CEABD10B (lookup_id),
add constraint FK9EC6BF07CEABD10B
    foreign key (lookup_id) references lookups (lookup_id);


# drop classification_id column
alter table entity_classifications
drop column classification_id;

alter table contact_classifications
drop column classification_id;

alter table product_price_classifications
drop column classification_id;


# drop classifications table
drop table classifications;

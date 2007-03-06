#
# Script to modify the database schema from 1.0-beta-1 to 1.0-beta-2
#

# add the allocated_amount column to financial_acts
alter table financial_acts
add column allocated_amount numeric(19,2);

update financial_acts
set allocated_amount = 0
where allocated_amount is null;
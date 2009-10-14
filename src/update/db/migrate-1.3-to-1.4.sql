# Remove any entity classifications linked to lookup.paymentType
delete from entity_classifications c 
using lookups l, entity_classifications c
where c.lookup_id = l.Lookup_id and arch_short_name = "lookup.paymentType";

# Remove lookup.paymentType entries
delete
from lookups
where arch_short_name = "lookup.paymentType";


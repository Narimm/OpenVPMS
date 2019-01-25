#
# OVPMS-2114 Pharmacy order discontinuation
#

UPDATE acts item
JOIN act_relationships r
  ON item.act_id = r.target_id
    AND r.arch_short_name = 'actRelationship.customerAccountInvoiceItem'
    AND item.arch_short_name = 'act.customerAccountInvoiceItem'
JOIN acts invoice
  ON r.source_id = invoice.act_id
    AND invoice.arch_short_name = 'act.customerAccountChargesInvoice'
JOIN act_details ordered
  ON ordered.act_id = item.act_id
    AND ordered.name = 'ordered'
    AND ordered.value = 'true'
SET item.status = CASE
    WHEN invoice.status = 'POSTED' THEN 'DISCONTINUED'
    ELSE 'ORDERED'
END;

DELETE ordered
FROM acts item
JOIN act_details ordered
  ON item.act_id = ordered.act_id
    AND item.arch_short_name = 'act.customerAccountInvoiceItem'
    AND ordered.name = 'ordered';

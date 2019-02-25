#
# OVPMS-2128 Replace Work List 'Create Flow Sheet' options with a checkbox to pre-select Create Flow Sheet in Patient
# Check In
#

UPDATE entities worklist
  JOIN entity_details create_flow_sheet
  ON worklist.entity_id = create_flow_sheet.entity_id
    AND create_flow_sheet.name = 'createFlowSheet'
SET create_flow_sheet.type  = 'boolean',
    create_flow_sheet.value = CASE
                                WHEN create_flow_sheet.value IN ('DEFAULT', 'PROMPT') THEN 'true'
                                ELSE 'false'
      END
WHERE worklist.arch_short_name = 'party.organisationWorkList';
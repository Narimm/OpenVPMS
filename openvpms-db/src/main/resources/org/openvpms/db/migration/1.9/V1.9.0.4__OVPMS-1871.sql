#
# Copy the Smart Flow Sheet API from the location to the practice.
# This assumes that there is only one key.
#

INSERT INTO entity_details (entity_id, type, value, name)
  SELECT *
  FROM
    (SELECT
       practice.entity_id,
       'string',
       apikey.value,
       'smartFlowSheetKey'
     FROM entities location
       JOIN entity_details apikey
         ON location.entity_id = apikey.entity_id
            AND location.arch_short_name = 'party.organisationLocation'
            AND apikey.name = 'smartFlowSheetKey'
            AND location.active = 1
       JOIN entities practice
         ON practice.arch_short_name = 'party.organisationPractice' AND practice.active = 1
     ORDER BY location.entity_id
     LIMIT 1) apikey
  WHERE NOT exists(SELECT *
                   FROM entities practice
                     JOIN entity_details practice_apikey
                       ON practice.entity_id = practice_apikey.entity_id
                          AND practice_apikey.name = 'smartFlowSheetKey'
                   WHERE practice.entity_id = apikey.entity_id);

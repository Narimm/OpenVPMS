#
# OVPMS-1568 Replace entity relationships to discounts with an entity link
#

DROP PROCEDURE IF EXISTS sp_replace_entity_relationship_with_link;

DELIMITER $$
CREATE PROCEDURE sp_replace_entity_relationship_with_link(IN relationship_type VARCHAR(255), IN link_type VARCHAR(255))
  BEGIN
    INSERT INTO entity_links (version, linkId, arch_short_name, arch_version, name, description, active_start_time,
                              active_end_time, sequence, source_id, target_id)
      SELECT
        version,
        linkId,
        link_type,
        '1.0',
        name,
        description,
        active_start_time,
        active_end_time,
        sequence,
        source_id,
        target_id
      FROM entity_relationships r
      WHERE r.arch_short_name = relationship_type
            AND NOT exists(SELECT *
                           FROM entity_links l
                           WHERE l.source_id = r.source_id
                                 AND l.target_id = r.target_id
                                 AND (l.active_start_time = r.active_start_time OR
                                      (l.active_start_time IS NULL AND l.active_start_time IS NULL))
                                 AND (l.active_end_time = r.active_end_time OR
                                      (l.active_end_time IS NULL AND l.active_end_time IS NULL))
                                 AND l.arch_short_name = link_type);

    #
    # Copy entity_relationship_details. These aren't used in OpenVPMS for discounts, but some users may have customised
    # them.
    #
    INSERT INTO entity_link_details (id, name, type, value)
      SELECT
        l.id,
        d.name,
        d.type,
        d.value
      FROM entity_relationships r
        JOIN entity_relationship_details d
          ON r.entity_relationship_id = d.entity_relationship_id
        JOIN entity_links l
          ON l.arch_short_name = link_type
             AND l.source_id = r.source_id AND l.target_id = r.target_id
             AND (l.active_start_time = r.active_start_time
                  OR (l.active_start_time IS NULL AND l.active_start_time IS NULL))
             AND (l.active_end_time = r.active_end_time
                  OR (l.active_end_time IS NULL AND l.active_end_time IS NULL))
      WHERE r.arch_short_name = relationship_type
            AND NOT exists(SELECT *
                           FROM entity_link_details ld
                           WHERE ld.id = l.id AND ld.name = d.name);

    # Remove the old relationships
    DELETE d
    FROM entity_relationship_details d
      JOIN entity_relationships r
        ON d.entity_relationship_id = r.entity_relationship_id
    WHERE r.arch_short_name = relationship_type;

    DELETE r
    FROM entity_relationships r
    WHERE r.arch_short_name = relationship_type;

  END $$
DELIMITER ;

CALL sp_replace_entity_relationship_with_link('entityRelationship.discountCustomer', 'entityLink.customerDiscount');
CALL sp_replace_entity_relationship_with_link('entityRelationship.discountPatient', 'entityLink.patientDiscount');
CALL sp_replace_entity_relationship_with_link('entityRelationship.discountProduct', 'entityLink.productDiscount');
CALL sp_replace_entity_relationship_with_link('entityRelationship.discountProductType',
                                              'entityLink.productTypeDiscount');
CALL sp_replace_entity_relationship_with_link('entityRelationship.discountType', 'entityLink.discountType');

DROP PROCEDURE sp_replace_entity_relationship_with_link;

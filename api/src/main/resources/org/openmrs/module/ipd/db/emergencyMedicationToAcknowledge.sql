SELECT DISTINCT
 COALESCE(
  (SELECT pri.identifier FROM patient_identifier pri
   INNER JOIN patient_identifier_type pit ON pri.identifier_type = pit.patient_identifier_type_id AND pit.retired = 0
   INNER JOIN global_property gp_ident ON gp_ident.property = 'bahmni.primaryIdentifierType' AND gp_ident.property_value = pit.uuid
   WHERE pri.patient_id = ma.patient_id AND pri.preferred = 1 AND pri.voided = 0 LIMIT 1),
  (SELECT pri2.identifier FROM patient_identifier pri2
   WHERE pri2.patient_id = ma.patient_id AND pri2.preferred = 1 AND pri2.voided = 0 LIMIT 1)
 ) AS identifier,
 CONCAT(pn.given_name, ' ', COALESCE(pn.family_name, '')) AS name,
 p.gender AS gender,
 p.uuid AS patient_uuid,
 p.birthdate AS date_of_birth,
 ma.uuid AS medication_administration_uuid,
 ma.administered_date_time AS administered_date_time,
 dg.name AS administered_drug_name,
 ma.dose AS administered_dose,
 cnd.name AS administered_dose_units,
 cnr.name AS administered_route,
 map.uuid AS medication_administration_performer_uuid,
 pr.uuid AS witness_provider_uuid,
 CONCAT_WS(' ', ppn.given_name, NULLIF(TRIM(ppn.family_name), '')) AS witness_provider_display,
 v.uuid AS visit_uuid
 FROM medication_administration_performer map
 INNER JOIN provider pr ON pr.provider_id = map.actor_id AND pr.retired = 0
 INNER JOIN person prov_person ON prov_person.person_id = pr.person_id AND prov_person.voided = 0
 LEFT JOIN person_name ppn ON prov_person.person_id = ppn.person_id AND ppn.voided = 0 AND ppn.preferred = 1
 INNER JOIN medication_administration ma ON map.medication_administration_id = ma.medication_administration_id AND ma.voided = 0
 INNER JOIN drug dg ON dg.drug_id = ma.drug_id AND dg.retired = 0
 INNER JOIN ipd_slot slot ON ma.medication_administration_id = slot.medication_administration_id AND slot.voided = 0
 INNER JOIN ipd_schedule schedule ON schedule.schedule_id = slot.schedule_id AND schedule.voided = 0
 INNER JOIN visit v ON v.visit_id = schedule.visit_id AND v.voided = 0
 INNER JOIN concept_name cn ON cn.concept_id = map.performer_function
  AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.locale = :locale AND cn.voided = 0
 LEFT JOIN concept_name cnd ON cnd.concept_id = ma.dose_units
  AND cnd.concept_name_type = 'FULLY_SPECIFIED' AND cnd.locale = :locale AND cnd.voided = 0
 LEFT JOIN concept_name cnr ON cnr.concept_id = ma.route
  AND cnr.concept_name_type = 'FULLY_SPECIFIED' AND cnr.locale = :locale AND cnr.voided = 0
 INNER JOIN person_name pn ON ma.patient_id = pn.person_id AND pn.voided = 0 AND pn.preferred = 1
 INNER JOIN person p ON p.person_id = ma.patient_id AND p.voided = 0
 WHERE cn.name = :witnessName
 AND pr.uuid = :providerUuid
 AND map.voided = 0{{LOCATION_FILTER}}

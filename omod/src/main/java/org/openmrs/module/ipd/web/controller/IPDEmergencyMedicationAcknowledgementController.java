package org.openmrs.module.ipd.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.dto.EmergencyMedicationToAcknowledgeDTO;
import org.openmrs.module.ipd.api.service.EmergencyMedicationAcknowledgementService;
import org.openmrs.module.ipd.web.contract.EmergencyMedicationToAcknowledgeRest;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

/**
 * REST equivalent of Bahmni:
 * {@code GET .../openmrs/ws/rest/v1/bahmnicore/sql?q=emrapi.sqlSearch.emergencyMedicationToAcknowledge&...}
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd")
@Slf4j
public class IPDEmergencyMedicationAcknowledgementController extends BaseRestController {

	private final EmergencyMedicationAcknowledgementService emergencyMedicationAcknowledgementService;

	@Autowired
	public IPDEmergencyMedicationAcknowledgementController(
			EmergencyMedicationAcknowledgementService emergencyMedicationAcknowledgementService) {
		this.emergencyMedicationAcknowledgementService = emergencyMedicationAcknowledgementService;
	}

	/**
	 * @param providerUuid  required (Bahmni: {@code provider_uuid})
	 * @param locationUuid optional (Bahmni: {@code location_uuid}); filters by visit or IPD slot location
	 */
	@RequestMapping(value = "/emergencyMedicationsToAcknowledge", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getEmergencyMedicationsToAcknowledge(
			@RequestParam(value = "providerUuid", required = false) String providerUuid,
			@RequestParam(value = "provider_uuid", required = false) String providerUuidSnake,
			@RequestParam(value = "locationUuid", required = false) String locationUuid,
			@RequestParam(value = "location_uuid", required = false) String locationUuidSnake) {
		try {
			if (!Context.getUserContext().hasPrivilege(PrivilegeConstants.GET_MEDICATION_ADMINISTRATION)) {
				return new ResponseEntity<>(
						RestUtil.wrapErrorResponse(new Exception(), "User doesn't have the following privilege: "
								+ PrivilegeConstants.GET_MEDICATION_ADMINISTRATION),
						FORBIDDEN);
			}
			String provider = providerUuid != null ? providerUuid : providerUuidSnake;
			String location = locationUuid != null ? locationUuid : locationUuidSnake;
			if (provider == null || provider.trim().isEmpty()) {
				return new ResponseEntity<>(RestUtil.wrapErrorResponse(new IllegalArgumentException("providerUuid is required"),
						"providerUuid (or provider_uuid) is required"), BAD_REQUEST);
			}
			List<EmergencyMedicationToAcknowledgeDTO> rows = emergencyMedicationAcknowledgementService
					.getEmergencyMedicationsToAcknowledge(provider.trim(), location == null ? null : location.trim());
			List<EmergencyMedicationToAcknowledgeRest> body = rows.stream().map(EmergencyMedicationToAcknowledgeRest::from)
					.collect(Collectors.toList());
			return new ResponseEntity<>(body, OK);
		} catch (Exception e) {
			log.error("Error loading emergency medications to acknowledge", e);
			return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
		}
	}
}

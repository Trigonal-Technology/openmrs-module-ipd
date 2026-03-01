package org.openmrs.module.ipd.web.postprocessor;

import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.ipd.api.events.IPDEventManager;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.module.ipd.web.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Intercepts OpenMRS encounter saves to trigger IPD-specific logic.
 *
 * <p>
 * <b>Replacement for Bahmni emrapi {@code EncounterTransactionHandler}:</b>
 * The original class implemented
 * {@code org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler},
 * which caused a hard dependency on the emrapi module. This class does NOT
 * implement that
 * interface. Instead it is called directly from
 * {@link IPDEncounterServiceAdvice} — a Spring AOP {@code AfterReturning}
 * advice on
 * {@link EncounterService#saveEncounter(Encounter)}.
 *
 * <p>
 * The logic inside the two trigger methods is identical to the original; only
 * the
 * invocation mechanism has changed.
 */
@Component
public class IPDTransactionHandler {

    private final IPDScheduleService ipdScheduleService;
    private final IPDEventManager eventManager;

    @Autowired
    public IPDTransactionHandler(IPDScheduleService ipdScheduleService, IPDEventManager eventManager) {
        this.ipdScheduleService = ipdScheduleService;
        this.eventManager = eventManager;
    }

    /**
     * Called after an encounter is persisted (read path — no action needed for
     * IPD).
     *
     * @param encounter the read encounter
     */
    public void afterRead(Encounter encounter) {
        // No implementation needed for the read path
    }

    /**
     * Called after an encounter is saved. Triggers IPD event processing and
     * drug-order stop logic.
     *
     * <p>
     * Replaces the original emrapi {@code forSave(Encounter, EncounterTransaction)}
     * callback.
     * Drug-order stop logic now reads stopped orders directly from
     * {@code encounter.getOrders()} via
     * {@link IPDScheduleService#handleDrugOrderStop(Encounter)}.
     *
     * @param encounter the just-saved OpenMRS Encounter
     */
    public void afterSave(Encounter encounter) {
        if (encounter.getEncounterType() != null) {
            IPDEventType eventType = eventManager.getEventTypeForEncounter(
                    encounter.getEncounterType().getName());
            if (eventType != null) {
                IPDEvent ipdEvent = new IPDEvent(
                        encounter.getUuid(),
                        encounter.getPatient().getUuid(),
                        eventType);
                eventManager.processEvent(ipdEvent);
            }
        }
        ipdScheduleService.handleDrugOrderStop(encounter);
    }
}

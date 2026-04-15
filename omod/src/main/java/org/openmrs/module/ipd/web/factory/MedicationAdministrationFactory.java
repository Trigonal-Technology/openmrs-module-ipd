package org.openmrs.module.ipd.web.factory;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.MedicationAdministrationNote;
import org.openmrs.module.ipd.api.model.MedicationAdministrationPerformer;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationPerformerRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class MedicationAdministrationFactory {

    public MedicationAdministration mapRequestToMedicationAdministration(MedicationAdministrationRequest request,
            MedicationAdministration existingMedicationAdministration) {

        final MedicationAdministration medicationAdministration;
        if (existingMedicationAdministration == null || existingMedicationAdministration.getId() == null) {
            medicationAdministration = new MedicationAdministration();
            medicationAdministration.setUuid(uuidOrGenerate(request.getUuid()));
            medicationAdministration.setAdministeredDateTime(request.getAdministeredDateTimeAsLocaltime());
            MedicationAdministration.MedicationAdministrationStatus status = MedicationAdministration.MedicationAdministrationStatus
                    .fromCode(request.getStatus());
            medicationAdministration.setStatus(status != null ? status
                    : MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
            medicationAdministration.setPatient(Context.getPatientService().getPatientByUuid(request.getPatientUuid()));
            medicationAdministration
                    .setEncounter(Context.getEncounterService().getEncounterByUuid(request.getEncounterUuid()));
            medicationAdministration
                    .setDrugOrder((DrugOrder) Context.getOrderService().getOrderByUuid(request.getOrderUuid()));
            medicationAdministration.setDrug(Context.getConceptService().getDrugByUuid(request.getDrugUuid()));
            medicationAdministration.setDosingInstructions(request.getDosingInstructions());
            medicationAdministration.setDose(request.getDose());
            medicationAdministration.setDoseUnits(Context.getConceptService().getConceptByUuid(request.getDoseUnits()));
            medicationAdministration.setRoute(Context.getConceptService().getConceptByUuid(request.getRoute()));
            medicationAdministration.setSite(Context.getConceptService().getConceptByUuid(request.getSite()));
        } else {
            // PATCH onto persistent row — sparse new entity had null id → Hibernate INSERT + null status blow-up.
            medicationAdministration = existingMedicationAdministration;
            if (StringUtils.isNotBlank(request.getStatus())) {
                MedicationAdministration.MedicationAdministrationStatus st = MedicationAdministration.MedicationAdministrationStatus
                        .fromCode(request.getStatus());
                if (st != null) {
                    medicationAdministration.setStatus(st);
                }
            }
            if (request.getAdministeredDateTime() != null) {
                medicationAdministration.setAdministeredDateTime(
                        new Date(TimeUnit.SECONDS.toMillis(request.getAdministeredDateTime())));
            }
        }
        List<MedicationAdministrationPerformer> providers = new ArrayList<>();
        if (request.getProviders() != null) {
            for (MedicationAdministrationPerformerRequest performer : request.getProviders()) {
                MedicationAdministrationPerformer newProvider = new MedicationAdministrationPerformer();
                newProvider.setUuid(uuidOrGenerate(performer.getUuid()));
                newProvider.setActor(Context.getProviderService().getProviderByUuid(performer.getProviderUuid()));
                newProvider.setFunction(
                        resolvePerformerFunctionConcept(Context.getConceptService(), performer.getFunction()));
                providers.add(newProvider);
            }
            if (existingMedicationAdministration != null && existingMedicationAdministration.getPerformers() != null) {
                providers.addAll(existingMedicationAdministration.getPerformers());
            }
        }
        medicationAdministration.setPerformers(new HashSet<>(providers));
        List<MedicationAdministrationNote> notes = new ArrayList<>();
        if (request.getNotes() != null) {
            for (MedicationAdministrationNoteRequest note : request.getNotes()) {
                MedicationAdministrationNote newNote = new MedicationAdministrationNote();
                newNote.setUuid(uuidOrGenerate(note.getUuid()));
                newNote.setAuthor(Context.getProviderService().getProviderByUuid(note.getAuthorUuid()));
                newNote.setText(note.getText());
                newNote.setRecordedTime(note.getRecordedTimeAsLocaltime());
                notes.add(newNote);
            }
            if (existingMedicationAdministration != null && existingMedicationAdministration.getNotes() != null) {
                notes.addAll(existingMedicationAdministration.getNotes());
            }
        }
        medicationAdministration.setNotes(new HashSet<>(notes));
        return medicationAdministration;
    }

    public MedicationAdministrationResponse mapMedicationAdministrationToResponse(
            MedicationAdministration medicationAdministration) {
        return MedicationAdministrationResponse.createFrom(medicationAdministration);
    }

    /**
     * Entities use {@code not-null} {@code uuid} columns; clients may omit uuid on create for the parent
     * {@link MedicationAdministration} and for nested notes/performers.
     */
    private static String uuidOrGenerate(String requested) {
        return StringUtils.isNotBlank(requested) ? requested : UUID.randomUUID().toString();
    }

    /**
     * Request {@code function} may be concept UUID (FHIR) or dictionary name e.g. {@code Performer}, {@code Witness}.
     */
    static Concept resolvePerformerFunctionConcept(ConceptService conceptService, String functionRef) {
        if (StringUtils.isBlank(functionRef)) {
            return null;
        }
        String trimmed = functionRef.trim();
        Concept byUuid = conceptService.getConceptByUuid(trimmed);
        if (byUuid != null) {
            return byUuid;
        }
        return conceptService.getConceptByName(trimmed);
    }

}

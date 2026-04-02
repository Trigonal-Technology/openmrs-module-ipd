package org.openmrs.module.ipd.web.util;

import org.openmrs.annotation.AddOnStartup;

public class PrivilegeConstants {

    @AddOnStartup(description = "Edit Medication Tasks description")
    public static final String EDIT_MEDICATION_TASKS = "Edit Medication Tasks";
    @AddOnStartup(description = "Delete Medication Tasks description")
    public static final String DELETE_MEDICATION_TASKS = "Delete Medication Tasks";
    @AddOnStartup(description = "Edit adhoc medication tasks description")
    public static final String EDIT_ADHOC_MEDICATION_TASKS = "Edit adhoc medication tasks";
    @AddOnStartup(description = "Edit Medication Administration description")
    public static final String EDIT_MEDICATION_ADMINISTRATION = "Edit Medication Administration";
    @AddOnStartup(description = "Get Medication Administration description")
    public static final String GET_MEDICATION_ADMINISTRATION = "Get Medication Administration";
    @AddOnStartup(description = "Get Medication Tasks description")
    public static final String GET_MEDICATION_TASKS = "Get Medication Tasks";

    @AddOnStartup(description = "Get IPD nursing/non-medication tasks")
    public static final String GET_TASKS = "Get Tasks";
    @AddOnStartup(description = "Create IPD nursing/non-medication tasks")
    public static final String ADD_TASKS = "Add Tasks";
    @AddOnStartup(description = "Edit IPD nursing/non-medication tasks")
    public static final String EDIT_TASKS = "Edit Tasks";
}

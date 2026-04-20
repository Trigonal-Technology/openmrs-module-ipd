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

    // Nursing Task Management Privileges
    @AddOnStartup(description = "Manage nursing task templates")
    public static final String MANAGE_TASK_TEMPLATES = "Manage Task Templates";
    @AddOnStartup(description = "Get nursing task templates")
    public static final String GET_TASK_TEMPLATES = "Get Task Templates";
    @AddOnStartup(description = "Apply task templates to patients")
    public static final String APPLY_TASK_TEMPLATES = "Apply Task Templates";
    @AddOnStartup(description = "Manage nursing task instances")
    public static final String MANAGE_TASK_INSTANCES = "Manage Task Instances";
    @AddOnStartup(description = "Get nursing task instances")
    public static final String GET_TASK_INSTANCES = "Get Task Instances";
    @AddOnStartup(description = "Complete nursing tasks")
    public static final String COMPLETE_TASKS = "Complete Tasks";
    @AddOnStartup(description = "Acknowledge tasks as doctor")
    public static final String ACKNOWLEDGE_TASKS = "Acknowledge Tasks";
    @AddOnStartup(description = "Manage task cleanup and archival")
    public static final String MANAGE_TASK_CLEANUP = "Manage Task Cleanup";
    @AddOnStartup(description = "View task reports")
    public static final String VIEW_TASK_REPORTS = "View Task Reports";
}

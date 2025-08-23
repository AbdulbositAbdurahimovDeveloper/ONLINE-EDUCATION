package uz.pdp.online_education.repository;

import uz.pdp.online_education.model.Module;

/**
 * Projection for {@link uz.pdp.online_education.model.ModuleEnrollment}
 */
public interface ModuleEnrollmentInfo {
    Module getModule();
}
package uz.pdp.online_education.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.pdp.online_education.payload.UserModuleStatusDTO;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ModuleRepositoryImpl implements ModuleRepositoryCustom {
    private final EntityManager entityManager;

    @Override
    public List<UserModuleStatusDTO> findUserModuleStatusesInCourse(Long userId, Long courseId) {
        // JPQLda Entity nomlari ishlatiladi, jadval nomlari emas.
        // Payment -> @Entity(name="payment")
        // ModuleEnrollment -> @Entity(name="module_enrollments")
        String jpql = "SELECT new uz.pdp.online_education.payload.UserModuleStatusDTO(" +
                "m.id, " +
                "(SELECT COUNT(me.id) > 0 FROM module_enrollments me WHERE me.user.id = :userId AND me.module.id = m.id), " +
                // "Payment" - bu klass nomi. "payments" emas.
                "(SELECT COUNT(p.id) > 0 FROM payment p WHERE p.user.id = :userId AND p.module.id = m.id AND p.status = 'SUCCESS')" +
                ") " +
                "FROM modules m WHERE m.course.id = :courseId";

        TypedQuery<UserModuleStatusDTO> query = entityManager.createQuery(jpql, UserModuleStatusDTO.class);
        query.setParameter("userId", userId);
        query.setParameter("courseId", courseId);
        return query.getResultList();
    }
}
package uz.pdp.online_education.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.payload.FilterDTO;
import uz.pdp.online_education.payload.course.CourseWithRatingDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<CourseWithRatingDTO> filterWithCriteria(FilterDTO filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 1. Asosiy so'rovni (ma'lumotlarni olish uchun) qurish
        CriteriaQuery<CourseWithRatingDTO> query = cb.createQuery(CourseWithRatingDTO.class);
        Root<Course> root = query.from(Course.class);

        Join<Course, Review> reviewJoin = root.join("reviews", JoinType.LEFT);
        Expression<Double> avgRating = cb.coalesce(cb.avg(reviewJoin.get("rating")), 0.0);

        Predicate finalPredicate = buildWhereClause(filter, cb, root, query);

        query.select(cb.construct(CourseWithRatingDTO.class, root, avgRating))
                .where(finalPredicate)
                .groupBy(root);

        if (filter.getReview() != null) {
            query.having(cb.le(avgRating, filter.getReview()));
        }

        applySorting(pageable, query, cb, root, avgRating);

        // Asosiy so'rovni bajarish
        TypedQuery<CourseWithRatingDTO> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<CourseWithRatingDTO> resultList = typedQuery.getResultList();

        // 2. Jami sonni to'g'ri hisoblash
        Long total = getTotalCount(filter, cb);

        return new PageImpl<>(resultList, pageable, total);
    }

    // Yordamchi metod: WHERE shartlarini qurish uchun (o'zgarishsiz)
    private Predicate buildWhereClause(FilterDTO filter, CriteriaBuilder cb, Root<Course> root, CriteriaQuery<?> query) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(filter.getSearch()) || (filter.getInstructorName() != null && !filter.getInstructorName().isEmpty())) {
            Join<Course, User> instructorJoin = root.join("instructor", JoinType.LEFT);
            Join<User, UserProfile> profileJoin = instructorJoin.join("profile", JoinType.LEFT);

            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get(Course.Fields.title)), searchPattern),
                        cb.like(cb.lower(root.get(Course.Fields.description)), searchPattern)
                ));
            }
            if (filter.getInstructorName() != null && !filter.getInstructorName().isEmpty()) {
                predicates.add(cb.or(
                        instructorJoin.get(User.Fields.username).in(filter.getInstructorName()),
                        profileJoin.get(UserProfile.Fields.firstName).in(filter.getInstructorName()),
                        profileJoin.get(UserProfile.Fields.lastName).in(filter.getInstructorName())
                ));
            }
        }

        if (filter.getCategoryTitle() != null && !filter.getCategoryTitle().isEmpty()) {
            predicates.add(root.get(Course.Fields.category).get(Category.Fields.name).in(filter.getCategoryTitle()));
        }

        if (filter.getFromPrice() != null || filter.getToPrice() != null) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Module> subRoot = subquery.from(Module.class);
            List<Predicate> subQueryPredicates = new ArrayList<>();
            subQueryPredicates.add(cb.equal(subRoot.get(Module.Fields.course), root));
            if (filter.getFromPrice() != null) {
                subQueryPredicates.add(cb.ge(subRoot.get(Module.Fields.price), (long)(filter.getFromPrice() * 100)));
            }
            if (filter.getToPrice() != null) {
                subQueryPredicates.add(cb.le(subRoot.get(Module.Fields.price), (long)(filter.getToPrice() * 100)));
            }
            subquery.select(cb.literal(1L)).where(cb.and(subQueryPredicates.toArray(new Predicate[0])));
            predicates.add(cb.exists(subquery));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    // Jami sonni hisoblash uchun YAKUNIY va TO'G'RI metod
    private Long getTotalCount(FilterDTO filter, CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Course> root = countQuery.from(Course.class);

        Predicate whereClause = buildWhereClause(filter, cb, root, countQuery);

        if (filter.getReview() != null) {
            // Agar reyting bo'yicha filtr bo'lsa, count hisoblash murakkablashadi.
            // Biz filtrga mos keladigan kurslarning ID'larini sanashimiz kerak.
            CriteriaQuery<Long> idQuery = cb.createQuery(Long.class);
            Root<Course> idRoot = idQuery.from(Course.class);

            // Barcha kerakli JOIN va Predicate'larni qaytadan qurish kerak
            Predicate idWhereClause = buildWhereClause(filter, cb, idRoot, idQuery);
            Join<Course, Review> reviewJoin = idRoot.join("reviews", JoinType.LEFT);

            idQuery.select(idRoot.get("id"))
                    .where(idWhereClause)
                    .groupBy(idRoot)
                    .having(cb.le(cb.coalesce(cb.avg(reviewJoin.get("rating")), 0.0), filter.getReview()));

            List<Long> ids = entityManager.createQuery(idQuery).getResultList();
            return (long) ids.size();

        } else {
            // Agar reyting filtri bo'lmasa, oddiy count ishlaydi
            countQuery.select(cb.count(root)).where(whereClause);
            return entityManager.createQuery(countQuery).getSingleResult();
        }
    }

    // Saralashni qo'llash uchun yordamchi metod
    private void applySorting(Pageable pageable, CriteriaQuery<CourseWithRatingDTO> query, CriteriaBuilder cb, Root<Course> root, Expression<Double> avgRating) {
        // Har doim reyting bo'yicha kamayish tartibida saralaymiz
        query.orderBy(
                cb.desc(avgRating),
                cb.asc(root.get(Course.Fields.title))
        );
    }
}
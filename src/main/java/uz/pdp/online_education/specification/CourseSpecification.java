package uz.pdp.online_education.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.payload.FilterDTO;

import java.util.ArrayList;
import java.util.List;

public class CourseSpecification {

    /**
     * Barcha filtrlash va saralash mantiqini o'zida jamlagan asosiy metod.
     */
    public static Specification<Course> filterAndPotentiallySort(FilterDTO filterDTO) {
        return (root, query, cb) -> {


            boolean needsReviewLogic = true;

            Join<Course, Review> reviewJoin = null;

            reviewJoin = root.join(Course.Fields.reviews, JoinType.LEFT);

            List<Predicate> predicates = createPredicates(filterDTO, root, query, cb, reviewJoin);

            query.groupBy(root.get(AbsLongEntity.Fields.id));


            if (filterDTO.getReview() == null) {
                Expression<Double> avgRating = cb.avg(reviewJoin.get(Review.Fields.rating));
                Expression<Double> coalesceRating = cb.coalesce(avgRating, 0.0);

                Order ratingOrder = cb.desc(coalesceRating);
                Order titleOrder = cb.asc(root.get(Course.Fields.title));
                query.orderBy(ratingOrder, titleOrder);
            } else {

                query.orderBy(cb.asc(root.get(Course.Fields.title)));
            }


            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Barcha filtrlash shartlarini (Predicate) yaratadigan yordamchi metod.
     */
    private static List<Predicate> createPredicates(FilterDTO filterDTO, Root<Course> root,
                                                    AbstractQuery<?> query, CriteriaBuilder cb,
                                                    Join<Course, Review> reviewJoin) {
        List<Predicate> predicates = new ArrayList<>();


        if (filterDTO.getSearch() != null && !filterDTO.getSearch().isEmpty()) {
            String searchTerm = "%" + filterDTO.getSearch().toLowerCase() + "%";
            Predicate titleLike = cb.like(cb.lower(root.get(Course.Fields.title)), searchTerm);
            Predicate descriptionLike = cb.like(cb.lower(root.get(Course.Fields.description)), searchTerm);
            predicates.add(cb.or(titleLike, descriptionLike));
        }


        if (filterDTO.getCategoryTitle() != null && !filterDTO.getCategoryTitle().isEmpty()) {
            predicates.add(root.get(Course.Fields.category).get(Category.Fields.name).in(filterDTO.getCategoryTitle()));
        }


        if (filterDTO.getInstructorName() != null && !filterDTO.getInstructorName().isEmpty()) {

            predicates.add(root.get(Course.Fields.instructor).get(User.Fields.profile).get(UserProfile.Fields.firstName).in(filterDTO.getInstructorName()));
        }


        if (filterDTO.getFromPrice() != null || filterDTO.getToPrice() != null) {

            Subquery<Module> moduleSubquery = query.subquery(Module.class);
            Root<Module> moduleRoot = moduleSubquery.from(Module.class);

            List<Predicate> subqueryPredicates = new ArrayList<>();

            subqueryPredicates.add(cb.equal(moduleRoot.get(Module.Fields.course), root));


            if (filterDTO.getFromPrice() != null) {
                Long fromPriceInTiyin = convertSomToTiyin(filterDTO.getFromPrice());
                subqueryPredicates.add(cb.greaterThanOrEqualTo(moduleRoot.get(Module.Fields.price), fromPriceInTiyin));
            }
            if (filterDTO.getToPrice() != null) {
                Long toPriceInTiyin = convertSomToTiyin(filterDTO.getToPrice());
                subqueryPredicates.add(cb.lessThanOrEqualTo(moduleRoot.get(Module.Fields.price), toPriceInTiyin));
            }

            moduleSubquery.select(moduleRoot).where(cb.and(subqueryPredicates.toArray(new Predicate[0])));

            predicates.add(cb.exists(moduleSubquery));
        }


        if (filterDTO.getReview() != null && reviewJoin != null) {
            Expression<Double> avgRating = cb.avg(reviewJoin.get(Review.Fields.rating));
            predicates.add(cb.greaterThanOrEqualTo(cb.coalesce(avgRating, 0.0), filterDTO.getReview().doubleValue()));
        }

        return predicates;
    }

    /**
     * Foydalanuvchi kiritgan so'mni (Double) bazadagi tiyinga (Long) o'giradi.
     */
    private static Long convertSomToTiyin(Double somValue) {
        if (somValue == null) {
            return null;
        }

        return (long) (somValue * 100);
    }
}
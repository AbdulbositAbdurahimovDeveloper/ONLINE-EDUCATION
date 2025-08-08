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

            // -- 1. Kerakli shartlarni va JOIN'larni aniqlash --
            // Reyting bilan bog'liq logika (filtr yoki saralash) kerakmi?
            boolean needsReviewLogic = true;

            Join<Course, Review> reviewJoin = null;
            // Har doim LEFT JOIN ishlatamiz, chunki sharhi yo'q kurslar ham chiqishi kerak.
            reviewJoin = root.join(Course.Fields.reviews, JoinType.LEFT);

            // -- 2. Filtrlash shartlarini (Predicates) qurish --
            // Narx filtri uchun subquery ishlatilgani uchun, bu metodga 'query' obyektini ham beramiz.
            List<Predicate> predicates = createPredicates(filterDTO, root, query, cb, reviewJoin);

            // -- 3. Agregat funksiya (AVG) ishlatilgan bo'lsa, GROUP BY shart --
            query.groupBy(root.get(AbsLongEntity.Fields.id));

            // -- 4. Saralash mantiqini qo'llash --
            // HOLAT B: Foydalanuvchi reytingni tanlamagan bo'lsa, reyting bo'yicha saralaymiz.
            if (filterDTO.getReview() == null) {
                Expression<Double> avgRating = cb.avg(reviewJoin.get(Review.Fields.rating));
                Expression<Double> coalesceRating = cb.coalesce(avgRating, 0.0);

                Order ratingOrder = cb.desc(coalesceRating);
                Order titleOrder = cb.asc(root.get(Course.Fields.title));
                query.orderBy(ratingOrder, titleOrder);
            } else {
                // HOLAT A: Foydalanuvchi reytingni tanlagan. Standart saralash (masalan, sarlavha bo'yicha).
                query.orderBy(cb.asc(root.get(Course.Fields.title)));
            }

            // Takrorlanuvchi natijalarni olib tashlash uchun (ayniqsa JOIN'lardan keyin muhim).
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

        // 1. Search (title va description bo'yicha)
        if (filterDTO.getSearch() != null && !filterDTO.getSearch().isEmpty()) {
            String searchTerm = "%" + filterDTO.getSearch().toLowerCase() + "%";
            Predicate titleLike = cb.like(cb.lower(root.get(Course.Fields.title)), searchTerm);
            Predicate descriptionLike = cb.like(cb.lower(root.get(Course.Fields.description)), searchTerm);
            predicates.add(cb.or(titleLike, descriptionLike));
        }

        // 2. Kategoriya bo'yicha
        if (filterDTO.getCategoryTitle() != null && !filterDTO.getCategoryTitle().isEmpty()) {
            predicates.add(root.get(Course.Fields.category).get(Category.Fields.name).in(filterDTO.getCategoryTitle()));
        }

        // 3. Instruktor bo'yicha (User -> UserProfile -> firstName)
        if (filterDTO.getInstructorName() != null && !filterDTO.getInstructorName().isEmpty()) {
            // Sizning kodingizdagi path: Course -> instructor(User) -> profile(UserProfile) -> firstName
            predicates.add(root.get(Course.Fields.instructor).get(User.Fields.profile).get(UserProfile.Fields.firstName).in(filterDTO.getInstructorName()));
        }

        // 4. Narx diapazoni bo'yicha (har bir modul alohida tekshiriladi)
        if (filterDTO.getFromPrice() != null || filterDTO.getToPrice() != null) {
            // Ichki so'rov (subquery) yordamida "kamida bitta shunday modul mavjudmi" deb tekshiramiz.
            Subquery<Module> moduleSubquery = query.subquery(Module.class);
            Root<Module> moduleRoot = moduleSubquery.from(Module.class);

            List<Predicate> subqueryPredicates = new ArrayList<>();
            // Ichki va tashqi so'rovlarni bog'laymiz
            subqueryPredicates.add(cb.equal(moduleRoot.get(Module.Fields.course), root));

            // Narx diapazonini tekshiramiz (tiyinga o'girib)
            if (filterDTO.getFromPrice() != null) {
                Long fromPriceInTiyin = convertSomToTiyin(filterDTO.getFromPrice());
                subqueryPredicates.add(cb.greaterThanOrEqualTo(moduleRoot.get(Module.Fields.price), fromPriceInTiyin));
            }
            if (filterDTO.getToPrice() != null) {
                Long toPriceInTiyin = convertSomToTiyin(filterDTO.getToPrice());
                subqueryPredicates.add(cb.lessThanOrEqualTo(moduleRoot.get(Module.Fields.price), toPriceInTiyin));
            }

            moduleSubquery.select(moduleRoot).where(cb.and(subqueryPredicates.toArray(new Predicate[0])));

            // Asosiy so'rovga EXISTS shartini qo'shamiz
            predicates.add(cb.exists(moduleSubquery));
        }

        // 5. Reyting bo'yicha filtr
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
        // 1 so'm = 100 tiyin
        return (long) (somValue * 100);
    }
}
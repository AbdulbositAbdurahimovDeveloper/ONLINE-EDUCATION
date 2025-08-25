package uz.pdp.online_education.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.Review;
import uz.pdp.online_education.payload.projection.CourseReviewProjection;
import uz.pdp.online_education.payload.projection.ReviewStatsProjection;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByCourseIdAndUserId(Long courseId, Long userId);

    // ReviewRepository.java

    @Query(
            value = """
                        SELECT
                            AVG(r.rating) AS averageRating,
                            COUNT(r.rating) AS totalReviews,
                            COUNT(r.id) AS totalComments,
                            COUNT(DISTINCT r.user_id) AS activeStudents
                        FROM
                            reviews r
                        INNER JOIN
                            courses c ON r.course_id = c.id
                        WHERE
                            c.instructor_id = :mentorId
                    """,
            nativeQuery = true
    )
    ReviewStatsProjection getOverallReviewStatsByMentorId(@Param("mentorId") Long mentorId);

    @Query(
            value = """
                        SELECT
                            u.id AS studentId,
                            r.course_id AS courseId,
                            COALESCE(up.first_name || ' ' || up.last_name, 'Anonim Foydalanuvchi') AS studentName,
                            r.rating AS rating,
                            r.comment AS comment,
                            r.created_at AS createdAt
                        FROM
                            reviews r
                        INNER JOIN
                            users u ON r.user_id = u.id
                        LEFT JOIN
                            user_profiles up ON u.id = up.user_id
                        WHERE
                            r.course_id = :courseId
                    """,
            countQuery = """
                        SELECT COUNT(r.id)
                        FROM reviews r
                        WHERE r.course_id = :courseId
                    """,
            nativeQuery = true
    )
    Page<CourseReviewProjection> findReviewsByCourseId(
            @Param("courseId") Long courseId,
            Pageable pageable
    );
}

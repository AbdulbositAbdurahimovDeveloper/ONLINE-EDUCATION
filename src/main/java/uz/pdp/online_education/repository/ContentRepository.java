package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.online_education.model.lesson.Content;
import uz.pdp.online_education.model.lesson.Lesson;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Modifying
    @Query("UPDATE Content c SET c.blockOrder = c.blockOrder - 1 WHERE c.lesson.id = :lessonId AND c.blockOrder > :blockOrder")
    void shiftBlockOrdersAfterDelete(Long lessonId, int blockOrder);

    List<Content> findAllByLessonIdOrderByBlockOrderAsc(Long lessonId);

    List<Content> findAllByLessonId(Long lessonId);


    /**
     * Berilgan ID'lar ro'yxati asosida bir nechta kontentning 'blockOrder'ini
     * bitta so'rovda yangilaydi. Bu PostgreSQL uchun maxsus so'rov.
     * @param orderedContentIds Tartiblangan kontent ID'lari
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE contents c SET
            block_order = new_order.ordering
        FROM (
            SELECT
                CAST(d.id AS BIGINT) as id,
                CAST(d.ordering AS INTEGER) as ordering
            FROM
                (VALUES :orderedContentIds) AS d (id, ordering)
        ) AS new_order
        WHERE c.id = new_order.id
    """, nativeQuery = true)
    void updateAllOrdersInBatch(@Param("orderedContentIds") List<Object[]> orderedContentIds);
}
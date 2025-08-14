package uz.pdp.online_education.service.interfaces;

public interface PurchaseService {

    /**
     * Foydalanuvchining ma'lum bir darsni o'z ichiga olgan modul uchun to'lov qilganini tekshiradi.
     * @param userId Foydalanuvchi ID si.
     * @param lessonId Dars ID si.
     * @return Agar modul sotib olingan bo'lsa `true`, aks holda `false`.
     */
    boolean hasLessonAccess(Long userId, Long lessonId);

    /**
     * Foydalanuvchining ma'lum bir kurs modullarining yetarli qismini sotib olganini tekshiradi.
     * @param userId Foydalanuvchi ID si.
     * @param courseId Kurs ID si.
     * @param requiredPercentage Talab qilingan foiz (masalan, 0.20 -> 20%).
     * @return Agar yetarlicha qismi sotib olingan bo'lsa `true`, aks holda `false`.
     */
    boolean hasSufficientCourseAccess(Long userId, Long courseId, double requiredPercentage);
}
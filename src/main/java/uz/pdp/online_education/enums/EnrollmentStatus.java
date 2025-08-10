package uz.pdp.online_education.enums;

public enum EnrollmentStatus {
    /**
     * Foydalanuvchi modulga yozildi, lekin hali to'lov qilmagan.
     * U faqat bepul darslarni ko'ra oladi.
     */
    PENDING_PAYMENT,

    /**
     * Foydalanuvchi to'lovni muvaffaqiyatli amalga oshirdi.
     * Barcha darslarga kirish huquqi bor.
     */
    ACTIVE,

    /**
     * Kurs tugallandi.
     */
    COMPLETED
}
package uz.pdp.online_education.telegram.enums;

public enum UserState {

    DEFAULT,
    AUTHENTICATED,
    UNREGISTERED,
    DASHBOARD,

    // --- ADMIN PANEL STATES ---
    ADMIN_MAIN_MENU,                // Asosiy admin menyusida (boshlang'ich holat)
    ADMIN_MANAGING_USERS,           // "Foydalanuvchilar" bo'limida
    ADMIN_MANAGING_COURSES,         // "Kurslar" bo'limida
    ADMIN_PREPARING_BROADCAST,      // "Xabar Yuborish" jarayonida
    ADMIN_VIEWING_STATISTICS,       // "Statistika" bo'limida (hozircha shart emas, lekin bo'lishi mumkin)

    STUDENT_MAIN_MENU,              // Student asosiy menyuda
    STUDENT_VIEWING_MY_COURSES,     // "Mening Kurslarim" ro'yxatini ko'ryapti
    STUDENT_VIEWING_ALL_COURSES,    // "Barcha Kurslar" ro'yxatini ko'ryapti
    STUDENT_MANAGING_BALANCE,       // "Balans va To'lovlar" bo'limida
    STUDENT_AWAITING_SUPPORT_MESSAGE, // "Yordam" uchun xabar kutilyapti

    AWAITING_BROADCAST_MESSAGE,
    ADMIN_AWAITING_USER_SEARCH_QUERY,
    ADMIN_VIEWING_SEARCH_RESULTS,
    ADMIN_AWAITING_COURSE_SEARCH_QUERY,

    // --- mentor creating modules ---
    NONE,

    AWAITING_COURSE_TITLE,
    AWAITING_COURSE_DESCRIPTION,
    AWAITING_COURSE_THUMBNAIL,
    AWAITING_COURSE_CATEGORY_CHOICE,
    AWAITING_COURSE_CONFIRMATION,

    AWAITING_MODULE_TITLE,
    AWAITING_MODULE_DESCRIPTION,
    AWAITING_MODULE_PRICE,
    AWAITING_MODULE_CONFIRMATION, USER_SUPPORT_MESSAGE,


}

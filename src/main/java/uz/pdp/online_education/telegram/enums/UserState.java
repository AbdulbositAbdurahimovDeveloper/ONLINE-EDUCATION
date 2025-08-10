package uz.pdp.online_education.telegram.enums;

public enum UserState {

    DEFAULT,
    AUTHENTICATED,

    // --- ADMIN PANEL STATES ---
    ADMIN_MAIN_MENU,                // Asosiy admin menyusida (boshlang'ich holat)
    ADMIN_MANAGING_USERS,           // "Foydalanuvchilar" bo'limida
    ADMIN_MANAGING_COURSES,         // "Kurslar" bo'limida
    ADMIN_PREPARING_BROADCAST,      // "Xabar Yuborish" jarayonida
    ADMIN_VIEWING_STATISTICS,       // "Statistika" bo'limida (hozircha shart emas, lekin bo'lishi mumkin)

    AWAITING_BROADCAST_MESSAGE;


    ;
}

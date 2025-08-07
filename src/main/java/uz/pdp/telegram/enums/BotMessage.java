package uz.pdp.telegram.enums;

public enum BotMessage {

    // Barcha xabarlar shu yerda, "KALIT("Matn")" formatida e'lon qilinadi
    WELCOME("Assalomu alaykum! Bizning online ta'lim platformamizga xush kelibsiz!"),
    MAIN_MENU("⬇️ Quyidagi bo'limlardan birini tanlang:"),
    ASK_CONTACT("Iltimos, profilingizni tasdiqlash uchun telefon raqamingizni yuboring."),
    
    // Xatoliklar
    ERROR_UNEXPECTED("Kutilmagan xatolik yuz berdi. Iltimos, keyinroq qayta urinib ko'ring."),
    ERROR_USER_NOT_FOUND("Sizning profilingiz topilmadi. Iltimos, avval saytdan ro'yxatdan o'ting."),

    // Muvaffaqiyatli xabarlar
    SUCCESS_CONNECTION("✅ Sizning profilingiz muvaffaqiyatli bog'landi!"),
    
    // Dinamik xabarlar uchun formatlangan matnlar
    COURSE_LIST_HEADER("📚 Siz a'zo bo'lgan kurslar ro'yxati:\n\n"),
    COURSE_ITEM_FORMAT("🔹 %s\n"), // %s o'rniga kurs nomi qo'yiladi
    
    // Agar biror kalit topilmasa qaytariladigan standart xabar
    KEY_NOT_FOUND("Xabar topilmadi."),

    CHANGED_ROLE("Sizning rolein almashtirildi");

    // --- Enum'ning ichki mexanizmi ---

    private final String message;

    BotMessage(String message) {
        this.message = message;
    }

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}

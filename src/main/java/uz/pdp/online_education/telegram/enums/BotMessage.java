package uz.pdp.online_education.telegram.enums;

import lombok.Getter;

@Getter
public enum BotMessage {

    // --- Common Messages ---
    WELCOME_FIRST_TIME("common.welcome-first-time"),
    ERROR_UNEXPECTED("common.error.unexpected"),
    ERROR_USER_NOT_FOUND("common.error.user-not-found"),
    KEY_NOT_FOUND("common.key-not-found"),

    // --- Role Changed Messages ---
    ROLE_CHANGED_STUDENT("common.role-changed.student"),
    ROLE_CHANGED_INSTRUCTOR("common.role-changed.instructor"),
    ROLE_CHANGED_ADMIN("common.role-changed.admin"),

    // --- Student Messages ---
    START_MESSAGE_STUDENT("student.start-message"),
    DASHBOARD_STUDENT("student.dashboard"),
    // --- Student Messages ---
    AUTH_LOGOUT_CONFIRMATION_TEXT("auth.logout.confirmation-text"),
    AUTH_LOGOUT_SUCCESS_TEXT("auth.logout.success-text"),

    // --- Student "My Courses" Flow ---
    MY_COURSES_TITLE("student.my-courses.title"),
    NO_ENROLLED_COURSES("student.my-courses.no-courses-found"),
    MODULES_LIST_TITLE("student.my-courses.modules-list-title"),
    LESSONS_LIST_TITLE("student.my-courses.lessons-list-title"),
    LESSON_DETAIL_TITLE("student.my-courses.lesson-detail.title"),
    LESSON_CONTENT_BUTTON_TEXT("student.my-courses.lesson-detail.content-button-text"),
    LESSON_LOCKED_MESSAGE("student.my-courses.lesson-locked"),
    BUY_MODULE_BUTTON("student.my-courses.buy-module-button"),
    QUIZ_REDIRECT_MESSAGE("student.my-courses.quiz-redirect"),

//    ALL_COURSES_CHOOSE_METHOD("student.all-courses.choose-method"),
//    ALL_COURSES_BY_CATEGORY_TITLE("student.all-courses.by-category.title"),
//    ALL_COURSES_BY_INSTRUCTOR_TITLE("student.all-courses.by-instructor.title"),
//    ALL_COURSES_LIST_TITLE("student.all-courses.list.title"),
//    ALL_COURSES_DETAIL_TITLE("student.all-courses.detail.title"),

    ALL_COURSES_ENTRY_TEXT("student.all-courses.entry-text"),
    ALL_COURSES_MENTOR_LESSONS_BUTTON("student.all-courses.mentor-lessons-button"),
    ALL_COURSES_CATEGORIES_BUTTON("student.all-courses.categories-button"),

    ALL_COURSES_MENTORS_LIST("student.all-courses.mentors-list"),
    ALL_COURSES_CATEGORIES_LIST("student.all-courses.categories-list"),
    ALL_COURSES_LIST_ITEM("student.all-courses.list-item"),

    ALL_COURSES_COURSES_LIST("student.all-courses.courses-list"),
    ALL_COURSES_COURSE_LIST_ITEM("student.all-courses.course-list-item"),

    COURSE_MODULES_LIST("student.course-modules.modules-list"),
    COURSE_MODULES_LIST_ITEM("student.course-modules.module-list-item"),

    LESSON_LIST_ITEM("student.lesson.list-item"),
    LESSON_STATUS_FREE("student.lesson.status-free"),
    LESSON_STATUS_PAID("student.lesson.status-paid"),
    LESSON_LIST_TITLE("student.lesson.list-title"),

    BALANCE_INFO_WITH_PENDING_PAYMENT("student.balance_info.with_pending_payment"),
    BALANCE_INFO_NO_PENDING_PAYMENT("student.balance_info.no_pending_payment"),

    BALANCE_BUTTON_PENDING("student.balance_info.button_pending_payments"),
    BALANCE_BUTTON_HISTORY("student.balance_info.button_payment_history"),
    BALANCE_BUTTON_BACK("student.balance_info.button_back_to_menu"),


//    LESSON_STATUS_FREE("student.lesson.status-free"),               // Bepul
//    LESSON_STATUS_PAID("student.lesson.status-paid"),               // Pullik
//    LESSON_CONTENT_LOCKED("student.lesson.content-locked"),         // Kontent yopilgan
//    LESSON_CONTENT_ITEM("student.lesson.content-item"),             // %d. %s — %s
//    LESSON_DETAILS_WITH_CONTENTS("student.lesson.details-with-contents"), // 📖 <b>%s</b> ...
//
//    LESSON_STATUS_FREE("student.lesson.status-free"),                // Bepul
//    LESSON_STATUS_PAID("student.lesson.status-paid"),                // Pullik
//    LESSON_CONTENT_LOCKED("student.lesson.content-locked"),          // Kontent yopilgan
//    LESSON_CONTENT_ITEM("student.lesson.content-item"),              // %d. %s — %s
//    LESSON_DETAILS_WITH_CONTENTS("student.lesson.details-with-contents"),

    MODULE_STATUS_SUB_AND_PURCHASED("student.course-modules.status.sub-and-purchased"),
    MODULE_STATUS_SUB_ONLY("student.course-modules.status.sub-only"),
    MODULE_STATUS_LOCKED("student.course-modules.status.locked"),

//    LESSON_LIST_TITLE("student.lesson.list-title"),
//    LESSON_LIST_ITEM("student.lesson.list-item"),
//    LESSON_STATUS_FREE("student.lesson.status-free"),
//    LESSON_STATUS_PAID("student.lesson.status-paid"),
//    LESSON_STATUS_LOCKED("student.lesson.status-locked"),








    // --- Instructor Messages ---
    START_MESSAGE_INSTRUCTOR("instructor.start-message"),
    DASHBOARD_INSTRUCTOR("instructor.dashboard"),

    // --- Admin Messages ---
    START_MESSAGE_ADMIN("admin.start-message"),
    DASHBOARD_ADMIN("admin.dashboard"),
    ADMIN_USERS_MENU("admin.users-menu"),
    ADMIN_COURSES_MENU("admin.courses-menu"),
    ADMIN_BROADCAST_INIT("admin.broadcast.init");


    private final String key;

    BotMessage(String key) {
        this.key = key;
    }

}
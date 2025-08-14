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

    ALL_COURSES_CHOOSE_METHOD("student.all-courses.choose-method"),
    ALL_COURSES_BY_CATEGORY_TITLE("student.all-courses.by-category.title"),
    ALL_COURSES_BY_INSTRUCTOR_TITLE("student.all-courses.by-instructor.title"),
    ALL_COURSES_LIST_TITLE("student.all-courses.list.title"),
    ALL_COURSES_DETAIL_TITLE("student.all-courses.detail.title"),

    /*
    STUDENT_MY_COURSES_HEADER("student.my-courses.header"),
    STUDENT_MY_COURSES_LIST_ITEM("student.my-courses.list-item"),
    STUDENT_MY_COURSES_NO_COURSES("student.my-courses.no-courses"),

    STUDENT_COURSE_MODULES_HEADER("student.course-modules.header"),
    STUDENT_COURSE_MODULES_LIST_ITEM("student.course-modules.list-item"),
    STUDENT_COURSE_MODULES_NO_MODULES("student.course-modules.no-modules"),

    STUDENT_LESSON_CONTENT_HEADER("student.lesson.content-header"),

    STUDENT_LESSON_MAIN_MENU("student.lesson.main-menu"),
    STUDENT_LESSON_QUIZ_TEXT("student.lesson.quiz-text"),
    STUDENT_LESSON_FULLY_PAID_MODULE("student.lesson.fully-paid-module"),

     */

    // --- Instructor Messages ---
    START_MESSAGE_INSTRUCTOR("instructor.start-message"),
    DASHBOARD_INSTRUCTOR("instructor.dashboard"),

    // --- Admin Messages ---
    START_MESSAGE_ADMIN("admin.start-message"),
    DASHBOARD_ADMIN("admin.dashboard"),
    ADMIN_USERS_MENU("admin.users-menu"),
    ADMIN_COURSES_MENU("admin.courses-menu"),
    ADMIN_BROADCAST_INIT("admin.broadcast.init");

    // --- Dynamic/Format-only keys (can be in common or a separate section) ---
    // Hozircha bu kalitni .yml faylga qo'shmadik,
    // agar kerak bo'lsa, 'common' ichiga qo'shish mumkin.
    // COURSE_ITEM_FORMAT("common.dynamic.course-item-format");

    private final String key;

    BotMessage(String key) {
        this.key = key;
    }

}
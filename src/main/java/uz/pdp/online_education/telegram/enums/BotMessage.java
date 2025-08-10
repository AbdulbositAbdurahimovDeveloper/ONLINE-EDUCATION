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
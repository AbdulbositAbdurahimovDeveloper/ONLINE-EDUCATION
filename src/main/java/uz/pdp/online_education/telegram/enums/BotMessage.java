package uz.pdp.online_education.telegram.enums;

import lombok.Getter;

@Getter
public enum BotMessage {

    WELCOME_FIRST_TIME("welcome-first-time"),

    START_MESSAGE_STUDENT("start-message-student"),
    START_MESSAGE_INSTRUCTOR("start-message-instructor"),
    START_MESSAGE_ADMIN("start-message-admin"),

    ROLE_CHANGED_STUDENT("role-changed.student"), // Nuqta ierarxiyani bildiradi
    ROLE_CHANGED_INSTRUCTOR("role-changed.instructor"),
    ROLE_CHANGED_ADMIN("role-changed.admin"),

    COURSE_ITEM_FORMAT("dynamic.course-item-format"),

    ERROR_UNEXPECTED("error.unexpected"),
    ERROR_USER_NOT_FOUND("error.user-not-found"),

    KEY_NOT_FOUND("key-not-found");

    private final String key;

    BotMessage(String key) {
        this.key = key;
    }

}

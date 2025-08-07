package uz.pdp.online_education.telegram.service;

import uz.pdp.online_education.enums.Role;

public interface RoleService {
    Role getCurrentRole(Long chatId);

    Role getUserRole(Long chatId);

    void update(Long chatId, String text);
}

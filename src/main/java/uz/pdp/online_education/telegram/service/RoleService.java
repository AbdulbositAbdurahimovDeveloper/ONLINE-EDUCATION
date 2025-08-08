package uz.pdp.online_education.telegram.service;

import uz.pdp.online_education.enums.Role;

public interface RoleService {
    Role getUserRole(Long chatId);

}

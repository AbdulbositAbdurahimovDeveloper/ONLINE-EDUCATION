package uz.pdp.online_education.service;

import uz.pdp.online_education.model.User;

public interface UserService {
    User loadUserByUsername(String username);
}

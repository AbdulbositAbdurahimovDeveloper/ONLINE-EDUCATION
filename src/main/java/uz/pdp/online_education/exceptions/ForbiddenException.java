package uz.pdp.online_education.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Bu exception foydalanuvchining ma'lum bir resursga yoki amalga
 * kirishga ruxsati bo'lmaganda tashlanadi.
 * @ResponseStatus(HttpStatus.FORBIDDEN) annotatsiyasi bu xatolik yuz berganda
 * API avtomatik ravishda HTTP 403 (Forbidden) status kodini qaytarishini ta'minlaydi.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
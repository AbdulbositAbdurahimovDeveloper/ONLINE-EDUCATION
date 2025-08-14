package uz.pdp.online_education.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Bu exception kiruvchi ma'lumotlar (DTO) validatsiyadan o'tmaganda tashlanadi.
 * @ResponseStatus(HttpStatus.BAD_REQUEST) annotatsiyasi bu xatolik yuz berganda
 * API avtomatik ravishda HTTP 400 (Bad Request) status kodini qaytarishini ta'minlaydi.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataValidationException extends RuntimeException {

    public DataValidationException(String message) {
        super(message);
    }
}
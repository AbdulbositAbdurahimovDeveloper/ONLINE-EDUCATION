package uz.pdp.online_education.telegram.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Vaqtinchalik, ko'p maydonli ma'lumotlarni Redis'da saqlash va boshqarish uchun servis.
 * Bu servis Redis'ning Hash ma'lumotlar turi va TTL (Time-To-Live) xususiyatidan foydalanadi
 * vaqtinchalik jarayonlarni (masalan, Telegram botdagi ko'p qadamli so'rovnomalar)
 * boshqarish uchun mo'ljallangan.
 */
public interface RedisTemporaryDataService {

    /**
     * Yangi vaqtinchalik jarayonni standart yashash vaqti (TTL) bilan boshlaydi.
     * Standart TTL qiymati 'application.yml' faylidagi 'app.cache.temporary-process-ttl-seconds'
     * xususiyatidan olinadi.
     *
     * @param key         Asosiy unikal kalit (masalan, "module_create:chatId:12345:courseId:99").
     * @param initialData Jarayon boshlanishidagi dastlabki ma'lumotlar (masalan, courseId).
     */
    void startProcess(String key, Map<String, Object> initialData);

    /**
     * Yangi vaqtinchalik jarayonni maxsus yashash vaqti (TTL) bilan boshlaydi.
     *
     * @param key         Asosiy unikal kalit.
     * @param initialData Jarayon boshlanishidagi dastlabki ma'lumotlar.
     * @param ttlSeconds  Jarayonning yashash vaqti (sekundda).
     */
    void startProcess(String key, Map<String, Object> initialData, long ttlSeconds);

    /**
     * Mavjud jarayonga bitta maydon va uning qiymatini qo'shadi yoki yangilaydi.
     *
     * @param key   Asosiy kalit.
     * @param field Qo'shiladigan yoki yangilanadigan maydon nomi (masalan, "title").
     * @param value Maydon qiymati.
     */
    void addField(String key, String field, Object value);

    /**
     * Mavjud jarayondan bir nechta maydonning qiymatini oladi.
     * Agar biror maydon topilmasa, u natijaviy Map'ga qo'shilmaydi.
     *
     * @param key    Asosiy kalit.
     * @param fields Olinishi kerak bo'lgan maydonlar ro'yxati (masalan, ["title", "description"]).
     * @return Topilgan maydonlar va ularning qiymatlari bilan Map.
     */
    Map<String, Object> getFields(String key, List<String> fields);

    /**
     * Mavjud jarayondagi barcha maydonlarni va ularning qiymatlarini oladi.
     *
     * @param key Asosiy kalit.
     * @return Jarayondagi barcha ma'lumotlar bilan to'ldirilgan Optional.
     * Agar kalit mavjud bo'lmasa, Optional.empty() qaytariladi.
     */
    Optional<Map<String, Object>> getAllFields(String key);

    /**
     * Jarayonni tugatadi va unga tegishli barcha vaqtinchalik ma'lumotlarni Redis'dan o'chiradi.
     * Bu metod TTL tugashini kutmasdan, darhol tozalash uchun ishlatiladi.
     *
     * @param key O'chirilishi kerak bo'lgan asosiy kalit.
     */
    void endProcess(String key);
}
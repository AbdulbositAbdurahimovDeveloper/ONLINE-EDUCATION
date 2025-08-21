package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.ReviewMapper;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.payload.review.ReviewCreateDTO;
import uz.pdp.online_education.payload.review.ReviewDTO;
import uz.pdp.online_education.payload.review.ReviewUpdateDTO;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.service.interfaces.EmailService;
import uz.pdp.online_education.service.interfaces.ReviewService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * Yangi Sharh (Review) yaratish yoki agar mavjud bo'lsa, uni yangilash metodi.
     *
     * Ushbu metod quyidagi qadamlarni bajaradi:
     * 1. Berilgan `courseId` orqali kursni topadi. Agar topilmasa, `EntityNotFoundException` tashlaydi.
     * 2. Foydalanuvchi (`currentUser`) shu kurs uchun allaqachon sharh yozganmi yoki yo'qligini tekshiradi.
     * 3. Agar sharh mavjud bo'lsa, uni yangi ma'lumotlar bilan yangilaydi (`rating`, `comment`).
     * 4. Agar sharh mavjud bo'lmasa, yangi `Review` obyektini yaratadi va uni `reviewRepository` ga saqlaydi.
     * 5. Sharh yaratilgani yoki yangilanganidan so'ng, `notificationService` orqali bildirishnoma yuboriladi.
     * 6. Operatsiyadan so'ng yaratilgan yoki yangilangan sharhni DTO ko'rinishida qaytaradi.
     *
     * `@Transactional` anotatsiyasi bu metodning barcha operatsiyalarini bir butunlikda bajarilishini ta'minlaydi. Agar biror qadamda xatolik yuz bersa, barcha o'zgarishlar bekor qilinadi.
     *
     * @param dto           Yangi sharh yoki mavjud sharhni yangilash uchun ma'lumotlarni o'z ichiga olgan `ReviewCreateDTO`.
     * @param currentUser   Sharhni yaratayotgan yoki yangilayotgan tizimdagi joriy foydalanuvchi (`User` obyekti).
     * @return Yaratilgan yoki yangilangan sharhning `ReviewDTO` ko'rinishi.
     * @throws EntityNotFoundException Agar berilgan `courseId` bilan kurs topilmasa.
     */

    //    @Override
//    @Transactional
//    public ReviewDTO create(ReviewCreateDTO dto, User currentUser) {
//        Course course = courseRepository.findById(dto.getCourseId())
//                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
//
//        Optional<Review> optionalReview = reviewRepository.findByCourseIdAndUserId(course.getId(), currentUser.getId());
//
//        Review review;
//
//        if (optionalReview.isPresent()) {
//            // Review mavjud, update qilamiz
//            review = optionalReview.get();
//            review.setRating(dto.getRating());
//            review.setComment(dto.getComment());
//            log.info("Review updated with id: {}", review.getId());
//        } else {
//            // Review mavjud emas, yangi yaratamiz
//            review = new Review(dto.getRating(),
//                    dto.getComment(),
//                    course,
//                    currentUser
//            );
//            review = reviewRepository.save(review);
//            log.info("Review created with id: {}", review.getId());
//        }
//
//        return reviewMapper.toDto(review);
//
//    }
    @Override
    @Transactional
    public ReviewDTO create(ReviewCreateDTO dto, User currentUser) {
        // 1. Kursni topish: Foydalanuvchi sharh yozmoqchi bo'lgan kursni ma'lumotlar bazasidan topamiz.
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found")); // Agar kurs topilmasa, xatolik tashlanadi.

        // 2. Mavjud sharhni tekshirish: Foydalanuvchi (`currentUser`) shu kurs uchun allaqachon sharh yozganmi?
        Optional<Review> optionalReview = reviewRepository.findByCourseIdAndUserId(course.getId(), currentUser.getId());

        Review review; // Sharhni saqlash uchun o'zgaruvchi.
        boolean isNewReview; // Bu sharh yangi yaratilayotganmi yoki mavjud sharh yangilanayotganmi, shuni ko'rsatuvchi flag.

        if (optionalReview.isPresent()) {
            // 3. Sharh mavjud bo'lsa: Uni yangilaymiz.
            review = optionalReview.get(); // Mavjud `Review` obyektini olamiz.
            // `Review` obyektini DTO dan kelgan yangi ma'lumotlar bilan yangilaymiz.
            review.setRating(dto.getRating());
            review.setComment(dto.getComment());
            // Yangilanganligi haqida log yozamiz.
            log.info("Review updated with id: {}", review.getId());
            isNewReview = false; // Bu yangi sharh emas, balki mavjud sharhning yangilanishi.
        } else {
            // 4. Sharh mavjud bo'lmasa: Yangisini yaratamiz.
            // Yangi `Review` obyektini yaratish uchun konstruktordan foydalanamiz.
            review = new Review(dto.getRating(), dto.getComment(), course, currentUser);
            // Yangi yaratilgan `Review` obyektini ma'lumotlar bazasiga saqlaymiz va qaytarilgan obyektni olamiz.
            review = reviewRepository.save(review);
            // Yaratilganligi haqida log yozamiz.
            log.info("Review created with id: {}", review.getId());
            isNewReview = true; // Bu yangi sharh.
        }

        // 5. Notifikatsiya yuborish: Sharh yaratilgani yoki yangilangani haqida xabar berish uchun `notificationService` chaqiriladi.
        // Bu bildirishnoma boshqa foydalanuvchilarga (masalan, kurs muallifiga) yoki tizimga bildirilishi mumkin.
        notificationService.sendReviewNotification(review, isNewReview);

        // 6. Natijani DTO ko'rinishida qaytarish: Operatsiya natijasini mijozga yuborish uchun `reviewMapper` yordamida DTO ga aylantiramiz.
        return reviewMapper.toDto(review);
    }

    /**
     * Berilgan `id` bo'yicha bitta Sharhni (Review) topish va uni DTO ko'rinishida qaytarish metodi.
     * Agar `id` bilan hech qanday sharh topilmasa, `EntityNotFoundException` tashlanadi.
     *
     * @param id Topilishi kerak bo'lgan sharhning identifikatori (ID).
     * @return Topilgan sharhning `ReviewDTO` ko'rinishi.
     * @throws EntityNotFoundException Agar berilgan `id` bilan hech qanday `Review` topilmasa.
     */
    @Override
    public ReviewDTO getById(Long id) {
        // Berilgan `id` bo'yicha `Review`ni topishga harakat qilamiz.
        // `findById` metodi `Optional<Review>` qaytaradi.
        // `orElseThrow` yordamida agar `Optional` bo'sh bo'lsa, `EntityNotFoundException` tashlanadi.
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        // Topilgan `Review` obyektini `reviewMapper` yordamida `ReviewDTO` ga aylantirib qaytaramiz.
        return reviewMapper.toDto(review);
    }

    /**
     * Barcha Sharhlarni (Reviews) olish va ularni DTO ko'rinishida List sifatida qaytarish metodi.
     * Bu metod barcha sharhlarni `reviewRepository` dan oladi, ularni `reviewMapper` yordamida DTO ga aylantiradi
     * va natijani `List<ReviewDTO>` sifatida qaytaradi.
     *
     * @return Barcha sharhlarning `ReviewDTO` ko'rinishidagi listi. Agar hech qanday sharh bo'lmasa, bo'sh list qaytariladi.
     */
    @Override
    public List<ReviewDTO> getAll() {
        // Barcha `Review` obyektlarini `reviewRepository` dan olamiz.
        // Stream API dan foydalanib, har bir `Review` obyektini `reviewMapper` yordamida `ReviewDTO` ga aylantiramiz.
        // Keyin `collect(Collectors.toList())` yordamida natijalarni `List<ReviewDTO>` ga yig'amiz.
        return reviewRepository.findAll().stream() // Barcha sharhlarni streamga olamiz.
                .map(reviewMapper::toDto) // Har bir `Review`ni `ReviewDTO` ga aylantiramiz.
                .collect(Collectors.toList()); // Olingan DTO'larni List ga yig'amiz.
    }

    /**
     * Berilgan `id` bo'yicha mavjud Sharhni yangilash metodi.
     * Ushbu metod `ReviewUpdateDTO` dan ma'lumotlarni olib, mavjud `Review` obyektini yangilaydi.
     *
     * @param id  Yangilanishi kerak bo'lgan sharhning ID si.
     * @param dto Yangilash uchun ma'lumotlarni o'z ichiga olgan `ReviewUpdateDTO`.
     * @return Yangilangan sharhning `ReviewDTO` ko'rinishi.
     * @throws EntityNotFoundException Agar berilgan `id` bilan sharh topilmasa.
     */
    @Override
    @Transactional
    public ReviewDTO update(Long id, ReviewUpdateDTO dto) {
        // Berilgan `id` bo'yicha `Review`ni topamiz, agar topilmasa istisno tashlaymiz.
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        // `reviewMapper` yordamida `ReviewUpdateDTO` dagi ma'lumotlarni topilgan `Review` obyektiga ko'chiradi.
        // Bu mapper metodi `Review` obyektining o'zini o'zgartiradi.
        reviewMapper.updateReview(review, dto);

        // Yangilangan `Review` obyektini ma'lumotlar bazasiga saqlaymiz.
        Review updated = reviewRepository.save(review);

        // Yangilanganligi haqida log yozamiz.
        log.info("Review updated with id: {}", updated.getId());

        // Yangilangan `Review` obyektini `ReviewDTO` ga aylantirib qaytaramiz.
        return reviewMapper.toDto(updated);
    }

    /**
     * Berilgan `id` bo'yicha Sharhni (Review) ma'lumotlar bazasidan o'chirish metodi.
     * Avval sharh mavjudligi tekshiriladi, so'ngra o'chirish amalga oshiriladi.
     *
     * @param id O'chirilishi kerak bo'lgan sharhning ID si.
     * @throws EntityNotFoundException Agar berilgan `id` bilan sharh topilmasa.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // Avval berilgan `id` bilan sharhning mavjudligini tekshiramiz.
        // `existsById` metodi `true` yoki `false` qaytaradi.
        if (!reviewRepository.existsById(id)) {
            // Agar sharh mavjud bo'lmasa, `EntityNotFoundException` tashlanadi.
            throw new EntityNotFoundException("Review not found");
        }
        // Sharhni faqat ID orqali ma'lumotlar bazasidan o'chirib tashlaymiz.
        reviewRepository.deleteById(id);
        // O'chirilganligi haqida ogohlantirish logini yozamiz.
        log.warn("Review deleted with id: {}", id);
    }
}
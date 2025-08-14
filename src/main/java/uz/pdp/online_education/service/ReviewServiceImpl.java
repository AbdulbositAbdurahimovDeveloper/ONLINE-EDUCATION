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
import uz.pdp.online_education.service.interfaces.TelegramService;

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
    private final EmailService emailService; // Email servisni inject qilamiz
    private final TelegramService telegramService;

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
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Optional<Review> optionalReview = reviewRepository.findByCourseIdAndUserId(course.getId(), currentUser.getId());

        Review review;
        boolean isNewReview;

        if (optionalReview.isPresent()) {
            // Sharh mavjud, uni yangilaymiz
            review = optionalReview.get();
            review.setRating(dto.getRating());
            review.setComment(dto.getComment());
            log.info("Review updated with id: {}", review.getId());
            isNewReview = false;
        } else {
            // Sharh mavjud emas, yangisini yaratamiz
            review = new Review(
                    dto.getRating(),
                    dto.getComment(),
                    course,
                    currentUser
            );
            review = reviewRepository.save(review);
            log.info("Review created with id: {}", review.getId());
            isNewReview = true;
        }

        // Kurs muallifiga xabar yuborish mantiqi
        sendNotificationToInstructor(course, currentUser,dto, isNewReview);

        return reviewMapper.toDto(review);
    }

    private void sendNotificationToInstructor(Course course, User reviewer, ReviewCreateDTO dto, boolean isNewReview) {
        User instructor = course.getInstructor();


        // Xabar matnini tayyorlash
        String reviewerName = (reviewer.getProfile() != null && reviewer.getProfile().getFirstName() != null)
                ? reviewer.getProfile().getFirstName()
                : reviewer.getUsername();
        String subject = isNewReview ? "Yangi sharh" : "Sharh yangilandi";
        String message = String.format(
                "Assalomu alaykum, %s.\n\n" +
                        "Sizning '%s' nomli kursingizga %s tomonidan %s sharh qoldirildi.\n\n" +
                        "Sharh matni:\n%s",
                (instructor.getProfile() != null ? instructor.getProfile().getFirstName() : instructor.getUsername()),
                course.getTitle(),
                reviewerName,
                (isNewReview ? "yangi" : "yangilangan"),
                dto.getComment() != null ? dto.getComment() : "(Sharh matni yo‘q)"
        );



        // Emailga yuborish
        if (instructor.getProfile() != null && instructor.getProfile().getEmail() != null) {
            emailService.sendSimpleNotification(instructor.getProfile().getEmail(), subject, message);
        }

        // Telegramga yuborish
        if (instructor.getTelegramUser() != null && instructor.getTelegramUser().getChatId() != null) {
            telegramService.sendNotification(String.valueOf(instructor.getTelegramUser().getChatId()), message);
        }
    }

    @Override
    public ReviewDTO getById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        return reviewMapper.toDto(review);
    }

    @Override
    public List<ReviewDTO> getAll() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDTO update(Long id, ReviewUpdateDTO dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        reviewMapper.updateReview(review, dto);
        Review updated = reviewRepository.save(review);
        log.info("Review updated with id: {}", updated.getId());
        return reviewMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("Review not found");
        }
        reviewRepository.deleteById(id);
        log.warn("Review deleted with id: {}", id);
    }
}

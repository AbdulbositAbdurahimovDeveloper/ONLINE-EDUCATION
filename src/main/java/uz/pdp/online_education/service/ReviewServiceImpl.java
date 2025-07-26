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

    @Override
    @Transactional
    public ReviewDTO create(ReviewCreateDTO dto, User currentUser) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Optional<Review> optionalReview = reviewRepository.findByCourseIdAndUserId(course.getId(), currentUser.getId());

        Review review;

        if (optionalReview.isPresent()) {
            // Review mavjud, update qilamiz
            review = optionalReview.get();
            review.setRating(dto.getRating());
            review.setComment(dto.getComment());
            log.info("Review updated with id: {}", review.getId());
        } else {
            // Review mavjud emas, yangi yaratamiz
            review = new Review(dto.getRating(),
                    dto.getComment(),
                    course,
                    currentUser
            );
            review = reviewRepository.save(review);
            log.info("Review created with id: {}", review.getId());
        }

        return reviewMapper.toDto(review);
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

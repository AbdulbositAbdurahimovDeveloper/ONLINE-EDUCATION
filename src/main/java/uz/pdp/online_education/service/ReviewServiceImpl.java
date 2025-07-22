package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.payload.*;
import uz.pdp.online_education.mapper.ReviewMapper;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;


    @Override
    public ReviewDTO create(ReviewCreateDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Review review = ReviewMapper.toEntity(dto, course, user);
        Review saved = reviewRepository.save(review);
        log.info("Review created with id: {}", saved.getId());
        return ReviewMapper.toDto(saved);
    }

    @Override
    public ReviewDTO getById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        return ReviewMapper.toDto(review);
    }

    @Override
    public List<ReviewDTO> getAll() {
        return reviewRepository.findAll().stream()
                .map(ReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO update(Long id, ReviewUpdateDTO dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        ReviewMapper.updateReview(review, dto);
        Review updated = reviewRepository.save(review);
        log.info("Review updated with id: {}", updated.getId());
        return ReviewMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("Review not found");
        }
        reviewRepository.deleteById(id);
        log.warn("Review deleted with id: {}", id);
    }
}

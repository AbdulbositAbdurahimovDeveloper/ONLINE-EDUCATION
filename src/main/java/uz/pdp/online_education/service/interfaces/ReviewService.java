package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.review.ReviewCreateDTO;
import uz.pdp.online_education.payload.review.ReviewDTO;
import uz.pdp.online_education.payload.review.ReviewUpdateDTO;

import java.util.List;

public interface ReviewService {

    ReviewDTO create(ReviewCreateDTO dto, User currentUser);

    ReviewDTO getById(Long id);

    List<ReviewDTO> getAll();

    ReviewDTO update(Long id, ReviewUpdateDTO dto);

    void delete(Long id);
}

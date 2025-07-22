package uz.pdp.online_education.service;

import uz.pdp.online_education.payload.*;

import java.util.List;

public interface ReviewService {

    ReviewDTO create(ReviewCreateDTO dto);

    ReviewDTO getById(Long id);

    List<ReviewDTO> getAll();

    ReviewDTO update(Long id, ReviewUpdateDTO dto);

    void delete(Long id);
}

package uz.pdp.online_education.service;

import uz.pdp.online_education.payload.CourseRequestDTO;
import uz.pdp.online_education.payload.CourseResponseDTO;

import java.util.List;

public interface CourseService {
    CourseResponseDTO create(CourseRequestDTO dto);
    CourseResponseDTO update(Long id, CourseRequestDTO dto);
    void delete(Long id);
    CourseResponseDTO getById(Long id);
    List<CourseResponseDTO> getAll();
    List<CourseResponseDTO> getByCategoryId(Long categoryId);
    List<CourseResponseDTO> getByInstructorId(Long instructorId);
}

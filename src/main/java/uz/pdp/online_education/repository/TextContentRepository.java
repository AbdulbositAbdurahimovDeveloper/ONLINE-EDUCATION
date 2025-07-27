package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.online_education.model.lesson.TextContent;

public interface TextContentRepository extends JpaRepository<TextContent, Long> {


}
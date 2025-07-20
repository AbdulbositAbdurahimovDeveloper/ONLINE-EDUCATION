package uz.pdp.online_education.model.lesson;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "text_contents") // Faqat JOINED va TABLE_PER_CLASS uchun
@DiscriminatorValue("TEXT") // Faqat SINGLE_TABLE va JOINED uchun
@FieldNameConstants
public class TextContent extends Content {

    @Column(columnDefinition = "TEXT")
    private String text;
}
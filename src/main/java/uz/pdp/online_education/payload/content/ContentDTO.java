package uz.pdp.online_education.payload.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import uz.pdp.online_education.payload.attachment.AttachmentContentSummaryDTO;
import uz.pdp.online_education.payload.quiz.QuizContentSummaryDTO;
import uz.pdp.online_education.payload.text.TextContentSummaryDTO;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,     // Tipni nomi bo'yicha aniqlaymiz
        include = JsonTypeInfo.As.PROPERTY, // Tipni alohida maydon sifatida qo'shamiz
        property = "contentType"        // Bu maydon nomi "contentType" bo'ladi
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AttachmentContentSummaryDTO.class, name = "ATTACHMENT"),
        @JsonSubTypes.Type(value = QuizContentSummaryDTO.class, name = "QUIZ"),
        @JsonSubTypes.Type(value = TextContentSummaryDTO.class, name = "TEXT")
})
public interface ContentDTO  {
     Long getId();
     Integer getBlockOrder();
     String getContentType();
}
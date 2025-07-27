package uz.pdp.online_education.service.interfaces;

import java.util.List;

public interface ContentService {

    void deleteContent(Long id);

    void updateAllContentOrders(Long lessonId, List<Long> orderedContentIds);

}

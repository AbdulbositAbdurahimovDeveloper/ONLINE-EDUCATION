//package uz.pdp.online_education.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import uz.pdp.online_education.payload.*;
//import uz.pdp.online_education.service.ReviewService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/reviews")
//@RequiredArgsConstructor
//public class ReviewController {
//
//    private final ReviewService reviewService;
//
//    @PostMapping
//    public ResponseEntity<ReviewDTO> create(@RequestBody ReviewCreateDTO dto) {
//        ReviewDTO reviewDTO = reviewService.create(dto);
//        return ResponseEntity.ok(reviewDTO);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ReviewDTO> getById(@PathVariable Long id) {
//        ReviewDTO reviewDTO = reviewService.getById(id);
//        return ResponseEntity.ok(reviewDTO);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<ReviewDTO>> getAll() {
//        List<ReviewDTO> allReviews = reviewService.getAll();
//        return ResponseEntity.ok(allReviews);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ReviewDTO> update(@PathVariable Long id,
//                                            @RequestBody ReviewUpdateDTO dto) {
//        ReviewDTO updatedReview = reviewService.update(id, dto);
//        return ResponseEntity.ok(updatedReview);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        reviewService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//}

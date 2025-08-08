package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ModuleOrderIndexDTO;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.module.ModuleUpdateDTO;

import uz.pdp.online_education.payload.payment.PaymentDTO;
import uz.pdp.online_education.service.interfaces.ModuleService;
import uz.pdp.online_education.service.interfaces.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final PaymentService paymentService;

    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserEnrolled(authentication, #id)")
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> read(@PathVariable Long id) {
        ModuleDetailDTO moduleDetailDTO = moduleService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> create(@RequestBody ModuleCreateDTO moduleCreateDTO) {
        ModuleDetailDTO moduleDetailDTO = moduleService.create(moduleCreateDTO);
        return ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @GetMapping("/{id}/lessons")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserEnrolled(authentication, #id)")
    public ResponseEntity<ResponseDTO<?>> readLessons(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<LessonResponseDTO> lessonResponseDTO = moduleService.readLessons(id,page,size);
        return ResponseEntity.ok(ResponseDTO.success(lessonResponseDTO));
    }

    @GetMapping("/{id}/payment")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> readPayments(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "0") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<PaymentDTO> paymentDTO = paymentService.readPayments(id,page,size);
        return  ResponseEntity.ok(ResponseDTO.success(paymentDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> update(@PathVariable Long id, @RequestBody ModuleUpdateDTO moduleUpdateDTO) {
        ModuleDetailDTO moduleDetailDTO = moduleService.update(id, moduleUpdateDTO);
        return ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @PatchMapping("/{courseId}/reorder")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> reorder(@PathVariable Long courseId, @RequestBody List<ModuleOrderIndexDTO> moduleOrderDTOS) {
        moduleService.updateModuleOrderIndex(courseId, moduleOrderDTOS);
        return ResponseEntity.ok(ResponseDTO.success("successful"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Module deleted"));
    }
}

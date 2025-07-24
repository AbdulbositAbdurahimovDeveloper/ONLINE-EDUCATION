package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.assembler.ModuleAssembler;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.module.ModuleUpdateDTO;
import uz.pdp.online_education.service.ModuleService;
import uz.pdp.online_education.service.interfaces.ModuleService;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserEnrolled(authentication, #id)")
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> read(@PathVariable Long id) {
        ModuleDetailDTO moduleDetailDTO = moduleService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> create(@RequestBody ModuleCreateDTO moduleCreateDTO) {
        ModuleDetailDTO moduleDetailDTO = moduleService.create(moduleCreateDTO);
        return  ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> update(@PathVariable Long id, @RequestBody ModuleUpdateDTO moduleUpdateDTO) {
        ModuleDetailDTO moduleDetailDTO = moduleService.update(id,moduleUpdateDTO);
        return  ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Module deleted"));
    }
}

package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.assembler.ModuleAssembler;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.service.interfaces.ModuleService;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final ModuleAssembler moduleAssembler;



    @GetMapping("/{slug}")
    public  ResponseEntity<ResponseDTO<ModuleDetailDTO>> read(@PathVariable String slug) {
        return null;
    }

//    @PostMapping
}

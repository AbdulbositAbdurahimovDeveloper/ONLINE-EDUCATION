package uz.pdp.online_education.assembler;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uz.pdp.online_education.controller.ModuleController;
import uz.pdp.online_education.mapper.ModuleMapper;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ModuleAssembler extends RepresentationModelAssemblerSupport<Module, ModuleDetailDTO> {

    private final ModuleMapper moduleMapper;


    public ModuleAssembler(ModuleMapper moduleMapper) {
        super(ModuleController.class, ModuleDetailDTO.class);
        this.moduleMapper = moduleMapper;
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public ModuleDetailDTO toModel(Module entity) {

        ModuleDetailDTO moduleDetailDTO = moduleMapper.toModuleDetailsDTO(entity);

        moduleDetailDTO.add(linkTo(methodOn(ModuleController.class)
                .read(moduleDetailDTO.getCourse().getSlug()))
                .withSelfRel());

        return moduleDetailDTO;
    }
}

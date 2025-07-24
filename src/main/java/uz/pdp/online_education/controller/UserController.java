package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.user.UserDTO;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
import uz.pdp.online_education.service.interfaces.UserService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
//    private final PagedResourcesAssembler<User> pagedResourcesAssembler;

    @PreAuthorize("hasAnyRole('ADMIN','INCTRUKTOR')")
    @GetMapping
    public ResponseEntity<ResponseDTO<PageDTO<UserDTO>>> read(@RequestParam(defaultValue = "0") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer size,
                                                                 PagedResourcesAssembler<User> assembler) {

        PageDTO<UserDTO> usersPage = userService.read(page, size);
        return ResponseEntity.ok(ResponseDTO.success(usersPage));

    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<ResponseDTO<UserDTO>> read(@PathVariable Long id) {
        UserDTO userDTO = userService.read(id);

        Link selfLink = linkTo(methodOn(UserController.class).read(id)).withSelfRel();
        Link collectionLink = linkTo(methodOn(UserController.class).read(0, 10, null)).withRel("all-users");
        Link photoUrl = linkTo(methodOn(AttachmentController.class).read(userDTO.getProfilePictureId())).withRel("photo");

        userDTO.add(selfLink, collectionLink, photoUrl);

        return ResponseEntity.ok(ResponseDTO.success(userDTO));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<ResponseDTO<UserDTO>> update(@PathVariable Long id, @RequestBody UserRegisterRequestDTO registerRequestDTO) {
        UserDTO userDTO = userService.update(id, registerRequestDTO);

        Link selfLink = linkTo(methodOn(UserController.class).read(id)).withSelfRel();
        Link collectionLink = linkTo(methodOn(UserController.class).read(null, null, null)).withRel("all-users");
        Link photoUrl = linkTo(methodOn(AttachmentController.class).read(userDTO.getProfilePictureId())).withRel("photo");

        userDTO.add(selfLink, collectionLink, photoUrl);

        return ResponseEntity.ok(ResponseDTO.success(userDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<ResponseDTO<?>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("User deleted with id: " + id));
    }


}

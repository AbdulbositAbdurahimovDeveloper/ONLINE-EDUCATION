package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.user.UserDTO;
import uz.pdp.online_education.payload.user.UserUpdateRequestDTO;
import uz.pdp.online_education.service.interfaces.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<ResponseDTO<PageDTO<UserDTO>>> read(@RequestParam(defaultValue = "0") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<UserDTO> usersPage = userService.read(page, size);
        return ResponseEntity.ok(ResponseDTO.success(usersPage));
    }


    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    @PreAuthorize("hasRole('ADMIN') or @securityExpression.isOwner(authentication, #id)")
    public ResponseEntity<ResponseDTO<UserDTO>> read(@PathVariable Long id) {
        UserDTO userDTO = userService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(userDTO));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityExpression.isOwner(authentication, #id)")
    public ResponseEntity<ResponseDTO<UserDTO>> update(@PathVariable Long id,
                                                       @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
        UserDTO userDTO = userService.update(id, userUpdateRequestDTO);
        return ResponseEntity.ok(ResponseDTO.success(userDTO));
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    @PreAuthorize("hasRole('ADMIN') or @securityExpression.isOwner(authentication, #id)")
    public ResponseEntity<ResponseDTO<?>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("User deleted with id: " + id));
    }


}

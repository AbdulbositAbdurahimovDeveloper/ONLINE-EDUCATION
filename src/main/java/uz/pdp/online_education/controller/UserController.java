    package uz.pdp.online_education.controller;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Tag(name = "User Controller", description = "APIs for managing users")
    public class UserController {

        private final UserService userService;

        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        @GetMapping
        @Operation(summary = "Get all users (paged)", description = "Retrieve all users with pagination")
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
        public ResponseEntity<ResponseDTO<PageDTO<UserDTO>>> read(
                @RequestParam(defaultValue = "0") Integer page,
                @RequestParam(defaultValue = "10") Integer size) {
            PageDTO<UserDTO> usersPage = userService.read(page, size);
            return ResponseEntity.ok(ResponseDTO.success(usersPage));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN') or @securityExpression.isOwner(authentication, #id)")
        @Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
        @ApiResponse(responseCode = "200", description = "User retrieved successfully")
        public ResponseEntity<ResponseDTO<UserDTO>> read(@PathVariable Long id) {
            UserDTO userDTO = userService.read(id);
            return ResponseEntity.ok(ResponseDTO.success(userDTO));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN') or @securityExpression.isOwner(authentication, #id)")
        @Operation(summary = "Update user", description = "Update user details by ID")
        @ApiResponse(responseCode = "200", description = "User updated successfully")
        public ResponseEntity<ResponseDTO<UserDTO>> update(
                @PathVariable Long id,
                @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
            UserDTO userDTO = userService.update(id, userUpdateRequestDTO);
            return ResponseEntity.ok(ResponseDTO.success(userDTO));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN') or @securityExpression.isOwner(authentication, #id)")
        @Operation(summary = "Delete user", description = "Delete a user by ID")
        @ApiResponse(responseCode = "200", description = "User deleted successfully")
        public ResponseEntity<ResponseDTO<?>> delete(@PathVariable Long id) {
            userService.delete(id);
            return ResponseEntity.ok(ResponseDTO.success("User deleted with id: " + id));
        }
    }

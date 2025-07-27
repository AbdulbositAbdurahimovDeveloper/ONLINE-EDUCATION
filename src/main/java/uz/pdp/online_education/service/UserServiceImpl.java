package uz.pdp.online_education.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.config.properties.JwtProperties;
import uz.pdp.online_education.config.security.JwtService;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.UserMapper;
import uz.pdp.online_education.model.Abs.AbsLongEntity;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.model.VerificationToken;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.user.*;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.repository.UserProfileRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.repository.VerificationTokenRepository;
import uz.pdp.online_education.service.interfaces.EmailService;
import uz.pdp.online_education.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // Konstruktor avtomatik yaratiladi
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;
    private final AttachmentRepository attachmentRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Override
    @Cacheable(value = "users", key = "#username")
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUser(username);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
    }

    /**
     * Authenticates a user and returns a rich TokenDTO with tokens and user details.
     *
     * @param loginDTO DTO containing username and password.
     * @return A ResponseDTO containing a fully populated TokenDTO on success.
     * @throws BadCredentialsException if authentication fails for any reason.
     */
    @Override
    public ResponseDTO<TokenDTO> login(LoginDTO loginDTO) {
        log.info("User '{}' attempting to log in", loginDTO.getUsername());

        String username = loginDTO.getUsername();

        User user = getUser(username);

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Hisob faollashtirilmagan. Iltimos, emailingizni tasdiqlang.");
        }

        try {

            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Bad credentials provided");
            }

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            TokenDTO tokenDto = TokenDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)

                    .expiresIn(jwtProperties.getAccessTokenExpiration().toSeconds())
                    .authorities(user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet()))
                    .username(user.getUsername())
                    .build();

            log.info("User '{}' successfully authenticated and tokens generated.", username);

            return ResponseDTO.success(tokenDto);

        } catch (UsernameNotFoundException e) {
            // Xavfsizlik uchun "User not found" xatoligini ham "Bad credentials" ga o'girib yuboramiz
            log.warn("Failed login attempt for non-existent user '{}'", username);
            throw new BadCredentialsException("Bad credentials provided");
        }
    }

    @Override
    @Transactional
    public RegistrationResponseDTO register(UserRegisterRequestDTO request, HttpServletRequest httpServletRequest) {
        // 1. Username yoki email bandligini tekshirish
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DataConflictException("Username already exists!");
        }
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new DataConflictException("Email already registered!");
        }

        if (userProfileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DataConflictException("Phone number already registered!");
        }

        Attachment attachment = attachmentRepository.findById(request.getProfilePictureId())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found!"));

        // 2. User obyektini yaratish
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT); // Standart rol berish

        // 3. UserProfile obyektini yaratish
        UserProfile profile = new UserProfile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setProfilePicture(attachment);
        profile.setBio(request.getBio());

        // 4. Eng muhim qadam: Ikkala obyektni bir-biriga bog'lash
        user.setProfile(profile);
        profile.setUser(user);

        // 5. User-ni saqlash (UserProfile @OneToOne da cascade=ALL bo'lgani uchun avtomatik saqlanadi)
        User savedUser = userRepository.save(user);

        // 6. Javob DTO-sini yaratib, qaytarish
//        return userMapper.toDto(savedUser);

        // 2. Verifikatsiya tokenini yaratish va saqlash
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, savedUser);
        verificationTokenRepository.save(verificationToken);

        // 3. Tasdiqlash xabarini yuborish
//        String confirmationUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        String confirmationUrl = getApplicationUrl(httpServletRequest);
        emailService.sendVerificationEmail(savedUser.getProfile().getEmail(), confirmationUrl + "/api/auth/verify?token=" + token);

        // 4. Javob DTO'sini shakllantirish
        UserSummaryDTO userSummary = UserSummaryDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getProfile().getEmail())
                .build();

        return RegistrationResponseDTO.builder()
                .message("Ro'yxatdan o'tish muvaffaqiyatli yakunlandi. Hisobingizni faollashtirish uchun emailingizga yuborilgan havolani bosing.")
                .user(userSummary)
                .build();


    }

    @Override
    public void verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            throw new BadCredentialsException("Yaroqsiz tasdiqlash tokeni.");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Tokenning amal qilish muddati tugagan.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public PageDTO<UserDTO> read(Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.ASC, AbsLongEntity.Fields.id);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<User> users = userRepository.findAll(pageRequest);
        return new PageDTO<>(
                users.stream().map(userMapper::toDTO).toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast(),
                users.isFirst(),
                users.getNumberOfElements(),
                users.isEmpty()
        );
    }


    @Override
    public UserDTO read(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        return userMapper.toDTO(user);
    }


    @Override
    @Transactional
    public UserDTO update(Long id, UserRegisterRequestDTO registerRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        UserProfile profile = user.getProfile();
        if (!Objects.equals(profile.getProfilePicture().getId(), registerRequestDTO.getProfilePictureId())) {
            Attachment attachment = attachmentRepository.findById(registerRequestDTO.getProfilePictureId())
                    .orElseThrow(() -> new EntityNotFoundException("Attachment not found!"));
            profile.setProfilePicture(attachment);
        }

        if (!Objects.equals(profile.getEmail(), registerRequestDTO.getEmail())) {
            profile.setEmail(registerRequestDTO.getEmail());
            user.setEnabled(false);
        }

        profile.setFirstName(registerRequestDTO.getFirstName());
        profile.setLastName(registerRequestDTO.getLastName());
        profile.setPhoneNumber(registerRequestDTO.getPhoneNumber());
        profile.setBio(registerRequestDTO.getBio());

        user.setProfile(profile);
        profile.setUser(user);
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        UserProfile profile = user.getProfile();

        userProfileRepository.deleteById(profile.getId());
        userRepository.deleteById(user.getId());
        log.info("User deleted with id: {}", id);
    }

    /**
     * Joriy so'rovdan dasturning asosiy URL'ini (masalan, "http://localhost:8080"
     * yoki "https://my-domain.com") oluvchi yordamchi metod.
     */
    private String getApplicationUrl(HttpServletRequest request) {
        String scheme = request.getScheme();             // http
        String serverName = request.getServerName();     // localhost
        int serverPort = request.getServerPort();        // 8080
        String contextPath = request.getContextPath();   // Agar bo'lsa, masalan, /my-app

        // Standart portlar (80 uchun http, 443 uchun https) bo'lsa, ularni ko'rsatmaslik
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName + contextPath;
        }

        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }

}
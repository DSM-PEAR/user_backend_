package com.dsmpear.main.service.user;

import com.dsmpear.main.entity.user.User;
import com.dsmpear.main.entity.user.UserRepository;
import com.dsmpear.main.entity.verifynumber.VerifyNumber;
import com.dsmpear.main.entity.verifynumber.VerifyNumberRepository;
import com.dsmpear.main.exceptions.*;
import com.dsmpear.main.payload.request.RegisterRequest;
import com.dsmpear.main.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailSender;
    private final VerifyNumberRepository numberRepository;

    @Override
    public void register(RegisterRequest request) {
        if (!request.isValidAddress("dsm.hs.kr"))
            throw new InvalidEmailAddressException();

        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isPresent() && user.get().getAuthStatus())
            throw new UserIsAlreadyRegisteredException();

        emailSender.sendAuthNumEmail(request.getEmail());

        userRepository.save(
                User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName())
                    .authStatus(false)
                    .build()
        );
    }

    @Override
    public void verify(int number, String email) {
        VerifyNumber verifyNumber = numberRepository.findByEmail(email)
                .orElseThrow(NumberNotFoundException::new);

        if (!verifyNumber.verifyNumber(number))
            throw new InvalidVerifyNumberException();

        userRepository.findByEmail(email)
                .map(user -> {
                    user.setToTrueAuthStatus();
                    return user;
                })
                .map(userRepository::save)
                .orElseThrow(UserNotFoundException::new);
    }
}
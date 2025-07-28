package org.annill.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.annill.security.dto.UserDto;
import org.annill.security.security.JwtUtils;
import org.annill.security.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserDto userDto = userService.registerOAuth2(authentication);
        if (userDto != null) {
            String token = jwtUtils.generateJwtToken(userDto);
            response.sendRedirect("/web/home?token=" + token);
        } else {
            response.sendRedirect("/web/error");
        }
    }

}

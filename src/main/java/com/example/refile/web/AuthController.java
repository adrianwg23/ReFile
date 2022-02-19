package com.example.refile.web;

import com.example.refile.model.User;
import com.example.refile.service.CredentialService;
import com.example.refile.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CredentialService credentialService;

    @Value("${app.host}")
    private String host;

    @Value("${app.front-end}")
    private String frontEndHost;

    @GetMapping("/login")
    public void login(HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        if (request.getHeader("Origin") != null) {
            response.resetBuffer();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader("Content-Type", "application/json");
            response.getOutputStream().print("{\"errorMessage\":\"HTTP requests with origin forbidden. " +
                    "Try redirecting to this endpoint inside your browser instead.\"}");
            response.flushBuffer();
        } else {
            response.setHeader("Location", host + "/oauth2/authorization/google");
            response.setStatus(302);
        }
    }

    @GetMapping("/oauth-callback")
    public ModelAndView authenticatedUser(HttpServletRequest request,
                                          @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client,
                                          @AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        User user = userService.putUser(email, name);

        if (credentialService.getCredential(user.getUserId()).isEmpty()) {
            String accessToken = client.getAccessToken().getTokenValue();
            String refreshToken = client.getRefreshToken().getTokenValue();
            credentialService.saveCredential(user.getUserId(), accessToken, refreshToken);
        }

        return new ModelAndView(String.format("redirect:http://localhost:3000?userId=%d", user.getUserId()));
    }
}

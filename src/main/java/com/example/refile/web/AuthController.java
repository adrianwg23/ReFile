package com.example.refile.web;

import com.example.refile.model.User;
import com.example.refile.service.CredentialService;
import com.example.refile.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CredentialService credentialService;

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("redirect:http://localhost:8080/oauth2/authorization/google");
    }

    // return user id, user email, user name, and persist user and credentials in db
    @GetMapping("/login-success")
    public ResponseEntity<?> loginSuccess(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client,
                                          @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        User user = userService.putUser(email, name);

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken().getTokenValue();
        credentialService.saveCredential(user.getUserId(), accessToken, refreshToken);

        return ResponseEntity.ok(user);
    }
}

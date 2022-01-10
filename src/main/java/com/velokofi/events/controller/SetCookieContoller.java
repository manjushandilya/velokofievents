package com.velokofi.events.controller;

import com.velokofi.events.Application;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Getter
@Setter
public class SetCookieContoller {

    private static final Logger LOG = LoggerFactory.getLogger(SetCookieContoller.class);

    @GetMapping("/setCookie")
    public RedirectView execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String clientId = (String) request.getAttribute(Application.COOKIE_ID);

        LOG.debug("Setting cookie with name as: " + Application.COOKIE_ID + " and value as: " + clientId);

        final Cookie cookie = new Cookie(Application.COOKIE_ID, clientId);
        cookie.setMaxAge(30 * 24 * 60 * 60); // expires in 30 days
        cookie.setPath("/");
        response.addCookie(cookie);

        return new RedirectView("/");
    }

}

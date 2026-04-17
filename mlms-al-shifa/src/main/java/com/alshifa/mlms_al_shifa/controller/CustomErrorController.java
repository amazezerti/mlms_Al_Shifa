package com.alshifa.mlms_al_shifa.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request,
                              Model model) {

        Object statusObj  = request.getAttribute(
                RequestDispatcher.ERROR_STATUS_CODE);
        Object uriObj     = request.getAttribute(
                RequestDispatcher.ERROR_REQUEST_URI);
        Object messageObj = request.getAttribute(
                RequestDispatcher.ERROR_MESSAGE);

        int code = 500;
        if (statusObj != null) {
            try {
                code = Integer.parseInt(statusObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        // Never show error page for a successful redirect (2xx)
        // This prevents the post-login redirect from hitting error
        if (code >= 200 && code < 300) {
            return "redirect:/dashboard";
        }

        String uri = uriObj != null ? uriObj.toString() : "";

        String title;
        String detail;

        switch (code) {
            case 400 -> {
                title  = "Bad Request";
                detail = "The server could not understand the "
                        + "request. Please check your input.";
            }
            case 401 -> {
                title  = "Unauthorised";
                detail = "You must be logged in to access "
                        + "this resource.";
            }
            case 403 -> {
                title  = "Access Denied";
                detail = "You do not have permission to view "
                        + "this page. Contact your system "
                        + "administrator if you believe "
                        + "this is an error.";
            }
            case 404 -> {
                title  = "Page Not Found";
                detail = "The page"
                        + (uri.isBlank() ? "" : " '" + uri + "'")
                        + " does not exist or has been moved.";
            }
            case 405 -> {
                title  = "Method Not Allowed";
                detail = "This action is not permitted "
                        + "on this resource.";
            }
            case 408 -> {
                title  = "Request Timeout";
                detail = "The request took too long. "
                        + "Please try again.";
            }
            case 500 -> {
                title  = "Internal Server Error";
                detail = "Something went wrong on our end. "
                        + "Please try again or contact your "
                        + "administrator.";
            }
            case 503 -> {
                title  = "Service Unavailable";
                detail = "The service is temporarily unavailable. "
                        + "Please try again later.";
            }
            default  -> {
                title  = "Unexpected Error";
                detail = (messageObj != null
                        && !messageObj.toString().isBlank())
                        ? messageObj.toString()
                        : "An unknown error occurred. Please "
                        + "contact your administrator.";
            }
        }

        model.addAttribute("errorCode",    String.valueOf(code));
        model.addAttribute("errorTitle",   title);
        model.addAttribute("errorMessage", detail);
        model.addAttribute("requestedUrl", uri);

        return "error/error";
    }

    @RequestMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("errorCode",    "403");
        model.addAttribute("errorTitle",   "Access Denied");
        model.addAttribute("errorMessage",
                "You do not have the required permissions to access "
                        + "this page. Contact your system administrator.");
        model.addAttribute("requestedUrl", "");
        return "error/error";
    }
}
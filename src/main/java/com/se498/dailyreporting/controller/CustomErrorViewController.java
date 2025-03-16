package com.se498.dailyreporting.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller to handle error pages with Thymeleaf
 */
@Controller
public class CustomErrorViewController implements ErrorController {

    /**
     * Handle error requests and route to appropriate error pages
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error attributes
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            model.addAttribute("status", statusCode);
            model.addAttribute("error", message != null ? message : "An error occurred");
            model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
            model.addAttribute("timestamp", new java.util.Date());

            // Add technical details for 500 errors (only shown to admins in the view)
            if (statusCode == 500 && exception != null) {
                model.addAttribute("exception", exception.toString());

                Throwable throwable = (Throwable) exception;
                StringBuilder stackTraceBuilder = new StringBuilder();
                for (StackTraceElement element : throwable.getStackTrace()) {
                    stackTraceBuilder.append(element.toString()).append("\n");
                }
                model.addAttribute("trace", stackTraceBuilder.toString());
            }

            // Route to specific error pages
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("pageTitle", "Not Found");
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("pageTitle", "Access Denied");
                return "error/403";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("pageTitle", "Server Error");
                return "error/500";
            }
        }

        // Generic error page
        model.addAttribute("pageTitle", "Error");
        return "error";
    }
}
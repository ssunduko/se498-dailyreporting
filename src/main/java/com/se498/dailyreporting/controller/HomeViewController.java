package com.se498.dailyreporting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for home/landing pages
 */
@Controller
@RequestMapping("/ui")
public class HomeViewController {

    /**
     * Home page for the UI
     */
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Construction Management Portal");
        return "home";
    }

    /**
     * About page
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About");
        return "about";
    }

    /**
     * Help page
     */
    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("pageTitle", "Help & Documentation");
        return "help";
    }
}
package de.uhh.lt.xpertfinder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    @GetMapping("/about")
    public String ui(Model model) {
        model.addAttribute("result", ""); // add this so the footer stays bottom
        return "about";
    }

}

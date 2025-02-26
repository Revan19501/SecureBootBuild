package ru.kata.spring.boot_security.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.util.*;

@Controller
public class UserController {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    UserController(RoleRepository roleRepository, UserService userService, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping()
    public String start() {
        return "index";
    }


    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @GetMapping("/user")
    public String user(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
        } else {
            model.addAttribute("error", "User not found");
        }

        return "user";
    }


    @GetMapping("admin/new")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "user-form";
    }

    @GetMapping("admin/edit/{username}")
    public String showEditUserForm(@PathVariable String username, Model model) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get()); // Извлекаем User из Optional
            model.addAttribute("allRoles", roleRepository.findAll());
            return "user-form";
        } else {
            model.addAttribute("error", "User not found");
            return "redirect:/admin";
        }
    }


    @GetMapping("/user-form")
    public String showUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "user-form";
    }

    @PostMapping("admin/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "user-form";
        }

        if (userService.existsByUsername(user.getUsername(), user.getId())) {
            bindingResult.rejectValue("username", "error.user", "Этот логин уже используется!");
            model.addAttribute("allRoles", roleRepository.findAll());
            return "user-form";
        }

        User userToSave = user.getId() != null ? userService.findById(user.getId()).orElse(new User()) : new User();

        userToSave.setName(user.getName());
        userToSave.setLastName(user.getLastName());
        userToSave.setAge(user.getAge());
        userToSave.setEnabled(user.isEnabled());
        userToSave.setUsername(user.getUsername());


        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            userToSave.setPassword(user.getPassword());
        }

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                roles.add(role);
            }
            userToSave.setRoles(roles);
        }

        userService.saveUser(userToSave);
        return "redirect:/admin";
    }


    @GetMapping("/403")
    public String accessDenied() {
        return "error";
    }


    @GetMapping("admin/delete/{username}")
    public String deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return "redirect:/admin";
    }

}

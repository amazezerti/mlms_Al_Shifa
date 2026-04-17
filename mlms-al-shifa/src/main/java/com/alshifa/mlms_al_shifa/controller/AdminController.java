package com.alshifa.mlms_al_shifa.controller;

import com.alshifa.mlms_al_shifa.model.*;
import com.alshifa.mlms_al_shifa.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost
        .PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support
        .RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService          userService;
    private final DepartmentService    departmentService;
    private final PatientService       patientService;
    private final DoctorProfileService doctorProfileService;
    private final AppointmentService   appointmentService;

    @Value("${app.admin.username:admin}")
    private String protectedAdminUsername;

    // ── Dashboard ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        List<User> allUsers = userService.getAll();
        List<Department> allDepts =
                departmentService.getAll();
        List<Department> activeDepts =
                departmentService.getAllActive();

        // Count users per role using plain Java loop
        int cAdmin = 0, cReceptionist = 0,
                cDoctor = 0, cTech = 0;
        for (User u : allUsers) {
            for (Role r : u.getRoles()) {
                switch (r.getName()) {
                    case "ROLE_ADMIN"
                            -> cAdmin++;
                    case "ROLE_RECEPTIONIST"
                            -> cReceptionist++;
                    case "ROLE_DOCTOR"
                            -> cDoctor++;
                    case "ROLE_LAB_TECHNICIAN"
                            -> cTech++;
                }
            }
        }

        // Build dept names as a plain pipe-separated
        // String — Java side, not Thymeleaf expression.
        // This is the only reliable way to pass
        // list data for Chart.js via data attributes.
        StringBuilder deptNamesStr  = new StringBuilder();
        StringBuilder deptCountsStr = new StringBuilder();
        for (int i = 0; i < activeDepts.size(); i++) {
            if (i > 0) {
                deptNamesStr.append("|");
                deptCountsStr.append("|");
            }
            deptNamesStr.append(
                    activeDepts.get(i).getName());
            deptCountsStr.append("1");
        }

        model.addAttribute("pageTitle",
                "Admin Dashboard");
        model.addAttribute("activePage",   "dashboard");

        // Stats for cards and hero
        model.addAttribute("totalUsers",
                allUsers.size());
        model.addAttribute("totalPatients",
                patientService.countAll());
        model.addAttribute("totalDepts",
                allDepts.size());
        model.addAttribute("totalDoctors",
                doctorProfileService
                        .getAllActiveDoctors().size());

        // Users list for dashboard table
        model.addAttribute("users", allUsers);

        // Chart data — all plain integers
        model.addAttribute("countAdmin",       cAdmin);
        model.addAttribute("countReceptionist",
                cReceptionist);
        model.addAttribute("countDoctor",      cDoctor);
        model.addAttribute("countTech",        cTech);

        // Dept chart data — plain pipe-separated strings
        // built in Java, not Thymeleaf
        model.addAttribute("deptNamesStr",
                deptNamesStr.toString());
        model.addAttribute("deptCountsStr",
                deptCountsStr.toString());

        return "admin/dashboard";
    }

    // ── Users list ─────────────────────────────────────────
    @GetMapping("/users")
    public String users(
            @RequestParam(required = false) String q,
            Model model) {
        model.addAttribute("pageTitle",
                "User Management");
        model.addAttribute("activePage", "users");
        model.addAttribute("users",
                q != null && !q.isBlank()
                        ? userService.search(q)
                        : userService.getAll());
        model.addAttribute("q", q);
        model.addAttribute("protectedAdmin",
                protectedAdminUsername);
        return "admin/users";
    }

    // ── Create user POST ───────────────────────────────────
    @PostMapping("/users/new")
    public String createUser(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam String role,
            RedirectAttributes ra) {
        try {
            userService.createUser(
                    fullName, username, password,
                    email, phone, role);
            ra.addFlashAttribute("successMsg",
                    "User '" + username
                            + "' created successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Edit user GET ──────────────────────────────────────
    @GetMapping("/users/{id}/edit")
    public String editUserPage(
            @PathVariable Long id, Model model) {
        User user = userService.getById(id);
        if (user.getUsername()
                .equals(protectedAdminUsername)) {
            return "redirect:/admin/users";
        }
        model.addAttribute("pageTitle",  "Edit User");
        model.addAttribute("activePage", "users");
        model.addAttribute("user",       user);
        return "admin/user-form";
    }

    // ── Edit user POST ─────────────────────────────────────
    @PostMapping("/users/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam String role,
            RedirectAttributes ra) {
        try {
            User user = userService.getById(id);
            if (user.getUsername()
                    .equals(protectedAdminUsername)) {
                ra.addFlashAttribute("errorMsg",
                        "Cannot modify the system admin.");
                return "redirect:/admin/users";
            }
            userService.updateUser(
                    id, fullName, email, phone, role);
            ra.addFlashAttribute("successMsg",
                    "User updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Toggle status ──────────────────────────────────────
    @PostMapping("/users/{id}/toggle")
    public String toggleUser(
            @PathVariable Long id,
            RedirectAttributes ra) {
        User user = userService.getById(id);
        if (user.getUsername()
                .equals(protectedAdminUsername)) {
            ra.addFlashAttribute("errorMsg",
                    "Cannot deactivate the system admin.");
            return "redirect:/admin/users";
        }
        boolean wasEnabled = user.isEnabled();
        userService.toggleEnabled(id);
        ra.addFlashAttribute("successMsg",
                "User " + (wasEnabled
                        ? "deactivated" : "activated")
                        + " successfully.");
        return "redirect:/admin/users";
    }

    // ── Reset password ─────────────────────────────────────
    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes ra) {
        try {
            User user = userService.getById(id);
            if (user.getUsername()
                    .equals(protectedAdminUsername)) {
                ra.addFlashAttribute("errorMsg",
                        "Cannot reset the system admin "
                                + "password here.");
                return "redirect:/admin/users";
            }
            userService.resetPassword(id, newPassword);
            ra.addFlashAttribute("successMsg",
                    "Password reset successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Delete user ────────────────────────────────────────
    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            User user = userService.getById(id);
            if (user.getUsername()
                    .equals(protectedAdminUsername)) {
                ra.addFlashAttribute("errorMsg",
                        "Cannot delete the system admin.");
                return "redirect:/admin/users";
            }
            userService.delete(id);
            ra.addFlashAttribute("successMsg",
                    "User deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    "Cannot delete: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Doctor profile GET ─────────────────────────────────
    @GetMapping("/users/{id}/doctor-profile")
    public String doctorProfilePage(
            @PathVariable Long id, Model model) {
        User user = userService.getById(id);
        model.addAttribute("pageTitle",
                "Doctor Profile");
        model.addAttribute("activePage", "users");
        model.addAttribute("user",       user);
        model.addAttribute("departments",
                departmentService.getAllActive());
        model.addAttribute("profile",
                doctorProfileService.getByUserId(id)
                        .orElse(null));
        return "admin/doctor-profile-form";
    }

    // ── Doctor profile POST ────────────────────────────────
    @PostMapping("/users/{id}/doctor-profile")
    public String saveDoctorProfile(
            @PathVariable Long id,
            @RequestParam Long departmentId,
            @RequestParam(required = false)
            String specialization,
            @RequestParam(required = false)
            String qualification,
            @RequestParam(required = false)
            String licenseNumber,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false,
                    defaultValue = "0")
            Integer yearsExperience,
            RedirectAttributes ra) {
        try {
            doctorProfileService.createOrUpdate(
                    id, departmentId, specialization,
                    qualification, licenseNumber,
                    bio, yearsExperience);
            ra.addFlashAttribute("successMsg",
                    "Doctor profile saved.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Availability GET ───────────────────────────────────
    @GetMapping("/users/{id}/availability")
    public String availabilityPage(
            @PathVariable Long id, Model model) {
        model.addAttribute("pageTitle",
                "Manage Availability");
        model.addAttribute("activePage", "users");
        model.addAttribute("user",
                userService.getById(id));
        model.addAttribute("slots",
                appointmentService.getDoctorCalendar(
                        id,
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now()
                                .plusDays(30)));
        model.addAttribute("today",
                java.time.LocalDate.now().toString());
        return "admin/doctor-availability";
    }

    // ── Availability POST ──────────────────────────────────
    @PostMapping("/users/{id}/availability/add")
    public String addSlot(
            @PathVariable Long id,
            @RequestParam
            @org.springframework.format.annotation
                    .DateTimeFormat(iso =
                    org.springframework.format.annotation
                            .DateTimeFormat.ISO.DATE)
            java.time.LocalDate slotDate,
            @RequestParam
            @org.springframework.format.annotation
                    .DateTimeFormat(iso =
                    org.springframework.format.annotation
                            .DateTimeFormat.ISO.TIME)
            java.time.LocalTime startTime,
            @RequestParam
            @org.springframework.format.annotation
                    .DateTimeFormat(iso =
                    org.springframework.format.annotation
                            .DateTimeFormat.ISO.TIME)
            java.time.LocalTime endTime,
            RedirectAttributes ra) {
        try {
            appointmentService.addAvailabilitySlot(
                    id, slotDate, startTime, endTime);
            ra.addFlashAttribute("successMsg",
                    "Slot added.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/admin/users/"
                + id + "/availability";
    }

    // ── Departments GET ────────────────────────────────────
    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("pageTitle",  "Departments");
        model.addAttribute("activePage", "departments");
        model.addAttribute("departments",
                departmentService.getAll());
        return "admin/departments";
    }

    // ── Department create POST ─────────────────────────────
    @PostMapping("/departments/new")
    public String createDepartment(
            @RequestParam String name,
            @RequestParam(required = false)
            String description,
            RedirectAttributes ra) {
        try {
            departmentService.create(name, description);
            ra.addFlashAttribute("successMsg",
                    "Department '" + name + "' created.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    // ── Department toggle POST ─────────────────────────────
    @PostMapping("/departments/{id}/toggle")
    public String toggleDepartment(
            @PathVariable Long id,
            RedirectAttributes ra) {
        departmentService.toggleActive(id);
        ra.addFlashAttribute("successMsg",
                "Department status updated.");
        return "redirect:/admin/departments";
    }

    // ── Department delete POST ─────────────────────────────
    @PostMapping("/departments/{id}/delete")
    public String deleteDepartment(
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            departmentService.delete(id);
            ra.addFlashAttribute("successMsg",
                    "Department deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    "Cannot delete: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    // ── Settings ───────────────────────────────────────────
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle",  "Settings");
        model.addAttribute("activePage", "settings");
        return "admin/settings";
    }
}
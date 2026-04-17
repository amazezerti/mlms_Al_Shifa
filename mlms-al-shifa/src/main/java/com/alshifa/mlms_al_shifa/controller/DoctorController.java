package com.alshifa.mlms_al_shifa.controller;

import com.alshifa.mlms_al_shifa.model.*;
import com.alshifa.mlms_al_shifa.repository.*;
import com.alshifa.mlms_al_shifa.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support
        .RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/doctor")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorController {

    private final PatientAssignmentService assignmentService;
    private final DoctorProfileService     doctorProfileService;
    private final DepartmentService        departmentService;
    private final AppointmentService       appointmentService;
    private final UserRepository           userRepository;
    private final TestResultRepository     testResultRepository;
    private final TestOrderRepository      testOrderRepository;
    private final OrderItemRepository      orderItemRepository;

    // ── Dashboard ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model m) {
        User doctor = userRepository
                .findByUsername(auth.getName()).orElseThrow();
        List<PatientAssignment> assignments =
                assignmentService
                        .getAssignmentsForDoctor(doctor);

        long pendingOrders =
                testOrderRepository
                        .countByStatusAndAssignedDoctor(
                                "DRAFT", doctor.getId());

        List<TestResult> pendingResults =
                safeResults("ENTERED", doctor);

        List<DoctorAvailability> upcomingAppointments =
                appointmentService
                        .getDoctorAppointments(doctor.getId());

        m.addAttribute("pageTitle",
                "Clinical Dashboard");
        m.addAttribute("activePage",    "dashboard");
        m.addAttribute("assignments",   assignments);
        m.addAttribute("assignedCount", assignments.size());
        m.addAttribute("pendingOrders", pendingOrders);
        m.addAttribute("pendingResults",
                pendingResults.size());
        m.addAttribute("upcomingAppointments",
                upcomingAppointments);
        m.addAttribute("profile",
                doctorProfileService
                        .getByUserId(doctor.getId())
                        .orElse(null));
        return "doctor/dashboard";
    }

    // ── My Patients ────────────────────────────────────────
    @GetMapping("/patients")
    public String myPatients(Authentication auth,
                             Model m) {
        User doctor = userRepository
                .findByUsername(auth.getName()).orElseThrow();
        long pendingOrders =
                testOrderRepository
                        .countByStatusAndAssignedDoctor(
                                "DRAFT", doctor.getId());
        List<TestResult> pendingResultsList =
                safeResults("ENTERED", doctor);
        m.addAttribute("pageTitle",    "My Patients");
        m.addAttribute("activePage",   "patients");
        m.addAttribute("assignments",
                assignmentService
                        .getAssignmentsForDoctor(doctor));
        m.addAttribute("pendingOrders",  pendingOrders);
        m.addAttribute("pendingResults", pendingResultsList.size());
        return "doctor/patients";
    }

    // ── Pending Orders ─────────────────────────────────────
    @GetMapping("/orders")
    public String orders(Authentication auth, Model m) {
        User doctor = userRepository
                .findByUsername(auth.getName()).orElseThrow();
        java.util.Optional<DoctorProfile> profileOpt =
                doctorProfileService.getByUserId(doctor.getId());
        List<TestOrder> pending;
        if (profileOpt.isPresent()
                && profileOpt.get().getDepartment() != null) {
            Long deptId =
                    profileOpt.get().getDepartment().getId();
            pending = testOrderRepository
                    .findDraftOrdersByDepartment(deptId);
        } else {
            pending = Collections.emptyList();
        }
        m.addAttribute("pageTitle",  "Pending Orders");
        m.addAttribute("activePage", "orders");
        m.addAttribute("orders",     pending);
        return "doctor/orders";
    }

    // ── Confirm order ──────────────────────────────────────
    @PostMapping("/orders/{id}/confirm")
    public String confirmOrder(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User doctor = userRepository
                    .findByUsername(auth.getName())
                    .orElseThrow();
            TestOrder order =
                    testOrderRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order not found"));
            order.setStatus("CONFIRMED");
            order.setConfirmedBy(doctor);
            order.setConfirmedAt(LocalDateTime.now());
            testOrderRepository.save(order);
            ra.addFlashAttribute("successMsg",
                    "Order confirmed successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/doctor/orders";
    }

    // ── Reject order ───────────────────────────────────────
    @PostMapping("/orders/{id}/reject")
    public String rejectOrder(
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            TestOrder order =
                    testOrderRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order not found"));
            order.setStatus("CANCELLED");
            testOrderRepository.save(order);
            ra.addFlashAttribute("successMsg",
                    "Order rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/doctor/orders";
    }

    // ── Verify Results ─────────────────────────────────────
    @GetMapping("/results")
    public String results(Authentication auth, Model m) {
        User doctor = userRepository
                .findByUsername(auth.getName()).orElseThrow();
        List<TestResult> resultList =
                safeResults("ENTERED", doctor);
        long flaggedCount = resultList.stream()
                .filter(r -> r.getFlag() != null
                        && !r.getFlag().equals("NORMAL"))
                .count();
        m.addAttribute("pageTitle",    "Verify Results");
        m.addAttribute("activePage",   "results");
        m.addAttribute("results",      resultList);
        m.addAttribute("flaggedCount", flaggedCount);
        return "doctor/results";
    }

    // ── Verify single result ───────────────────────────────
    @PostMapping("/results/{id}/verify")
    public String verifyResult(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User doctor = userRepository
                    .findByUsername(auth.getName())
                    .orElseThrow();
            TestResult result =
                    testResultRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Result not found"));

            result.setStatus("VERIFIED");
            result.setVerifiedBy(doctor);
            result.setVerifiedAt(LocalDateTime.now());
            if (remarks != null && !remarks.isBlank()) {
                result.setRemarks(remarks);
            }
            testResultRepository.save(result);

            // FIX: Auto-complete the order when all
            // items in the order are verified
            checkAndCompleteOrder(
                    result.getOrderItem().getOrder());

            ra.addFlashAttribute("successMsg",
                    "Result verified successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/doctor/results";
    }

    /*
     * NEW: Check if every order item has a verified
     * result. If yes, mark the order as COMPLETED.
     * This fixes test orders stuck at IN_PROGRESS.
     */
    private void checkAndCompleteOrder(TestOrder order) {
        List<OrderItem> items =
                orderItemRepository.findByOrderId(
                        order.getId());
        if (items == null || items.isEmpty()) return;

        boolean allVerified = items.stream().allMatch(
                item -> testResultRepository
                        .existsByOrderItemIdAndStatus(
                                item.getId(), "VERIFIED"));

        if (allVerified) {
            order.setStatus("COMPLETED");
            testOrderRepository.save(order);
        }
    }

    // ── Verified results history (stays after verify) ─────
    @GetMapping("/results/history")
    public String resultsHistory(
            Authentication auth, Model m) {
        User doctor = userRepository
                .findByUsername(auth.getName()).orElseThrow();
        m.addAttribute("pageTitle",
                "Verified Results History");
        m.addAttribute("activePage", "results");
        m.addAttribute("results",
                safeResults("VERIFIED", doctor));
        return "doctor/results-history";
    }

    // ── Doctor Appointments ────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(Authentication auth,
                               Model m) {
        User doctor = userRepository
                .findByUsername(auth.getName()).orElseThrow();
        List<DoctorAvailability> appointments =
                appointmentService
                        .getDoctorAppointments(doctor.getId());
        m.addAttribute("pageTitle",  "My Appointments");
        m.addAttribute("activePage", "appointments");
        m.addAttribute("appointments", appointments);
        return "doctor/appointments";
    }

    // ── Print result for a patient ─────────────────────────
    @GetMapping("/print/{orderId}")
    public String printResult(
            @PathVariable Long orderId, Model m) {
        TestOrder order =
                testOrderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new RuntimeException("Order not found"));
        List<TestResult> results =
                testResultRepository
                        .findVerifiedByOrderId(orderId);
        m.addAttribute("pageTitle",  "Print Report");
        m.addAttribute("activePage", "results");
        m.addAttribute("order",      order);
        m.addAttribute("results",    results);
        return "doctor/print-report";
    }

    // ── Departments ────────────────────────────────────────
    @GetMapping("/departments")
    public String departments(Model m) {
        m.addAttribute("pageTitle",  "Departments");
        m.addAttribute("activePage", "departments");
        m.addAttribute("departments",
                departmentService.getAllActive());
        return "doctor/departments";
    }

    // ── Settings ───────────────────────────────────────────
    @GetMapping("/settings")
    public String settings(Authentication auth, Model m) {
        User doctor = userRepository
                .findByUsername(auth.getName()).orElseThrow();
        m.addAttribute("pageTitle",  "Settings");
        m.addAttribute("activePage", "settings");
        m.addAttribute("user",       doctor);
        m.addAttribute("profile",
                doctorProfileService
                        .getByUserId(doctor.getId())
                        .orElse(null));
        return "doctor/settings";
    }

    // ── Helper ─────────────────────────────────────────────
    private List<TestResult> safeResults(
            String status, User doctor) {
        try {
            List<TestResult> r =
                    testResultRepository
                            .findByStatusAndOrderItemOrderConfirmedBy(
                                    status, doctor);
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
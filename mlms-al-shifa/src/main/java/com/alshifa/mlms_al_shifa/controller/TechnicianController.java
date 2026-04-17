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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
@PreAuthorize("hasRole('LAB_TECHNICIAN')")
@RequiredArgsConstructor
public class TechnicianController {

    private final DepartmentService    departmentService;
    private final UserRepository       userRepo;
    private final TestOrderRepository  testOrderRepository;
    private final SampleRepository     sampleRepository;
    private final TestResultRepository testResultRepository;
    private final OrderItemRepository  orderItemRepository;

    // ── Dashboard ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model m) {
        User tech = userRepo
                .findByUsername(auth.getName()).orElseThrow();

        /*
         * FIX: findConfirmedOrders now returns
         * CONFIRMED and IN_PROGRESS, not just CONFIRMED.
         */
        List<TestOrder> activeOrders =
                testOrderRepository.findConfirmedOrders();

        List<Sample> pendingSamples =
                sampleRepository.findPendingSamples();

        List<TestResult> myResults =
                safeList(() -> testResultRepository
                        .findAllByTechnician(tech));

        long pendingEntry = myResults.stream()
                .filter(r -> "PENDING".equals(r.getStatus())
                        || "ENTERED".equals(r.getStatus()))
                .count();

        m.addAttribute("pageTitle",  "Lab Workstation");
        m.addAttribute("activePage", "dashboard");
        m.addAttribute("assignedCount",
                activeOrders.size());
        m.addAttribute("pendingSamplesCount",
                pendingSamples.size());
        m.addAttribute("resultsToEnterCount",
                (int) pendingEntry);
        m.addAttribute("recentOrders",
                activeOrders.stream().limit(5).toList());
        return "technician/dashboard";
    }

    // ── Assigned Orders (ALL — not just CONFIRMED) ─────────
    @GetMapping("/orders")
    public String orders(Model m) {
        /*
         * FIX: Returns CONFIRMED and IN_PROGRESS orders.
         * Previously only CONFIRMED — orders disappeared
         * after sample collection changed status.
         */
        List<TestOrder> orders =
                testOrderRepository.findConfirmedOrders();
        m.addAttribute("pageTitle",  "Assigned Orders");
        m.addAttribute("activePage", "orders");
        m.addAttribute("orders",
                orders != null
                        ? orders : Collections.emptyList());
        return "technician/orders";
    }

    // ── Sample Collection ──────────────────────────────────
    @GetMapping("/samples")
    public String samples(Model m) {
        /*
         * FIX: Sample collection dropdown now shows
         * ALL lab-relevant orders (CONFIRMED + IN_PROGRESS).
         * Previously only CONFIRMED — new orders
         * added after first sample were missing.
         */
        List<TestOrder> allLabOrders =
                testOrderRepository.findConfirmedOrders();

        List<Sample> existingSamples =
                sampleRepository.findPendingSamples();

        m.addAttribute("pageTitle",  "Sample Collection");
        m.addAttribute("activePage", "samples");
        m.addAttribute("samples",    existingSamples);
        m.addAttribute("orders",     allLabOrders);
        m.addAttribute("departments",
                departmentService.getAllActive());
        return "technician/samples";
    }

    @PostMapping("/samples/collect")
    public String collectSample(
            @RequestParam Long orderId,
            @RequestParam String barcode,
            @RequestParam Long departmentId,
            @RequestParam(required = false)
            String sampleType,
            @RequestParam(required = false) String notes,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            if (sampleRepository.existsByBarcode(barcode)) {
                ra.addFlashAttribute("errorMsg",
                        "Barcode '" + barcode
                                + "' already exists.");
                return "redirect:/technician/samples";
            }
            User tech = userRepo
                    .findByUsername(auth.getName())
                    .orElseThrow();
            TestOrder order =
                    testOrderRepository.findById(orderId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order not found"));
            Department dept =
                    departmentService.getById(departmentId);

            Sample sample = Sample.builder()
                    .barcode(barcode)
                    .order(order)
                    .department(dept)
                    .sampleType(sampleType)
                    .status("COLLECTED")
                    .collectedBy(tech)
                    .notes(notes)
                    .build();
            sampleRepository.save(sample);

            order.setStatus("IN_PROGRESS");
            testOrderRepository.save(order);

            ra.addFlashAttribute("successMsg",
                    "Sample collected: " + barcode);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/technician/samples";
    }

    // ── Result Entry ───────────────────────────────────────
    @GetMapping("/results")
    public String results(Authentication auth, Model m) {
        User tech = userRepo
                .findByUsername(auth.getName()).orElseThrow();

        List<OrderItem> pendingItems =
                orderItemRepository.findPendingConfirmedItems();

        // Build samples map per order
        Map<Long, List<Sample>> samplesByOrder =
                new HashMap<>();
        if (pendingItems != null) {
            for (OrderItem item : pendingItems) {
                Long oid = item.getOrder().getId();
                samplesByOrder.computeIfAbsent(oid,
                        k -> sampleRepository
                                .findByOrderId(k));
            }
        }

        m.addAttribute("pageTitle",  "Enter Results");
        m.addAttribute("activePage", "results");
        m.addAttribute("pendingItems",
                pendingItems != null
                        ? pendingItems
                        : Collections.emptyList());
        m.addAttribute("samplesByOrder", samplesByOrder);
        return "technician/results";
    }

    @PostMapping("/results/submit")
    public String submitResult(
            @RequestParam Long orderItemId,
            @RequestParam Long sampleId,
            @RequestParam(required = false)
            String resultValue,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false)
            String normalRange,
            @RequestParam(required = false) String flag,
            @RequestParam(required = false) String remarks,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User tech = userRepo
                    .findByUsername(auth.getName())
                    .orElseThrow();
            OrderItem item =
                    orderItemRepository.findById(orderItemId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order item not found"));
            Sample sample =
                    sampleRepository.findById(sampleId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Sample not found"));

            TestResult result = TestResult.builder()
                    .orderItem(item)
                    .sample(sample)
                    .resultValue(resultValue)
                    .unit(unit)
                    .normalRange(normalRange)
                    .flag(flag)
                    .remarks(remarks)
                    .status("ENTERED")
                    .enteredBy(tech)
                    .build();
            testResultRepository.save(result);

            item.setItemStatus("IN_PROGRESS");
            orderItemRepository.save(item);

            ra.addFlashAttribute("successMsg",
                    "Result submitted for verification.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/technician/results";
    }

    // ── NEW: Result History (stays after submission) ───────
    @GetMapping("/results/history")
    public String resultsHistory(Authentication auth,
                                 Model m) {
        User tech = userRepo
                .findByUsername(auth.getName()).orElseThrow();
        List<TestResult> allResults =
                safeList(() -> testResultRepository
                        .findAllByTechnician(tech));
        m.addAttribute("pageTitle",
                "Results History");
        m.addAttribute("activePage", "results");
        m.addAttribute("results",    allResults);
        return "technician/results-history";
    }

    // ── NEW: Print report for a specific order ─────────────
    @GetMapping("/print/{orderId}")
    public String printReport(
            @PathVariable Long orderId, Model m) {
        TestOrder order =
                testOrderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"));
        List<TestResult> results =
                testResultRepository
                        .findAllByOrderId(orderId);
        List<Sample> samples =
                sampleRepository.findByOrderId(orderId);

        m.addAttribute("pageTitle",  "Print Report");
        m.addAttribute("activePage", "results");
        m.addAttribute("order",      order);
        m.addAttribute("results",    results);
        m.addAttribute("samples",    samples);
        return "technician/print-report";
    }

    // ── Departments ────────────────────────────────────────
    @GetMapping("/departments")
    public String departments(Model m) {
        m.addAttribute("pageTitle",  "Lab Departments");
        m.addAttribute("activePage", "departments");
        m.addAttribute("departments",
                departmentService.getAllActive());
        return "technician/departments";
    }

    // ── Settings ───────────────────────────────────────────
    @GetMapping("/settings")
    public String settings(Authentication auth, Model m) {
        User tech = userRepo
                .findByUsername(auth.getName()).orElseThrow();
        m.addAttribute("pageTitle",  "Settings");
        m.addAttribute("activePage", "settings");
        m.addAttribute("user",       tech);
        return "technician/settings";
    }

    // ── Helper ─────────────────────────────────────────────
    private <T> List<T> safeList(
            java.util.function.Supplier<List<T>> fn) {
        try {
            List<T> r = fn.get();
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
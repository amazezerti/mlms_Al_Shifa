package com.alshifa.mlms_al_shifa.controller;

import com.alshifa.mlms_al_shifa.model.*;
import com.alshifa.mlms_al_shifa.repository.*;
import com.alshifa.mlms_al_shifa.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/receptionist")
@PreAuthorize("hasRole('RECEPTIONIST')")
@RequiredArgsConstructor
public class ReceptionistController {

    private final PatientService           patientService;
    private final DepartmentService        departmentService;
    private final DoctorProfileService     doctorProfileService;
    private final PatientAssignmentService assignmentService;
    private final AppointmentService       appointmentService;
    private final UserRepository           userRepo;
    private final TestOrderRepository      testOrderRepository;
    private final TestResultRepository     testResultRepository;
    private final TestCatalogRepository    testCatalogRepository;
    private final OrderItemRepository      orderItemRepository;
    private final InvoiceRepository        invoiceRepository;
    private final PaymentRepository        paymentRepository;

    // ── Dashboard ──────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle",     "Reception Desk");
        model.addAttribute("activePage",    "dashboard");
        model.addAttribute("totalPatients",
                patientService.countAll());
        model.addAttribute("departments",
                departmentService.getAllActive());
        model.addAttribute("totalDoctors",
                doctorProfileService.getAllActiveDoctors().size());
        model.addAttribute("todayOrders",
                testOrderRepository
                        .findByStatusOrderByOrderedAtDesc("DRAFT")
                        .size());
        model.addAttribute("pendingInvoices",
                invoiceRepository
                        .findByStatus("UNPAID").size());
        return "receptionist/dashboard";
    }

    // ── Patients ───────────────────────────────────────────────
    @GetMapping("/patients")
    public String patients(
            @RequestParam(required = false) String q,
            Model model) {
        model.addAttribute("pageTitle",  "Patients");
        model.addAttribute("activePage", "patients");
        model.addAttribute("patients",
                q != null && !q.isBlank()
                        ? patientService.search(q)
                        : patientService.getAll());
        model.addAttribute("departments",
                departmentService.getAllActive());
        model.addAttribute("q", q);
        return "receptionist/patients";
    }

    @PostMapping("/patients/register")
    public String registerPatient(
            @RequestParam String fullName,
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam String gender,
            @RequestParam String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String bloodGroup,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User registrar = userRepo
                    .findByUsername(auth.getName())
                    .orElseThrow();
            patientService.register(
                    fullName, dob, gender, phone,
                    address, bloodGroup, registrar);
            ra.addFlashAttribute("successMsg",
                    "Patient '" + fullName
                            + "' registered successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/receptionist/patients";
    }

    @PostMapping("/patients/{id}/assign")
    public String assignPatient(
            @PathVariable Long id,
            @RequestParam Long departmentId,
            @RequestParam(required = false) String notes,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User assignedBy = userRepo
                    .findByUsername(auth.getName())
                    .orElseThrow();
            PatientAssignment assignment =
                    assignmentService.assignToLeastLoadedDoctor(
                            id, departmentId,
                            assignedBy.getId(), notes);
            ra.addFlashAttribute("successMsg",
                    "Patient assigned to Dr. "
                            + assignment.getDoctor().getFullName()
                            + " in "
                            + assignment.getDepartment().getName());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/receptionist/patients";
    }

    // ── Doctors ────────────────────────────────────────────────
    @GetMapping("/doctors")
    public String doctors(
            @RequestParam(required = false) Long deptId,
            Model model) {
        model.addAttribute("pageTitle",  "Doctors");
        model.addAttribute("activePage", "doctors");
        model.addAttribute("departments",
                departmentService.getAllActive());

        if (deptId != null) {
            model.addAttribute("doctors",
                    doctorProfileService
                            .getDoctorsByDepartment(deptId));
            model.addAttribute("doctorUsers",
                    Collections.emptyList());
        } else {
            model.addAttribute("doctors",
                    doctorProfileService.getAllActiveDoctors());
            model.addAttribute("doctorUsers",
                    doctorProfileService.getAllDoctorUsers());
        }
        model.addAttribute("selectedDept", deptId);
        return "receptionist/doctors";
    }

    // ── Appointments ───────────────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(
            @RequestParam(required = false) Long doctorId,
            Model model) {

        List<User> allDoctors =
                doctorProfileService.getAllDoctorUsers();

        model.addAttribute("pageTitle",        "Appointments");
        model.addAttribute("activePage",       "appointments");
        model.addAttribute("allDoctors",       allDoctors);
        model.addAttribute("patients",
                patientService.getAll());
        model.addAttribute("selectedDoctorId", doctorId);

        if (doctorId != null) {
            model.addAttribute("slots",
                    appointmentService
                            .getAllFutureSlots(doctorId));
        } else {
            model.addAttribute("slots",
                    Collections.emptyList());
        }
        return "receptionist/appointments";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(
            @RequestParam Long slotId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String reason,
            RedirectAttributes ra) {
        try {
            appointmentService.bookSlot(
                    slotId, patientId, reason);
            ra.addFlashAttribute("successMsg",
                    "Appointment booked successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/receptionist/appointments";
    }


    // ── Test Orders ────────────────────────────────────────────
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("pageTitle",  "Test Orders");
        model.addAttribute("activePage", "orders");
        model.addAttribute("patients",
                patientService.getAll());
        model.addAttribute("testCatalog",
                testCatalogRepository.findByActiveTrue());
        model.addAttribute("departments",
                departmentService.getAllActive());
        model.addAttribute("orders",
                testOrderRepository.findAllActiveOrders());
        return "receptionist/orders";
    }

    @PostMapping("/orders/create")
    public String createOrder(
            @RequestParam Long patientId,
            @RequestParam(required = false) List<Long> testIds,
            @RequestParam(required = false)
            String clinicalNote,
            @RequestParam(required = false,
                    defaultValue = "NORMAL") String priority,
            Authentication auth,
            RedirectAttributes ra) {
        if (testIds == null || testIds.isEmpty()) {
            ra.addFlashAttribute("errorMsg",
                    "Please select at least one test.");
            return "redirect:/receptionist/orders";
        }
        try {
            User receptionist = userRepo
                    .findByUsername(auth.getName())
                    .orElseThrow();
            Patient patient = patientService
                    .getById(patientId);

            String code = "ORD-" + System.currentTimeMillis()
                    % 100000;

            TestOrder order = TestOrder.builder()
                    .orderCode(code)
                    .patient(patient)
                    .status("DRAFT")
                    .priority(priority)
                    .clinicalNote(clinicalNote)
                    .createdBy(receptionist)
                    .build();

            TestOrder saved =
                    testOrderRepository.save(order);

            BigDecimal total = BigDecimal.ZERO;
            for (Long testId : testIds) {
                TestCatalog test =
                        testCatalogRepository.findById(testId)
                                .orElse(null);
                if (test == null) continue;
                OrderItem item = OrderItem.builder()
                        .order(saved)
                        .test(test)
                        .itemStatus("PENDING")
                        .unitPrice(test.getPrice())
                        .build();
                orderItemRepository.save(item);
                total = total.add(test.getPrice());
            }

            ra.addFlashAttribute("successMsg",
                    "Test order " + code
                            + " created. Awaiting doctor confirmation.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/receptionist/orders";
    }

    // ── Invoices ───────────────────────────────────────────────
    @GetMapping("/invoices")
    public String invoices(Model model) {
        model.addAttribute("pageTitle",  "Invoices");
        model.addAttribute("activePage", "invoices");
        model.addAttribute("patients",
                patientService.getAll());
        model.addAttribute("orders",
                testOrderRepository.findInvoiceableOrders());
        model.addAttribute("unpaidInvoices",
                invoiceRepository.findByStatus("UNPAID"));
        model.addAttribute("paidInvoices",
                invoiceRepository.findByStatus("PAID"));
        model.addAttribute("partialInvoices",
                invoiceRepository.findByStatus("PARTIAL"));
        model.addAttribute("allInvoices",
                invoiceRepository.findAll());
        return "receptionist/invoices";
    }

    @PostMapping("/invoices/create")
    public String createInvoice(
            @RequestParam Long patientId,
            @RequestParam Long orderId,
            @RequestParam(required = false,
                    defaultValue = "0") int discountPct,
            @RequestParam(required = false,
                    defaultValue = "CASH") String paymentMethod,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User receptionist = userRepo
                    .findByUsername(auth.getName())
                    .orElseThrow();
            Patient patient = patientService
                    .getById(patientId);
            TestOrder order = testOrderRepository
                    .findById(orderId)
                    .orElseThrow(() ->
                            new RuntimeException("Order not found"));

            // Auto-calculate total from order items
            BigDecimal totalAmount = orderItemRepository
                    .findByOrderId(orderId).stream()
                    .map(item -> item.getUnitPrice() != null
                            ? item.getUnitPrice()
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discount =
                    totalAmount.multiply(
                            BigDecimal.valueOf(
                                    discountPct / 100.0));
            BigDecimal netAmount =
                    totalAmount.subtract(discount);

            String invoiceNumber = "INV-"
                    + System.currentTimeMillis() % 1000000;

            Invoice invoice = Invoice.builder()
                    .invoiceNumber(invoiceNumber)
                    .order(order)
                    .patient(patient)
                    .totalAmount(totalAmount)
                    .discount(discount)
                    .netAmount(netAmount)
                    .status("UNPAID")
                    .issuedBy(receptionist)
                    .build();

            invoiceRepository.save(invoice);
            ra.addFlashAttribute("successMsg",
                    "Invoice " + invoiceNumber
                            + " generated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/receptionist/invoices";
    }

    @PostMapping("/invoices/pay")
    public String recordPayment(
            @RequestParam Long invoiceId,
            @RequestParam BigDecimal amountPaid,
            @RequestParam String paymentMethod,
            @RequestParam(required = false)
            String referenceNumber,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User receptionist = userRepo
                    .findByUsername(auth.getName())
                    .orElseThrow();
            Invoice invoice = invoiceRepository
                    .findById(invoiceId)
                    .orElseThrow(() ->
                            new RuntimeException("Invoice not found"));

            Payment payment = Payment.builder()
                    .invoice(invoice)
                    .amountPaid(amountPaid)
                    .paymentMethod(paymentMethod)
                    .referenceNumber(referenceNumber)
                    .receivedBy(receptionist)
                    .build();
            paymentRepository.save(payment);

            if (amountPaid.compareTo(invoice.getNetAmount())
                    >= 0) {
                invoice.setStatus("PAID");
            } else {
                invoice.setStatus("PARTIAL");
            }
            invoiceRepository.save(invoice);

            ra.addFlashAttribute("successMsg",
                    "Payment of " + amountPaid
                            + " XAF recorded successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage());
        }
        return "redirect:/receptionist/invoices";
    }

    @GetMapping("/invoices/{id}/print")
    public String printInvoice(
            @PathVariable Long id, Model model) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Invoice not found"));
        model.addAttribute("invoice", invoice);
        model.addAttribute("payments",
                paymentRepository.findByInvoiceId(id));
        return "receptionist/invoice-print";
    }

    // ── Payments ───────────────────────────────────────────────
    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("pageTitle",  "Payments");
        model.addAttribute("activePage", "payments");
        model.addAttribute("payments",
                paymentRepository.findAll());
        return "receptionist/payments";
    }

    // ── Print Results (list) ──────────────────────────────────
    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("pageTitle",  "Print Results");
        model.addAttribute("activePage", "print");
        model.addAttribute("results",
                testResultRepository
                        .findAllVerifiedResults());
        model.addAttribute("patients",
                patientService.getAll());
        return "receptionist/print";
    }

    // ── Print single order report ──────────────────────────────
    @GetMapping("/print/order/{orderId}")
    public String printOrderReport(
            @PathVariable Long orderId, Model model) {
        TestOrder order = testOrderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found: " + orderId));
        List<TestResult> results =
                testResultRepository
                        .findVerifiedByOrderId(orderId);
        model.addAttribute("pageTitle",  "Print Report");
        model.addAttribute("activePage", "print");
        model.addAttribute("order",   order);
        model.addAttribute("results", results);
        return "doctor/print-report";
    }

    // ── Settings ───────────────────────────────────────────────
    @GetMapping("/settings")
    public String settings(Authentication auth, Model model) {
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow();
        model.addAttribute("pageTitle",  "Settings");
        model.addAttribute("activePage", "settings");
        model.addAttribute("user",       user);
        return "receptionist/settings";
    }
}
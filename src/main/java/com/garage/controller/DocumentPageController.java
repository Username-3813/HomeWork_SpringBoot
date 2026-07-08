package com.garage.controller;

import com.garage.model.Document;
import com.garage.service.DocumentService;
import com.garage.service.UserService;
import com.garage.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentPageController {

    private final DocumentService documentService;
    private final VehicleService vehicleService;
    private final UserService userService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).getId();
    }

    @GetMapping("/vehicle/{vehicleId}")
    public String listDocuments(@PathVariable Long vehicleId, Model model) {
        Long userId = getCurrentUserId();
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        List<Document> documents = documentService.getDocumentsByVehicle(vehicleId, userId);
        model.addAttribute("documents", documents);
        model.addAttribute("vehicleId", vehicleId);
        return "documents/list";
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file,
                                 @RequestParam("vehicleId") Long vehicleId,
                                 @RequestParam("documentName") String documentName,
                                 RedirectAttributes redirectAttributes) throws IOException {
        documentService.uploadDocument(file, vehicleId, getCurrentUserId(), documentName);
        redirectAttributes.addFlashAttribute("success", "Документ загружен!");
        return "redirect:/documents/vehicle/" + vehicleId;
    }

    @GetMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Document document = documentService.getDocumentById(id, userId);
        Long vehicleId = document.getVehicle().getId();
        documentService.deleteDocument(id, userId);
        return "redirect:/documents/vehicle/" + vehicleId;
    }
}
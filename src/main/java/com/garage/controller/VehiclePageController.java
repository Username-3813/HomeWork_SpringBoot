package com.garage.controller;

import com.garage.dto.VehicleRequest;
import com.garage.model.Vehicle;
import com.garage.service.UserService;
import com.garage.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehiclePageController {

    private final VehicleService vehicleService;
    private final UserService userService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).getId();
    }

    @GetMapping
    public String listVehicles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            Model model) {
        Long userId = getCurrentUserId();
        List<Vehicle> vehicles = vehicleService.getUserVehicles(userId);

        if (type != null && !type.isEmpty()) {
            vehicles = vehicles.stream()
                    .filter(v -> type.equals(v.getType()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isEmpty()) {
            String lowerSearch = search.toLowerCase();
            vehicles = vehicles.stream()
                    .filter(v ->
                            v.getName().toLowerCase().contains(lowerSearch) ||
                            (v.getBrand() != null && v.getBrand().toLowerCase().contains(lowerSearch)) ||
                            (v.getModel() != null && v.getModel().toLowerCase().contains(lowerSearch)) ||
                            (v.getLicensePlate() != null && v.getLicensePlate().toLowerCase().contains(lowerSearch))
                    )
                    .collect(Collectors.toList());
        }

        model.addAttribute("vehicles", vehicles);
        return "vehicles/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("vehicle", new VehicleRequest());
        model.addAttribute("isEdit", false);
        return "vehicles/form";
    }

    @PostMapping("/add")
    public String addVehicle(@ModelAttribute VehicleRequest dto,
                             RedirectAttributes redirectAttributes) throws Exception {
        if (!"Другое".equals(dto.getType()) && (dto.getName() == null || dto.getName().isEmpty())) {
            dto.setName(dto.getType());
        }
        if ("Другое".equals(dto.getType()) && (dto.getName() == null || dto.getName().isEmpty())) {
            redirectAttributes.addFlashAttribute("error", "При выборе типа 'Другое' необходимо указать название");
            return "redirect:/vehicles/add";
        }

        Vehicle savedVehicle = vehicleService.addVehicle(dto, getCurrentUserId());

        if (dto.getPhotoFile() != null && !dto.getPhotoFile().isEmpty()) {
            vehicleService.savePhoto(savedVehicle.getId(), dto.getPhotoFile(), getCurrentUserId());
        }

        redirectAttributes.addFlashAttribute("success", "ТС добавлено успешно!");
        return "redirect:/vehicles/" + savedVehicle.getId();
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Long userId = getCurrentUserId();
        Vehicle vehicle = vehicleService.getVehicleByIdAndUser(id, userId);
        VehicleRequest dto = new VehicleRequest();
        dto.setName(vehicle.getName());
        dto.setType(vehicle.getType());
        dto.setBrand(vehicle.getBrand());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setVin(vehicle.getVin());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setInitialOdometer(vehicle.getInitialOdometer());
        dto.setOdometerUnit(vehicle.getOdometerUnit());

        model.addAttribute("vehicle", dto);
        model.addAttribute("vehicleId", id);
        model.addAttribute("isEdit", true);
        return "vehicles/form";
    }

    @PostMapping("/edit/{id}")
    public String updateVehicle(@PathVariable Long id,
                                @ModelAttribute VehicleRequest dto,
                                RedirectAttributes redirectAttributes) throws Exception {
        if (!"Другое".equals(dto.getType()) && (dto.getName() == null || dto.getName().isEmpty())) {
            dto.setName(dto.getType());
        }
        if ("Другое".equals(dto.getType()) && (dto.getName() == null || dto.getName().isEmpty())) {
            redirectAttributes.addFlashAttribute("error", "При выборе типа 'Другое' необходимо указать название");
            return "redirect:/vehicles/edit/" + id;
        }

        vehicleService.updateVehicle(id, dto, getCurrentUserId());

        if (dto.getPhotoFile() != null && !dto.getPhotoFile().isEmpty()) {
            vehicleService.savePhoto(id, dto.getPhotoFile(), getCurrentUserId());
        }

        redirectAttributes.addFlashAttribute("success", "ТС обновлено!");
        return "redirect:/vehicles/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id, getCurrentUserId());
        return "redirect:/vehicles";
    }

    @PostMapping("/upload-photo/{id}")
    public String uploadPhoto(@PathVariable Long id,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) throws IOException {
        vehicleService.savePhoto(id, file, getCurrentUserId());
        redirectAttributes.addFlashAttribute("success", "Фото загружено!");
        return "redirect:/vehicles/" + id;
    }

    @GetMapping("/{id}")
    public String showDetail(@PathVariable Long id, Model model) {
        Long userId = getCurrentUserId();
        Vehicle vehicle = vehicleService.getVehicleByIdAndUser(id, userId);
        model.addAttribute("vehicle", vehicle);
        return "vehicles/detail";
    }
}
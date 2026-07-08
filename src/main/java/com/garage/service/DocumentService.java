package com.garage.service;

import com.garage.model.Document;
import com.garage.model.Vehicle;
import com.garage.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final VehicleService vehicleService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public Document uploadDocument(MultipartFile file, Long vehicleId, Long userId, String documentName) throws IOException {
        Vehicle vehicle = vehicleService.getVehicleByIdAndUser(vehicleId, userId);

        Path uploadPath = Paths.get(uploadDir, "documents");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String filePathStr = filePath.toString().replace("\\", "/");

        Document document = new Document();
        document.setVehicle(vehicle);
        document.setName(documentName);
        document.setType(file.getContentType());
        document.setFilePath(filePathStr);
        document.setFileSize(file.getSize());

        log.info("Загружен документ {} для ТС {}", documentName, vehicle.getName());
        return documentRepository.save(document);
    }

    public Document getDocumentById(Long id, Long userId) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Документ не найден (ID: " + id + ")"));
        vehicleService.getVehicleByIdAndUser(document.getVehicle().getId(), userId);
        return document;
    }

    // ИСПРАВЛЕННЫЙ МЕТОД — исключаем документы страховок
    public List<Document> getDocumentsByVehicle(Long vehicleId, Long userId) {
        vehicleService.getVehicleByIdAndUser(vehicleId, userId);
        return documentRepository.findNonInsuranceDocumentsByVehicleId(vehicleId);
    }

    @Transactional
    public void deleteDocument(Long id, Long userId) {
        Document document = getDocumentById(id, userId);
        try {
            Path filePath = Paths.get(document.getFilePath());
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Файл документа удалён: {}", document.getFilePath());
            } else {
                log.warn("Файл не найден при удалении: {}", document.getFilePath());
            }
        } catch (IOException e) {
            log.error("Ошибка при удалении файла: {}", document.getFilePath(), e);
        }
        documentRepository.deleteById(id);
        log.info("Запись о документе удалена (ID: {})", id);
    }
}
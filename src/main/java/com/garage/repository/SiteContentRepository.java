package com.garage.repository;

import com.garage.model.SiteContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteContentRepository extends JpaRepository<SiteContent, String> {
    Optional<SiteContent> findByPage(String page);
}
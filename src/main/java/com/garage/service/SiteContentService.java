package com.garage.service;

import com.garage.model.SiteContent;
import com.garage.repository.SiteContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class SiteContentService {

    private final SiteContentRepository repository;

    public String getContent(String page, String defaultContent) {
        return repository.findByPage(page)
                .map(SiteContent::getContent)
                .orElse(defaultContent);
    }

    @Transactional
    public void saveContent(String page, String content) {
        SiteContent sc = repository.findById(page).orElse(new SiteContent());
        sc.setPage(page);
        sc.setContent(content);
        repository.save(sc);
    }
}
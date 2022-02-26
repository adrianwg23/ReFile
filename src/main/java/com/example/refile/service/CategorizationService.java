package com.example.refile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategorizationService {

    public List<String> extractCategories(String text, Set<String> categories, Set<String> seenCategories) {
        String lowerCaseText = text.toLowerCase();
        List<String> containedCategories = new ArrayList<>();

        categories.stream()
                  .filter(category -> !seenCategories.contains(category))
                  .forEach(category -> {
                      if (lowerCaseText.contains(category.toLowerCase())) {
                          containedCategories.add(category);
                      }
                  });

        return containedCategories;
    }
}

package com.example.refile.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategorizationService {

    private static final Map<String, Set<String>> CATEGORIES = ImmutableMap.of(
            "Invoice", ImmutableSet.of("invoice", "invoices"),
            "Receipt", ImmutableSet.of("receipt", "receipts"),
            "Contract", ImmutableSet.of("contract", "contracts")
    );

    public List<String> extractCategories(String text) {
        String lowerCaseText = text.toLowerCase();
        List<String> containedCategories = new ArrayList<>();

        CATEGORIES.forEach((category, variants) -> {
            Optional<String> match = variants.stream().filter(lowerCaseText::contains)
                                             .findFirst();
            if (match.isPresent()) {
                containedCategories.add(category);
            }
        });

        return containedCategories;
    }

}

package com.example.refile.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ThreadDto {

    private final String thread;
    private final String senderEmail;

}
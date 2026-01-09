package com.github.juliusd.ueberboeseapi.bmx;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ueberboese.bmx")
public record UeberboeseApiUrlProperties(String baseUrl) {}

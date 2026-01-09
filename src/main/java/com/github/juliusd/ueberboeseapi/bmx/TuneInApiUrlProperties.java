package com.github.juliusd.ueberboeseapi.bmx;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tunein.api")
public record TuneInApiUrlProperties(String describeUrl, String streamUrl) {}

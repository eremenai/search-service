package com.neviswealth.searchservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "search.threshold")
public class SearchingProperties {

    private double embedding = 0.2;
    private double similarity = 0.15;

    public double getEmbedding() {
        return embedding;
    }

    public void setEmbedding(double embedding) {
        this.embedding = embedding;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}

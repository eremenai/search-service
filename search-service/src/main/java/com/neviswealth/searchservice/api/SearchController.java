package com.neviswealth.searchservice.api;

import com.neviswealth.searchservice.api.dto.SearchResultDto;
import com.neviswealth.searchservice.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/search")
@Validated
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public SearchResultDto search(@RequestParam("q") @NotBlank String query,
                                        @RequestParam(value = "clientId", required = false) UUID clientId) {
        return searchService.search(query, clientId);
    }
}

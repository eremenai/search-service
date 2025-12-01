package com.neviswealth.searchservice.api;

import com.neviswealth.searchservice.api.dto.ClientDto;
import com.neviswealth.searchservice.api.dto.CreateClientRequest;
import com.neviswealth.searchservice.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientDto> allClients() {
        return clientService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDto createClient(@Valid @RequestBody CreateClientRequest request) {
        return clientService.createClient(request);
    }

    @GetMapping("/{id}")
    public ClientDto getClient(@PathVariable("id") UUID id) {
        return clientService.getClient(id);
    }
}

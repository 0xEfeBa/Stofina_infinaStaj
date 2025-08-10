package com.stofina.app.customerservice.controller;

import com.stofina.app.customerservice.dto.CorporateCustomerDto;
import com.stofina.app.customerservice.request.corporatecustomer.CreateCorporateCustomerRequest;
import com.stofina.app.customerservice.request.corporatecustomer.UpdateCorporateCustomerRequest;
import com.stofina.app.customerservice.service.ICorporateCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.stofina.app.customerservice.constant.CustomerConstants.*;

@RestController
@RequestMapping(API_PREFIX+API_VERSION_V1+API_CORPORATE)
@RequiredArgsConstructor
@Tag(name = "Corporate Customers", description = "APIs for managing corporate customers")
public class CorporateCustomerController {

    private final ICorporateCustomerService corporateCustomerService;

    @Operation(summary = "Create Corporate Customer", description = "Create corporate customer with base customerId")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = CorporateCustomerDto.class))))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CorporateCustomerDto create(@Valid @RequestBody CreateCorporateCustomerRequest request) {
        return corporateCustomerService.create( request);
    }

    @Operation(summary = "Get Corporate Customer by ID", description = "Get corporate customer details by base customerId")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = CorporateCustomerDto.class))))
    @GetMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public CorporateCustomerDto getById(@PathVariable("customerId") Long customerId) {
        return corporateCustomerService.getById(customerId);
    }

    @Operation(summary = "Update Corporate Customer", description = "Update corporate customer by customerId")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = CorporateCustomerDto.class))))
    @PutMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public CorporateCustomerDto update(@PathVariable ("customerId") Long customerId,
                                       @Valid @RequestBody UpdateCorporateCustomerRequest request) {
        return corporateCustomerService.update(customerId, request);
    }

    @Operation(summary = "Delete Corporate Customer", description = "Delete corporate customer by customerId")
    @ApiResponses(@ApiResponse(responseCode = "204", description = "No Content"))
    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long customerId) {
        corporateCustomerService.delete(customerId);
    }

    @Operation(summary = "List All Corporate Customers", description = "List all corporate customers")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = CorporateCustomerDto.class))))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CorporateCustomerDto> getAll() {
        return corporateCustomerService.getAll();
    }
}

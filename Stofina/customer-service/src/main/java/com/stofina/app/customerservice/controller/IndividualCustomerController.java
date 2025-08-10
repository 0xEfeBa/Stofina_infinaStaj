package com.stofina.app.customerservice.controller;

import com.stofina.app.customerservice.dto.IndividualCustomerDto;
import com.stofina.app.customerservice.request.individualcustomer.CreateIndividualCustomerRequest;
import com.stofina.app.customerservice.request.individualcustomer.UpdateIndividualCustomerRequest;
import com.stofina.app.customerservice.service.IIndividualCustomerService;
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
@RequestMapping(API_PREFIX+API_VERSION_V1+API_INDIVIDUAL)
@RequiredArgsConstructor
@Tag(name = "Individual Customers", description = "APIs for managing individual customers")
public class IndividualCustomerController {

    private final IIndividualCustomerService individualCustomerService;

    @Operation(summary = "Create Individual Customer", description = "Create individual customer with base customerId")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = IndividualCustomerDto.class))))
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public IndividualCustomerDto create(@Valid @RequestBody CreateIndividualCustomerRequest request) {
        return individualCustomerService.create( request);
    }

    @Operation(summary = "Get Individual Customer by ID", description = "Get individual customer details by base customerId")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = IndividualCustomerDto.class))))
    @GetMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public IndividualCustomerDto getById(@PathVariable("customerId") Long customerId) {
        return individualCustomerService.getById(customerId);
    }

    @Operation(summary = "Update Individual Customer", description = "Update individual customer by customerId")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = IndividualCustomerDto.class))))
    @PutMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public IndividualCustomerDto update(@PathVariable ("customerId") Long customerId,
                                        @Valid @RequestBody UpdateIndividualCustomerRequest request) {
        return individualCustomerService.update(customerId, request);
    }

    @Operation(summary = "Delete Individual Customer", description = "Delete individual customer by customerId")
    @ApiResponses(@ApiResponse(responseCode = "204", description = "No Content"))
    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable ("customerId") Long customerId) {
        individualCustomerService.delete(customerId);
    }

    @Operation(summary = "List All Individual Customers", description = "List all individual customers")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = IndividualCustomerDto.class))))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<IndividualCustomerDto> getAll() {
        return individualCustomerService.getAll();
    }
}

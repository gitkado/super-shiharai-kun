package com.example.presentation.controller

import com.example.config.security.UserPrincipal
import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.service.InvoiceService
import com.example.presentation.dto.request.RegisterInvoiceRequest
import com.example.presentation.dto.response.ErrorResponse
import com.example.presentation.dto.response.ValidationError
import com.example.presentation.dto.response.toInvoiceResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class InvoiceController(private val invoiceService: InvoiceService) {
    suspend fun registerInvoice(call: ApplicationCall) {
        val userPrincipal = call.principal<UserPrincipal>()!!
        val request = call.receive<RegisterInvoiceRequest>()

        try {
            val invoice =
                invoiceService.registerInvoice(
                    userId = userPrincipal.userId,
                    issueDate = request.issueDate,
                    paymentAmount = Money.of(request.paymentAmount),
                    paymentDueDate = request.paymentDueDate,
                )

            call.respond(HttpStatusCode.Created, invoice.toInvoiceResponse())
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(listOf(ValidationError("validation", e.message ?: "Invalid input"))),
            )
        }
    }
}

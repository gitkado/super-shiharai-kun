package com.example.presentation.controller

import com.example.config.security.UserPrincipal
import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.service.InvoiceService
import com.example.presentation.dto.request.RegisterInvoiceRequest
import com.example.presentation.dto.response.toInvoiceResponse
import com.example.presentation.dto.response.toInvoiceResponseList
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.time.LocalDate

class InvoiceController(private val invoiceService: InvoiceService) {
    suspend fun registerInvoice(call: ApplicationCall) {
        val userPrincipal = call.principal<UserPrincipal>()!!
        val request = call.receive<RegisterInvoiceRequest>()

        val invoice =
            invoiceService.registerInvoice(
                userId = userPrincipal.userId,
                issueDate = request.issueDate,
                paymentAmount = Money.of(request.paymentAmount),
                paymentDueDate = request.paymentDueDate,
            )

        call.respond(HttpStatusCode.Created, invoice.toInvoiceResponse())
    }

    suspend fun getInvoices(call: ApplicationCall) {
        val userPrincipal = call.principal<UserPrincipal>()!!

        // Parse query parameters
        val paymentDueFromStr = call.request.queryParameters["paymentDueFrom"]
        val paymentDueToStr = call.request.queryParameters["paymentDueTo"]

        val paymentDueFrom = paymentDueFromStr?.let { LocalDate.parse(it) }
        val paymentDueTo = paymentDueToStr?.let { LocalDate.parse(it) }

        val invoices =
            invoiceService.getInvoices(
                userId = userPrincipal.userId,
                paymentDueFrom = paymentDueFrom,
                paymentDueTo = paymentDueTo,
            )

        call.respond(HttpStatusCode.OK, invoices.toInvoiceResponseList())
    }
}

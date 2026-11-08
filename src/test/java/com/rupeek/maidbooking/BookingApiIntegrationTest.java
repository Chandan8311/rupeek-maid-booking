package com.rupeek.maidbooking;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingApiIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void onboardDiscoverBookPayAndCancelThroughJsonBodies() throws Exception {
        LocalDateTime starts = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                .plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0);
        DayOfWeek weekday = starts.getDayOfWeek();

        String maidJson = mvc.perform(post("/api/v1/maids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Anita", "locality", "HSR Layout", "gender", "FEMALE",
                                "rating", 4.7, "hourly_rate", 250,
                                "skills", List.of("cleaning", "cooking"),
                                "availability", List.of(Map.of("day_of_week", weekday.name(),
                                        "from", "08:00:00", "to", "20:00:00"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hourly_rate").value(250))
                .andExpect(jsonPath("$.availability[0].day_of_week").value(weekday.name()))
                .andReturn().getResponse().getContentAsString();
        String maidId = mapper.readTree(maidJson).get("id").asText();

        mvc.perform(get("/api/v1/maids/" + maidId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Anita"));
        mvc.perform(get("/api/v1/maids")
                        .param("locality", "hsr layout")
                        .param("serviceType", "cleaning")
                        .param("durationMinutes", "60")
                        .param("startAt", starts.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(maidId));


        String bookingPayload = mapper.writeValueAsString(Map.of(
                "maid_id", maidId, "customer_id", "customer-api", "service_type", "cleaning",
                "type", "SCHEDULED", "start_at", starts.toString(), "duration_minutes", 60));

        String booked = mvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON).content(bookingPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customer_id").value("customer-api"))
                .andReturn().getResponse().getContentAsString();
        String bookingId = mapper.readTree(booked).get("id").asText();
        String occurrenceId = mapper.readTree(booked).get("occurrences").get(0).get("id").asText();

        mvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON).content(bookingPayload))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "booking_id", bookingId, "occurrence_id", occurrenceId,
                                "method", "UPI", "idempotency_key", "api-pay-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("UPI"))
                .andExpect(jsonPath("$.idempotency_key").value("api-pay-1"));

        mvc.perform(post("/api/v1/bookings/cancellations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "booking_id", bookingId, "scope", "OCCURRENCE",
                                "occurrence_id", occurrenceId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled_occurrence_ids[0]").value(occurrenceId));

        mvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON).content(bookingPayload))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidRequestAndMissingResourceReturnClientErrors() throws Exception {
        mvc.perform(post("/api/v1/maids")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/bookings/cancellations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "booking_id", UUID.randomUUID().toString(), "scope", "OCCURRENCE"))))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/bookings/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}

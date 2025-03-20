package fr.formationacademy.scpiinvestpluspartner.feign;

import fr.formationacademy.scpiinvestpluspartner.dto.EmailDtoIn;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "notification-service", url = "${notification-service-url}")
public interface NotificationClient {

    @PostMapping("api/v1/email/send")
    ResponseEntity sendEmail(@RequestBody EmailDtoIn email);
}

package com.lgali.request.deposit;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lgali.common.dto.DepositRequestDTO;
import com.lgali.common.exception.GlobalException;
import com.lgali.request.security.SupabaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
@Slf4j
public class DepositRequestResource {

    private final DepositRequestService depositRequestService;

    @PostMapping(value = "/receive", consumes = { MediaType.APPLICATION_JSON_VALUE,
                                                  MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<DepositRequestDTO> receiveDepositRequest(final @AuthenticationPrincipal SupabaseUser user,
                                                                   final @RequestBody DepositRequestBody depositRequestBody)
      throws GlobalException, IOException {

        log.info("Receive request {} from user {} ", depositRequestBody, user.getId());

        final DepositRequestDTO dto = DepositRequestDTO.builder()
                                                       .userID(user.getId())
                                                       .contentImage(depositRequestBody.getContentImage())
                                                       .requestText(depositRequestBody.getRequestText())
                                                       .latitude(depositRequestBody.getLatitude())
                                                       .longitude(depositRequestBody.getLongitude())
                                                       .build();

        final DepositRequestDTO savedDTO =
          depositRequestService.saveAndSendDepositRequest(dto);

        return ResponseEntity.ok(savedDTO);
    }

    @GetMapping(value = "/get/all",
      produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<List<DepositRequestDTO>> getAllRequests(final @AuthenticationPrincipal SupabaseUser user)
      throws GlobalException {
        log.info("Get all requests for user {} ", user.getId());
        final List<DepositRequestDTO> depositRequestDTOList = depositRequestService.getAllRequests(user.getId());
        return ResponseEntity.ok(depositRequestDTOList);
    }

    @PutMapping(value = "/status/{id}")
    public ResponseEntity<String> updateStatus(@PathVariable(value = "id") Long id) {
        log.info("Update status for request ID {} ", id);
        depositRequestService.updateStatusToFailed(id);
        return ResponseEntity.ok("Failed status");
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<String> deleteByIDId(@PathVariable(value = "id") Long id)
      throws GlobalException {
        depositRequestService.deleteByID(id);
        return ResponseEntity.ok("deleted successfully");
    }

}

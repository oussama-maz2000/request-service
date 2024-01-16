package com.lgali.request.deposit;

import java.time.ZonedDateTime;

import com.lgali.common.enums.DepositRequestStatus;
import lombok.Data;

@Data
public class DepositRequestBody {
    private Long id;
    private byte[] contentImage;
    private String requestText;
    private double longitude;
    private double               latitude;
    private DepositRequestStatus status;
    private String               userID;
    private ZonedDateTime        receivedRequestTimeUTC;
    private ZonedDateTime        lastUpdateUTC;
}

package com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustlessWorkSendResponse {
    private String status;
    private String message;
    private String contractId;
    private String txHash;
    private EscrowData escrow;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowData {
        private BigDecimal amount;
        private Map<String, String> roles;
        private Map<String, Object> flags;
        private String description;
        private String engagementId;
        private List<MilestoneData> milestones;
        private Double platformFee;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MilestoneData {
            private Boolean approved;
            private String description;
            private String evidence;
            private String status;
            private String amount;
        }
    }
}
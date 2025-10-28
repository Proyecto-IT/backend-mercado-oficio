package com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustlessWorkEscrowData {
    private String contractId;
    private String signer;
    private String type;
    private String engagementId;
    private String title;
    private String description;
    private List<MilestoneInfo> milestones;
    private Double platformFee;
    private Long receiverMemo;
    private Map<String, String> roles;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneInfo {
        private Boolean approved;
        private String description;
        private String evidence;
        private String status;
        private String amount;
        private Map<String, Boolean> flags;
    }
}
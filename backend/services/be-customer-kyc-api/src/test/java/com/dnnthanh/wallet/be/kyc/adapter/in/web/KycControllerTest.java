package com.dnnthanh.wallet.be.kyc.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dnnthanh.wallet.be.kyc.adapter.in.web.mapper.KycApiMapper;
import com.dnnthanh.wallet.be.kyc.api.request.UpsertKycDraftRequest;
import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import com.dnnthanh.wallet.be.kyc.application.port.in.GetKycReviewQuery;
import com.dnnthanh.wallet.be.kyc.application.port.in.GetMyKycQuery;
import com.dnnthanh.wallet.be.kyc.application.port.in.ReviewKycUseCase;
import com.dnnthanh.wallet.be.kyc.application.port.in.SubmitMyKycUseCase;
import com.dnnthanh.wallet.be.kyc.application.port.in.UpsertMyKycDraftUseCase;
import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycStatus;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class KycControllerTest {
    @Mock private GetMyKycQuery getMyKycQuery;
    @Mock private UpsertMyKycDraftUseCase upsertMyKycDraftUseCase;
    @Mock private SubmitMyKycUseCase submitMyKycUseCase;
    @Mock private GetKycReviewQuery getKycReviewQuery;
    @Mock private ReviewKycUseCase reviewKycUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        KycController controller =
                new KycController(
                        getMyKycQuery,
                        upsertMyKycDraftUseCase,
                        submitMyKycUseCase,
                        getKycReviewQuery,
                        reviewKycUseCase,
                        Mappers.getMapper(KycApiMapper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void draftResponseMasksRawDocumentAndFingerprint() throws Exception {
        when(upsertMyKycDraftUseCase.upsertMyDraft(any())).thenReturn(view());

        mockMvc.perform(
                        put("/private/api/v1/kyc/me/draft")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "legalName":"Nguyen Van A",
                                          "dateOfBirth":"1998-04-12",
                                          "nationality":"VN",
                                          "documentType":"NATIONAL_ID",
                                          "documentNumber":"123456789012",
                                          "documentCountry":"VN",
                                          "documentExpiresAt":"2032-04-12",
                                          "addressLine1":"1 Nguyen Hue",
                                          "city":"Ho Chi Minh City",
                                          "country":"VN"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentLast4").value("9012"))
                .andExpect(content().string(not(containsString("123456789012"))))
                .andExpect(content().string(not(containsString("fingerprint-1"))));
    }

    @Test
    void endpointsDeclareRequiredEffectivePermissions() throws Exception {
        assertPermission("getMyKyc", "KYC_SELF_READ");
        assertPermission("upsertMyDraft", "KYC_SELF_WRITE", UpsertKycDraftRequest.class);
        assertPermission("submitMyKyc", "KYC_SELF_SUBMIT");
        assertPermission("getReview", "KYC_REVIEW", UUID.class);
        assertPermission(
                "review",
                "KYC_REVIEW",
                UUID.class,
                com.dnnthanh.wallet.be.kyc.api.request.KycReviewDecisionRequest.class);
    }

    private static void assertPermission(
            String methodName, String permission, Class<?>... parameterTypes) throws Exception {
        Method method = KycController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).contains(permission);
    }

    private static KycView view() {
        return new KycView(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "user-1",
                "/bank/demo-branch",
                "Nguyen Van A",
                LocalDate.of(1998, 4, 12),
                "VN",
                KycDocumentType.NATIONAL_ID,
                "9012",
                "VN",
                LocalDate.of(2032, 4, 12),
                "1 Nguyen Hue",
                "Ho Chi Minh City",
                "VN",
                KycStatus.DRAFT,
                null,
                null,
                null,
                null,
                Instant.parse("2026-08-06T10:00:00Z"),
                Instant.parse("2026-08-06T10:00:00Z"));
    }
}

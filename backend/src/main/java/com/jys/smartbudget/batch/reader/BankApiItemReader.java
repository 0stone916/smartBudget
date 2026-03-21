package com.jys.smartbudget.batch.reader;

import com.jys.smartbudget.dto.BankTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@StepScope
public class BankApiItemReader extends AbstractItemCountingItemStreamItemReader<BankTransactionResponse> {

    private final RestTemplate restTemplate = new RestTemplate();
    private List<BankTransactionResponse> results;
    private int page = 0;
    private final int pageSize = 100;

    private final String targetDate;
    private final String accountNumber;

    // JobParameters에서 accountNumber와 targetDate를 모두 주입받음
    public BankApiItemReader(
            @Value("#{jobParameters['targetDate']}") String targetDate,
            @Value("#{jobParameters['accountNumber']}") String accountNumber) {
        this.targetDate = targetDate;
        this.accountNumber = accountNumber;
        setName("bankApiItemReader");
    }

    @Override
    protected BankTransactionResponse doRead() throws Exception {
        if (results == null || results.isEmpty()) {
            results = fetchFromBankApi();
            if (results == null || results.isEmpty()) {
                return null;
            }
        }
        return results.remove(0);
    }

    private List<BankTransactionResponse> fetchFromBankApi() {
        // URL에 accountNumber 파라미터 추가
        String url = String.format("http://localhost:8081/api/v1/transactions?date=%s&accountNumber=%s&page=%d&size=%d",
                targetDate, accountNumber, page, pageSize);

        log.debug("Bank API 요청 URL -> {}", url);

        try {
            ResponseEntity<BankTransactionResponse[]> response = restTemplate.getForEntity(url,
                    BankTransactionResponse[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                log.debug("받은 데이터 개수 -> {}", response.getBody().length);
                page++;
                return new ArrayList<>(Arrays.asList(response.getBody()));
            }
        } catch (Exception e) {
            log.error("API 호출 중 에러 발생 (계좌: {}, 날짜: {}) -> {}", accountNumber, targetDate, e.getMessage());
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    protected void doOpen() throws Exception {
    }

    @Override
    protected void doClose() throws Exception {
    }
}
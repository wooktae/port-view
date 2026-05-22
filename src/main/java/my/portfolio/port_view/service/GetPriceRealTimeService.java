package my.portfolio.port_view.service;

import my.portfolio.port_view.config.ConnectorProperties;
import my.portfolio.port_view.dto.GetPriceRealTimeDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GetPriceRealTimeService {

    private final RestTemplate restTemplate;
    private final ConnectorProperties connectorProperties;

    public GetPriceRealTimeService(ConnectorProperties connectorProperties) {
        this.restTemplate = new RestTemplate();
        this.connectorProperties = connectorProperties;
    }

    public GetPriceRealTimeDTO getRealtimePrice(String code) {
        String resolvedCode = code == null ? "" : code.trim();

        if (resolvedCode.isEmpty()) {
            return null;
        }

        String url = UriComponentsBuilder
                .fromUriString(connectorProperties.buildUrl(
                        connectorProperties.getApi().getRealtimePricePath()
                ))
                .queryParam("code", resolvedCode)
                .toUriString();

        try {
            GetPriceRealTimeDTO dto = restTemplate.getForObject(url, GetPriceRealTimeDTO.class);

            if (dto != null) {
                dto.setCode(resolvedCode);
            }

            return dto;
        } catch (Exception e) {
            System.err.println("[GetPriceRealTimeService] 실시간 시세 조회 실패. code="
                    + resolvedCode + ", url=" + url + ", message=" + e.getMessage());
            return null;
        }
    }
}
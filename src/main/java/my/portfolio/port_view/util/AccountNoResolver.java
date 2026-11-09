package my.portfolio.port_view.util;

import my.portfolio.port_view.config.PortfolioViewProperties;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AccountNoResolver {

    private final PortfolioViewProperties portfolioViewProperties;

    public AccountNoResolver(PortfolioViewProperties portfolioViewProperties) {
        this.portfolioViewProperties = portfolioViewProperties;
    }

    public String resolve(String accountNo) {
        if (accountNo != null && !accountNo.isBlank()) {
            String normalized = accountNo.trim();
            if (!isValid(normalized)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account number format.");
            }
            return normalized;
        }

        String configured = portfolioViewProperties.getAccount().getDefaultAccountNo();
        if (configured == null || !isValid(configured.trim())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Default account configuration is invalid.");
        }
        return configured.trim();
    }

    private boolean isValid(String accountNo) {
        return accountNo.matches("[0-9]{8}");
    }
}

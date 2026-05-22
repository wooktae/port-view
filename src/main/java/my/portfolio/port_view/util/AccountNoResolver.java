package my.portfolio.port_view.util;

import my.portfolio.port_view.config.PortfolioViewProperties;
import org.springframework.stereotype.Component;

@Component
public class AccountNoResolver {

    private final PortfolioViewProperties portfolioViewProperties;

    public AccountNoResolver(PortfolioViewProperties portfolioViewProperties) {
        this.portfolioViewProperties = portfolioViewProperties;
    }

    public String resolve(String accountNo) {
        if (accountNo != null && !accountNo.isBlank()) {
            return accountNo;
        }

        return portfolioViewProperties.getAccount().getDefaultAccountNo();
    }
}
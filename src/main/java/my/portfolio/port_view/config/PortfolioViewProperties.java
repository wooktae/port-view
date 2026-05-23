package my.portfolio.port_view.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portfolio.view")
public class PortfolioViewProperties {

    private Account account = new Account();

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public static class Account {

        private String defaultAccountNo;

        public String getDefaultAccountNo() {
            return defaultAccountNo;
        }

        public void setDefaultAccountNo(String defaultAccountNo) {
            this.defaultAccountNo = defaultAccountNo;
        }
    }
}

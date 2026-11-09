package my.portfolio.port_view.util;

import my.portfolio.port_view.config.PortfolioViewProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class AccountNoResolverTests {
    private final PortfolioViewProperties properties = new PortfolioViewProperties();
    private final AccountNoResolver resolver = new AccountNoResolver(properties);

    @Test
    void resolvesOnlyEightAsciiDigitsAndPreservesExplicitSelection() {
        properties.getAccount().setDefaultAccountNo("00000001"); // Synthetic fixture only.
        assertEquals("00000001", resolver.resolve(null));
        assertEquals("00000001", resolver.resolve(" "));
        assertEquals("00000002", resolver.resolve(" 00000002 "));
    }

    @Test
    void rejectsMalformedRequestWithoutEchoingItsValue() {
        for (String input : new String[]{"{\"PAPER_ACNT\":\"private-value\"}", "[object Object]",
                "1234567", "123456789", "１２３４５６７８", "00000001-01", "-", "private-value"}) {
            ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> resolver.resolve(input));
            assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
            assertEquals("Invalid account number format.", error.getReason());
            assertFalse(error.getMessage().contains(input));
        }
    }

    @Test
    void failsClosedForMissingOrJsonDefaultWithoutParsingSecretContents() {
        for (String configured : new String[]{null, "", " ", "{\"PAPER_ACNT\":\"private-value\"}"}) {
            properties.getAccount().setDefaultAccountNo(configured);
            ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> resolver.resolve(null));
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
            assertEquals("Default account configuration is invalid.", error.getReason());
        }
    }
}

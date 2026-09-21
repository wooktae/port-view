package my.portfolio.port_view.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

class LocaleSwitchIntegrationTests {

    private static final String LOCALE_PROBE_PATH = "/locale-probe";

    @Test
    void selectsPersistsAndChangesLocaleThroughTheMvcRequestFlow() throws Exception {
        LocaleConfig localeConfig = new LocaleConfig();
        LocaleResolver localeResolver = localeConfig.localeResolver();

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new LocaleProbeController())
                .setLocaleResolver(localeResolver)
                .addInterceptors(localeConfig.localeChangeInterceptor())
                .build();

        MvcResult koreanSelection = mockMvc.perform(get(LOCALE_PROBE_PATH).queryParam("lang", "ko"))
                .andExpect(status().isOk())
                .andExpect(content().string("ko"))
                .andReturn();

        Cookie koreanCookie = koreanSelection.getResponse().getCookie(LocaleConfig.LOCALE_COOKIE_NAME);
        assertNotNull(koreanCookie);
        assertEquals("ko", koreanCookie.getValue());

        mockMvc.perform(get(LOCALE_PROBE_PATH).cookie(koreanCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("ko"));

        MvcResult englishSelection = mockMvc.perform(
                        get(LOCALE_PROBE_PATH)
                                .cookie(koreanCookie)
                                .queryParam("lang", "en")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("en"))
                .andReturn();

        Cookie englishCookie = englishSelection.getResponse().getCookie(LocaleConfig.LOCALE_COOKIE_NAME);
        assertNotNull(englishCookie);
        assertEquals("en", englishCookie.getValue());
    }

    @RestController
    static class LocaleProbeController {

        @GetMapping(LOCALE_PROBE_PATH)
        String currentLocale() {
            return LocaleContextHolder.getLocale().getLanguage();
        }
    }
}

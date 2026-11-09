package my.portfolio.port_view.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import my.portfolio.port_view.dto.dashboard.DashboardViewDTO;
import my.portfolio.port_view.dto.order.OrderDetailPageDTO;
import my.portfolio.port_view.dto.order.OrderPageDTO;
import my.portfolio.port_view.dto.position.PositionPageDTO;
import my.portfolio.port_view.dto.strategy.*;
import my.portfolio.port_view.entity.*;
import my.portfolio.port_view.repository.*;
import my.portfolio.port_view.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("ci")
@SpringBootTest(properties = {
        "portfolio.view.account.default-account-no=00000001",
        "portfolio.dashboard.snapshot-refresh-enabled=false"
})
class AccountContextIntegrationTests {
    // These accounts and all repository data are synthetic; no RDS or AWS access.
    private static final String DEFAULT_ACCOUNT = "00000001";
    private static final String SELECTED_ACCOUNT = "00000002";
    @Autowired WebApplicationContext context;
    @MockitoBean ConnectorBalanceSnapshotRepository balances;
    @MockitoBean ConnectorPositionSnapshotRepository positions;
    @MockitoBean ConnectorOrderRequestRepository orders;
    @MockitoBean ConnectorOrderEventRepository events;
    @MockitoBean ConnectorFillRepository fills;
    @MockitoBean StrategyExecutionViewService strategies;
    @MockitoBean ReportService reports;
    @MockitoBean OperatingResearchVersionProvider operatingVersion;
    @MockitoBean ResearchChampionPromotionService promotion;
    @MockitoBean ConnectorSnapshotRefreshService snapshotRefresh;
    @MockitoBean ConnectorOrderRefreshService orderRefresh;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(strategies.getPlanPage()).thenReturn(new StrategyExecutionPlanPageDto());
        when(strategies.getCurrentPositionSummary()).thenReturn(java.util.Map.of());
        ConnectorBalanceSnapshot balance = new ConnectorBalanceSnapshot();
        balance.setAccountNo(DEFAULT_ACCOUNT);
        balance.setAsOfDate(LocalDate.of(2026, 11, 9));
        balance.setTotalEvalAmount(new BigDecimal("9000000"));
        when(balances.findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(DEFAULT_ACCOUNT)).thenReturn(Optional.of(balance));
        when(balances.findTopByAccountNoOrderByAsOfDateAscAsOfTsAsc(DEFAULT_ACCOUNT)).thenReturn(Optional.of(balance));
        ConnectorPositionSnapshot position = new ConnectorPositionSnapshot();
        position.setAccountNo(DEFAULT_ACCOUNT);
        position.setTickerCode("000001");
        position.setStockName("Fixture Stock");
        position.setQuantity(1);
        position.setAsOfDate(balance.getAsOfDate());
        position.setEvalAmount(new BigDecimal("200000"));
        when(positions.findLatestPositionsByAccountNo(DEFAULT_ACCOUNT)).thenReturn(List.of(position));
        when(positions.findPositionHistoryByAccountNo(DEFAULT_ACCOUNT)).thenReturn(List.of(position));
        List<ConnectorOrderRequest> requests = LongStream.rangeClosed(1, 53).mapToObj(this::order).toList();
        when(orders.findRecentOrdersByAccountNo(DEFAULT_ACCOUNT, 100)).thenReturn(requests);
        when(orders.findOneByIdAndAccountNo(1L, DEFAULT_ACCOUNT)).thenReturn(requests.get(0));
        when(orders.findOrderChain(1L, DEFAULT_ACCOUNT)).thenReturn(List.of(requests.get(0)));
        ConnectorFill fill = new ConnectorFill();
        fill.setId(1L);
        fill.setOrderRequestId(1L);
        fill.setFillQty(1);
        when(fills.findFillsByOrderRequestIds(List.of(1L))).thenReturn(List.of(fill));
    }

    private ConnectorOrderRequest order(long id) {
        ConnectorOrderRequest order = new ConnectorOrderRequest();
        order.setId(id);
        order.setAccountNo(DEFAULT_ACCOUNT);
        order.setTickerCode("000001");
        order.setRequestType("BUY");
        order.setRequestStatus("FILLED");
        order.setRequestedAt(OffsetDateTime.parse("2026-11-04T09:00:00Z"));
        return order;
    }

    @Test
    void firstDashboardAndAccountViewsUseTheSameValidatedDefault() throws Exception {
        MvcResult dashboard = mvc.perform(get("/"))
                .andExpect(status().isOk()).andExpect(model().attribute("accountNo", DEFAULT_ACCOUNT)).andReturn();
        assertSidebar(dashboard, DEFAULT_ACCOUNT);
        DashboardViewDTO dto = (DashboardViewDTO) dashboard.getModelAndView().getModel().get("dashboard");
        assertEquals("2026-11-09", dto.getAsOfDate());
        assertEquals(1, dto.getPositionCount());
        MvcResult balance = mvc.perform(get("/balance-summary").queryParam("accountNo", DEFAULT_ACCOUNT))
                .andExpect(status().isOk()).andExpect(model().attribute("holdingCount", 1)).andReturn();
        assertSidebar(balance, DEFAULT_ACCOUNT);
        MvcResult position = mvc.perform(get("/positions").queryParam("accountNo", DEFAULT_ACCOUNT))
                .andExpect(status().isOk()).andReturn();
        assertSidebar(position, DEFAULT_ACCOUNT);
        assertEquals(1, ((PositionPageDTO) position.getModelAndView().getModel().get("page")).getPositionCount());
        MvcResult orderList = mvc.perform(get("/orders").queryParam("accountNo", DEFAULT_ACCOUNT))
                .andExpect(status().isOk()).andReturn();
        assertEquals(53, ((OrderPageDTO) orderList.getModelAndView().getModel().get("page")).getTotalCount());
        MvcResult detail = mvc.perform(get("/orders/1").queryParam("accountNo", DEFAULT_ACCOUNT))
                .andExpect(status().isOk()).andReturn();
        assertEquals(1, ((OrderDetailPageDTO) detail.getModelAndView().getModel().get("page")).getFillCount());
        verifyNoInteractions(snapshotRefresh, orderRefresh, promotion);
    }

    @Test
    void explicitAccountSurvivesMenuAndLanguageNavigationWithoutChangingStrategyQueries() throws Exception {
        for (String path : List.of("/dashboard", "/balance-summary", "/positions", "/orders",
                "/strategy/execution/plans", "/strategy/reports/latest")) {
            MvcResult result = mvc.perform(get(path).queryParam("accountNo", SELECTED_ACCOUNT).queryParam("lang", "ko"))
                    .andExpect(status().isOk()).andExpect(model().attribute("accountNo", SELECTED_ACCOUNT)).andReturn();
            assertSidebar(result, SELECTED_ACCOUNT);
        }
        verify(strategies).getPlanPage();
        verify(reports).getLatestReport();
        verifyNoInteractions(snapshotRefresh, orderRefresh, promotion);
    }

    @Test
    void invalidInputIsRejectedBeforeAnyAccountQueryOrExecution() throws Exception {
        clearInvocations(balances, positions, orders, strategies, reports);
        for (String path : List.of("/", "/dashboard", "/balance-summary", "/positions", "/orders",
                "/strategy/execution/plans", "/strategy/reports/latest")) {
            mvc.perform(get(path).queryParam("accountNo", "{\"PAPER_ACNT\":\"private-value\"}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(balances, positions, orders, strategies, reports, snapshotRefresh, orderRefresh, promotion);
    }

    @Test
    void reportVersionSelectionKeepsAccountAndExistingVersionQuery() throws Exception {
        when(reports.getStrategyConfigVersions()).thenReturn(List.of("version-fixture"));
        when(operatingVersion.getOperatingVersion()).thenReturn("version-fixture");
        ReportBacktestSummaryDto summary = new ReportBacktestSummaryDto(1L, "run-fixture", "strategy-fixture",
                "engine-fixture", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 11, 8),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, 0, null);
        when(reports.getLatestReportByStrategyConfigVersion("version-fixture"))
                .thenReturn(new ReportStrategyPageDto(summary, List.of(), List.of(), List.of(), List.of(),
                        List.of(), List.of(), List.of(), List.of()));
        MvcResult result = mvc.perform(get("/strategy/reports/latest").queryParam("version", "version-fixture")
                        .queryParam("accountNo", SELECTED_ACCOUNT))
                .andExpect(status().isOk()).andExpect(model().attribute("selectedVersion", "version-fixture")).andReturn();
        assertSidebar(result, SELECTED_ACCOUNT);
        assertTrue(result.getResponse().getContentAsString().contains("name=\"accountNo\" value=\"" + SELECTED_ACCOUNT + "\""));
        verify(reports).getLatestReportByStrategyConfigVersion("version-fixture");
    }

    @Test
    void historicalPlanDoesNotReplaceTheNavigationAccount() throws Exception {
        StrategyExecutionPlanDto plan = new StrategyExecutionPlanDto(7L, LocalDate.of(2026, 8, 13),
                "strategy-fixture", "version-fixture", null, "NEUTRAL", BigDecimal.ZERO, "NORMAL",
                DEFAULT_ACCOUNT, "READY", 0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, null, null);
        StrategyExecutionPlanPageDto page = new StrategyExecutionPlanPageDto();
        page.setPlans(List.of(plan));
        when(strategies.getPlanPage()).thenReturn(page);
        when(strategies.getPlan(7L)).thenReturn(plan);
        for (String path : List.of("/strategy/execution/plans", "/strategy/execution/plans/7")) {
            MvcResult result = mvc.perform(get(path).queryParam("accountNo", SELECTED_ACCOUNT))
                    .andExpect(status().isOk()).andReturn();
            assertSidebar(result, SELECTED_ACCOUNT);
        }
        verify(strategies).getPlan(7L);
        verifyNoInteractions(promotion, snapshotRefresh, orderRefresh);
    }

    private void assertSidebar(MvcResult result, String account) throws Exception {
        String html = result.getResponse().getContentAsString();
        for (String path : List.of("/dashboard", "/balance-summary", "/positions", "/orders",
                "/strategy/execution/plans", "/strategy/reports/latest")) {
            assertTrue(html.contains("href=\"" + path + "?accountNo=" + account + "\""), path);
        }
        assertTrue(html.contains("lang=en&amp;accountNo=" + account));
        assertTrue(html.contains("lang=ko&amp;accountNo=" + account));
        assertFalse(html.contains("accountNo=%7B"));
        assertFalse(html.contains("PAPER_ACNT"));
    }
}

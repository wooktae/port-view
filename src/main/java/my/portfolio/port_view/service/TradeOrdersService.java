package my.portfolio.port_view.service;

import my.portfolio.port_view.entity.TradeOrders;
import my.portfolio.port_view.repository.TradeOrdersRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeOrdersService {

    private final TradeOrdersRepository tradeOrdersRepository;

    public TradeOrdersService(TradeOrdersRepository tradeOrdersRepository) {
        this.tradeOrdersRepository = tradeOrdersRepository;
    }

    public List<TradeOrders> listByAccount(String accountNo) {
        return tradeOrdersRepository.findByAccountNoOrderByOrderTimeDescIdDesc(accountNo);
    }
}
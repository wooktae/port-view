package my.portfolio.port_view.repository;

import my.portfolio.port_view.entity.BalanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceSummaryRepository extends JpaRepository<BalanceSummary, Long> {

    List<BalanceSummary> findByAccountNoOrderByAsOfDateDescIdDesc(String accountNo);

    Optional<BalanceSummary> findTopByAccountNoOrderByAsOfDateDescIdDesc(String accountNo);
}
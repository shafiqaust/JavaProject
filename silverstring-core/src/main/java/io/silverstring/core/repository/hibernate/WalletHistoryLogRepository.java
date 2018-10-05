package io.silverstring.core.repository.hibernate;

import io.silverstring.domain.hibernate.WalletHistoryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletHistoryLogRepository extends JpaRepository<WalletHistoryLog, Long> {
}

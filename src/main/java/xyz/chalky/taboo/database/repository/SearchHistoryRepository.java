package xyz.chalky.taboo.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import xyz.chalky.taboo.database.model.SearchHistory;

import java.util.List;

@Repository
@Component
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    @Query("select s from SearchHistory s where s.userId = ?1")
    List<SearchHistory> findByUserId(Long userId);

}
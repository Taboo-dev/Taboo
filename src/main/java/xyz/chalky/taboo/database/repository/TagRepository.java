package xyz.chalky.taboo.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import xyz.chalky.taboo.database.model.Tag;

import java.util.List;

@Repository
@Component
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("select t from Tag t where t.guildId = ?1")
    List<Tag> findByGuildId(Long guildId);

}
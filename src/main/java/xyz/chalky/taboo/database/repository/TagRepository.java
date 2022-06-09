package xyz.chalky.taboo.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import xyz.chalky.taboo.database.model.Tag;

@Repository
@Component
public interface TagRepository extends JpaRepository<Tag, Long> {}
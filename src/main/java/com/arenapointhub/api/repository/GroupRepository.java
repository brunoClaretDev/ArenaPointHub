package com.arenapointhub.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.arenapointhub.api.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    // Adicione esta linha para permitir a busca de grupos por ID de categoria
    List<Group> findByCategoryId(Long categoryId);

}
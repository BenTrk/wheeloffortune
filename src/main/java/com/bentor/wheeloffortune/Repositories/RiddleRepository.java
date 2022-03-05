package com.bentor.wheeloffortune.Repositories;

import com.bentor.wheeloffortune.Classes.Riddle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RiddleRepository extends JpaRepository<Riddle, Integer> {
    List<Riddle> findRiddleByWasUsed(@Param("wasUsed") Boolean wasUsed);
}

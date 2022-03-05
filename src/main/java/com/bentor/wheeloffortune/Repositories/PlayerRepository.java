package com.bentor.wheeloffortune.Repositories;

import com.bentor.wheeloffortune.Classes.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}

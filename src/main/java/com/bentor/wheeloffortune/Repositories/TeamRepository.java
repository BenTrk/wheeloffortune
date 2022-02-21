package com.bentor.wheeloffortune.Repositories;

import com.bentor.wheeloffortune.Classes.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, String> {
    Team findTeamByName(@Param("name") String teamName);
}

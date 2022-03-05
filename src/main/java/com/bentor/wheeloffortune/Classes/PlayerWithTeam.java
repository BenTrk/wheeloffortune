package com.bentor.wheeloffortune.Classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerWithTeam {
    Long playerId;
    String name;
    String teamName;
    Long teamId;
}

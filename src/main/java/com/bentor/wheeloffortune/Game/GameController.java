package com.bentor.wheeloffortune.Game;

import com.bentor.wheeloffortune.Repositories.PlayerRepository;
import com.bentor.wheeloffortune.Repositories.RiddleRepository;
import com.bentor.wheeloffortune.Repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@CrossOrigin("http://localhost:8081/")
public class GameController {

    private final GameService gameService;
    private final RiddleRepository riddleRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public GameController(GameService gameService, RiddleRepository riddleRepository, TeamRepository teamRepository, PlayerRepository playerRepository){
        this.gameService = gameService;
        this.riddleRepository = riddleRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @GetMapping(path = "/game")
    public String getRiddle() throws IOException {
        if(riddleRepository.count()<1){
            gameService.readRiddles(riddleRepository);
        }
        // First, send just '_' for every character. (charSequence, copy that to sendCharSequence,
        // replace every letter with '_'). Return String sendCharSequenceToString.
        // Make it as post mapping. In body, request:
        // String guess - so the software can see if a guess is coming in
        //    - case notEmpty: see if it is the correct answer. Calculate points, get new riddle
        //      or calculate points lost and move on
        //    - case empty: ignore
        // char character - so the software can check if there is a hit.
        //    - get charSequence, go through it and count occurrences. If more than 0,
        //      include these to the right place in sendCharSequence.
        //      Calculate points and send back sendCharSequenceToString.
        return gameService.getRiddle(riddleRepository);
    }

    //Testing purposes!
    @GetMapping(path="/setup")
    public String setTeamAndPlayers(){
        teamRepository.save(gameService.createTeam("Csapatnév"));
        playerRepository.save(gameService.createPlayer(teamRepository, "Béla", "Csapatnév"));
        playerRepository.save(gameService.createPlayer(teamRepository, "Géza", "Csapatnév"));
        playerRepository.save(gameService.createPlayer(teamRepository, "Kató", "Csapatnév"));
        return "Teams and players set.";
    }
}

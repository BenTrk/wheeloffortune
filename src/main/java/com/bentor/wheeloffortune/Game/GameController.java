package com.bentor.wheeloffortune.Game;

import com.bentor.wheeloffortune.Classes.Guess;
import com.bentor.wheeloffortune.Classes.Team;
import com.bentor.wheeloffortune.Repositories.PlayerRepository;
import com.bentor.wheeloffortune.Repositories.RiddleRepository;
import com.bentor.wheeloffortune.Repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin("http://localhost:8081/")
public class GameController {

    private final GameService gameService;
    private final RiddleRepository riddleRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private String riddle;

    @Autowired
    public GameController(GameService gameService, RiddleRepository riddleRepository,
                          TeamRepository teamRepository,
                          PlayerRepository playerRepository) throws IOException {
        this.gameService = gameService;
        this.riddleRepository = riddleRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;

        gameService.readRiddles(riddleRepository);
        this.riddle = gameService.getRiddle(riddleRepository);
    }

    //this may be not needed.
    @GetMapping(path = "/getriddle")
    public String getRiddle() throws IOException {
        if(riddleRepository.count()<1){
            gameService.readRiddles(riddleRepository);
        }
        return gameService.getRiddle(riddleRepository);
    }

    //this is to show the riddle
    @GetMapping(path = "/game")
    public String gameLogic() {
        return gameService.turnRiddleToCode(riddle);
    }

    //this is to get the guesses for chars
    @PostMapping(path = "/guesschar")
    @ResponseBody
    public String guessChar(@RequestBody Guess guess){
        Team team = teamRepository.findTeamByName(guess.getTeamName());
        return gameService.guessFunction(team, guess.getPrize(), guess.getGuess(),
                riddle, teamRepository);
    }

    //this is for special stuff
    @PostMapping(path = "/special")
    @ResponseBody
    public String specialCards(){ return null; }

    //this is to get the guess for the riddle
    @PostMapping(path = "/guessriddle")
    public String guessRiddle(){ return null; }

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

package com.bentor.wheeloffortune.Game;

import com.bentor.wheeloffortune.Classes.Guess;
import com.bentor.wheeloffortune.Classes.Prize;
import com.bentor.wheeloffortune.Classes.PrizeMoney;
import com.bentor.wheeloffortune.Classes.Team;
import com.bentor.wheeloffortune.Repositories.PlayerRepository;
import com.bentor.wheeloffortune.Repositories.RiddleRepository;
import com.bentor.wheeloffortune.Repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin("http://localhost:8081/")
public class GameController {

    private final GameService gameService;
    private final RiddleRepository riddleRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private String riddle;
    private String codedRiddle;
    private final ArrayList<Prize> prizeList;
    private Team teamInPlay;
    private Prize prize;
    private Integer guessMoney;

    @Autowired
    public GameController(GameService gameService, RiddleRepository riddleRepository,
                          TeamRepository teamRepository,
                          PlayerRepository playerRepository) throws IOException {
        this.gameService = gameService;
        this.riddleRepository = riddleRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;

        gameService.readRiddles(riddleRepository);
        this.prizeList = gameService.prizeInitialize();
    }

    //this may be not necessary
    @GetMapping(path = "/newriddle")
    public String getNewRiddle() {
        this.riddle = gameService.getRiddle(this.riddleRepository);
        return "New riddle set.";
    }
    //this is to show the riddle
    //when start and when new riddle, getTeamInPlay() should be called!
    @GetMapping(path = "/game")
    public String gameLogic() {
        this.riddle = gameService.getRiddle(this.riddleRepository);
        this.codedRiddle = gameService.turnRiddleToCode(this.riddle);
        return this.codedRiddle;
    }

    //this is to handle which team can play
    //check this with get http every time needed
    @GetMapping(path = "/teaminplay")
    public String getTeamInPlay(){
        this.teamInPlay = gameService.whichTeamPlays(this.teamInPlay, this.teamRepository);
        return teamInPlay.getName();
    }

    //this is to send the list of teams to show actual standings
    @GetMapping(path = "/teamlist")
    public List<Team> getTeamList(){
        return teamRepository.findAll();
    }

    //send list of teams every time needed!(to update standings)
    //send button - send char guess - also dialog! Dialog every time response comes from backend
    //  with prize. For bankrupt, 50/50 and double, just a dialog with an ok button.
    //dialogs - v-if, specials, these have their own buttons
    //guess button - send string guess and guess prize, use dialog for this too!

    //this is to handle incoming ids from prize buttons
    @PostMapping(path = "/prizehandler")
    @ResponseBody
    public List<Boolean> prizeHandler(@RequestBody String id){
        //Gets 0= in body. Dont know why. Something with JSON maybe?
        String realIdString = id.substring(0, id.length()-1);
        int realId = Integer.parseInt(realIdString);
        Collections.shuffle(this.prizeList);
        this.prize = this.prizeList.get(realId);
        return gameService.specialHandler(this.prize, this.teamInPlay, this.teamRepository);
    }

    //This is to send the money value if wheeloffortune shows money
    @GetMapping(path = "/getmoney")
    public Integer getMoney(){
        return this.prize.getValue();
    }

    //this is to get the guesses for chars from dialog Guess
    @PostMapping(path = "/guesschar")
    @ResponseBody
    public PrizeMoney guessChar(@RequestBody String guessChar){
        System.out.println(guessChar);
        Character c = guessChar.charAt(0);
        System.out.println(c);
        PrizeMoney pm = gameService.guessFunction(this.teamInPlay, this.prize.getValue(), c,
                this.riddle, this.codedRiddle, this.teamRepository);
        this.codedRiddle = pm.getRiddle();
        return pm;
    }

    //this is to buy letter (100000 money) from dialog BuyLetter
    @GetMapping(path = "/marketplace")
    public String buyLetter(){
        return gameService.buyLetter(this.teamInPlay, this.teamRepository);
    }

    //this is to silence a team from dialog Silencer
    @PostMapping(path = "/silencer")
    @ResponseBody
    public String silenceTeam(@RequestBody Team team){
        gameService.silenceTeam(team, this.teamRepository);
        return "Team is silenced.";
    }

    //write guess auction function!
    //frontend - guess riddle button click -> dialog Auction. When players say auction is over,
    //   Auctions sends Post request with list of Guesses. Frontend shows Guess Riddle dialog,
    //   sends String guess. Takes boolean Response.
    //backend - receives list, takes the highest bidder, sets teamInPlay and guessMoney. Receives
    //   String guess, calls guessRiddle(), returns boolean Response.

    //this is to get the auction results from dialog Auction
    @PostMapping(path = "/auction")
    @ResponseBody
    public String getAuctionData(@RequestBody List<Guess> guesses){
        Guess highestGuess = gameService.getAuctionData(guesses);
        teamInPlay = highestGuess.getTeam();
        guessMoney = highestGuess.getMoney();
        return "Highest bidder set.";
    }

    //this is to get the guess for the riddle from dialog GuessRiddle
    @PostMapping(path = "/guessriddle")
    @ResponseBody
    public Boolean guessRiddle(@RequestBody String guess){
        return gameService.guessRiddle(guess, this.riddle, this.teamInPlay, this.guessMoney, this.teamRepository);
    }

    //Testing purposes!
    @GetMapping(path="/setup")
    public String setTeamAndPlayers(){
        if (teamRepository.findAll().size() < 1) {
            teamRepository.save(gameService.createTeam("Csapatnév"));
            teamRepository.save(gameService.createTeam("Csapatnév 2"));
            teamRepository.save(gameService.createTeam("Csapatnév 3"));
            playerRepository.save(gameService.createPlayer(teamRepository, "Béla", "Csapatnév"));
            playerRepository.save(gameService.createPlayer(teamRepository, "Géza", "Csapatnév"));
            playerRepository.save(gameService.createPlayer(teamRepository, "Kató", "Csapatnév"));
            playerRepository.save(gameService.createPlayer(teamRepository, "Sanyi", "Csapatnév 2"));
            playerRepository.save(gameService.createPlayer(teamRepository, "Bandi", "Csapatnév 2"));
            playerRepository.save(gameService.createPlayer(teamRepository, "Mari", "Csapatnév 3"));
            playerRepository.save(gameService.createPlayer(teamRepository, "Juli", "Csapatnév 3"));
        }
        return "Teams and players set.";
    }
}

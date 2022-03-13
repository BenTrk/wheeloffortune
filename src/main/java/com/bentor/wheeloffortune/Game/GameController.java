package com.bentor.wheeloffortune.Game;

import com.bentor.wheeloffortune.Classes.*;
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
    private Riddle riddle;
    private Riddle codedRiddle;
    private final ArrayList<Prize> prizeList;
    private Team teamInPlay;
    private Prize prize;
    private Integer guessMoney;
    private Guess highestGuess;
    private Integer round;

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
        this.round = 0;
        this.riddle = gameService.getRiddle(this.riddleRepository);
    }

    //this may be not necessary
    @GetMapping(path = "/newriddle")
    public String getNewRiddle() {
        if (this.round == 5){
            return "End";
        } else {
            this.riddle = gameService.getRiddle(this.riddleRepository);
            return "New riddle set.";
        }
    }
    //this is to show the riddle
    //Failsafe mode should be: frontend does not call this, only currentRiddle;
    //  this.riddle is set when GameController is initiated
    //  bonus: round should be handled as computed, and when GameController is initiated,
    //  this.round = (riddleRepository.findall where wasUsed = true).count
    @GetMapping(path = "/game")
    public Riddle gameLogic() {
        if (this.round == 5){
            Riddle endRiddle = new Riddle("End", "End", false);
            return endRiddle;
        } else {
            //this.riddle = gameService.getRiddle(this.riddleRepository);
            System.out.println("Riddle: " + this.riddle.getRiddle());
            this.codedRiddle = gameService.turnRiddleToCode(this.riddle);
            return this.codedRiddle;
        }
    }

    @GetMapping(path = "/currentriddle")
    public Riddle currentRiddle(){
        if (this.round == 5){
            Riddle endRiddle = new Riddle("End", "End", false);
            return endRiddle;
        } else {
            return this.codedRiddle;
        }
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
                this.riddle.getRiddle(), this.codedRiddle.getRiddle(), this.teamRepository);
        this.codedRiddle.setRiddle(pm.getRiddle());
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

    //this is to get the auction results from dialog Auction
    @PostMapping(path = "/auction")
    @ResponseBody
    public String getAuctionData(@RequestBody Guess guess){
        //get string, and convert it to guess
        Guess g = gameService.getAuctionData(guess, teamRepository);
        if (g != null) {
            this.highestGuess = g;
            teamInPlay = highestGuess.getTeam();
            guessMoney = highestGuess.getMoney();
        }
        return "Highest bidder set.";
    }

    @GetMapping(path = "/highestbid")
    public Team getHighestBidderTeam() { return this.highestGuess.getTeam(); }

    //this is to get the guess for the riddle from dialog GuessRiddle
    @PostMapping(path = "/guessriddle")
    @ResponseBody
    public String guessRiddle(@RequestBody RiddleGuess guess){
        System.out.println(guess.getGuess());
        Boolean isGuessed = gameService.guessRiddle(guess.getGuess(),this.codedRiddle.getRiddle(), this.riddle.getRiddle(), this.teamInPlay, this.guessMoney, this.teamRepository);
        if (isGuessed){
            this.round++;
            System.out.println("Round: " + this.round);
            if (this.round == 5){
                System.out.println("Round: " + this.round);
                this.riddle.setRiddle("End");
                return "End";
            } else {
                System.out.println("Round: " + this.round);
                this.riddle = gameService.getRiddle(this.riddleRepository);
                this.codedRiddle = gameService.turnRiddleToCode(this.riddle);
                this.highestGuess = null;
                return "Congratulations!";
            }
        } else {
            return "No, sorry, but no.";
        }
    }

    @GetMapping(path = "/winner")
    public List<Team> getWinner() {
        return gameService.getWinner(teamRepository);
    }

    @GetMapping(path = "/tableteams")
    public List<TableTeam> getTableTeams() {
        List<Team> allteams = teamRepository.findAll();
        return gameService.getTableTeams(allteams);
    }

    @PostMapping(path = "/createteam")
    @ResponseBody
    public String createTeam(@RequestBody Team team){
        Team t = gameService.createTeam(team.getName());
        t.setIsSilenced(false);
        teamRepository.save(t);
        if (teamRepository.findAll().size() == 1){
            this.teamInPlay = t;
        }
        return "Success.";
    }

    @PostMapping(path = "/deleteteam")
    @ResponseBody
    public String deleteTeam(@RequestBody Team team){
        teamRepository.deleteById(team.getId());
        return "Success";
    }

    @PostMapping(path = "/editteam")
    @ResponseBody
    public String editTeam(@RequestBody Team team){
        Team t = teamRepository.getById(team.getId());
        t.setName(team.getName());
        teamRepository.save(t);
        return "Success";
    }

    @GetMapping(path = "/getplayers")
    public List<PlayerWithTeam> getPlayers(){
        List<PlayerWithTeam> playerWithTeamList = new ArrayList<>();
        for (Player player : playerRepository.findAll()){
            PlayerWithTeam pt = new PlayerWithTeam(player.getId(), player.getName(), player.getTeam().getName(), player.getTeam().getId());
            System.out.println("Team name: " + pt.getTeamName());
            playerWithTeamList.add(pt);
        }
        return playerWithTeamList;
    }

    @PostMapping(path = "/createplayer")
    @ResponseBody
    public String createPlayer(@RequestBody PlayerWithTeam playerWithTeam){
        Team team = teamRepository.findById(playerWithTeam.getTeamId()).get();
        Player player = new Player(playerWithTeam.getName(), team);
        playerRepository.save(player);
        return "Success.";
    }

    @PostMapping(path = "/deleteplayer")
    @ResponseBody
    public String deletePlayer(@RequestBody PlayerWithTeam playerWithTeam){
        playerRepository.deleteById(playerWithTeam.getPlayerId());
        return "Success.";
    }

    @PostMapping(path = "/editplayer")
    @ResponseBody
    public String editPlayer(@RequestBody PlayerWithTeam playerWithTeam){
        Player player = playerRepository.findById(playerWithTeam.getPlayerId()).get();
        Team team = teamRepository.findById(playerWithTeam.getTeamId()).get();
        player.setName(playerWithTeam.getName());
        player.setTeam(team);
        playerRepository.save(player);
        return "Success.";
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

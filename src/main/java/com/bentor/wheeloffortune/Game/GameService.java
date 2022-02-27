package com.bentor.wheeloffortune.Game;

import com.bentor.wheeloffortune.Classes.*;
import com.bentor.wheeloffortune.Finals.PrizeList;
import com.bentor.wheeloffortune.Repositories.RiddleRepository;
import com.bentor.wheeloffortune.Repositories.TeamRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GameService {

    public Team whichTeamPlays(Team teamInPlay, TeamRepository teamRepository){
        Long teamInPlayId;
        List<Team> teamList = teamRepository.findAll();
        try{
            teamInPlayId = teamInPlay.getId();
        } catch (NullPointerException ex){ teamInPlayId = -1L; }
        //if we just started the game, or we are at the end of the list
        if (teamInPlayId == -1L || teamInPlayId.equals(teamList.get(teamList.size() - 1).getId())){
            teamInPlayId = 0L;
            Team t = teamList.get(Math.toIntExact(teamInPlayId));
            Team team = teamRepository.findById(t.getId()).orElse(null);

            while (team.getIsSilenced()){
                teamInPlayId = teamRepository.findById(team.getId()).orElse(null).getId();
                team.setIsSilenced(false);
                teamRepository.save(team);
                if (teamInPlayId < teamList.get(teamList.size()-1).getId()){
                team = teamRepository.findById(teamInPlayId).orElse(null);
                } else {
                    teamInPlayId = 0L;
                    t = teamList.get(Math.toIntExact(teamInPlayId));
                    team = teamRepository.findById(t.getId()).orElse(null);
                }
            }
            return team;
        }
        //if we are not at the end of the list
        else {
            Long i = teamInPlay.getId()+1;
            Optional<Team> optionalTeamInPlay = teamRepository.findById(i);
            //isPresent check!
            Team team = optionalTeamInPlay.orElse(null);
            //Not elegant, but I will leave this as is. Can cause nullpointexception, but I know that it
            //wont. The id I provide above MUST be a valid team id - or the problem is not here.
            while (team.getIsSilenced()){
                i++;
                team.setIsSilenced(false);
                teamRepository.save(team);
                if (i < teamList.get(teamList.size()-1).getId()) {
                    optionalTeamInPlay = teamRepository.findById(i);
                    team = optionalTeamInPlay.orElse(null);
                } else {
                    i = 0L;
                    Team t = teamList.get(Math.toIntExact(teamInPlayId));
                    team = teamRepository.findById(t.getId()).orElse(null);
                }
            }
            return team;
        }
    }

    public void readRiddles(RiddleRepository riddleRepository) throws IOException {
        //read riddle from file - put content in MySQL
        File file = new File("C:/Users/User/IdeaProjects/wheeloffortune/src/main/resources/riddles.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        //read all lines of riddles file
        String line;
        while ((line = br.readLine()) != null) {
            Riddle riddle = new Riddle(line, false);
            riddleRepository.save(riddle);
        }
    }

    public String getRiddle(RiddleRepository riddleRepository) {
        List<Riddle> riddlesList = riddleRepository.findRiddleByWasUsed(false);
        String riddle;
        //generate a random number and get the random riddle, then remove it from the list so it wont be assigned again.
        Random random = new Random();
        int maxRand = riddlesList.size();
        int rnd = random.nextInt(maxRand);
        riddle = riddlesList.get(rnd).getRiddle();
        Riddle currentRiddle = riddlesList.get(rnd);
        currentRiddle.setWasUsed(true);
        riddleRepository.save(currentRiddle);
        return riddle;
    }

    public String turnRiddleToCode(String riddle){
        char[] charSequence = riddle.toCharArray();
        for (int i = 0; i < charSequence.length; i++) {
            char c = charSequence[i];
            if (Character.isLetter(c)) {
                c = '_';
            }
            charSequence[i] = c;
        }
        return String.valueOf(charSequence);
    }

    public Team createTeam(String name){
        return new Team(name);
    }

    public Player createPlayer(TeamRepository teamRepository, String playerName, String teamName){
        //in realtime, use ID here. Send team list to frontend, get id of selected team.
        Team team = teamRepository.findTeamByName(teamName);
        return new Player(playerName, team);
    }

    public PrizeMoney guessFunction(Team team, int prize, Character guess, String riddle, String codedRiddle,
                                TeamRepository teamRepository) {
        char[] charSequence = riddle.toCharArray();
        char[] codedCharSequence = codedRiddle.toCharArray();
        Character g = Character.toLowerCase(guess);
        int counter = 0;
        for (int i = 0; i < charSequence.length; i++) {
            Character c = Character.toLowerCase(charSequence[i]);
            if (!c.equals(g) && Character.isLetter(c)) {
                c = codedCharSequence[i];
            } else if (c.equals(g) && Character.isLetter(c)){
                counter++;
            }
            charSequence[i] = c;
        }
        team.setMoney(prize*counter);
        teamRepository.save(team);

        PrizeMoney prizeMoney = new PrizeMoney(counter, prize*counter, String.valueOf(charSequence));
        //next team comes
        return prizeMoney;
    }

    public ArrayList<Prize> prizeInitialize(){
        return PrizeList.prizeInitialize();
    }

    //on frontend, use v-method, call dialog shower :) in axios or use if in script!
    public List<Boolean> specialHandler(Prize prize, Team team, TeamRepository teamRepository){
        //for readability, added names to booleans - on frontend, ids must be used with dialogs!
        Boolean money = false, bankrupt  = false, fiftyFifty  = false, doubleMoney = false,
                marketplace = false, silencer = false, nothing = false;
        List<Boolean> allMoneyBooleans;
        switch(prize.getSpecial()){
            case '-':
                //get guess, use guessFunction
                //return boolean, and create function to handle the getMoney.
                money = true;
                allMoneyBooleans = Stream.of(money, bankrupt, fiftyFifty, doubleMoney, marketplace, silencer, nothing)
                        .collect(Collectors.toList());
                return allMoneyBooleans;
            case 'b':
                bankrupt = true;
                allMoneyBooleans = Stream.of(money, bankrupt, fiftyFifty, doubleMoney, marketplace, silencer, nothing)
                        .collect(Collectors.toList());
                team.setMoney(0);
                teamRepository.save(team);
                //next team comes
                return allMoneyBooleans;
            case 'h':
                fiftyFifty = true;
                allMoneyBooleans = Stream.of(money, bankrupt, fiftyFifty, doubleMoney, marketplace, silencer, nothing)
                        .collect(Collectors.toList());
                team.setMoney(team.getMoney()/2);
                teamRepository.save(team);
                //next team comes
                return allMoneyBooleans;
            case 'd':
                doubleMoney = true;
                allMoneyBooleans = Stream.of(money, bankrupt, fiftyFifty, doubleMoney, marketplace, silencer, nothing)
                        .collect(Collectors.toList());
                team.setMoney(team.getMoney()*2);
                teamRepository.save(team);
                //next team comes
                return allMoneyBooleans;
            case 'l':
                marketplace = true;
                allMoneyBooleans = Stream.of(money, bankrupt, fiftyFifty, doubleMoney, marketplace, silencer, nothing)
                        .collect(Collectors.toList());
                //set prize of letter! Get bool Y/N to see if team
                //buys letter or not, count money, quizmaster will send the letter for it.
                return allMoneyBooleans;
            case 'o':
                silencer = true;
                allMoneyBooleans = Stream.of(money, bankrupt, fiftyFifty, doubleMoney, marketplace, silencer, nothing)
                        .collect(Collectors.toList());
                //get silenced team --- next team comes function must consider list of silenced teams.
                return allMoneyBooleans;
            case 'n':
                nothing = true;
                allMoneyBooleans = Stream.of(money, bankrupt, fiftyFifty, doubleMoney, marketplace, silencer, nothing)
                        .collect(Collectors.toList());
                //next team comes
                return allMoneyBooleans;
        }
        return null;
    }

    public void buyLetter(Boolean isBuy, Team team, TeamRepository teamRepository) {
        if (isBuy){
            team.setMoney(team.getMoney()-100000);
            teamRepository.save(team);
        }
    }

    public void silenceTeam(Team team, TeamRepository teamRepository) {
        team.setIsSilenced(true);
        teamRepository.save(team);
    }

    public Boolean guessRiddle(String guess, String riddle, Team team, Integer guessMoney, TeamRepository teamRepository) {
        if (guess.equalsIgnoreCase(riddle)){
            team.setMoney(team.getMoney()-guessMoney);
            teamRepository.save(team);
            return true;
        } else {
            team.setMoney(team.getMoney()-guessMoney);
            teamRepository.save(team);
            return false;
        }
    }

    public Guess getAuctionData(List<Guess> guesses) {
        Guess highestGuess = guesses.get(0);
        for(Guess guess : guesses){
            if (guess.getMoney() > highestGuess.getMoney()){
                highestGuess = guess;
            }
        }
        return highestGuess;
    }
}


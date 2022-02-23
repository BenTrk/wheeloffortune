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

@Service
public class GameService {

    public Team whichTeamPlays(Team teamInPlay, TeamRepository teamRepository){
        List<Team> teamList = teamRepository.findAll();
        Long teamInPlayId;
        try{
            teamInPlayId = teamInPlay.getId();
        } catch (NullPointerException ex){ teamInPlayId = -1L; }
        //if we just started the game, or we are at the end of the list
        if (teamInPlayId == -1L || teamInPlayId == teamList.size()-1){
            teamInPlayId = 0L;
            Team team = teamList.get(Math.toIntExact(teamInPlayId));
            while (team.getIsSilenced()){
                teamInPlayId++;
                team.setIsSilenced(false);
                teamRepository.save(team);
                if (teamInPlayId < teamList.size()-1){
                team = teamList.get(Math.toIntExact(teamInPlayId));
                } else {
                    teamInPlayId = 0L;
                    team = teamList.get(Math.toIntExact(teamInPlayId));
                }
            }
            return team;
        }
        //if we are not at the end of the list
        else {
            int i = (int) (teamInPlayId + 1);
            Optional<Team> optionalTeamInPlay = teamRepository.findById(String.valueOf(i));
            //isPresent check!
            Team team = optionalTeamInPlay.orElse(null);
            //Not elegant, but I will leave this as is. Can cause nullpointexception, but I know that it
            //wont. The id I provide above MUST be a valid team id - or the problem is not here.
            while (team.getIsSilenced()){
                i++;
                team.setIsSilenced(false);
                teamRepository.save(team);
                if (i < teamList.size()-1) {
                    optionalTeamInPlay = teamRepository.findById(String.valueOf(i));
                    team = optionalTeamInPlay.orElse(null);
                } else {
                    i = 0;
                    optionalTeamInPlay = teamRepository.findById(String.valueOf(i));
                    team = optionalTeamInPlay.orElse(null);
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

    public String guessFunction(Team team, int prize, Character guess, String riddle,
                                TeamRepository teamRepository) {
        char[] charSequence = riddle.toCharArray();
        Character g = Character.toLowerCase(guess);
        int counter = 0;
        for (int i = 0; i < charSequence.length; i++) {
            Character c = Character.toLowerCase(charSequence[i]);
            if (!c.equals(g) && Character.isLetter(c)) {
                c = '_';
            } else if (c.equals(g) && Character.isLetter(c)){
                counter++;
            }
            charSequence[i] = c;
        }
        team.setMoney(prize*counter);
        teamRepository.save(team);
        //next team comes
        return String.valueOf(charSequence);
    }

    public ArrayList<Prize> prizeInitialize(){
        return PrizeList.prizeInitialize();
    }

    //on frontend, use v-if!
    public String specialHandler(Prize prize, Team team, TeamRepository teamRepository){
        switch(prize.getSpecial()){
            case '-':
                //get guess, use guessFunction
                return String.valueOf(prize.getValue());
            case 'b':
                team.setMoney(0);
                teamRepository.save(team);
                //next team comes
                return "Bankrupt";
            case 'h':
                team.setMoney(team.getMoney()/2);
                teamRepository.save(team);
                //next team comes
                return "50/50";
            case 'd':
                team.setMoney(team.getMoney()*2);
                teamRepository.save(team);
                //next team comes
                return "Double";
            case 'l':
                //set prize of letter! Get bool Y/N to see if team
                //buys letter or not, count money, quizmaster will send the letter for it.
                return "Marketplace";
            case 'o':
                //get silenced team --- next team comes function must consider list of silenced teams.
                return "Silencer";
            case 'n':
                //next team comes
                return "Nothing";
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


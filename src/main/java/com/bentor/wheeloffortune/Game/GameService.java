package com.bentor.wheeloffortune.Game;

import com.bentor.wheeloffortune.Classes.*;
import com.bentor.wheeloffortune.Finals.PrizeList;
import com.bentor.wheeloffortune.Repositories.RiddleRepository;
import com.bentor.wheeloffortune.Repositories.TeamRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GameService {
    List<Guess> guessList = new ArrayList<>();

    public Team whichTeamPlays(Team teamInPlay, TeamRepository teamRepository){
        //Works only with preset teams, or teams set with all preset teams removed! Fix this.
        //Should go though the list. Only get team from repo when needed to be saved.
        List<Team> teamList = teamRepository.findAll();
        teamList.sort(Comparator.comparing(Team::getId));
        int indexOfTeam = -1;
        try {
            for (int i = 0; i < teamList.size(); i++) {
                if (Objects.equals(teamList.get(i).getId(), teamInPlay.getId())) {
                    indexOfTeam = i;
                    System.out.println("TeamInPlay Id in list: " + indexOfTeam);
                }
            }
        } catch (NullPointerException ex) {System.out.println("TeamInPlay Id: " + indexOfTeam);}
        //if we just started the game, or we are at the end of the list
        if (indexOfTeam == -1 || indexOfTeam == teamList.size() - 1){
            indexOfTeam = 0;
            Team t = teamList.get(indexOfTeam);
            while (t.getIsSilenced()){
                Team saveTeam = teamRepository.findById(t.getId()).get();
                saveTeam.setIsSilenced(false);
                teamRepository.save(saveTeam);
                //so its content is updated every time a team changes in repo - isSilenced. :)
                teamList = teamRepository.findAll();
                indexOfTeam++;
                if (indexOfTeam < teamList.size()) {
                    t = teamList.get(indexOfTeam);
                } else {
                    indexOfTeam = 0;
                    t = teamList.get(indexOfTeam);
                }
            }
            System.out.println("last team in list: " + t.getName() + " " + t.getIsSilenced());
            return t;
        }
        //if we are not at the end of the list
        else {
            indexOfTeam++;
            System.out.println("IndexOfTeam: " + indexOfTeam);
            Team t = teamList.get(indexOfTeam);
            //Not elegant, but I will leave this as is. Can cause nullpointexception, but I know that it
            //wont. The id I provide above MUST be a valid team id - or the problem is not here.
            while (t.getIsSilenced()){
                Team saveTeam = teamRepository.findById(t.getId()).get();
                saveTeam.setIsSilenced(false);
                teamRepository.save(saveTeam);
                teamList = teamRepository.findAll();
                indexOfTeam++;
                System.out.println("team is silenced: " + t.getName() + "i: " + indexOfTeam);
                if (indexOfTeam < teamList.size()) {
                    t = teamList.get(indexOfTeam);
                    System.out.println("teamid is less than the last teamid: " + t.getName());
                } else {
                    indexOfTeam = 0;
                    t = teamList.get(indexOfTeam);
                    System.out.println("teamid is the last team in list: " + t.getName());
                }
            }
            System.out.println("not last team in list: " + t.getName());
            return t;
        }
    }

    //when live: this is not needed. Read content from MySQL
    public void readRiddles(RiddleRepository riddleRepository) throws IOException {
        //read riddle from file - put content in MySQL
        //hint and riddle is separated by ';'
        File file = new File("C:/Users/User/IdeaProjects/wheeloffortune/src/main/resources/riddles.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        //read all lines of riddles file
        String line;
        while ((line = br.readLine()) != null) {
            String[] lineSplit = line.split("; ");
            String hintString = lineSplit[0];
            String riddleString = lineSplit[1];
            Riddle riddle = new Riddle(hintString, riddleString, false);
            riddleRepository.save(riddle);
        }
    }

    //when above is set, must return Riddle so frontend can have both hint and riddle.
    public Riddle getRiddle(RiddleRepository riddleRepository) {
            List<Riddle> riddlesList = riddleRepository.findRiddleByWasUsed(false);
            //generate a random number and get the random riddle, then remove it from the list so it wont be assigned again.
            Random random = new Random();
            int maxRand = riddlesList.size();
            int rnd = random.nextInt(maxRand);
            Riddle currentRiddle = riddlesList.get(rnd);
            Riddle currentFromRepo = riddleRepository.findById(currentRiddle.getId()).orElse(null);
            currentFromRepo.setWasUsed(true);
            riddleRepository.save(currentFromRepo);
            return currentFromRepo;
    }

    //again, when above is set, must return Riddle so frontend can have both hint and riddle.
    public Riddle turnRiddleToCode(Riddle riddle){
        Riddle toCodeRiddle = new Riddle();
        char[] charSequence = riddle.getRiddle().toCharArray();
        for (int i = 0; i < charSequence.length; i++) {
            char c = charSequence[i];
            if (Character.isLetter(c)) {
                c = '_';
            }
            charSequence[i] = c;
        }
        toCodeRiddle.setRiddle(String.valueOf(charSequence));
        toCodeRiddle.setHint(riddle.getHint());
        System.out.println("turnedtoCode:" + toCodeRiddle.getRiddle() + "not turned: " + riddle.getRiddle());
        return toCodeRiddle;
    }

    public Team createTeam(String name){
        return new Team(name, 0, false);
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
        System.out.println(riddle + " " + codedRiddle);
        Character g = Character.toLowerCase(guess);
        int counter = 0;
        for (int i = 0; i < charSequence.length; i++) {
            System.out.println("char: " + charSequence[i] + "guesschar: " + g);
            Character c = Character.toLowerCase(charSequence[i]);
            if (!c.equals(g) && Character.isLetter(c)) {
                c = codedCharSequence[i];
            } else if (c.equals(g) && Character.isLetter(c) && !c.equals(codedCharSequence[i])){
                counter++;
            }
            charSequence[i] = c;
        }
        team.setMoney(team.getMoney() + prize*counter);
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

    public String buyLetter(Team team, TeamRepository teamRepository) {
        if(team.getMoney() > 99999) {
            team.setMoney(team.getMoney() - 100000);
            teamRepository.save(team);
            return "Tell the others if you received the secret.";
        } else return "I need more money for this information.";
    }

    public void silenceTeam(Team team, TeamRepository teamRepository) {
        team.setIsSilenced(true);
        teamRepository.save(team);
        System.out.println(team.getIsSilenced());
    }

    public Boolean guessRiddle(String guess, String codedRiddle, String riddle, Team team, Integer guessMoney, TeamRepository teamRepository) {
        this.guessList.clear();
        Integer result = 0;
        for (char c : codedRiddle.toCharArray()){
            if (!Character.isLetter(c) && c != ',' && c != '.' && c != ' '){
                result++;
            }
        }
        Integer riddleGuessPrize = guessMoney * result;

        if (guess.equalsIgnoreCase(riddle)){
            //result: guessmoney * letters not known. Should be ADDED - good guess.
            team.setMoney(team.getMoney() + riddleGuessPrize);
            teamRepository.save(team);
            return true;
        } else {
            //result: guessmoney * letters not known. Should be REMOVED - bad guess.
            team.setMoney(team.getMoney()-riddleGuessPrize);
            if (team.getMoney() < 0){
                team.setMoney(0);
            }
            teamRepository.save(team);
            return false;
        }
    }

    public Guess getAuctionData(Guess guess, TeamRepository teamRepository) {
        System.out.println("Guessmoney: " + guess.getMoney());
        Team guessingTeam = teamRepository.findById(guess.getTeam().getId()).orElse(null);
        if (guess.getMoney() < guessingTeam.getMoney()) {
            Guess highestGuess = guess;
            this.guessList.add(guess);
            System.out.println("Team money: " + guessingTeam.getMoney() + " Guess money: " + guess.getMoney()
            + "Result: " + (guessingTeam.getMoney() - guess.getMoney()));
            //Here money should not be removed from account, this is just bidding.
            //guessingTeam.setMoney(guessingTeam.getMoney() - guess.getMoney());
            //teamRepository.save(guessingTeam);
            for (Guess g : this.guessList) {
                if (g.getMoney() > highestGuess.getMoney()) {
                    highestGuess = g;
                }
            }
            return highestGuess;
        }
        return null;
    }

    public List<Team> getWinner(TeamRepository teamRepository){
        List<Team> teamsEndGame = teamRepository.findAll();
        List<Team> winnersList = new ArrayList<>();
        Team winner = teamsEndGame.get(0);
        winnersList.add(winner);
        for (int i = 1; i < teamsEndGame.size(); i++){
            Team t = teamsEndGame.get(i);
            if (t.getMoney() == winner.getMoney()){
                winnersList.add(t);
            } else if (t.getMoney() > winner.getMoney()){
                winnersList.clear();
                winner = t;
                winnersList.add(winner);
            }
        }
        return winnersList;
    }

    public List<TableTeam> getTableTeams(List<Team> allteams) {
        List<TableTeam> tableTeams = new ArrayList<>();
        for(Team t : allteams) {
            TableTeam tt = new TableTeam(t.getName(), t.getId());
            tableTeams.add(tt);
        }
        return tableTeams;
    }
}


package com.bentor.wheeloffortune.Game;

import com.bentor.wheeloffortune.Classes.Player;
import com.bentor.wheeloffortune.Classes.Riddle;
import com.bentor.wheeloffortune.Classes.Team;
import com.bentor.wheeloffortune.Repositories.RiddleRepository;
import com.bentor.wheeloffortune.Repositories.TeamRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Random;

@Service
public class GameService {

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

    public String getRiddle(RiddleRepository riddleRepository) throws IOException {
        List<Riddle> riddlesList = riddleRepository.findRiddleByWasUsed(false);
        String riddle;
        //generate a random number and get the random riddle, then remove it from the list so it wont be assigned again.
        Random random = new Random();
        int maxRand = riddlesList.size();
        int rnd = random.nextInt(maxRand);
        riddle = riddlesList.get(rnd).getRiddle();
        Riddle currentRiddle = riddlesList.get(rnd);
        riddleRepository.delete(currentRiddle);
        currentRiddle.setWasUsed(true);
        riddleRepository.save(currentRiddle);
        return riddle;
    }

    public String turnRiddleToCode(String riddle){
        char[] charSequence = riddle.toCharArray();
        char[] sendCharSequence = new char[charSequence.length];
        for (int i = 0; i < charSequence.length; i++) {
            char c = charSequence[i];
            if (Character.isLetter(c)) {
                c = '_';
            }
            sendCharSequence[i] = c;
        }
        final String sendCharSequenceToString = String.valueOf(sendCharSequence);
        return sendCharSequenceToString;
    }

    public Team createTeam(String name){
        Team team = new Team(name);
        return team;
    }

    public Player createPlayer(TeamRepository teamRepository, String playerName, String teamName){
        Team team = teamRepository.findTeamByName(teamName);
        Player player = new Player(playerName, team);
        return player;
    }
}

package com.bentor.wheeloffortune.Classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "riddles")
public class Riddle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;
    String riddle;
    boolean wasUsed;

    public Riddle(String riddle, boolean wasUsed) {
        this.riddle = riddle;
        this.wasUsed = wasUsed;
    }
}

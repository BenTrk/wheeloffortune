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
    String hint;
    boolean wasUsed;

    public Riddle(String hint, String riddle, boolean wasUsed) {
        this.hint = hint;
        this.riddle = riddle;
        this.wasUsed = wasUsed;
    }
}

package fr.paolo.wattebled.bomberman.data.model;

import lombok.*;

import javax.persistence.*;

@Entity @Table(name = "player")
@AllArgsConstructor @NoArgsConstructor @ToString @With
@Getter @Setter
public class PlayerModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Long lastBomb;
    public Long lastMovement;
    public int lives;
    public String name;
    public int posX;
    public int posY;
    @ManyToOne public GameModel game;
}

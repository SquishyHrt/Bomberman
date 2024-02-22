package fr.paolo.wattebled.bomberman.domain.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@With @Value @AllArgsConstructor
public class PlayerEntity {
    public Long id;
    public Long lastBomb;
    public Long lastMovement;
    public int lives;
    public String name;
    public int posX;
    public int posY;
}


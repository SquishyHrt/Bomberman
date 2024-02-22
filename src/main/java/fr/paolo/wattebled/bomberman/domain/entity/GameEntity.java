package fr.paolo.wattebled.bomberman.domain.entity;

import fr.paolo.wattebled.bomberman.utils.GameState;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@With @Value @AllArgsConstructor
public class GameEntity {
    public Long id;
    public LocalDateTime startTime;
    public GameState state;
    public List<PlayerEntity> players;
    public List<String> map;
}
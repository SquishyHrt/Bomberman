package fr.paolo.wattebled.bomberman.presentation.rest.response;

import fr.paolo.wattebled.bomberman.utils.GameState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class DetailGameResponse {
    public LocalDateTime startTime;
    public GameState state;
    public List<Player> players;
    public List<String> map;
    public Long id;

    @AllArgsConstructor
    public static class Player {
        public Long id;
        public String name;
        public int lives;
        public int posX;
        public int posY;
    }
}

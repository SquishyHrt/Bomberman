package fr.paolo.wattebled.bomberman.presentation.rest.response;

import fr.paolo.wattebled.bomberman.utils.GameState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class SimpleGameResponse {
    public Long id;
    public Long players;
    public GameState state;
}

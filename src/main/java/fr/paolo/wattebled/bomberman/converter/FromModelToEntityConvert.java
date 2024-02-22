package fr.paolo.wattebled.bomberman.converter;

import fr.paolo.wattebled.bomberman.data.model.GameModel;
import fr.paolo.wattebled.bomberman.data.model.PlayerModel;
import fr.paolo.wattebled.bomberman.domain.entity.GameEntity;
import fr.paolo.wattebled.bomberman.domain.entity.PlayerEntity;

import java.util.List;

public class FromModelToEntityConvert {
    public static GameEntity convertGame(GameModel gameModel) {
        if (gameModel == null) {
            return null;
        }
        List<PlayerEntity> playerEntities = gameModel.getPlayers().stream()
                .map(FromModelToEntityConvert::convertPlayer)
                .toList();
        return new GameEntity(
                gameModel.getId(),
                gameModel.getStartTime(),
                gameModel.getState(),
                playerEntities,
                gameModel.getMap());
    }

    public static PlayerEntity convertPlayer(PlayerModel playerModel) {
        if (playerModel == null) {
            return null;
        }
        return new PlayerEntity(
                playerModel.getId(),
                playerModel.getLastBomb(),
                playerModel.getLastMovement(),
                playerModel.getLives(),
                playerModel.getName(),
                playerModel.getPosX(),
                playerModel.getPosY());
    }
}

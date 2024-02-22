package fr.paolo.wattebled.bomberman.data.repository;

import fr.paolo.wattebled.bomberman.data.model.GameModel;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

public class GameRepository implements PanacheRepository<GameModel> {
}
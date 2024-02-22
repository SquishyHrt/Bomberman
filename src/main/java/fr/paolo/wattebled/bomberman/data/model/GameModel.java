package fr.paolo.wattebled.bomberman.data.model;

import fr.paolo.wattebled.bomberman.utils.GameState;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "game")
@AllArgsConstructor @NoArgsConstructor @With @ToString
@Getter @Setter
public class GameModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public LocalDateTime startTime;
    public GameState state;
    @OneToMany(mappedBy = "game", orphanRemoval = true, cascade = CascadeType.ALL) public List<PlayerModel> players;
    public @ElementCollection @CollectionTable(name = "game_map", joinColumns = @JoinColumn(name = "game_id")) @LazyCollection(LazyCollectionOption.FALSE) List<String> map;
}
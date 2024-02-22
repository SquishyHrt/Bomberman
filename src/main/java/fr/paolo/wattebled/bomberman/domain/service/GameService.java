package fr.paolo.wattebled.bomberman.domain.service;

import fr.paolo.wattebled.bomberman.converter.FromModelToEntityConvert;
import fr.paolo.wattebled.bomberman.data.model.GameModel;
import fr.paolo.wattebled.bomberman.data.model.PlayerModel;
import fr.paolo.wattebled.bomberman.data.repository.GameRepository;
import fr.paolo.wattebled.bomberman.data.repository.PlayerRepository;
import fr.paolo.wattebled.bomberman.domain.entity.GameEntity;
import fr.paolo.wattebled.bomberman.utils.GameState;
import fr.paolo.wattebled.bomberman.utils.MapParsing;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class GameService {
    GameRepository gameRepository;
    PlayerRepository playerRepository;

    private int offset = 1;

    @ConfigProperty(name = "JWS_DELAY_MOVEMENT")
    int delayMovement;

    @ConfigProperty(name = "JWS_TICK_DURATION")
    int tickDuration;

    @ConfigProperty(name = "JWS_DELAY_BOMB")
    int delayBomb;

    @ConfigProperty(name = "JWS_MAP_PATH")
    String mapPath;

    public GameService() {
        this.gameRepository = new GameRepository();
        this.playerRepository = new PlayerRepository();
    }

    @Transactional
    public List<GameEntity> getAllGames() {
        List<GameModel> tmp = gameRepository.findAll().stream().toList();

        return tmp.stream()
                .map(FromModelToEntityConvert::convertGame)
                .toList();
    }

    @Transactional
    public GameEntity createGame(String playerName) {
        GameModel gameModel = new GameModel()
                .withPlayers(new ArrayList<>())
                .withStartTime(LocalDateTime.now())
                .withState(GameState.STARTING)
                .withMap(MapParsing.getMap(mapPath));

        PlayerModel playerModel = new PlayerModel()
                .withName(playerName)
                .withLives(3)
                .withPosX(1)
                .withPosY(1)
                .withGame(gameModel)
                .withLastBomb(System.currentTimeMillis() - (long) delayBomb * tickDuration)
                .withLastMovement(System.currentTimeMillis() - (long) delayMovement * tickDuration);

        gameModel.players.add(playerModel);

        gameRepository.persist(gameModel);
        playerRepository.persist(playerModel);

        return FromModelToEntityConvert.convertGame(gameModel);
    }

    @Transactional
    public GameEntity getGameById(Integer id) {
        GameModel tmp = gameRepository.findById((long) id);
        return FromModelToEntityConvert.convertGame(tmp);
    }

    @Transactional
    public GameEntity joinGame(int id, String newPlayerName) {
        GameModel tmp = gameRepository.findById((long) id);
        if (tmp == null) {
            throw new NoSuchElementException(); // 404
        }

        if (tmp.players.size() >= 4 || tmp.state == GameState.FINISHED || tmp.state == GameState.RUNNING) {
            throw new BadRequestException(); // 400
        }

        PlayerModel newPlayer = createNewPlayer(tmp, newPlayerName);
        tmp.players.add(newPlayer);
        playerRepository.persist(newPlayer);

        return getGameById(id);
    }

    @Transactional
    public GameEntity startGame(Integer id) {
        GameModel tmp = gameRepository.findById((long) id);
        if (tmp == null || tmp.state == GameState.FINISHED) {
            throw new NoSuchElementException(); // 404
        }

        tmp.state = (tmp.players.size() <= 1) ? GameState.FINISHED : GameState.RUNNING;
        tmp.startTime = LocalDateTime.now();

        return getGameById(id);
    }

    @Transactional
    public GameEntity plantBomb(Integer gameId, Integer playerId, Integer x, Integer y) {
        if (gameId == null || playerId == null) {
            throw new BadRequestException(); // 404
        }

        GameModel tmpGame = gameRepository.findById((long) gameId);
        PlayerModel tmpPlayer = playerRepository.findById((long) playerId);
        if (tmpGame == null || tmpPlayer == null) {
            throw new NoSuchElementException(); // 404
        }

        if (x == null || y == null) {
            throw new BadRequestException(); // 400
        }

        if (tmpGame.state == GameState.FINISHED || tmpGame.state == GameState.STARTING ||
                tmpPlayer.lives <= 0 || tmpPlayer.posX != x || tmpPlayer.posY != y) {
            throw new BadRequestException(); // 400
        }

        if (!canPlantBomb(tmpPlayer)) {
            throw new ArithmeticException(); // 429
        }

        tmpPlayer.lastBomb = System.currentTimeMillis();

        List<String> decodeMap = MapParsing.decodeMap(tmpGame.map);
        StringBuilder line = new StringBuilder(decodeMap.get(y));
        line.setCharAt(x, 'B');
        decodeMap.set(y, line.toString());
        List<String> encodeMap = MapParsing.encodeMap(decodeMap);

        tmpGame.map = List.copyOf(encodeMap);
        return getGameById(gameId);
    }

    @Transactional
    public void explodeBomb(int gameId, int x, int y) {
        GameModel tmp = gameRepository.findById((long) gameId);

        tmp.players.forEach(p -> {
            if (p.posX == x && (p.posY == y - 1 || p.posY == y + 1))
                p.lives--;
            if (p.posY == y && (p.posX == x - 1 || p.posX == x + 1))
                p.lives--;
            if (p.posY == y && p.posX == x)
                p.lives--;
        });
        long playersAlive = tmp.players.stream().filter(p -> p.lives > 0).count();
        if (playersAlive <= 1) {
            tmp.state = GameState.FINISHED;
        }

        List<String> fullMap = MapParsing.decodeMap(tmp.map);

        // Vertical checks
        StringBuilder line = new StringBuilder(fullMap.get(y + 1));
        if (line.charAt(x) == 'W')
            line.setCharAt(x, 'G');
        fullMap.set(y + 1, line.toString());

        line = new StringBuilder(fullMap.get(y - 1));
        if (line.charAt(x) == 'W')
            line.setCharAt(x, 'G');
        fullMap.set(y - 1, line.toString());

        //Horizontal checks
        line = new StringBuilder(fullMap.get(y));
        if (line.charAt(x + 1) == 'W')
            line.setCharAt(x + 1, 'G');
        if (line.charAt(x - 1) == 'W')
            line.setCharAt(x - 1, 'G');
        line.setCharAt(x, 'G');
        fullMap.set(y, line.toString());

        tmp.map = MapParsing.encodeMap(fullMap);
    }


    @Transactional
    public GameEntity movePlayer(Integer gameId, Integer playerId, Integer x, Integer y) {
        if (gameId == null || playerId == null || x == null || y == null) {
            throw new BadRequestException(); // 400
        }

        GameModel tmpGame = gameRepository.findById((long) gameId);
        PlayerModel tmpPlayer = playerRepository.findById((long) playerId);
        if (tmpGame == null || tmpPlayer == null) {
            throw new NoSuchElementException(); // 404
        }

        if (tmpGame.state == GameState.FINISHED || tmpGame.state == GameState.STARTING ||
                tmpPlayer.lives <= 0 || !isMoveLegal(tmpPlayer.posX, tmpPlayer.posY, x, y, gameId)) {
            throw new BadRequestException(); // 400
        }

        if (!canMove(tmpPlayer)) {
            throw new ArithmeticException(); // 429
        }

        tmpPlayer.posX = x;
        tmpPlayer.posY = y;
        tmpPlayer.lastMovement = System.currentTimeMillis();

        return getGameById(gameId);
    }

    @Transactional
    public void shrinkMap(int gameId) {
        GameModel tmpGame = gameRepository.findById((long) gameId);
        List<String> fullMap = MapParsing.decodeMap(tmpGame.map);

        // first line
        StringBuilder line = new StringBuilder(fullMap.get(offset));
        for (int i = offset; i < fullMap.get(offset).length() - offset; i++) {
            line.setCharAt(i, 'M');
        }
        fullMap.set(offset, line.toString());

        // last line
        line = new StringBuilder(fullMap.get(fullMap.size() - offset - 1));
        for (int i = offset; i < fullMap.get(fullMap.size() - offset - 1).length() - offset; i++) {
            line.setCharAt(i, 'M');
        }
        fullMap.set(fullMap.size() - offset - 1, line.toString());

        // first column
        for (int i = offset; i < fullMap.size() - offset; i++) {
            line = new StringBuilder(fullMap.get(i));
            line.setCharAt(offset, 'M');
            fullMap.set(i, line.toString());
        }

        // last column
        for (int i = offset; i < fullMap.size() - offset; i++) {
            line = new StringBuilder(fullMap.get(i));
            line.setCharAt(fullMap.get(i).length() - offset - 1, 'M');
            fullMap.set(i, line.toString());
        }

        tmpGame.map = MapParsing.encodeMap(fullMap);
        offset++;

        // Kill players that are outside the map or on a M
        tmpGame.players.forEach(p -> {
            if (fullMap.get(p.posY).charAt(p.posX) == 'M') {
                p.lives = 0;
            }
        });
        if (tmpGame.players.stream().filter(p -> p.lives > 0).count() <= 1) {
            tmpGame.state = GameState.FINISHED;
        }
    }

    private boolean isMoveLegal(int prevPosX, int prevPosY, int newPosX, int newPosY, int gameId) {
        GameModel tmpGame = gameRepository.findById((long) gameId);

        List<String> decodedMap = MapParsing.decodeMap(tmpGame.map);
        String line = decodedMap.get(newPosY);
        if (line.charAt(newPosX) != 'G') {
            return false;
        }

        int diffX = prevPosX - newPosX;
        int diffY = prevPosY - newPosY;
        if (diffX > 1 || diffY > 1) {
            return false;
        }

        return ((diffX != 0) ^ (diffY != 0));
    }

    private PlayerModel createNewPlayer(GameModel game, String name) {
        PlayerModel res = new PlayerModel()
                .withLives(3)
                .withName(name)
                .withGame(game);

        switch (game.players.size()) {
            case 0:
                res.setPosX(1);
                res.setPosY(1);
                break;
            case 1:
                res.setPosX(15);
                res.setPosY(1);
                break;
            case 2:
                res.setPosX(15);
                res.setPosY(13);
                break;
            case 3:
                res.setPosX(1);
                res.setPosY(13);
                break;
            default:
                break;
        }

        return res;
    }

    private boolean canMove(PlayerModel player) {
        if (player.lastMovement == null) {
            return true;
        }

        long diff = System.currentTimeMillis() - player.lastMovement;
        return diff > (long) delayMovement * tickDuration;
    }

    private boolean canPlantBomb(PlayerModel player) {
        if (player.lastBomb == null) {
            return true;
        }

        long diff = System.currentTimeMillis() - player.lastBomb;
        return diff > (long) delayBomb * tickDuration;
    }
}
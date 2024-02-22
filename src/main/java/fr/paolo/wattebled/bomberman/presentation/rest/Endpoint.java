package fr.paolo.wattebled.bomberman.presentation.rest;

import fr.paolo.wattebled.bomberman.domain.entity.GameEntity;
import fr.paolo.wattebled.bomberman.domain.service.GameService;
import fr.paolo.wattebled.bomberman.presentation.rest.request.CoordsRequest;
import fr.paolo.wattebled.bomberman.presentation.rest.request.NameRequest;
import fr.paolo.wattebled.bomberman.presentation.rest.response.DetailGameResponse;
import fr.paolo.wattebled.bomberman.presentation.rest.response.SimpleGameResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.*;

@Path("/")
@Produces("application/json")
@Consumes("application/json")
public class Endpoint {
    @Inject
    GameService service;

    @ConfigProperty(name = "JWS_TICK_DURATION")
    int tickDuration;

    @ConfigProperty(name = "JWS_DELAY_BOMB")
    int delayBomb;

    @ConfigProperty(name = "JWS_DELAY_FREE")
    int delayFree;

    @ConfigProperty(name = "JWS_DELAY_SHRINK")
    int delayShrink;

    @GET
    @Path("/games")
    public Response getGamesEndpoint() {
        // 200: The list of games
        // No other status code
        List<GameEntity> gameSet = service.getAllGames();
        if (gameSet == null) {
            return Response.ok(new ArrayList<>()).build();
        }

        List<SimpleGameResponse> response = gameSet.stream()
                .map(g -> new SimpleGameResponse(g.id, (long) g.players.size(), g.state))
                .toList();
        return Response.ok(response).build();
    }

    @POST
    @Path("/games")
    public Response createNewGameEndpoint(NameRequest request) {
        // 200: The game has been created
        // 400: The request is null, or the player name is null
        if (request == null || request.name == null) {
            return Response.status(400).build();
        }

        GameEntity newGame = service.createGame(request.name);

        return createDetailGameResponse(newGame);
    }

    @GET
    @Path("/games/{gameId}")
    public Response getGameInfoEndpoint(@PathParam("gameId") Integer gameId) {
        // 200: The game info of this id
        // 404: The game of this id does not exist
        if (gameId == null) {
            return Response.status(400).build();
        }

        GameEntity newGame = service.getGameById(gameId);

        if (newGame == null) {
            return Response.status(404).build();
        }

        return createDetailGameResponse(newGame);
    }

    @POST
    @Path("/games/{gameId}")
    public Response joinGameEndpoint(@PathParam("gameId") Integer gameId, NameRequest request) {
        // 200: Game successfully joined
        // 400: The request is null, or the player name is null or the game cannot be started (already started, too many players)"
        // 404: The game of this id does not exist
        if (gameId == null || request == null || request.name == null || request.name.isEmpty()) {
            return Response.status(400).build();
        }

        GameEntity newGame;
        try {
            newGame = service.joinGame(gameId, request.name);
        } catch (NoSuchElementException e) {
            return Response.status(404).build();
        } catch (BadRequestException e) {
            return Response.status(400).build();
        }

        return createDetailGameResponse(newGame);
    }

    @PATCH
    @Path("/games/{gameId}/start")
    public Response startGameEndpoint(@PathParam("gameId") Integer gameId) {
        // 200: Game successfully started
        // 404: The game of this id does not exist
        if (gameId == null) {
            return Response.status(400).build();
        }

        GameEntity newGame;
        try {
            newGame = service.startGame(gameId);
        } catch (NoSuchElementException e) {
            return Response.status(404).build();
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                service.shrinkMap(gameId);
            }
        }, (long) tickDuration * delayFree, (long) tickDuration * delayShrink);

        return createDetailGameResponse(newGame);
    }

    @POST
    @Path("/games/{gameId}/players/{playerId}/bomb")
    public Response putBombEndpoint(@PathParam("gameId") Integer gameId, @PathParam("playerId") Integer playerId, CoordsRequest request) {
        // 200: Bomb successfully put
        // 400: The request is null, or the game is not started or the player is already dead, or the coords are wrong.
        // 404: The game of this id does not exist or the player of this id does not exist
        // 429: The player has already put a bomb in the last X ticks
        if (request == null) {
            return Response.status(400).build();
        }

        GameEntity newGame;
        try {
            newGame = service.plantBomb(gameId, playerId, request.posX, request.posY);
        } catch (NoSuchElementException e) {
            return Response.status(404).build();
        } catch (BadRequestException e) {
            return Response.status(400).build();
        } catch (ArithmeticException e) {
            return Response.status(429).build();
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                service.explodeBomb(gameId, request.posX, request.posY);
            }
        }, (long) tickDuration * delayBomb);

        return createDetailGameResponse(newGame);
    }

    @POST
    @Path("/games/{gameId}/players/{playerId}/move")
    public Response movePlayerEndpoint(@PathParam("gameId") Integer gameId, @PathParam("playerId") Integer playerId, CoordsRequest request) {
        // 200: Player successfully moved
        // 400: The request is null, or the game is not started or the player is already dead, or the coords are wrong or the player is not allowed to move.
        // 404: The game of this id does not exist or the player of this id does not exist
        // 429: The player has already moved in the last X ticks
        if (request == null) {
            return Response.status(400).build();
        }

        GameEntity newGame;
        try {
            newGame = service.movePlayer(gameId, playerId, request.posX, request.posY);
        } catch (NoSuchElementException e) {
            return Response.status(404).build();
        } catch (BadRequestException e) {
            return Response.status(400).build();
        } catch (ArithmeticException e) {
            return Response.status(429).build();
        }

        return createDetailGameResponse(newGame);
    }

    private Response createDetailGameResponse(GameEntity newGame) {
        DetailGameResponse response = new DetailGameResponse(
                LocalDateTime.now(),
                newGame.state,
                new ArrayList<>(),
                List.copyOf(newGame.map),
                newGame.id);

        newGame.getPlayers().forEach(i ->
                response.players.add(
                        new DetailGameResponse.Player(i.id, i.name, i.lives, i.posX, i.posY)
                ));

        return Response.ok(response).build();
    }
}

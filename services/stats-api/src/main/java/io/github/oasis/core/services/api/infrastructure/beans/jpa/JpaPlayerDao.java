package io.github.oasis.core.services.api.infrastructure.beans.jpa;

import io.github.oasis.core.external.storage.PlayerDao;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.infrastructure.repository.dto.PlayerDto;
import io.github.oasis.core.services.api.infrastructure.repository.jpa.PlayerRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
class JpaPlayerDao implements PlayerDao {

    private final PlayerRepository playerRepository;

    JpaPlayerDao(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public PlayerObject readPlayer(long userId) {
        var player = playerRepository.findById(userId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));

        return PlayerObject.builder()
                .gender(player.getGender())
                .timeZone(player.getTimezone())
                .displayName(player.getDisplayName())
                .avatarRef(player.getAvatarRef())
                .createdAt(player.getCreatedAt().toEpochMilli())
                .updatedAt(player.getUpdatedAt().toEpochMilli())
                .email(player.getEmail())
                .id(player.getId())
                .active(player.getIsActive())
                .teamIds(player.getTeams().stream().map(it -> it.getGame().getId()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public PlayerObject readPlayer(String email) {
        var playerWithEmail = PlayerDto.builder().email(email).build();
        var example = Example.of(playerWithEmail, ExampleMatcher.matchingAll()
                .withMatcher("email", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.EXACT)));

        var player = playerRepository.findOne(example)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));

        return PlayerObject.builder()
                .gender(player.getGender())
                .timeZone(player.getTimezone())
                .displayName(player.getDisplayName())
                .avatarRef(player.getAvatarRef())
                .createdAt(player.getCreatedAt().toEpochMilli())
                .updatedAt(player.getUpdatedAt().toEpochMilli())
                .email(player.getEmail())
                .id(player.getId())
                .active(player.getIsActive())
                .teamIds(player.getTeams().stream().map(it -> it.getTeam().getId()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public PlayerObject addPlayer(PlayerObject newUser) {
        var player = playerRepository.save(PlayerDto.builder()
                .avatarRef(newUser.getAvatarRef())
                .email(newUser.getEmail())
                .timezone(newUser.getTimeZone())
                .gender(newUser.getGender())
                .displayName(newUser.getDisplayName())
                .build());

        return newUser.toBuilder()
                .id(player.getId())
                .build();
    }

    @Override
    public boolean existsPlayer(String email) {
        var playerWithEmail = PlayerDto.builder().email(email).build();
        var example = Example.of(playerWithEmail, ExampleMatcher.matchingAll()
                .withMatcher("email", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.EXACT)));

        return playerRepository.findOne(example).isPresent();
    }

    @Override
    public boolean existsPlayer(long userId) {
        return playerRepository.findById(userId).isPresent();
    }

    @Override
    public PlayerObject updatePlayer(long userId, PlayerObject updatedUser) {
        var player = playerRepository.findById(userId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));

        player.setGender(updatedUser.getGender());
        player.setAvatarRef(updatedUser.getAvatarRef());
        player.setDisplayName(updatedUser.getDisplayName());

        player = playerRepository.save(player);

        return PlayerObject.builder()
                .gender(player.getGender())
                .timeZone(player.getTimezone())
                .displayName(player.getDisplayName())
                .avatarRef(player.getAvatarRef())
                .createdAt(player.getCreatedAt().toEpochMilli())
                .updatedAt(player.getUpdatedAt().toEpochMilli())
                .email(player.getEmail())
                .id(player.getId())
                .active(player.getIsActive())
                .build();
    }

    @Override
    public PlayerObject deletePlayer(long userId) {
        var player = playerRepository.findById(userId)
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND));
        playerRepository.delete(player);

        return PlayerObject.builder()
                .gender(player.getGender())
                .timeZone(player.getTimezone())
                .displayName(player.getDisplayName())
                .avatarRef(player.getAvatarRef())
                .createdAt(player.getCreatedAt().toEpochMilli())
                .updatedAt(player.getUpdatedAt().toEpochMilli())
                .email(player.getEmail())
                .id(player.getId())
                .active(player.getIsActive())
                .build();
    }


}

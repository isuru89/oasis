package io.github.isuru.oasis.injector;

/**
 * @author iweerarathna
 */
class ContextInfo {

    private long gameId;

    long getGameId() {
        return gameId;
    }

    void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public static void main(String[] args) {
        String text = "game.o{gid}.milestone";
        System.out.println(text.replace("{gid}", "1"));
    }
}

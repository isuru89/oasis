package io.github.isuru.oasis.services.test.routes;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.services.api.dto.AddGameDto;
import io.github.isuru.oasis.services.test.dto.AddResponse;
import io.github.isuru.oasis.services.test.dto.BoolResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

/**
 * @author iweerarathna
 */
public interface IOasisDefApi {

    @POST("def/game")
    Call<AddResponse> addGame(@Header("Authorization") String auth, @Body AddGameDto addGameDto);
    @GET("def/game/all")
    Call<List<GameDef>> listGames();
    @GET("def/game/{gameId}")
    Call<GameDef> readGame(@Path("gameId") long gameId);
    @DELETE("def/game/{gameId}")
    Call<BoolResponse> disableGame(@Header("Authorization") String auth, @Path("gameId") long gameId);

    @POST("def/game/{gameId}/kpi")
    Call<AddResponse> addKpi(@Header("Authorization") String auth,
                              @Path("gameId") long gameId,
                              @Body KpiDef kpiDef);
    @GET("def/game/{gameId}/kpi/all")
    Call<List<KpiDef>> listKpis(@Path("gameId") long gameId);
    @GET("def/game/{gameId}/{kpiId}")
    Call<KpiDef> readKpi(@Path("gameId") long gameId, @Path("kpiId") long kpiId);
    @DELETE("def/game/{gameId}/{kpiId}")
    Call<BoolResponse> disableKpi(@Header("Authorization") String auth,
                                   @Path("gameId") long gameId,
                                   @Path("kpiId") long kpiId);

}

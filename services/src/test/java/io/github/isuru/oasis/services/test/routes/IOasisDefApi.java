package io.github.isuru.oasis.services.test.routes;

import io.github.isuru.oasis.model.OState;
import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.services.dto.defs.AddGameDto;
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
    @GET("def/game/{gameId}/kpi/{kpiId}")
    Call<KpiDef> readKpi(@Path("gameId") long gameId, @Path("kpiId") long kpiId);
    @DELETE("def/game/{gameId}/kpi/{kpiId}")
    Call<BoolResponse> disableKpi(@Header("Authorization") String auth,
                                   @Path("gameId") long gameId,
                                   @Path("kpiId") long kpiId);

    @POST("def/game/{gameId}/point")
    Call<AddResponse> addPointRule(@Header("Authorization") String auth,
                                   @Path("gameId") long gameId,
                                   @Body PointDef pointDef);
    @GET("def/game/{gameId}/point/all")
    Call<List<PointDef>> listPointRules(@Path("gameId") long gameId);
    @GET("def/game/{gameId}/point/{pointId}")
    Call<PointDef> readPointRule(@Path("gameId") long gameId, @Path("pointId") long pointId);
    @DELETE("def/game/{gameId}/point/{pointId}")
    Call<BoolResponse> disablePointRule(@Header("Authorization") String auth,
                                  @Path("gameId") long gameId,
                                  @Path("pointId") long pointId);

    @POST("def/game/{gameId}/badge")
    Call<AddResponse> addBadgeRule(@Header("Authorization") String auth,
                                   @Path("gameId") long gameId,
                                   @Body BadgeDef badgeDef);
    @GET("def/game/{gameId}/badge/all")
    Call<List<BadgeDef>> listBadgeRules(@Path("gameId") long gameId);
    @GET("def/game/{gameId}/badge/{badgeId}")
    Call<BadgeDef> readBadgeRule(@Path("gameId") long gameId, @Path("badgeId") long badgeId);
    @DELETE("def/game/{gameId}/badge/{badgeId}")
    Call<BoolResponse> disableBadgeRule(@Header("Authorization") String auth,
                                        @Path("gameId") long gameId,
                                        @Path("badgeId") long badgeId);

    @POST("def/game/{gameId}/milestone")
    Call<AddResponse> addMilestoneRule(@Header("Authorization") String auth,
                                   @Path("gameId") long gameId,
                                   @Body MilestoneDef milestoneDef);
    @GET("def/game/{gameId}/milestone/all")
    Call<List<MilestoneDef>> listMilestoneRules(@Path("gameId") long gameId);
    @GET("def/game/{gameId}/milestone/{milestoneId}")
    Call<MilestoneDef> readMilestoneRule(@Path("gameId") long gameId, @Path("milestoneId") long milestoneId);
    @DELETE("def/game/{gameId}/milestone/{milestoneId}")
    Call<BoolResponse> disableMilestoneRule(@Header("Authorization") String auth,
                                        @Path("gameId") long gameId,
                                        @Path("milestoneId") long milestoneId);

    @POST("def/game/{gameId}/state")
    Call<AddResponse> addStateRule(@Header("Authorization") String auth,
                                       @Path("gameId") long gameId,
                                       @Body OState oState);
    @GET("def/game/{gameId}/state/all")
    Call<List<MilestoneDef>> listStateRules(@Path("gameId") long gameId);
    @GET("def/game/{gameId}/state/{stateId}")
    Call<MilestoneDef> readStateRule(@Path("gameId") long gameId, @Path("stateId") long stateId);
    @DELETE("def/game/{gameId}/state/{stateId}")
    Call<BoolResponse> disableStateRule(@Header("Authorization") String auth,
                                            @Path("gameId") long gameId,
                                            @Path("stateId") long stateId);
}

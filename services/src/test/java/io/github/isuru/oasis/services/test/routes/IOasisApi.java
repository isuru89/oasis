package io.github.isuru.oasis.services.test.routes;

import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.Map;

/**
 * @author iweerarathna
 */
public interface IOasisApi {

    @GET("echo")
    Call<Map<String, Object>> echo();


    @POST("auth/login")
    Call<Map<String, Object>> login(@Header("Authorization") String basicAuth);
    @POST("auth/logout")
    Call<Map<String, Object>> logout(@Header("Authorization") String authHeader);


    @POST("control/game/:gameId/start")
    Call<Map<String, Object>> gameStart(@Header("Authorization") String authHeader, @Path("gameId") long gameId);
    @POST("control/game/:gameId/stop")
    Call<Map<String, Object>> gameStop(@Header("Authorization") String authHeader, @Path("gameId") long gameId);
    @POST("control/challenge/:cid/start")
    Call<Map<String, Object>> challengeStart(@Header("Authorization") String authHeader, @Path("cid") long challengeId);
    @POST("control/challenge/:cid/stop")
    Call<Map<String, Object>> challengeStop(@Header("Authorization") String authHeader, @Path("cid") long challengeId);


    @POST("admin/user/add")
    Call<Map<String, Object>> userAdd(@Body UserProfile userProfile);
    @POST("admin/user/:uid/edit")
    Call<Map<String, Object>> userEdit(@Path("uid") long userId, @Body UserProfile userProfile);
    @GET("admin/user/:uid")
    Call<Map<String, Object>> userGet(@Path("uid") long userId);
    @GET("admin/user/ext/:uid")
    Call<Map<String, Object>> userGetByExt(@Path("uid") long userId);
    @DELETE("admin/user/:uid")
    Call<Map<String, Object>> userDelete(@Path("uid") long userId);

    @POST("admin/team/add")
    Call<Map<String, Object>> teamAdd(@Body TeamProfile teamProfile);
    @POST("admin/team/:tid/edit")
    Call<Map<String, Object>> teamEdit(@Path("tid") long teamId, @Body TeamProfile teamProfile);
    @GET("admin/team/:tid")
    Call<Map<String, Object>> teamGet(@Path("tid") long teamId);
    @POST("admin/team/:tid/users")
    Call<Map<String, Object>> teamGetUsers(@Path("tid") long teamId);

    @POST("admin/scope/add")
    Call<Map<String, Object>> teamScopeAdd(@Body TeamScope scope);
    @POST("admin/scope/:tsid/edit")
    Call<Map<String, Object>> teamScopeEdit(@Path("tsid") long scopeId, @Body TeamScope scope);
    @POST("admin/scope/list")
    Call<Map<String, Object>> teamScopeList();
    @GET("admin/scope/:tsid")
    Call<Map<String, Object>> teamScopeGet(@Path("tsid") long scopeId);
    @POST("admin/scope/:tsid/teams")
    Call<Map<String, Object>> teamScopeGetTeams(@Path("tsid") long scopeId);

    @POST("admin/user/add-to-team")
    Call<Map<String, Object>> userAddToTeam(@Body UserTeam userTeam);
    @POST("admin/user/:uid/current-team")
    Call<Map<String, Object>> userFindCurrentTeam(@Path("uid") long teamId);

}

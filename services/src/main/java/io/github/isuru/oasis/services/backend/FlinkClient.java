package io.github.isuru.oasis.services.backend;

import io.github.isuru.oasis.services.backend.model.JarListInfo;
import io.github.isuru.oasis.services.backend.model.JarRunResponse;
import io.github.isuru.oasis.services.backend.model.JarUploadResponse;
import io.github.isuru.oasis.services.backend.model.JobSaveRequest;
import io.github.isuru.oasis.services.backend.model.JobsStatusResponse;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FlinkClient {

    @GET("/jars")
    Observable<JarListInfo> getJars();

    @Headers("Content-Type: application/x-java-archive")
    @Multipart
    @POST("/jars/upload")
    Observable<JarUploadResponse> uploadJar(@Part MultipartBody.Part file);

    @DELETE("/jars/{jarid}")
    Observable<Void> deleteJar(@Path("jarid") String jarId);

    @GET("/jobs")
    Observable<JobsStatusResponse> jobs();

    @POST("/jars/{jarid}/run")
    Observable<JarRunResponse> runJar(@Path("jarid") String jarId,
                                @Query("program-args") String programArgs,
                                @Query("parallelism") int parallelism,
                                @Query("allowNonRestoredState") boolean allowNonResoredState,
                                @Query("savepointPath") String savepointPath);

    @POST("/jobs/:jobid/savepoints")
    Observable<Void> jobSaveAndClose(@Path("jabid") String jobId,
                               @Body JobSaveRequest saveRequest);
}

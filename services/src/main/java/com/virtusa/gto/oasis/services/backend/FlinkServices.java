package com.virtusa.gto.oasis.services.backend;

import com.virtusa.gto.oasis.services.backend.model.JarListInfo;
import com.virtusa.gto.oasis.services.backend.model.JarRunResponse;
import com.virtusa.gto.oasis.services.backend.model.JarUploadResponse;
import com.virtusa.gto.oasis.services.backend.model.JobSaveRequest;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface FlinkServices {

    @GET("/jars")
    Call<JarListInfo> getJars();

    @Headers("Content-Type: application/x-java-archive")
    @Multipart
    @POST("/jars/upload")
    Call<JarUploadResponse> uploadJar(@Part MultipartBody.Part file);

    @DELETE("/jars/{jarid}")
    Call<Void> deleteJar(@Path("jarid") String jarId);

    @POST("/jars/{jarid}/run")
    Call<JarRunResponse> runJar(@Path("jarid") String jarId,
                                @Query("program-args") String programArgs,
                                @Query("entry-class") String mainClass,
                                @Query("parallelism") int parallelism,
                                @Query("allowNonRestoredState") boolean allowNonResoredState,
                                @Query("savepointPath") String savepointPath);

    @POST("/jobs/:jobid/savepoints")
    Call<Void> jobSaveAndClose(@Path("jabid") String jarId,
                               @Body JobSaveRequest saveRequest);
}

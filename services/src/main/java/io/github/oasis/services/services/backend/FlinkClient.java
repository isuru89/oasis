/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services.backend;

import io.github.oasis.services.services.backend.model.JarListInfo;
import io.github.oasis.services.services.backend.model.JarRunResponse;
import io.github.oasis.services.services.backend.model.JarUploadResponse;
import io.github.oasis.services.services.backend.model.JobSaveRequest;
import io.github.oasis.services.services.backend.model.JobsStatusResponse;
import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FlinkClient {

    @GET("/jars")
    Observable<JarListInfo> getJars();

    @Multipart
    @POST("/jars/upload")
    Observable<JarUploadResponse> uploadJar(@Part MultipartBody.Part file);

    @DELETE("/jars/{jarid}")
    Completable deleteJar(@Path("jarid") String jarId);

    @GET("/jobs")
    Observable<JobsStatusResponse> jobs();

    @POST("/jars/{jarid}/run")
    Observable<JarRunResponse> runJar(@Path("jarid") String jarId,
                                      @Query("program-args") String programArgs,
                                      @Query("parallelism") int parallelism,
                                      @Query("allowNonRestoredState") boolean allowNonResoredState,
                                      @Query("savepointPath") String savepointPath);

    @POST("/jobs/:jobid/savepoints")
    Completable jobSaveAndClose(@Path("jabid") String jobId,
                               @Body JobSaveRequest saveRequest);
}

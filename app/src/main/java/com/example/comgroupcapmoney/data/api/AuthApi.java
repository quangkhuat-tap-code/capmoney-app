package com.example.comgroupcapmoney.data.api;

import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.AuthResult;
import com.example.comgroupcapmoney.model.UserProfile;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/register")
    Call<ApiResponse<AuthResult>> register(@Body Map<String, Object> body);

    @POST("auth/login")
    Call<ApiResponse<AuthResult>> login(@Body Map<String, Object> body);

    @GET("auth/profile")
    Call<ApiResponse<UserProfile>> getProfile();
}

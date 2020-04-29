package com.trackkers.tmark.webApi;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface ApiInterface {

    @FormUrlEncoded
    @POST("employeeLogin")
    Call<ApiResponse> Login(
            @Field("companyName") String companyName,
            @Field("mobile") String mobile,
            @Field("password") String password,
            @Field("firebaseToken") String firebaseToken
    );

    @FormUrlEncoded
    @POST("profile")
    Call<ApiResponse> Profile(@Field("token") String token);

    @FormUrlEncoded
    @POST("updatePassword")
    Call<ApiResponse> updatePassword(@Field("token") String token,
                                     @Field("currentPassword") String currentPassword,
                                     @Field("newPassword") String newPassword
    );

    @FormUrlEncoded
    @POST("pendingRoutes")
    Call<ApiResponse> PendingRoutes(@Field("token") String token);

    @FormUrlEncoded
    @POST("verifiedAssignRoutes")
    Call<ApiResponse> verifiedAssignRoutes(@Field("token") String token);

    @FormUrlEncoded
    @POST("addFiledOfficerGuard")
    Call<ApiResponse> addFiledOfficerGuard(@Field("token") String token,
                                           @Field("name") String name,
                                           @Field("mobile") String mobile,
                                           @Field("address") String address,
                                           @Field("isLiveTrackingEnable") String isLiveTrackingEnable,
                                           @Field("empcode") String empcode,
                                           @Field("password") String password,
                                           @Field("type") String type
    );

    @FormUrlEncoded
    @POST("allCheckpointDetailsForRoute")
    Call<ApiResponse> AllCheckpoints(@Field("token") String token,
                                     @Field("routeID") String routeID
    );

    @FormUrlEncoded
    @POST("verifyCheckPoint")
    Call<ApiResponse> VerifyCheckpoint(@Field("token") String token,
                                       @Field("checkPointId") String checkPointId,
                                       @Field("latitude") String latitude,
                                       @Field("longitude") String longitude
    );

    @Multipart
    @POST("siteVisit/operationVisitByScanQr")
    Call<ApiResponse> operationVisitByScanQr(@Part("token") RequestBody token,
                                             @Part("type") RequestBody type,
                                             @Part("id") RequestBody id,
                                             @Part("latitude") RequestBody latitude,
                                             @Part("longitude") RequestBody longitude,
                                             @Part MultipartBody.Part image
    );

    @FormUrlEncoded
    @POST("siteVisit/getAllSitesForFo")
    Call<ApiResponse> SiteDetails(@Field("token") String token);

    @Multipart
    @POST("siteVisit/startSiteVisit")
    Call<ApiResponse> StartSiteVisit(@Part("token") RequestBody token,
                                     @Part("suid") RequestBody suid,
                                     @Part("visitStartLatitude") RequestBody visitStartLatitude,
                                     @Part("visitStartLongitude") RequestBody visitStartLongitude,
                                     @Part MultipartBody.Part selfie

    );

    @FormUrlEncoded
    @POST("siteVisit/getFoSiteSurvey")
    Call<SurveyResponse> getFoSiteSurvey(@Field("token") String token,
                                         @Field("suid") String suid
    );

    @FormUrlEncoded
    @POST("siteVisit/siteInstructionApi")
    Call<ApiResponse> siteInstruction(@Field("suid") String suid);


    @FormUrlEncoded
    @POST("siteVisit/endSiteVisit")
    Call<ApiResponse> EndSiteVisit(@Field("token") String token,
                                   @Field("visitToken") String visitToken,
                                   @Field("survuid") String survuid,
                                   @Field("visitEndLatitude") String visitEndLatitude,
                                   @Field("visitEndLongitude") String visitEndLongitude,
                                   @Field("data") String responseData
    );


    @Multipart
    @POST("siteVisit/postTextOrImage")
    Call<ApiResponse> PostTextOrImage(@Part("token") RequestBody token,
                                      @Part("visitToken") RequestBody visitToken,
                                      @Part("type") RequestBody type,
                                      @Part("text") RequestBody text,
                                      @Part MultipartBody.Part selfie
    );

    @FormUrlEncoded
    @POST("getAllGuardsListInFieldOffice")
    Call<ApiResponse> getAllGuardsListInFieldOffice(@Field("token") String token);

    @FormUrlEncoded
    @POST("fielOfficerToAssignGuard")
    Call<ApiResponse> fielOfficerToAssignGuard(@Field("euid") String euid,
                                               @Field("ruid") String ruid
    );

    @FormUrlEncoded
    @POST("partialListGuard")
    Call<ApiResponse> partialListGuard(@Field("token") String token,
                                       @Field("routeId") String routeId
    );

    @FormUrlEncoded
    @POST("unAssignGuardToRoute")
    Call<ApiResponse> unassignGuardToRoute(@Field("empId") String empId,
                                           @Field("routeId") String routeId
    );

    @FormUrlEncoded
    @POST("getFoVerificationHistory")
    Call<ApiResponseHistory> VerificationHistoryFo(@Field("token") String token);

    @FormUrlEncoded
    @POST("getAllRouteForGuard")
    Call<ApiResponse> getAllRouteForGuard(@Field("token") String token);

    @FormUrlEncoded
    @POST("guardPartialDetails")
    Call<ApiResponse> GetGuardPartialDetails(@Field("token") String token,
                                             @Field("routeId") String routeID
    );

    @Multipart
    @POST("guardCheckIn")
    Call<ApiResponse> GuardCheckIn(
            @Part("token") RequestBody token,
            @Part("routeId") RequestBody routeId,
            @Part MultipartBody.Part selfie,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("deviceId") RequestBody deviceId,
            @Part("batteryStatus") RequestBody batteryStatus
    );

    @FormUrlEncoded
    @POST("guardCheckOut")
    Call<ApiResponse> GuardCheckOut(@Field("token") String token,
                                    @Field("routeId") String routeID,
                                    @Field("latitude") String latitude,
                                    @Field("longitude") String longitude,
                                    @Field("batteryStatus") String batteryStatus
    );

    @Multipart
    @POST("guardScanCheckpoint")
    Call<ApiResponse> GuardScanCheckpoint(@Part("token") RequestBody token,
                                          @Part("checkPointId") RequestBody chkuid,
                                          @Part("deviceId") RequestBody deviceId,
                                          @Part("latitude") RequestBody latitude,
                                          @Part("longitude") RequestBody longitude,
                                          @Part("batteryStatus") RequestBody batteryStatus,
                                          @Part MultipartBody.Part guardScanImage
    );

    @FormUrlEncoded
    @POST("guardScanHistory")
    Call<ApiResponseHistoryGuard> guardScanHistory(@Field("token") String token,
                                                   @Field("date") String date
    );

    @FormUrlEncoded
    @POST("administratorDetailsHelp")
    Call<ApiResponse> AdministratorDetailsHelp(@Field("token") String token);

    @FormUrlEncoded
    @POST("alarmMissingGuard")
    Call<ApiResponse> AlarmMissingGuard(@Field("token") String token,
                                        @Field("routeId") String routeId
    );

    @FormUrlEncoded
    @POST("routeSpecificMultipleGuardLogin")
    Call<ApiResponseOperations> MultipleGuardsLogin(
            @Field("companyEmail") String companyEmail,
            @Field("siteCode") String siteCode,
            @Field("routeCode") String routeCode
    );

    @FormUrlEncoded
    @POST("multipleGuardPartialDetails")
    Call<ApiResponseOperations> multipleGuardPartialDetails(@Field("euid") String euid,
                                                            @Field("routeId") String routeID
    );

    @Multipart
    @POST("guardMultipleCheckIn")
    Call<ApiResponseOperations> guardMultipleCheckIn(
            @Part("euid") RequestBody euid,
            @Part("routeId") RequestBody routeId,
            @Part MultipartBody.Part selfie,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("deviceId") RequestBody deviceId,
            @Part("batteryStatus") RequestBody batteryStatus
    );

    @FormUrlEncoded
    @POST("guardMultipleCheckOut")
    Call<ApiResponseOperations> guardMultipleCheckOut(@Field("euid") String euid,
                                                      @Field("routeId") String routeID,
                                                      @Field("latitude") String latitude,
                                                      @Field("longitude") String longitude,
                                                      @Field("batteryStatus") String batteryStatus
    );

    @FormUrlEncoded
    @POST("eRegisterGuardHistory")
    Call<ApiResponse> guardMultipleHistory(@Field("euid") String euid,
                                           @Field("ruid") String ruid,
                                           @Field("date") String date
    );

    @FormUrlEncoded
    @POST("operationalPartial")
    Call<ApiResponse> operationalPartial(
            @Field("token") String token
    );

    @FormUrlEncoded
    @POST("operationalHistory")
    Call<ApiResponseHistoryOperational> operationalHistory(
            @Field("token") String token,
            @Field("dateSearchData") String dateSearchData
    );

    @Multipart
    @POST("operationalCheckIn")
    Call<ApiResponse> operationalCheckIn(
            @Part("token") RequestBody token,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("batteryStatus") RequestBody batteryStatus,
            @Part("message") RequestBody message,
            @Part MultipartBody.Part selfie
    );

    @Multipart
    @POST("communication")
    Call<ApiResponse> communication(
            @Part("token") RequestBody token,
            @Part("type") RequestBody type,
            @Part("text") RequestBody text,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part MultipartBody.Part image,
            @Part("isOperational") RequestBody isOperational
    );

    @FormUrlEncoded
    @POST("viewDocument")
    Call<ApiResponse> viewDocument(
            @Field("token") String token
    );

    @FormUrlEncoded
    @POST("empConenyanceData")
    Call<ApiResponse> empConenyanceData(
            @Field("token") String token,
            @Field("distanceTraveled") String distanceTraveled,
            @Field("fare") String fare
    );

    @FormUrlEncoded
    @POST("operationalCheckOut")
    Call<ApiResponse> operationalCheckOut(
            @Field("token") String token,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("batteryStatus") String batteryStatus
    );

    @FormUrlEncoded
    @POST("logoutEmp")
    Call<ApiResponse> logoutEmp(
            @Field("token") String token
    );

    @FormUrlEncoded
    @POST("sos")
    Call<ApiResponse> sos(
            @Field("token") String token
    );

}

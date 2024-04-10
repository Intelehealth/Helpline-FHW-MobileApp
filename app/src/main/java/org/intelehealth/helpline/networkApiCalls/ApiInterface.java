package org.intelehealth.helpline.networkApiCalls;


import org.intelehealth.helpline.activities.callflow.models.CallFlowResponse;
import org.intelehealth.helpline.activities.callflow.models.CallFlowResponseModelClass;
import org.intelehealth.helpline.activities.callflow.models.MissedCallsResponseModel;
import org.intelehealth.helpline.models.ChangePasswordModel_New;
import org.intelehealth.helpline.models.ChangePasswordParamsModel_New;
import org.intelehealth.helpline.models.CheckAppUpdateRes;
import org.intelehealth.helpline.models.DownloadMindMapRes;
import org.intelehealth.helpline.models.ForgotPasswordApiResponseModel_New;
import org.intelehealth.helpline.models.Location;
import org.intelehealth.helpline.models.OTPVerificationParamsModel_New;
import org.intelehealth.helpline.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.helpline.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.helpline.models.RequestOTPParamsModel_New;
import org.intelehealth.helpline.models.ResetPasswordResModel_New;
import org.intelehealth.helpline.models.Results;
import org.intelehealth.helpline.models.callflow.CallFlowRequestParamsModel;
import org.intelehealth.helpline.models.dto.ResponseDTO;
import org.intelehealth.helpline.models.hwprofile.Profile;
import org.intelehealth.helpline.models.hwprofile.ProfileCreateAttribute;
import org.intelehealth.helpline.models.hwprofile.ProfileUpdateAge;
import org.intelehealth.helpline.models.hwprofile.ProfileUpdateAttribute;
import org.intelehealth.helpline.models.loginModel.LoginModel;
import org.intelehealth.helpline.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.helpline.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.helpline.models.prescriptionUpload.EndVisitEncounterPrescription;
import org.intelehealth.helpline.models.prescriptionUpload.EndVisitResponseBody;
import org.intelehealth.helpline.models.prescriptionUpload.ObsPrescResponse;
import org.intelehealth.helpline.models.prescriptionUpload.ObsPrescription;
import org.intelehealth.helpline.models.providerImageRequestModel.ProviderProfile;
import org.intelehealth.helpline.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.helpline.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.helpline.models.statewise_location.District_Sanch_Village;
import org.intelehealth.helpline.models.statewise_location.State;
import org.intelehealth.helpline.utilities.authJWT_API.AuthJWTBody;
import org.intelehealth.helpline.utilities.authJWT_API.AuthJWTResponse;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {


    //State-wise location
    @GET("location?tag=State&v=custom:(uuid,display)")
    Observable<State> STATE_OBSERVABLE();

    //District-wise location
    @GET("location/{state_uuid}?&v=custom:(uuid,display,childLocations:(uuid,display))")
    Observable<District_Sanch_Village> DISTRICT_SANCH_VILLAGE_OBSERVABLE(@Path("state_uuid") String state_uuid, @Header("Authorization") String authHeader);

    @GET("location?tag=Login%20Location")
    Observable<Results<Location>> LOCATION_OBSERVABLE(@Query("v") String representation);


    @DELETE
    Call<Void> DELETE_ENCOUNTER(@Url String url,
                                @Header("Authorization") String authHeader);

    //EMR-Middleware/webapi/pull/pulldata/
    @GET
    Call<ResponseDTO> RESPONSE_DTO_CALL(@Url String url,
                                        @Header("Authorization") String authHeader);

    @GET
    Observable<LoginModel> LOGIN_MODEL_OBSERVABLE(@Url String url,
                                                  @Header("Authorization") String authHeader);


    @GET
    Observable<LoginProviderModel> LOGIN_PROVIDER_MODEL_OBSERVABLE(@Url String url,
                                                                   @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST
    Single<PushResponseApiCall> PUSH_RESPONSE_API_CALL_OBSERVABLE(@Url String url,
                                                                  @Header("Authorization") String authHeader,
                                                                  @Body PushRequestApiCall pushRequestApiCall);

    @GET
    Observable<ResponseBody> PERSON_PROFILE_PIC_DOWNLOAD(@Url String url,
                                                         @Header("Authorization") String authHeader);

    @POST
    Single<ResponseBody> PERSON_PROFILE_PIC_UPLOAD(@Url String url,
                                                   @Header("Authorization") String authHeader,
                                                   @Body PatientProfile patientProfile);

    @GET
    Observable<ResponseBody> OBS_IMAGE_DOWNLOAD(@Url String url,
                                                @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST
    @Multipart
    Observable<ObsJsonResponse> OBS_JSON_RESPONSE_OBSERVABLE(@Url String url,
                                                             @Header("Authorization") String authHeader,
                                                             @Part MultipartBody.Part image,
                                                             @Part("json") ObsPushDTO obsJsonRequest);

    @DELETE
    Observable<Void> DELETE_OBS_IMAGE(@Url String url, @Header("Authorization") String authHeader);


    @GET("/api/mindmap/download")
    Observable<DownloadMindMapRes> DOWNLOAD_MIND_MAP_RES_OBSERVABLE(@Query("key") String licenseKey);

    @GET("/intelehealth/app_update.json")
    Single<CheckAppUpdateRes> checkAppUpdate();

    @Headers({"Accept: application/json"})
    @POST
    Observable<EndVisitResponseBody> END_VISIT_RESPONSE_BODY_OBSERVABLE(
            @Url String url,
            @Body EndVisitEncounterPrescription endVisitEncounterPrescription,
            @Header("Authorization") String authHeader);

    @POST
    Observable<ObsPrescResponse> OBS_PRESCRIPTION_UPLOAD
            (@Url String url,
             @Body ObsPrescription prescription,
             @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST
    Observable<ResponseBody> OBS_SIGNANDSUBMIT_STATUS(
            @Url String url,
            @Body EndVisitEncounterPrescription prescription,
            @Header("Authorization") String authHeader);

    @DELETE
    Observable<Response<Void>> DELETE_PRESCOBS_ITEM(
            @Url String url,
            @Header("Authorization") String authHeader);


    @POST("/openmrs/ws/rest/v1/password")
    Observable<ResponseBody> CHANGE_PASSWORD_OBSERVABLE(@Body ChangePasswordModel_New changePasswordParamsModel_new,
                                                        @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST("/api/auth/requestOtp")
    Observable<ForgotPasswordApiResponseModel_New> REQUEST_OTP_OBSERVABLE(@Body RequestOTPParamsModel_New requestOTPParamsModel_new);

    @Headers({"Accept: application/json"})
    @POST("/api/auth/verifyOtp")
    Observable<ForgotPasswordApiResponseModel_New> VERFIY_OTP_OBSERVABLE(@Body OTPVerificationParamsModel_New OTPVerificationParamsModel_new);

    @POST("api/openmrs/forgetPassword/resetPassword/{userUuid}")
    Call<ResetPasswordResModel_New> resetPassword(@Path("userUuid") String userUuid,
                                                  @Body ChangePasswordParamsModel_New changePasswordParamsModel_new,
                                                  @Header("Authorization") String authHeader);

    @POST("/api/auth/resetPassword/{userUuid}")
    Observable<ResetPasswordResModel_New> RESET_PASSWORD_OBSERVABLE(@Path("userUuid") String userUuid,
                                                                    @Body ChangePasswordParamsModel_New changePasswordParamsModel_new);


    @POST
    Single<ResponseBody> PROVIDER_PROFILE_PIC_UPLOAD(@Url String url,
                                                     @Body ProviderProfile patientProfile,
                                                     @Header("Authorization") String authHeader);


    @GET
    Observable<ResponseBody> PROVIDER_PROFILE_PIC_DOWNLOAD(@Url String url,
                                                           @Header("Authorization") String authHeader);

    @GET
    Observable<Profile> PROVIDER_PROFILE_DETAILS_DOWNLOAD(@Url String url,
                                                          @Header("Authorization") String authHeader);

    @POST("/openmrs/ws/rest/v1/person/{userUuid}")
    Observable<ResponseBody> PROFILE_AGE_UPDATE(@Path("userUuid") String userUuid,
                                                @Body ProfileUpdateAge profileUpdateAge, @Header("Authorization") String authHeader);

    @POST("/openmrs/ws/rest/v1/provider/{userUuid}/attribute")
    Observable<ResponseBody> PROFILE_ATTRIBUTE_CREATE(@Path("userUuid") String userUuid,
                                                      @Body ProfileCreateAttribute profileCreateAttribute, @Header("Authorization") String authHeader);

    @POST("attribute/{attributeUuid}")
    Observable<ResponseBody> PROFILE_ATTRIBUTE_UPDATE(@Path("attributeUuid") String attributeUuid,
                                                      @Body ProfileUpdateAttribute profileUpdateAttribute, @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST
    Observable<AuthJWTResponse> AUTH_LOGIN_JWT_API(
            @Url String url,
            @Body AuthJWTBody authJWTBody
    );

    @POST("/api/mindmap/startCall")
    Observable<CallFlowResponse> initiateCallFlow(@Body CallFlowRequestParamsModel callFlowRequestParamsModel);


    @GET
    Observable<CallFlowResponseModelClass> getCallRecordings(@Url String url,
                                                             @Header("Authorization") String authHeader);

    @GET
    Observable<MissedCallsResponseModel> getMissedCalls(@Url String url,
                                                        @Header("Authorization") String authHeader);

    /*   @POST
       Observable<JSONObject> sendSMS(@Url String url,
                                @Body SendSMSRequestModel sendSMSRequestModel);

   */
    @POST
    Observable<ResponseBody> sendSMS(@Url String url,
                                   @Query("module") String module,
                                   @Query("apikey") String apikey,
                                   @Query("to") String to,
                                   @Query("from") String from,
                                   @Query("msg") String msg,
                                   @Query("ctid") String ctid);


}

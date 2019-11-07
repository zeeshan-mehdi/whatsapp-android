package com.shaikhutech.whatsapp.Fragments;

import com.shaikhutech.whatsapp.Notifications.MyResponse;
import com.shaikhutech.whatsapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers (
        {
            "Content-Type:application/json",
            "Authorization-key=AAAAEYLtyIw:APA91bFfEMkYn0xuWrQcSUamRL-qAfw4_q8vllv6cVclZYSbwaSxISOGn0dOdAHHsMWrvnsAQFoHnN1AAusT20C749WxQmHJRh9Vv-RgBqZjxcCN1gUNrb-JE1Nrzar_AWL0Gs38R3_z"
        }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);


}

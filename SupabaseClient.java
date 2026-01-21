package com.example.expenseapp.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    // Make sure this URL ends with /rest/v1/
    private static final String BASE_URL = "https://wqvypkcapvlnvmenhqdf.supabase.co/rest/v1/";

    // Your anon key
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indxdnlwa2NhcHZsbnZtZW5ocWRmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc5Njk0MjcsImV4cCI6MjA4MzU0NTQyN30.A5sDFseDAspuZtCetgkp5hh9orYWV4FDA9Yb22_8n-s";

    private static Retrofit retrofit;
    private static SupabaseService service;

    public static SupabaseService getService() {
        if (service == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> chain.proceed(
                            chain.request()
                                    .newBuilder()
                                    .addHeader("apikey", SUPABASE_KEY)
                                    .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Prefer", "return=representation")
                                    .build()
                    ))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(SupabaseService.class);
        }
        return service;
    }
}
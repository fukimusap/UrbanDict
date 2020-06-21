package nike.urbandict.api

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface UrbanDictApi {


    @GET("/define")
    suspend fun define(@Query("term") term: String): DefineResponse

    companion object {
        const val DEFAULT_URL = "https://mashape-community-urban-dictionary.p.rapidapi.com"
    }
}

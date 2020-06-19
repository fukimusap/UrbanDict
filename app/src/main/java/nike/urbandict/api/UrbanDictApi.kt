package nike.urbandict.api

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface UrbanDictApi {

    @Headers(
        "x-rapidapi-host: mashape-community-urban-dictionary.p.rapidapi.com",
        "x-rapidapi-key: f2ba86279fmsh184468eceeb0123p116c5djsn2d8cd17af2d8"
    )
    @GET("/define")
    suspend fun define(@Query("term") term: String): DefineResponse

    companion object {
        const val DEFAULT_URL = "https://mashape-community-urban-dictionary.p.rapidapi.com"
    }
}

import com.example.playlistmaker.TracksResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("/search?entity=song")
    fun search(@Query("term") text: String): Call<TracksResponse>
    @GET("/lookup")
    fun lookup(@Query("id") id: Long): Call<TracksResponse>
}
package com.together.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.annotation.IntDef
import com.firebase.ui.auth.AuthUI
import com.together.base.UiState
import com.together.repository.Result
import kotlin.reflect.full.memberProperties


object TryIndef {

    const val ARTICLE_LIST = 0  //L
    const val STAT_2 = 2
    const val STAT_3 = 3

    @Retention(AnnotationRetention.SOURCE)
    @IntDef( ARTICLE_LIST, STAT_2, STAT_3)
    annotation class FireDataBaseType

}

object DataHelper {

    fun dataArticleToUi(item: Result.Article) : UiState.Article {
        val unitList = prepareUnitData(item)
        val defaultUnit = unitList.firstOrNull { it.name == "kg" }
        val indexOfDefault = if (defaultUnit != null) unitList.indexOf(defaultUnit) else 0

        return UiState.Article(

            _id = item.id,
            productName = item.productName,
            productDescription = item.productDescription,
            remoteImageUrl = item.imageUrl,

            units = unitList,

            //unit = unitList[indexOfDefault].name,
//            pricePerUnit = unitList[indexOfDefault].price,

            discount = item.discount,
            _mode = item.mode,
            available = item.available
        )
    }


    private fun prepareUnitData(article: Result.Article): List<UiState.Unit> {
        // units = "2,50:€:kg;0,50:€:Bund"
        val units = article.unit.split(";")
        val result = mutableListOf<UiState.Unit>()
        units.forEach {
            val p = it.split(":")
            val w: String = if (p.size > 1) p[1] else ""
            val r = UiState.Unit(
                name = p[0],
                price = p[0],
                averageWeight = w
//                ,
//                mode = map[it.key] ?: -1
            )
            result.add(r)
        }
        return result.toList()

    }

    private val map = HashMap<String, Int>().apply {
        put("kg", 0)
        put("Stück", 1)
        put("Bund", 2)
        put("Schale", 3)
    }

}


inline fun <reified T : UiState> errorActions(profile: T, action: () -> Unit)  : Boolean {
    val toBeChecked =
        T::class.memberProperties.filter { !it.name.startsWith("_") }
    toBeChecked.forEach { prop ->
        val p = prop.get(profile) as String
        if(p.isEmpty()) {
            action()
//            MainMessagePipe.uiEvent.onNext(UiEvent.ShowToast(requireContext(), R.string.developer_error_hint))
            return false
        }
    }
    return true
}

object AQ {

    private fun getConnectionManager(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun isConnectedTo(connectivityManager: ConnectivityManager, type: Int) : Boolean {
//        @deprecated Applications should instead use {@link NetworkCapabilities#hasCapability} or WIFI_SERVICE
        val ni = connectivityManager.activeNetworkInfo
        return ni !=null && ni.isConnected && ni.type ==  type //ConnectivityManager.TYPE_WIFI
    }

    fun hasInternetConnection(context: Context): Boolean {
        return isConnectedTo(getConnectionManager(context),ConnectivityManager.TYPE_WIFI) ||
                isConnectedTo(getConnectionManager(context),ConnectivityManager.TYPE_MOBILE)
    }


    fun getFirebaseUIStarter(): Intent {
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers).build()
    }

}
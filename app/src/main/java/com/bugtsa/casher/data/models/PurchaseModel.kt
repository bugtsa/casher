package com.bugtsa.casher.data.models

import com.bugtsa.casher.data.dto.CategoryDto
import com.bugtsa.casher.networking.CasherApi
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseModel @Inject constructor(injectCasherRestApi: CasherApi) {

    companion object {
        private var instance: PurchaseModel? = null
    }

    init {
        instance = this
    }

    var sizePurchaseList: Int = 0
    private val casherRestApi = injectCasherRestApi

    fun getCategoriesList(): Flowable<List<CategoryDto>> {
        return casherRestApi.getCategories()
                .flatMapIterable { res -> res }
                .map { res -> CategoryDto(res.id?.toLong() ?: 0L, res.name ?: "") }
                .toList()
                .toFlowable()
    }

    fun getCategoriesStringList(): Flowable<List<String>> {
        return casherRestApi.getCategories()
                .flatMapIterable { res -> res }
                .map { res -> res.name ?: "" }
                .toList()
                .toFlowable()
    }
}
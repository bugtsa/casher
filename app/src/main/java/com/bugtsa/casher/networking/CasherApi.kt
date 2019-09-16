package com.bugtsa.casher.networking

import com.bugtsa.casher.data.dto.CategoryDto
import com.bugtsa.casher.data.dto.CategoryRes
import com.bugtsa.casher.data.dto.PaymentDto
import com.bugtsa.casher.data.dto.PaymentsByDayRes
import com.bugtsa.casher.utils.ConstantManager.CategoryNetwork.CATEGORY_NAME_METHOD
import com.bugtsa.casher.utils.ConstantManager.Network.LAST_PAGE_PAYMENT_NAME_METHOD
import com.bugtsa.casher.utils.ConstantManager.Network.PAGE_PAYMENT_NAME_METHOD
import com.bugtsa.casher.utils.ConstantManager.Network.PAYMENT_NAME_METHOD
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.FormBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CasherApi {

    @GET(CATEGORY_NAME_METHOD)
    fun getCategories(): Flowable<List<CategoryRes>>

    @POST(CATEGORY_NAME_METHOD)
    fun addCategory(@Body nameCategory: FormBody): Single<CategoryDto>

    @GET("$PAYMENT_NAME_METHOD$PAGE_PAYMENT_NAME_METHOD$LAST_PAGE_PAYMENT_NAME_METHOD")
    fun getPagedPayments(): Flowable<List<PaymentsByDayRes>>

    @POST("$PAYMENT_NAME_METHOD/full")
    fun addPayment(@Body payment: FormBody): Single<PaymentDto>
}
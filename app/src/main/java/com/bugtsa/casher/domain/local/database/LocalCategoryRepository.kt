package com.bugtsa.casher.domain.local.database

import com.bugtsa.casher.data.dto.CategoryDto
import io.reactivex.Flowable
import io.reactivex.Single

interface LocalCategoryRepository {
    fun add(category: CategoryDto): Single<CategoryDto>
    fun getCategoriesList(): Flowable<List<CategoryDto  >>
}
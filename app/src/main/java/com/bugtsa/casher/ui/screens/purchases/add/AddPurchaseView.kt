package com.bugtsa.casher.ui.screens.purchases.add

interface AddPurchaseView {
    fun completedAddPurchase()

    fun showProgressBar()
    fun hideProgressBar()

    fun setSearchText(result: String)

    fun setupCategoriesList(categoriesList: List<String>)

    fun setupCurrentDateAndTime(dateAndTimeString: String)
    fun setupCustomDateAndTime(date: String, time: String)
    fun showDatePicker()
    fun showTimePicker()
}
package com.bugtsa.casher.ui.screens.purchases.add_purchase

import com.bugtsa.casher.arch.models.PurchaseModel
import com.bugtsa.casher.data.dto.PurchaseDto
import com.bugtsa.casher.networking.GoogleSheetService
import com.bugtsa.casher.utils.ConstantManager.Companion.END_COLUMN_SHEET
import com.bugtsa.casher.utils.ConstantManager.Companion.START_COLUMN_SHEET
import com.bugtsa.casher.utils.ConstantManager.Companion.PURCHASE_TABLE_NAME_SHEET
import com.bugtsa.casher.utils.GoogleSheetManager.Companion.OWN_GOOGLE_SHEET_ID
import com.bugtsa.casher.utils.SoftwareUtils
import com.bugtsa.casher.utils.SoftwareUtils.Companion.getCurrentTimeStamp
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import com.bugtsa.casher.data.LocalCategoryDataStore
import com.bugtsa.casher.utils.ParentConstantManager.Companion.CATEGORIES_TABLE_NAME_SHEET
import com.bugtsa.casher.utils.ParentConstantManager.Companion.DELIMITER_BETWEEN_COLUMNS
import com.bugtsa.casher.utils.ParentConstantManager.Companion.MAJOR_DIMENSION
import com.bugtsa.casher.utils.ParentConstantManager.Companion.VALUE_INPUT_OPTION
import com.maxproj.calendarpicker.Models.YearMonthDay
import io.reactivex.Flowable

import timber.log.Timber
import java.util.concurrent.TimeUnit


class AddPurchasePresenter @Inject constructor(googleSheetService: GoogleSheetService,
                                               compositeDisposable: CompositeDisposable) {

    private var serviceSheets: Sheets = googleSheetService.mService
    private var disposableSubscriptions: CompositeDisposable = compositeDisposable

    @Inject
    lateinit var purchaseModel: PurchaseModel
    @Inject
    lateinit var localCategoryDataStore: LocalCategoryDataStore

    var lastNotEmptyPurchaseRow: Int = 0
    var installDate: String = ""

    lateinit var addPurchaseView: AddPurchaseView


    //region ================ Base Methods =================

    fun onAttachView(addPurchaseView: AddPurchaseView) {
        this.addPurchaseView = addPurchaseView
        lastNotEmptyPurchaseRow = purchaseModel.sizePurchaseList
    }

    fun onViewDestroy() {
        disposableSubscriptions.dispose()
    }

    //endregion

    //region ================= Categories From Database =================

    fun checkExistCategoriesInDatabase() {
        disposableSubscriptions.add(
                localCategoryDataStore.getCategoriesList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap { categoryList ->
                            val nameList: MutableList<String> = mutableListOf()
                            for (categoryEntity in categoryList) {
                                nameList.add(categoryEntity.name)
                            }
                            Flowable.fromArray(nameList)
                        }
                        .subscribe({ categoriesList: List<String> ->
                            addPurchaseView.setupCategoriesList(categoriesList)
                            Timber.d("get all categories")
                        },
                                { t -> Timber.e(t, "error at check exist categories " + t) }))
    }

    //endregion


    //region ================= Request to add purchase =================

    fun addPurchase(pricePurchase: String, categoryPurchase: String) {
        addPurchaseView.showProgressBar()
        disposableSubscriptions.add(
                addPurchaseSubscriber(serviceSheets,
                        PurchaseDto(pricePurchase,
                                SoftwareUtils.timeStampToString(getCurrentTimeStamp(), Locale.getDefault()),
                                categoryPurchase))!!
                        .subscribe(this::onBatchPurchasesCollected,
                                this::onBatchPurchasesCollectionFailure))
        performCheckStorageCategoriesList(categoryPurchase)
    }

    //endregion

    //region ================= Add & Check Server Categories List =================

    private fun performCheckStorageCategoriesList(currentCategory: String) {
        disposableSubscriptions.add(
                localCategoryDataStore.getCategoriesList()
                        .subscribeOn(Schedulers.io())
                        .map { it.mapNotNull { it.name } }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ storageCategoriesList: List<String> ->
                            if (!isContainsCurrentCategoryInDatabase(currentCategory, storageCategoriesList)) {
                                addCategoryToDatabase(currentCategory)
                                addCategoryToServer(serviceSheets, currentCategory, storageCategoriesList.size + 1)
                            }
                        },
                                { t -> Timber.e(t, "error at check exist categories") }))
    }

    private fun isContainsCurrentCategoryInDatabase(currentCategory: String, storageCategoriesList: List<String>): Boolean {
        if (!storageCategoriesList.isEmpty()) {
            for (category in storageCategoriesList) {
                if (storageCategoriesList.contains(currentCategory)) {
                    return true
                }
            }
        }
        return false
    }

    private fun addCategoryToDatabase(category: String) {
        disposableSubscriptions.add(
                localCategoryDataStore.add(category)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ Timber.d("add category to database success") },
                                { t -> Timber.e(t, "add category to database error") }))
    }

    private fun addCategoryToServer(service: Sheets, currentCategory: String, firstEmptyRow: Int){
        val data: MutableList<Any> = mutableListOf(currentCategory)
        val arrayData = mutableListOf(data)

        val valueData: ValueRange = ValueRange()
                .setRange(CATEGORIES_TABLE_NAME_SHEET + START_COLUMN_SHEET + firstEmptyRow)
                .setValues(arrayData)
                .setMajorDimension(MAJOR_DIMENSION)
        val batchData: BatchUpdateValuesRequest = BatchUpdateValuesRequest()
                .setValueInputOption(VALUE_INPUT_OPTION)
                .setData(mutableListOf(valueData))

        disposableSubscriptions.add(Single.just("")
                .subscribeOn(Schedulers.newThread())
                .flatMap { emptyString ->
                    Single.just(service!!.spreadsheets().values()
                            .batchUpdate(OWN_GOOGLE_SHEET_ID, batchData)
                            .execute())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ Timber.d("add category to server success") },
                        { t -> Timber.e(t, "add category to server error") }))
    }

    //endregion

    //region ================= Purchase Subscriber =================

    private fun addPurchaseSubscriber(service: Sheets, purchase: PurchaseDto): Single<BatchUpdateValuesResponse>? {
        val data: MutableList<Any> = mutableListOf(purchase.price, purchase.time, purchase.category)
        val arrayData = mutableListOf(data)
        purchaseModel.sizePurchaseList = lastNotEmptyPurchaseRow + 1
        lastNotEmptyPurchaseRow = purchaseModel.sizePurchaseList
        val valueData: ValueRange = ValueRange()
                .setRange(PURCHASE_TABLE_NAME_SHEET + START_COLUMN_SHEET + lastNotEmptyPurchaseRow + DELIMITER_BETWEEN_COLUMNS + END_COLUMN_SHEET + lastNotEmptyPurchaseRow)
                .setValues(arrayData)
                .setMajorDimension(MAJOR_DIMENSION)
        val batchData: BatchUpdateValuesRequest = BatchUpdateValuesRequest()
                .setValueInputOption(VALUE_INPUT_OPTION)
                .setData(mutableListOf(valueData))

        return Single.just("")
                .subscribeOn(Schedulers.newThread())
                .flatMap { _ ->
                    Single.just(service!!.spreadsheets().values()
                            .batchUpdate(OWN_GOOGLE_SHEET_ID, batchData)
                            .execute())
                }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onBatchPurchasesCollected(batchUpdateValuesRes: BatchUpdateValuesResponse) {
        if (!batchUpdateValuesRes.isEmpty()) {
            addPurchaseView.hideProgressBar()
            addPurchaseView.completedAddPurchase()
        }
    }

    fun onBatchPurchasesCollectionFailure(throwable: Throwable) {
    }

    //endregion

    //region ================= Setup Current Date =================

    fun setupCurrentDate() {
        addPurchaseView.setupCurrentDate(SoftwareUtils.modernTimeStampToString(getCurrentTimeStamp(), Locale.getDefault()))
        refreshCurrentDate()
    }


    private fun refreshCurrentDate() {
        disposableSubscriptions.add(Flowable
                .interval(10, TimeUnit.SECONDS)
                .flatMap { t ->
                    Flowable.just(
                            SoftwareUtils.modernTimeStampToString(getCurrentTimeStamp(), Locale.getDefault()))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result -> addPurchaseView.setupCurrentDate(result) }, { err -> }))
    }

    fun checkShowDateAndTimePickers(checked: Boolean) {
        if (checked) {
            addPurchaseView.setupDatePicker()
            disposableSubscriptions.clear()
        } else {
            setupCurrentDate()
        }
    }

    fun changeCalendar(selectedDate: YearMonthDay) {
        installDate = "" + String.format("%02d", selectedDate.day) + "." +
                String.format("%02d", selectedDate.month) + "." +
                selectedDate.year
                        .toString()
                        .substring(selectedDate.year.toString().length - 2)
        addPurchaseView.setupTimePicker()
    }

    fun changeTime(hourString: String, minuteString: String) {
        addPurchaseView.setupChangedDate(installDate, hourString + ":" + minuteString)
    }

    //endregion
}
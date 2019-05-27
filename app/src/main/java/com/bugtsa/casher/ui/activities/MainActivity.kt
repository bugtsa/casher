package com.bugtsa.casher.ui.activities

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bugtsa.casher.navigation.NavigationStack
import com.bugtsa.casher.navigation.TabBar
import com.bugtsa.casher.ui.screens.TestForm
import com.bugtsa.casher.ui.screens.TestScreen
import com.bugtsa.casher.ui.screens.purchases.show.PurchasesScreen
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_root.*
import pro.horovodovodo4ka.bones.Bone
import pro.horovodovodo4ka.bones.Finger
import pro.horovodovodo4ka.bones.Spine
import pro.horovodovodo4ka.bones.Wrist
import pro.horovodovodo4ka.bones.extensions.glueWith
import pro.horovodovodo4ka.bones.extensions.processBackPress
import pro.horovodovodo4ka.bones.statesstore.EmergencyPersister
import pro.horovodovodo4ka.bones.statesstore.EmergencyPersisterInterface
import pro.horovodovodo4ka.bones.ui.SpineNavigatorInterface
import pro.horovodovodo4ka.bones.ui.delegates.NavigatorDelayedTransactions
import pro.horovodovodo4ka.bones.ui.delegates.SpineNavigator
import pro.horovodovodo4ka.bones.ui.helpers.ActivityAppRestartCleaner
import toothpick.Scope
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Demo bone. Realize support of exiting from app and back presses.
 */
class RootBone(root: Bone) :
        Spine(root),
        Wrist.Listener,
        Finger.Listener,
        Spine.Listener {

    init {
        persistSibling = true
    }

    private var canExit = false

    fun dropExitStatus() {
        canExit = false
    }

    override fun fingerSwitched(transition: Wrist.TransitionType) = dropExitStatus()
    override fun phalanxSwitched(transition: Finger.TransitionType) = dropExitStatus()
    override fun boneSwitched(transition: Spine.TransitionType) = dropExitStatus()

    fun processBack(preExitCallback: () -> Unit): Boolean {
        if (processBackPress()) {
            if (!canExit) {
                preExitCallback()
                canExit = true
                return false
            }
        }
        return canExit
    }
}

/**
 * Demo activity.
 * Uses [EmergencyPersister] for bone survive between configuration changes.
 * Also uses [ActivityAppRestartCleaner] for cleanup fragments when activity restarts with some bundle data and cannot
 * load bones which died on application terminate.
 *
 * @see EmergencyPersisterInterface
 * @see ActivityAppRestartCleaner
 */
@SuppressLint("MissingSuperCall")
class MainActivity : AppCompatActivity(),
        SpineNavigatorInterface<RootBone> by SpineNavigator(),
        EmergencyPersisterInterface<MainActivity> by EmergencyPersister(),
        ActivityAppRestartCleaner,
        RootView {

    init {
        managerProvider = ::getSupportFragmentManager
    }

    companion object {
        const val REQUEST_CODE_EMAIL = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    }

    @Inject
    lateinit var presenter: RootPresenter

    private lateinit var activityScope: Scope

    override fun onBackPressed() {
        if (bone.processBack { Toast.makeText(this, """Press "back" button again to exit.""", Toast.LENGTH_LONG).show() }) finish()
    }

    override fun onResume() {
        super.onResume()

        emergencyRemovePin()

        bone.dropExitStatus()

        NavigatorDelayedTransactions.executePendingTransactions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())
        activityScope = Toothpick.openScopes(application, this)
        Toothpick.inject(this, activityScope)

        presenter.onAttachView(this)

        if (!emergencyLoad(savedInstanceState, this)) {

            super<ActivityAppRestartCleaner>.onCreate(savedInstanceState)

            bone = RootBone(
                    TabBar(
                            NavigationStack(TestScreen()),
                            TestForm(),
                            TestScreen()
                    )
            )

            glueWith(bone)
            bone.isActive = true

            refreshUI()
        } else {
            glueWith(bone)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        emergencyPin(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        with(bone) {
            sibling = null // remove strong pointer to existing activity instance
            emergencySave {
                it.bone = this
            }
        }
    }

    //region ================= Request Permissions =================

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != RESULT_OK) {
                    showText("This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.")
                } else {
                    bone.present(PurchasesScreen())
                }
                REQUEST_CODE_EMAIL -> if (data != null && data.extras != null) {
                    presenter.saveAccountName(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))
                }
            }
        }
    }

    //endregion

    //region ================= Setup Ui =================

    private fun showText(caption: String) {
        status_tv.text = caption
        status_tv.visibility = View.VISIBLE
    }

    //endregion

}
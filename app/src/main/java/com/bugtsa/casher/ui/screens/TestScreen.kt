package com.bugtsa.casher.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import com.bugtsa.casher.R
import com.bugtsa.casher.ui.navigation.NavigationStackPresentable
import kotlinx.android.synthetic.main.fragment_phalanx_test.*
import pro.horovodovodo4ka.bones.Finger
import pro.horovodovodo4ka.bones.Phalanx
import pro.horovodovodo4ka.bones.extensions.uuid
import pro.horovodovodo4ka.bones.persistance.BonePersisterInterface
import pro.horovodovodo4ka.bones.ui.FragmentSibling
import pro.horovodovodo4ka.bones.ui.delegates.Page
import java.util.*


class TestScreen : Phalanx(), NavigationStackPresentable {

    override val seed = { ScreenFragment() }

    val color = Random().let {
        Color.argb(255, it.nextInt(256), it.nextInt(256), it.nextInt(256))
    }

    val uuid = String.uuid()

    override val fragmentTitle: String
        get() = "[${(parentBone as? Finger)?.phalanxes?.size ?: 0}] " + uuid.substring(0..1)
}

@SuppressLint("MissingSuperCall")
class ScreenFragment : androidx.fragment.app.Fragment(),
    BonePersisterInterface<TestScreen>,
    FragmentSibling<TestScreen> by Page() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_phalanx_test, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(bone.color)
        test_label.text = bone.uuid

        refreshUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super<BonePersisterInterface>.onSaveInstanceState(outState)
        super<androidx.fragment.app.Fragment>.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BonePersisterInterface>.onCreate(savedInstanceState)
        super<androidx.fragment.app.Fragment>.onCreate(savedInstanceState)
    }
}
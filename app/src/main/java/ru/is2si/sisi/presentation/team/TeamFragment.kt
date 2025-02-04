package ru.is2si.sisi.presentation.team

import android.Manifest.permission.CALL_PHONE
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_team.*
import ru.is2si.sisi.R
import ru.is2si.sisi.base.ActionBarFragment
import ru.is2si.sisi.base.extension.*
import ru.is2si.sisi.base.switcher.ViewStateSwitcher
import ru.is2si.sisi.presentation.design.dialog.AlertBottomSheetFragment
import ru.is2si.sisi.presentation.design.dialog.AlertBottomSheetFragment.Companion.withCancelText
import ru.is2si.sisi.presentation.design.dialog.AlertBottomSheetFragment.Companion.withCancelable
import ru.is2si.sisi.presentation.design.dialog.AlertBottomSheetFragment.Companion.withMessage
import ru.is2si.sisi.presentation.design.dialog.AlertBottomSheetFragment.Companion.withOkText
import ru.is2si.sisi.presentation.design.dialog.AlertBottomSheetFragment.Companion.withTarget
import ru.is2si.sisi.presentation.design.dialog.AlertBottomSheetFragment.ControlResult.OK
import ru.is2si.sisi.presentation.model.TeamView
import javax.inject.Inject

class TeamFragment :
        ActionBarFragment<TeamContract.Presenter>(),
        TeamContract.View {

    @Inject
    lateinit var stateSwitcher: ViewStateSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_team, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        presenter.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PHONE_PERMISSION) {
            if (AlertBottomSheetFragment.getResult(data) == OK)
                startActivity(requireContext().appSettingsIntent())
        }
    }

    private fun setupViews() {
        vPhone.onClick { checkPhonePermissions() }
    }

    override fun setTeam(team: TeamView) {
        // TODO: RB 2019-06-23 change for actual data
        tvTeamName.text = team.teamName
        tvStartName.text = "1500 км за 10 часов"
        tvFinishTimeLimit.text = "16:46:01"
        tvMaxNormalFinishTime.text = "10:00:00"
        tvPenaltyPoints.text = "0.35"
        tvTechnicalDetails.text = "Тут будет техническая информация"
    }

    override fun phoneCall() {
        val phone = getString(R.string.team_phone_number)
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)
    }

    private fun checkPhonePermissions() {
        when (beforeRequestPermissions(REQUEST_PHONE, CALL_PHONE)) {
            BeforeRequestPermissionResult.AlreadyGranted -> presenter.onPhoneClick()

            BeforeRequestPermissionResult.ShowRationale -> beforeRequestPermissions(
                    REQUEST_PHONE,
                    true,
                    CALL_PHONE
            )
            BeforeRequestPermissionResult.Requested -> Unit
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (afterRequestPermissions(permissions, grantResults)) {
            AfterRequestPermissionsResult.Granted -> {
                presenter.onPhoneClick()
            }
            AfterRequestPermissionsResult.NeverAskAgain -> {
                AlertBottomSheetFragment()
                        .withMessage(getString(R.string.team_phone_requested))
                        .withOkText(getString(R.string.dialog_settings))
                        .withCancelText(getString(R.string.dialog_cancel))
                        .withCancelable(false)
                        .withTarget(this, REQUEST_PHONE_PERMISSION)
                        .show(requireFragmentManager(), TAG_PHONE_PERMISSION)
            }
        }
    }

    override fun showLoading() = stateSwitcher.switchToLoading()

    override fun showError(message: String?) = stateSwitcher.switchToError(message) {
        stateSwitcher.switchToMain()
    }

    override fun findToolbar(): Toolbar? = view?.findViewById(R.id.tActionBar)

    override fun setupActionBar() = setActionBar(findToolbar()) {
        setTitle(R.string.team_title)
        setDisplayHomeAsUpEnabled(false)
    }

    companion object {
        private const val REQUEST_PHONE = 501
        private const val REQUEST_PHONE_PERMISSION = 3000
        private const val TAG_PHONE_PERMISSION = "phone_permission"

        fun newInstance(): TeamFragment = TeamFragment()
    }
}
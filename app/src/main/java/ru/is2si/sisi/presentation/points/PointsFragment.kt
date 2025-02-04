package ru.is2si.sisi.presentation.points

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_points.*
import ru.is2si.sisi.R
import ru.is2si.sisi.base.ActionBarFragment
import ru.is2si.sisi.base.DelegationAdapter
import ru.is2si.sisi.base.extension.onClick
import ru.is2si.sisi.base.extension.setActionBar
import ru.is2si.sisi.base.navigation.Navigator
import ru.is2si.sisi.base.navigation.NavigatorProvider
import ru.is2si.sisi.base.switcher.ViewStateSwitcher
import ru.is2si.sisi.presentation.main.NavigationActivity
import ru.is2si.sisi.presentation.model.PointView
import javax.inject.Inject

class PointsFragment :
        ActionBarFragment<PointsContract.Presenter>(),
        NavigatorProvider,
        PointsContract.View {

    @Inject
    lateinit var stateSwitcher: ViewStateSwitcher
    private lateinit var adapter: DelegationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_points, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        presenter.start()
    }

    private fun setupViews() {
        setupRecyclerView()
        btnAdd.onClick {
            val name = tietPoint.text.toString()
            if (name.isNotEmpty()) {
                presenter.addPoint(name.toInt())
                tietPoint.text?.clear()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = DelegationAdapter()
        rvPoints.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val policyDelegate = PointDelegate(requireContext()) {
            presenter.removePoint(it)
        }
        adapter.delegatesManager
                .addDelegate(policyDelegate)
        rvPoints.adapter = adapter
    }

    override fun showPoints(points: List<PointView>) {
        adapter.items = points
        showSummaryBalls(points)
    }

    override fun showPointsByRemove(position: Int) {
        adapter.notifyItemRemoved(position)
        showSummaryBalls(adapter.items.map { it as PointView })
    }

    override fun showSummaryBalls(points: List<PointView>) {
        val sum = points.sumBy { it.value }
        tvSummary.text = sum.toString()
    }

    override fun showLoading() = stateSwitcher.switchToLoading()

    override fun showError(message: String?) = stateSwitcher.switchToError(message) {
        stateSwitcher.switchToMain()
    }

    override fun findToolbar(): Toolbar? = view?.findViewById(R.id.tActionBar)

    override fun setupActionBar() = setActionBar(findToolbar()) {
        setTitle(R.string.points_title)
        setDisplayHomeAsUpEnabled(false)
    }

    override fun getNavigator(): Navigator = (requireActivity() as NavigationActivity).getMainNavigator()

    companion object {
        fun newInstance(): PointsFragment = PointsFragment()
    }
}
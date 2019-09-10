package ru.is2si.sisi.presentation.finish

import ru.is2si.sisi.base.BasePresenter
import ru.is2si.sisi.base.extension.getDateTimeOfPattern
import ru.is2si.sisi.base.rx.RxSchedulers
import ru.is2si.sisi.domain.UseCase.None
import ru.is2si.sisi.domain.auth.GetSaveTeam
import ru.is2si.sisi.domain.auth.GetTeamPin
import ru.is2si.sisi.domain.finish.Finish
import ru.is2si.sisi.domain.finish.FinishTeam
import ru.is2si.sisi.domain.points.GetSelectPoints
import ru.is2si.sisi.domain.result.CompetitionResult
import ru.is2si.sisi.presentation.model.asView
import javax.inject.Inject

class FinishPresenter @Inject constructor(
        private val getTeamPin: GetTeamPin,
        private val getSaveTeam: GetSaveTeam,
        private val finishTeam: FinishTeam,
        private val getSelectPoints: GetSelectPoints,
        private val rxSchedulers: RxSchedulers
) : BasePresenter<FinishContract.View>(), FinishContract.Presenter {

    override fun start() {
        view.showLoading()
        getFinishData()
    }

    private fun getFinishData() {
        disposables += getSelectPoints.execute(None())
                .map { it.sumBy { point -> point.pointBall } }
                .flatMap { preliminaryPoints ->
                    getSaveTeam.execute(None())
                            .map { competition ->
                                competition as CompetitionResult
                                preliminaryPoints to competition.asView()
                            }
                }
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .subscribe({
                    view.showMain()
                    val (preliminaryPoints, competition) = it
                    val maxNormalTime = competition
                            .competition
                            ?.dataEndMax
                            ?.getDateTimeOfPattern()
                            ?: ""
                    val amountPenaltyPoints = competition.penaltyBally
                    view.showFinishData(maxNormalTime, amountPenaltyPoints, preliminaryPoints.toString())
                }) { view.showError(it.message, it) }

    }

    override fun finishTeam() {
        view.showLoading()
        disposables += getTeamPin.execute(None())
                .flatMap { pin ->
                    getSaveTeam.execute(None())
                            .map { competition ->
                                competition as CompetitionResult
                                pin to competition.id
                            }
                }
                .flatMap {
                    val (pin, teamId) = it
                    finishTeam.execute(FinishTeam.Param(teamId, pin))
                }
                .map(Finish::asView)
                .subscribeOn(rxSchedulers.io)
                .observeOn(rxSchedulers.ui)
                .doOnSuccess { getFinishData() }
                .subscribe({
                    view.showMain()
                    view.showFixFinishTime(it.dataTimeFinish.getDateTimeOfPattern())
                }) { view.showError(it.message, it) }
    }

}
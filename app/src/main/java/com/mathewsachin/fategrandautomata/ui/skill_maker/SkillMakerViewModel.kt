package com.mathewsachin.fategrandautomata.ui.skill_maker

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences

class SkillMakerViewModel @ViewModelInject constructor(
    val prefs: IPreferences,
    @Assisted val savedState: SavedStateHandle
) : ViewModel() {
    companion object {
        const val NoEnemy = -1
    }

    val battleConfigKey: String = savedState[SkillMakerActivityArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get Battle Config key")

    val battleConfig = prefs.forBattleConfig(battleConfigKey)

    val state = savedState.get(::savedState.name)
        ?: SkillMakerSavedState()

    private val model: SkillMakerModel
    private val _stage: MutableLiveData<Int>

    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> = _currentIndex

    init {
        model = if (state.skillString != null) {
            SkillMakerModel(state.skillString)
        } else {
            val skillString = battleConfig.skillCommand
            val m = try {
                SkillMakerModel(skillString)
            } catch (e: Exception) {
                SkillMakerModel("")
            }

            if (skillString.isNotEmpty()) {
                when (val l = m.skillCommand.last()) {
                    is SkillMakerEntry.Action -> when (l.action) {
                        is AutoSkillAction.Atk -> {
                            m.skillCommand.removeLast()
                            m.skillCommand.add(SkillMakerEntry.Next.Wave(l.action))
                        }
                        else -> {
                            // do nothing
                        }
                    }
                }
            }

            m
        }

        _stage = if (state.skillString != null) {
            MutableLiveData(state.stage)
        } else {
            MutableLiveData(
                model.skillCommand.count { it is SkillMakerEntry.Next.Wave } + 1
            )
        }

        _currentIndex.value = if (state.skillString != null) {
            state.currentIndex
        } else model.skillCommand.lastIndex
    }

    private var currentSkill = state.currentSkill

    fun saveState() {
        val saveState = SkillMakerSavedState(
            skillString = model.toString(),
            enemyTarget = enemyTarget.value ?: NoEnemy,
            stage = stage.value ?: 1,
            currentSkill = currentSkill,
            currentIndex = currentIndex.value ?: 0
        )

        savedState.set(::savedState.name, saveState)
    }

    override fun onCleared() {
        super.onCleared()

        saveState()
    }

    private val _skillCommand = MutableLiveData(model.skillCommand)

    val skillCommand: LiveData<List<SkillMakerEntry>> = Transformations.map(_skillCommand) { it }

    private fun notifySkillCommandUpdate() {
        _skillCommand.value = model.skillCommand
    }

    private fun getSkillCmdString() = model.toString()

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index

        notifySkillCommandUpdate()
        revertToPreviousEnemyTarget()
    }

    private fun add(entry: SkillMakerEntry) {
        model.skillCommand.add((currentIndex.value ?: 0) + 1, entry)
        _currentIndex.next()

        notifySkillCommandUpdate()
    }

    private fun undo() {
        model.skillCommand.removeAt(currentIndex.value ?: 0)
        _currentIndex.prev()

        notifySkillCommandUpdate()
    }

    private fun isEmpty() = currentIndex.value == 0

    private var last: SkillMakerEntry
        get() = model.skillCommand[currentIndex.value ?: 0]
        set(value) {
            model.skillCommand[currentIndex.value ?: 0] = value

            notifySkillCommandUpdate()
        }

    private fun reverseIterate(): List<SkillMakerEntry> =
        model.skillCommand
            .take((currentIndex.value ?: 0) + 1)
            .reversed()

    private val _enemyTarget = MutableLiveData(state.enemyTarget)

    val enemyTarget: LiveData<Int> = _enemyTarget

    fun setEnemyTarget(target: Int) {
        _enemyTarget.value = target

        if (target == NoEnemy) {
            return
        }

        val targetCmd = SkillMakerEntry.Action(
            AutoSkillAction.TargetEnemy(
                EnemyTarget.list[target - 1]
            )
        )

        val l = last

        // Merge consecutive target changes
        if (!isEmpty() && l is SkillMakerEntry.Action && l.action is AutoSkillAction.TargetEnemy) {
            last = targetCmd
        } else {
            add(targetCmd)
        }
    }

    fun unSelectTargets() = setEnemyTarget(NoEnemy)

    fun MutableLiveData<Int>.next() {
        value = (value ?: 1) + 1
    }

    fun MutableLiveData<Int>.prev() {
        value = (value ?: 1) - 1
    }

    val stage: LiveData<Int> = _stage
    private fun prevStage() = _stage.prev()

    fun initSkill(SkillCode: Char) {
        currentSkill = SkillCode
    }

    fun targetSkill(target: ServantTarget?) {
        val skill = (Skill.Servant.list + Skill.Master.list)
            .first { it.autoSkillCode == currentSkill }

        add(
            SkillMakerEntry.Action(
                when (skill) {
                    is Skill.Servant -> AutoSkillAction.ServantSkill(skill, target)
                    is Skill.Master -> AutoSkillAction.MasterSkill(skill, target)
                }
            )
        )
    }

    fun finish(): String {
        _currentIndex.value = model.skillCommand.lastIndex

        while (last.let { l -> l is SkillMakerEntry.Next && l.action == AutoSkillAction.Atk.noOp() }) {
            undo()
        }

        return getSkillCmdString()
    }

    fun nextTurn(atk: AutoSkillAction.Atk) = add(SkillMakerEntry.Next.Turn(atk))

    fun nextStage(atk: AutoSkillAction.Atk) {
        _stage.next()

        // Uncheck selected targets
        unSelectTargets()

        add(SkillMakerEntry.Next.Wave(atk))
    }

    fun commitOrderChange(
        starting: OrderChangeMember.Starting,
        sub: OrderChangeMember.Sub
    ) {
        add(
            SkillMakerEntry.Action(
                AutoSkillAction.OrderChange(
                    starting,
                    sub
                )
            )
        )
    }

    private fun revertToPreviousEnemyTarget() {
        // Find the previous target, but within the same wave
        val previousTarget = reverseIterate()
            .asSequence()
            .takeWhile { it !is SkillMakerEntry.Next.Wave }
            .filterIsInstance<SkillMakerEntry.Action>()
            .map { it.action }
            .filterIsInstance<AutoSkillAction.TargetEnemy>()
            .firstOrNull()

        if (previousTarget == null) {
            unSelectTargets()
            return
        }

        val target = when (previousTarget.enemy.autoSkillCode) {
            '1' -> 1
            '2' -> 2
            '3' -> 3
            else -> return
        }

        _enemyTarget.value = target
    }

    private fun undoStageOrTurn() {
        // Decrement Battle/Turn count
        if (last is SkillMakerEntry.Next.Wave) {
            prevStage()
        }

        // Undo the Battle/Turn change
        undo()

        revertToPreviousEnemyTarget()
    }

    fun clearAll() {
        _currentIndex.value = model.skillCommand.lastIndex

        while (!isEmpty()) {
            onUndo()
        }
    }

    fun onUndo() {
        if (!isEmpty()) {
            // Un-select target
            when (val last = last) {
                // Battle/Turn change
                is SkillMakerEntry.Next -> {
                    undoStageOrTurn()
                }
                is SkillMakerEntry.Action -> {
                    if (last.action is AutoSkillAction.TargetEnemy) {
                        undo()
                        revertToPreviousEnemyTarget()
                    } else undo()
                }
                // Do nothing
                is SkillMakerEntry.Start -> {
                }
            }
        }
    }

    init {
        revertToPreviousEnemyTarget()
    }
}

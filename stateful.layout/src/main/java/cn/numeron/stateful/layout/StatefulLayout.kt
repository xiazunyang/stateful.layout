package cn.numeron.stateful.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import cn.numeron.common.State

class StatefulLayout @JvmOverloads constructor(c: Context, a: AttributeSet? = null, i: Int = 0) : FrameLayout(c, a, i) {

    private var previousState: State? = null

    var state: State = State.Empty
        set(value) {
            if (value != field) {
                previousState = field
                field = value
            }
            changeView()
        }

    private val emptyViews
        get() = findChildrenByState(State.Empty)

    private val loadingViews
        get() = findChildrenByState(State.Loading)

    private val failureViews
        get() = findChildrenByState(State.Failure)

    private val contentViews
        get() = findChildrenByState(State.Success)

    private val defaultState: Int
    private val emptyLayoutId: Int
    private val loadingLayoutId: Int
    private val failureLayoutId: Int

    private var animationEnabled: Boolean

    init {
        val tArray = c.obtainStyledAttributes(a, R.styleable.StatefulLayout)

        emptyLayoutId = tArray.getResourceId(
            R.styleable.StatefulLayout_stateful_empty_layout,
            R.layout.state_empty_layout
        )

        failureLayoutId = tArray.getResourceId(
            R.styleable.StatefulLayout_stateful_failure_layout,
            R.layout.state_failure_layout
        )

        loadingLayoutId = tArray.getResourceId(
            R.styleable.StatefulLayout_stateful_loading_layout,
            R.layout.state_loading_layout
        )

        //分配默认状态
        defaultState = tArray.getInt(R.styleable.StatefulLayout_state, 0)
        //是否启动状态切换动画
        animationEnabled = tArray.getBoolean(R.styleable.StatefulLayout_animation_enabled, true)

        tArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (emptyViews.isEmpty()) {
            View.inflate(context, emptyLayoutId, this)
        }
        if (loadingViews.isEmpty()) {
            View.inflate(context, loadingLayoutId, this)
        }
        if (failureViews.isEmpty()) {
            View.inflate(context, failureLayoutId, this)
        }
        state = State.values().first {
            it.ordinal == defaultState
        }
    }

    private fun findChildrenByState(state: State): List<View> {
        return (0..childCount)
            .mapNotNull(::getChildAt)
            .filter {
                val layoutParams = it.layoutParams
                layoutParams is LayoutParams && layoutParams.state == state
            }
    }

    private fun changeView() {
        if (previousState != State.Loading && state == State.Loading) {
            loadingViews.forEach(::show)
        } else {
            loadingViews.forEach(::hide)
        }
        if (previousState != State.Success && state == State.Success) {
            contentViews.forEach(::show)
        } else {
            contentViews.forEach(::hide)
        }
        if (previousState != State.Failure && state == State.Failure) {
            failureViews.forEach(::show)
        } else {
            failureViews.forEach(::hide)
        }
        if (previousState != State.Empty && state == State.Empty) {
            emptyViews.forEach(::show)
        } else {
            emptyViews.forEach(::hide)
        }
    }

    private fun show(view: View) {
        if (view.visibility != VISIBLE) {
            view.visibility = VISIBLE
            val layoutParams = view.layoutParams
            if (animationEnabled && layoutParams is LayoutParams) {
                view.clearAnimation()
                view.startAnimation(
                    AnimationUtils.loadAnimation(
                        view.context,
                        layoutParams.showAnimationResId
                    )
                )
            }
        }
    }

    private fun hide(view: View) {
        if (view.visibility != GONE) {
            view.visibility = GONE
            val layoutParams = view.layoutParams
            if (animationEnabled && layoutParams is LayoutParams) {
                view.clearAnimation()
                view.startAnimation(
                    AnimationUtils.loadAnimation(
                        view.context,
                        layoutParams.hideAnimationResId
                    )
                )
            }
        }
    }

    override fun generateDefaultLayoutParams(): FrameLayout.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?): FrameLayout.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return when (lp) {
            is LayoutParams -> LayoutParams(lp)
            is MarginLayoutParams -> LayoutParams(lp)
            is ViewGroup.LayoutParams -> LayoutParams(lp)
            else -> generateDefaultLayoutParams()
        }
    }

    inner class LayoutParams : FrameLayout.LayoutParams {

        val state: State
        var showAnimationResId = R.anim.anim_state_layout_show
        var hideAnimationResId = R.anim.anim_state_layout_hide

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.StatefulLayout_Layout)
            val stateValue = typedArray.getInt(R.styleable.StatefulLayout_Layout_layout_state, 3)
            this.state = State.values()[stateValue]
            showAnimationResId = typedArray.getResourceId(R.styleable.StatefulLayout_Layout_stateful_animation_show, showAnimationResId)
            hideAnimationResId = typedArray.getResourceId(R.styleable.StatefulLayout_Layout_stateful_animation_hide, hideAnimationResId)
            typedArray.recycle()
        }

        constructor(width: Int, height: Int, s: State) : super(width, height) {
            this.state = s
        }

        constructor(width: Int, height: Int) : this(width, height, State.Success)

        constructor(source: ViewGroup.LayoutParams) : super(source) {
            this.state = State.Success
        }

        constructor(source: MarginLayoutParams) : super(source) {
            this.state = State.Success
        }

        constructor(source: LayoutParams) : super(source) {
            this.state = source.state
            this.showAnimationResId = source.showAnimationResId
            this.hideAnimationResId = source.hideAnimationResId
        }

    }

}
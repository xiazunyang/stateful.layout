package cn.numeron.stateful.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import cn.numeron.common.State

class StatefulLayout @JvmOverloads constructor(
    c: Context,
    a: AttributeSet? = null,
    i: Int = R.attr.statefulLayoutStyle,
    s: Int = R.style.StatefulLayoutStyle
) : FrameLayout(c, a, i, s) {

    var state: State = State.Empty
        set(value) {
            if (value != field) {
                field = value
            }
            changeView()
        }

    private val emptyLayoutId: Int
    private val loadingLayoutId: Int
    private val failureLayoutId: Int

    private var animationEnabled = true

    init {
        val tArray = c.obtainStyledAttributes(a, R.styleable.StatefulLayout, i, s)

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

        //获取默认状态
        val defaultState = tArray.getInt(R.styleable.StatefulLayout_state, 0)
        //设置默认的状态
        state = State.values().first {
            it.ordinal == defaultState
        }
        //是否启动状态切换动画
        animationEnabled =
            tArray.getBoolean(R.styleable.StatefulLayout_animation_enabled, animationEnabled)

        tArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        //根据拥有的状态，决定加载剩余的状态布局
        val (hasEmpty, hasLoading, hasFailure) = checkState()
        val inflater = LayoutInflater.from(context)
        if (!hasEmpty) {
            val emptyLayout = inflater.inflate(emptyLayoutId, this, false)
            addView(emptyLayout, resetLayoutParamsState(emptyLayout, State.Empty))
        }
        if (!hasLoading) {
            val loadingLayout = inflater.inflate(loadingLayoutId, this, false)
            addView(loadingLayout, resetLayoutParamsState(loadingLayout, State.Loading))
        }
        if (!hasFailure) {
            val failureLayout = inflater.inflate(failureLayoutId, this, false)
            addView(failureLayout, resetLayoutParamsState(failureLayout, State.Failure))
        }
    }

    private fun checkState(): Triple<Boolean, Boolean, Boolean> {
        var hasEmpty = false
        var hasLoading = false
        var hasFailure = false
        repeat(childCount) {
            val childView = getChildAt(it)
            val layoutParams = childView.layoutParams as LayoutParams
            val state = layoutParams.state
            if (!hasEmpty) {
                hasEmpty = state == State.Empty
            }
            if (!hasLoading) {
                hasLoading = state == State.Loading
            }
            if (!hasFailure) {
                hasFailure = state == State.Failure
            }
        }
        return Triple(hasEmpty, hasLoading, hasFailure)
    }

    private fun resetLayoutParamsState(view: View, state: State): LayoutParams {
        val oldLayoutParams = view.layoutParams
        if (oldLayoutParams is LayoutParams) {
            if (oldLayoutParams.state != state) {
                oldLayoutParams.state = state
            }
            return oldLayoutParams
        }
        val layoutParams = generateLayoutParams(oldLayoutParams)
        layoutParams.state = state
        return layoutParams
    }

    private fun changeView() {
        repeat(childCount) { index ->
            val childView = getChildAt(index)
            val childLayoutParams = childView.layoutParams as LayoutParams
            if (childLayoutParams.state == this.state) {
                show(childView, childLayoutParams)
            } else {
                hide(childView, childLayoutParams)
            }
        }
    }

    private fun show(view: View, layoutParams: LayoutParams) {
        if (view.visibility != VISIBLE) {
            view.visibility = VISIBLE
            if (animationEnabled) {
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

    private fun hide(view: View, layoutParams: LayoutParams) {
        if (view.visibility == VISIBLE) {
            view.visibility = if (layoutParams.invisible) INVISIBLE else GONE
            if (animationEnabled) {
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

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): LayoutParams {
        return when (lp) {
            is MarginLayoutParams -> LayoutParams(lp)
            is ViewGroup.LayoutParams -> LayoutParams(lp)
            else -> generateDefaultLayoutParams()
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        if (params is LayoutParams) {
            //根据当前的状态与child的布局属性，决定child是否显示
            child.visibility = when {
                state == params.state -> VISIBLE
                params.invisible -> INVISIBLE
                else -> GONE
            }
        }
    }

    class LayoutParams : FrameLayout.LayoutParams {

        var invisible = false
        var state = State.Success
        var showAnimationResId = R.anim.anim_state_layout_show
        var hideAnimationResId = R.anim.anim_state_layout_hide

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.StatefulLayout_Layout)
            invisible = typedArray.getBoolean(
                R.styleable.StatefulLayout_Layout_stateful_invisible,
                invisible
            )
            val stateValue = typedArray.getInt(R.styleable.StatefulLayout_Layout_layout_state, 3)
            state = State.values()[stateValue]
            showAnimationResId = typedArray.getResourceId(
                R.styleable.StatefulLayout_Layout_stateful_animation_show,
                showAnimationResId
            )
            hideAnimationResId = typedArray.getResourceId(
                R.styleable.StatefulLayout_Layout_stateful_animation_hide,
                hideAnimationResId
            )
            typedArray.recycle()
        }

        constructor(width: Int, height: Int, state: State) : super(width, height) {
            this.state = state
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.LayoutParams) : super(source)

        constructor(source: MarginLayoutParams) : super(source)

    }

}
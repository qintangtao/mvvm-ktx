package me.tang.mvvm.state.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.InflateException
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import me.tang.mvvm.state.R


//https://github.com/LZKDreamer/StateLayout
class StateLayout : FrameLayout {

    // loading page
    private lateinit var mLoadingView: View
    private var mLoadingTextView: TextView? = null
    private var mLoadingText: String? = null

    // error page
    private lateinit var mErrorView: View
    private var mErrorImageView: ImageView? = null
    private var mErrorTextView: TextView? = null
    private var mErrorSrc: Drawable? = null
    private var mErrorText: String? = null

    // network error page
    private lateinit var mNetErrorView: View
    private var mNetErrorImageView: ImageView? = null
    private var mNetErrorTextView: TextView? = null
    private var mNetErrorSrc: Drawable? = null
    private var mNetErrorText: String? = null

    // empty page
    private lateinit var mEmptyView: View
    private var mEmptyImageView: ImageView? = null
    private var mEmptyTextView: TextView? = null
    private var mEmptySrc: Drawable? = null
    private var mEmptyText: String? = null

    private var mTextColor: Int = 0
    private var mTextSize: Int = 0

    // content page
    private lateinit  var mContentView: View

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.StateLayout)
        mLoadingView = View.inflate(context, typedArray.getResourceId(R.styleable.StateLayout_state_loading_layout, R.layout.default_loading_view), null)
        addView(mLoadingView)
        mErrorView = View.inflate(context, typedArray.getResourceId(R.styleable.StateLayout_state_error_layout, R.layout.default_error_view), null)
        addView(mErrorView)
        mNetErrorView = View.inflate(context, typedArray.getResourceId(R.styleable.StateLayout_state_net_error_layout, R.layout.default_net_error_view), null)
        addView(mNetErrorView)
        mEmptyView = View.inflate(context, typedArray.getResourceId(R.styleable.StateLayout_state_empty_layout, R.layout.default_empty_view), null)
        addView(mEmptyView)

        mErrorImageView = mErrorView.findViewById(R.id.image_error) as? ImageView
        mNetErrorImageView = mNetErrorView.findViewById(R.id.image_net_error) as? ImageView
        mEmptyImageView = mEmptyView.findViewById(R.id.image_empty) as? ImageView

        mLoadingTextView = mLoadingView.findViewById(R.id.text_loading) as? TextView
        mErrorTextView = mErrorView.findViewById(R.id.text_error) as? TextView
        mNetErrorTextView = mNetErrorView.findViewById(R.id.text_net_error) as? TextView
        mEmptyTextView = mEmptyView.findViewById(R.id.text_empty) as? TextView

        mErrorSrc = typedArray.getDrawable(R.styleable.StateLayout_state_error_src)
        mNetErrorSrc = typedArray.getDrawable(R.styleable.StateLayout_state_net_error_src)
        mEmptySrc = typedArray.getDrawable(R.styleable.StateLayout_state_empty_src)

        mLoadingText = typedArray.getString(R.styleable.StateLayout_state_loading_text)
        mErrorText = typedArray.getString(R.styleable.StateLayout_state_error_text)
        mNetErrorText = typedArray.getString(R.styleable.StateLayout_state_net_error_text)
        mEmptyText = typedArray.getString(R.styleable.StateLayout_state_empty_text)

        mTextColor = typedArray.getColor(R.styleable.StateLayout_state_textColor, ContextCompat.getColor(context, R.color.state_text_color))
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.StateLayout_state_textSize, resources.getDimensionPixelSize(
            R.dimen.state_text_size))
        typedArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 5) {
            throw InflateException("Must only child view")
        }
        for (i in 0..(childCount-1)) {
            var child = getChildAt(i)
            if (child != mLoadingView && child != mErrorView && child != mNetErrorView && child != mEmptyView) {
                mContentView = child
                break
            }
        }

        mErrorImageView?.run {
            if (mErrorSrc != null) setImageDrawable(mErrorSrc)
        }

        mNetErrorImageView?.run {
            if (mNetErrorSrc != null) setImageDrawable(mNetErrorSrc)
        }

        mEmptyImageView?.run {
            if (mEmptySrc != null) setImageDrawable(mEmptySrc)
        }

        mLoadingTextView?.run {
            if (!mLoadingText.isNullOrEmpty()) {
                text = mLoadingText
            }
            setTextColor(mTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
        }
        mErrorTextView?.run {
            if (!mErrorText.isNullOrEmpty()) {
                text = mErrorText
            }
            setTextColor(mTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
        }
        mNetErrorTextView?.run {
            if (!mNetErrorText.isNullOrEmpty()) {
                text = mNetErrorText
            }
            setTextColor(mTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
        }
        mEmptyTextView?.run {
            if (!mEmptyText.isNullOrEmpty()) {
                text = mEmptyText
            }
            setTextColor(mTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
        }

        showLoadingView()
    }

    fun showLoadingView() {
        showLoadingView(null)
    }

    fun showLoadingView(text: String?) {
        showView(mLoadingView)
        mLoadingTextView?.text = if (text.isNullOrEmpty())
            if (mLoadingText.isNullOrEmpty()) resources.getString(R.string.state_loading)  else mLoadingText else text
    }

    fun showErrorView() {
        showErrorView(null)
    }

    fun showErrorView(text: String?) {
        showView(mErrorView)
        mErrorTextView?.text = if (text.isNullOrEmpty())
            if (mErrorText.isNullOrEmpty()) resources.getString(R.string.state_error)  else mErrorText else text
    }

    fun showNetErrorView() {
        showNetErrorView(null)
    }

    fun showNetErrorView(text: String?) {
        showView(mNetErrorView)
        mNetErrorTextView?.text = if (text.isNullOrEmpty())
            if (mNetErrorText.isNullOrEmpty()) resources.getString(R.string.state_net_error)  else mNetErrorText else text
    }

    fun showEmptyView() {
        showEmptyView(null)
    }

    fun showEmptyView(text: String?) {
        showView(mEmptyView)
        mEmptyTextView?.text = if (text.isNullOrEmpty())
            if (mEmptyText.isNullOrEmpty()) resources.getString(R.string.state_empty)  else mEmptyText else text
    }

    fun showContentView() {
        showView(mContentView)
    }

    fun setEmptyClickListener(onClick: (View?) -> Unit) {
        mEmptyView.setOnClickListener {
            onClick(it)
        }
    }

    fun setErrorClickListener(onClick: (View?) -> Unit) {
        mErrorView.setOnClickListener {
            onClick(it)
        }
    }

    fun setNetErrorClickListener(onClick: (View?) -> Unit) {
        mNetErrorView.setOnClickListener {
            onClick(it)
        }
    }

    fun setOnClickListener(id: Int, onClick: (View?) -> Unit) {
        var view: View? = findViewById(id)
        view?.setOnClickListener {
            onClick(it)
        }
    }

    fun setEmptyClickListener(listener: OnClickListener?) {
        listener?.let {
            mEmptyView.setOnClickListener(listener)
        }
    }

    fun setErrorClickListener(listener: OnClickListener?) {
        listener?.let {
            mErrorView.setOnClickListener(listener)
        }
    }

    fun setOnClickListener(id: Int, listener: OnClickListener?) {
        listener?.let {
            var view: View? = findViewById(id)
            view?.setOnClickListener(it)
        }
    }

    private fun showView(view: View) {
        for (i in 0..(childCount-1)) {
            var child = getChildAt(i)
            child!!.visibility = if (child == view) View.VISIBLE else View.GONE
        }
    }
}
package me.tang.mvvm.base

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.blankj.utilcode.util.ToastUtils
import me.tang.mvvm.R
import me.tang.mvvm.event.Message
import me.tang.mvvm.ext.getContentLayout
import me.tang.mvvm.network.RESULT
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VM : BaseViewModel, DB : ViewBinding> : AppCompatActivity() {

    protected lateinit var viewModel: VM

    protected lateinit var mBinding: DB

    private var dialog: MaterialDialog? = null

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewDataBinding()
        createViewModel()
        registorDefUIChange()
        initView(savedInstanceState)
        initData()
    }

    private fun initViewDataBinding() {
        val cls =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<*>
        if (ViewDataBinding::class.java != cls && ViewDataBinding::class.java.isAssignableFrom(cls)) {
            val inflateMethod = cls.getMethod("inflate", LayoutInflater::class.java)
            mBinding = inflateMethod.invoke(null, layoutInflater) as DB
            (mBinding as ViewDataBinding).lifecycleOwner = this
            setContentView(mBinding.root)
        } else if (ViewBinding::class.java != cls && ViewBinding::class.java.isAssignableFrom(cls)) {
            val inflateMethod = cls.getMethod("inflate", LayoutInflater::class.java)
            mBinding = inflateMethod.invoke(null, layoutInflater) as DB
            setContentView(mBinding.root)
        } else {
            throw Exception("Need to enabled ViewBinding or ViewDataBinding")
        }
    }

    private fun registorDefUIChange() {
        viewModel.defUI.start.observe(this, Observer {
            onLoadStart()
        })
        viewModel.defUI.error.observe(this, Observer {
            onLoadEvent(it)
        })
        viewModel.defUI.result.observe(this, Observer {
            onLoadResult(it)
        })
        viewModel.defUI.complete.observe(this, Observer {
            onLoadCompleted()
        })
    }

    open fun onLoadStart() {
        showProgressDialog()
    }

    open fun onLoadEvent(msg: Message) {
        ToastUtils.showLong("${msg.code}:${msg.msg}")
    }

    open fun onLoadResult(code: Int) {
        when (code) {
            RESULT.END.code ->
                ToastUtils.showLong(RESULT.END.msg)
            RESULT.EMPTY.code ->
                ToastUtils.showLong(RESULT.EMPTY.msg)
            else -> {}
        }
    }

    open fun onLoadCompleted() {
        dismissProgressDialog()
    }

    fun showProgressDialog(resId: Int = R.string.now_loading) {
        if (dialog == null) {
            dialog = MaterialDialog(this)
                .cancelable(false)
                .cornerRadius(8f)
                .customView(R.layout.custom_progress_dialog_view, noVerticalPadding = true)
                .lifecycleOwner(this)
                .maxWidth(R.dimen.dialog_width)
            dialog?.getContentLayout().let {
                val tvTip = it?.findViewById(R.id.tvTip) as TextView
                tvTip.setText(resId)
            }
        }
        dialog?.show()
    }

    fun dismissProgressDialog() {
        dialog?.run { if (isShowing) dismiss() }
    }

    private fun createViewModel() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val tp = type.actualTypeArguments[0]
            val tClass = tp as? Class<VM> ?: BaseViewModel::class.java
            //viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(tClass) as VM
            viewModel =
                ViewModelProvider(viewModelStore, defaultViewModelProviderFactory).get(tClass) as VM
            lifecycle.addObserver(viewModel)
        }
    }
}
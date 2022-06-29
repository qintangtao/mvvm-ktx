package me.tang.mvvm.network

enum class RESULT(val code: Int, val msg: String) {
    SUCCESS(0, "有数据"),
    END(1, "没有更多数据了"),
    EMPTY(2, "暂无数据");
}
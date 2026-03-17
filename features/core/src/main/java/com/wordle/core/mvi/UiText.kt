import android.content.Context

sealed interface UiText {
    data class StringRes(val id: Int) : UiText
    data class Plain(val value: String) : UiText

    fun resolve(context: Context): String = when (this) {
        is StringRes -> context.getString(id)
        is Plain     -> value
    }
}
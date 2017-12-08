package com.hendraanggrian.widget

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils.copySpansFrom
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import com.hendraanggrian.socialview.SocialView
import com.hendraanggrian.socialview.SocialViewImpl

class SocialAutoCompleteTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.support.v7.appcompat.R.attr.autoCompleteTextViewStyle
) : AppCompatMultiAutoCompleteTextView(context, attrs, defStyleAttr), SocialView {

    private val mImpl: SocialView = SocialViewImpl(this, attrs)
    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(editable: Editable?) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s.isNotEmpty() && start < s.length) when (s[start]) {
                '#' -> if (adapter !== hashtagAdapter) setAdapter(hashtagAdapter)
                '@' -> if (adapter !== mentionAdapter) setAdapter(mentionAdapter)
            }
        }
    }
    private val mEnabledSymbols: CharArray
        get() = ArrayList<Char>().apply {
            if (isHashtagEnabled) add('#')
            if (isMentionEnabled) add('@')
        }.toCharArray()

    var hashtagAdapter: ArrayAdapter<*>? = null
    var mentionAdapter: ArrayAdapter<*>? = null

    init {
        addTextChangedListener(mTextWatcher)
        setTokenizer(SymbolsTokenizer(mEnabledSymbols))
    }

    override var isHashtagEnabled: Boolean
        get() = mImpl.isHashtagEnabled
        set(value) {
            mImpl.isHashtagEnabled = value
            setTokenizer(SymbolsTokenizer(mEnabledSymbols))
        }

    override var isMentionEnabled: Boolean
        get() = mImpl.isMentionEnabled
        set(value) {
            mImpl.isMentionEnabled = value
            setTokenizer(SymbolsTokenizer(mEnabledSymbols))
        }

    override var isHyperlinkEnabled: Boolean
        get() = mImpl.isHyperlinkEnabled
        set(value) {
            mImpl.isHyperlinkEnabled = value
            setTokenizer(SymbolsTokenizer(mEnabledSymbols))
        }

    override var hashtagColor: ColorStateList
        get() = mImpl.hashtagColor
        set(color) {
            mImpl.hashtagColor = color
        }

    override var mentionColor: ColorStateList
        get() = mImpl.mentionColor
        set(color) {
            mImpl.mentionColor = color
        }

    override var hyperlinkColor: ColorStateList
        get() = mImpl.hyperlinkColor
        set(color) {
            mImpl.hyperlinkColor = color
        }

    override fun setOnHashtagClickListener(listener: ((view: SocialView, String) -> Unit)?) = mImpl.setOnHashtagClickListener(listener)

    override fun setOnMentionClickListener(listener: ((view: SocialView, String) -> Unit)?) = mImpl.setOnMentionClickListener(listener)

    override fun setOnHyperlinkClickListener(listener: ((view: SocialView, String) -> Unit)?) = mImpl.setOnHyperlinkClickListener(listener)

    override fun setHashtagTextChangedListener(watcher: ((view: SocialView, String) -> Unit)?) = mImpl.setHashtagTextChangedListener(watcher)

    override fun setMentionTextChangedListener(watcher: ((view: SocialView, String) -> Unit)?) = mImpl.setMentionTextChangedListener(watcher)

    override fun colorize() = mImpl.colorize()

    /**
     * While [MultiAutoCompleteTextView.CommaTokenizer] tracks only comma symbol,
     * [SymbolsTokenizer] can track multiple characters, in this instance, are hashtag and at symbol.
     */
    class SymbolsTokenizer(private val symbols: CharArray) : MultiAutoCompleteTextView.Tokenizer {

        override fun findTokenStart(text: CharSequence, cursor: Int): Int {
            var i = cursor
            while (i > 0 && !symbols.contains(text[i - 1])) i--
            while (i < cursor && text[i] == ' ') i++
            return i
        }

        override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
            var i = cursor
            val len = text.length
            while (i < len) if (symbols.contains(text[i])) return i else i++
            return len
        }

        override fun terminateToken(text: CharSequence): CharSequence {
            var i = text.length
            while (i > 0 && text[i - 1] == ' ') i--
            return when {
                i > 0 && symbols.contains(text[i - 1]) -> text
                text is Spanned -> {
                    val sp = SpannableString("$text ")
                    copySpansFrom(text, 0, text.length, Any::class.java, sp, 0)
                    sp
                }
                else -> "$text "
            }
        }
    }
}
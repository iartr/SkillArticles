package ru.skillbranch.skillarticles.data.repositories

import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP =
        "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "(~~.+?~~)"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val BLOCK_CODE_GROUP = "(^```[\\s\\S]+?```$)"
    private const val ORDER_LIST_GROUP = "(^\\d{1,2}\\.\\s.+?$)"

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP" +
            "|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    fun parse(string: String) =
        MarkdownText(
            findElements(
                string
            )
        )

    fun clear(string: String?): String? {
        string ?: return null
        var clearString = ""
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            // we found a mark
            if (lastStartIndex < startIndex) {
                // check what was before the mark
                clearString += string.substring(lastStartIndex, startIndex)
            }
            // check what kind of mark this was
            var text: String
            val groups = 1..11
            var group = -1
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                //NOT FOUND -> BREAK
                -1 -> break@loop
                //1 -> LIST GROUP
                1 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex).toString()
                    val subs =
                        clear(
                            text
                        )
                    clearString += if (subs.isNullOrEmpty()) text else subs

                    lastStartIndex = endIndex
                }

                //2 -> HEADER GROUP
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.substring(startIndex, endIndex))
                    val level = reg!!.value.length

                    clearString += string.substring(startIndex + level.inc(), endIndex)
                    lastStartIndex = endIndex
                }

                //3 -> QUOTE_GROUP
                3 -> {
                    text = string.substring(startIndex.plus(2), endIndex)
                    val subs =
                        clear(
                            text
                        )
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }

                //4 -> ITALIC_GROUP
                4 -> {
                    text = string.substring(startIndex.inc(), endIndex.dec())
                    val subs =
                        clear(
                            text
                        )
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }

                //5 -> BOLD_GROUP
                5 -> {
                    text = string.substring(startIndex.plus(2), endIndex.plus(-2))
                    val subs =
                        clear(
                            text
                        )
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }

                //6 -> STRIKE_GROUP
                6 -> {
                    text = string.substring(startIndex.plus(2), endIndex.plus(-2))
                    val subs =
                        clear(
                            text
                        )
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }

                //7 -> RULE_GROUP
                7 -> {
                    clearString += " "
                    lastStartIndex = endIndex
                }

                //8 -> INLINE CODE GROUP
                8 -> {
                    text = string.substring(startIndex.inc(), endIndex.dec())
                    clearString += text
                    lastStartIndex = endIndex
                }

                //9 -> LINK_GROUP
                9 -> {
                    text = string.substring(startIndex, endIndex)
                    val (title: String, _) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    clearString += title
                    lastStartIndex = endIndex
                }


                //10 -> BLOCK CODE
                10 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.plus(-3)).toString()
                    clearString += text
                    lastStartIndex = endIndex
                }

                //11 -> ORDERED LIST
                11 -> {
                    val reg = "(^\\d+)".toRegex().find(string.substring(startIndex, endIndex))
                    val order = reg!!.value
                    text =
                        string.subSequence(startIndex.plus(order.length + 2), endIndex).toString()
                    val subs =
                        clear(
                            text
                        )
                    clearString += if (subs.isNullOrEmpty()) text else subs
                    lastStartIndex = endIndex
                }
            }
        }

        // check if there's any more text left
        if (lastStartIndex < string.length) {
            clearString += string.substring(lastStartIndex, string.length)
        }

        return clearString
    }

    /**
     * Find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0 // Индекс, с которого матчер будет постоянно искать следующее вхождение

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            // Значит до начального индекса есть текст
            if (lastStartIndex < startIndex) {
                parents.add(
                    Element.Text(
                        string.subSequence(lastStartIndex, startIndex)
                    )
                )
            }

            //found text
            var text: CharSequence

            //groups range for iterate by groups
            val groups = 1..11
            var group = -1
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                //NOT FOUND -> BREAK
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    //text without "*. "
                    text = string.subSequence(startIndex + 2, endIndex)

                    //find inner elements
                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.UnorderedListItem(
                            text,
                            subs
                        )
                    parents.add(element)

                    //next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }

                //HEADER
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length

                    //text without "{#} "
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element =
                        Element.Header(
                            level,
                            text
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    text = string.subSequence(startIndex + 2, endIndex)
                    val subelements =
                        findElements(
                            text
                        )
                    val element =
                        Element.Quote(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                4 -> {
                    //text without "*{}*"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements =
                        findElements(
                            text
                        )
                    val element =
                        Element.Italic(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {
                    //text without "**{}**"
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements =
                        findElements(
                            text
                        )
                    val element =
                        Element.Bold(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements =
                        findElements(
                            text
                        )
                    val element =
                        Element.Strike(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {
                    //text without "***" insert empty character
                    val element =
                        Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //INLINE CODE
                8 -> {
                    //text without "`{}`"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element =
                        Element.InlineCode(
                            text
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                9 -> {
                    //full text for regex
                    text = string.subSequence(startIndex, endIndex)
                    val (title:String, link:String) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element =
                        Element.Link(
                            link,
                            title
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //10 -> BLOCK CODE
                10 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.minus(3)).toString()

                    if (text.contains(LINE_SEPARATOR)) {
                        for ((index, line) in text.lines().withIndex()) {
                            when (index) {
                                text.lines().lastIndex -> parents.add(
                                    Element.BlockCode(
                                        Element.BlockCode.Type.END,
                                        line
                                    )
                                )
                                0 -> parents.add(
                                    Element.BlockCode(
                                        Element.BlockCode.Type.START,
                                        line + LINE_SEPARATOR
                                    )
                                )
                                else -> parents.add(
                                    Element.BlockCode(
                                        Element.BlockCode.Type.MIDDLE,
                                        line + LINE_SEPARATOR
                                    )
                                )
                            }
                        }
                    } else parents.add(
                        Element.BlockCode(
                            Element.BlockCode.Type.SINGLE,
                            text
                        )
                    )

                    lastStartIndex = endIndex
                }

                //11 -> NUMERIC LIST
                11 -> {
                    val reg = "(^\\d{1,2}.)".toRegex().find(string.substring(startIndex, endIndex))
                    val order = reg!!.value
                    text =
                        string.subSequence(startIndex.plus(order.length.inc()), endIndex).toString()

                    val subs =
                        findElements(
                            text
                        )
                    val element =
                        Element.OrderedListItem(
                            order,
                            text.toString(),
                            subs
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }

        }

        // Дополнительная проверка, не забыли ли добавить остаток текста
        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(
                Element.Text(
                    text
                )
            )
        }

        return parents
    }
}

// Класс-обертка для большей читаемости
data class MarkdownText(val elements: List<Element>)

// Элемент Markdown разметки
sealed class Element {
    abstract val text: CharSequence
    abstract val elements: List<Element> // Каждый Markdown элемент может содержать дочерние Markdown элементы

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    // Ненумерованный список
    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ", //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }
}
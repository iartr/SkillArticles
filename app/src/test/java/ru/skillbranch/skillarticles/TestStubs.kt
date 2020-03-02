package ru.skillbranch.skillarticles

val unorderedListString: String = """
before simple text
* Unordered list can use asterisks list
- Or minuses
+ Or pluses
after simple text
""".trimIndent()
val expectedUnorderedList: List<String> = listOf(
    "Unordered list can use asterisks list",
    "Or minuses",
    "Or pluses"
)

val headerString = """
before simple text
# Header1
## Header2 Header2
### Header3 Header3 Header3
#### Header4 Header4 Header4 Header4
##### Header5 Header5 Header5 Header5 Header5
###### Header6 Header6 Header6 Header6 Header6 Header6
after simple text
"""
val expectedHeader: List<String> = listOf(
    "Header1",
    "Header2 Header2",
    "Header3 Header3 Header3",
    "Header4 Header4 Header4 Header4",
    "Header5 Header5 Header5 Header5 Header5",
    "Header6 Header6 Header6 Header6 Header6 Header6"
)

val quoteString = """
before simple text
> Blockquotes are very handy in email to emulate reply text.
> This line is part of the same quote.
after simple text
""".trimIndent()
val expectedQuote: List<String> = listOf(
    "Blockquotes are very handy in email to emulate reply text.",
    "This line is part of the same quote."
)

val italicString: String = """
Emphasis, aka italics, with *asterisks* or _underscores_.
""".trimIndent()
val expectedItalic: List<String> = listOf(
    "asterisks",
    "underscores"
)

val boldString: String = """
Strong emphasis, aka bold, with **asterisks** or __underscores__.
""".trimIndent()

val expectedBold: List<String> = listOf(
    "asterisks",
    "underscores"
)

val strikeString: String = """
Strikethrough uses two tildes. ~~Scratch this.~~
""".trimIndent()

val expectedStrike: List<String> = listOf(
    "Scratch this."
)

val combineEmphasisString = """
Combined emphasis with **asterisks and _underscores_**.
or emphasis with __underscores and *asterisks*__.
or _underscores for italic and **asterisks for inner bold**_.
or *asterisks for italic and __underscores for inner bold__*.
or strikethrough ~~two tildes for strike~~
and combine with asterisks and underscores ~~two tildes for strike and __underscores for inner strike bold__ and **asterisks for inner strike bold**~~.
and combined emphasis together ~~two tildes for strike and __underscores for inner *strike italic bold*__ and **asterisks for inner _strike italic bold_**~~.
""".trimIndent()

val expectedCombine: Map<String, List<String>> = mapOf(
    "bold" to listOf(
        "asterisks and _underscores_",
        "underscores and *asterisks*",
        "asterisks for inner bold",
        "underscores for inner bold",
        "underscores for inner strike bold",
        "asterisks for inner strike bold",
        "underscores for inner *strike italic bold*",
        "asterisks for inner _strike italic bold_"
    ),
    "italic" to listOf(
        "underscores",
        "asterisks",
        "underscores for italic and **asterisks for inner bold**",
        "asterisks for italic and __underscores for inner bold__",
        "strike italic bold",
        "strike italic bold"
    ),
    "strike" to listOf(
        "two tildes for strike",
        "two tildes for strike and __underscores for inner strike bold__ and **asterisks for inner strike bold**",
        "two tildes for strike and __underscores for inner *strike italic bold*__ and **asterisks for inner _strike italic bold_**"
    )
)

val ruleString = """
before simple text
___
---
***
after simple text
"""

val inlineString: String = """
before simple text `code` split `code with line break
not` work `only inline` after simple text
""".trimIndent()
val expectedInline: List<String> = listOf(
    "code",
    "only inline"
)

val linkString: String = """
before simple text 
[I`am yandex link](https://www.yandex.ru)
[I`am google link](https://www.google.com)
after simple text
""".trimIndent()
val expectedLink: Map<String, List<String>> = mapOf(
    "titles" to listOf(
        "I`am yandex link",
        "I`am google link"
    ),
    "links" to listOf(
        "https://www.yandex.ru",
        "https://www.google.com"
    )
)


val markdownString = """
before header text
# Header1 first line margin middle line without margin last line with margin
## Header2 Header2
### Header3 Header3 Header3
#### Header4 Header4 Header4 Header4
##### Header5 Header5 Header5 Header5 Header5
###### Header6 Header6 Header6 Header6 Header6 Header6
after header text and break line

Emphasis, aka italics, with *asterisks* or _underscores_.

Strong emphasis, aka bold, with **asterisks** or __underscores__.

Strikethrough uses two tildes. ~~Scratch this.~~

Combined emphasis with **asterisks and _underscores_**.
or emphasis with __underscores and *asterisks*__.
or _underscores for italic and **asterisks for inner bold**_.
or *asterisks for italic and __underscores for inner bold__*.
or strikethrough ~~two tildes for strike~~

And combine with asterisks and underscores ~~two tildes for strike and __underscores for inner strike bold__ and **asterisks for inner strike bold**~~.
and combined emphasis together ~~two tildes for strike and __underscores for inner *strike italic bold*__ and **asterisks for inner _strike italic bold_**~~.

* Unordered list can use double **asterisks** or double __underscores__ for emphasis aka **bold**
- Use minuses for list item and _underscores_ and *asterisks* for emphasis aka *italic*
+ Or use plus for list item and ~~double tildes~~ for strike

1. First ordered list item
2. Second item
3. Third item.

> Blockquotes are very handy in ~~email~~ to emulate reply text.
> This line is *part* of __the__ same quote.

Use ` for wrap `inline code` split `code with line break
not` work `only inline`

simple single line 

Use ``` for wrap block code
```code block.code block.code block```
also it work for multiline code block 
```multiline code block
multiline code block
multiline code block
multiline code block```
Use three underscore character _ in new line for horizontal divider
___
or three asterisks
***
or three minus
---

simple text and break line

For inline link use `[for title]` and `(for link)` 
example link: [I'm an inline-style link](https://www.google.com)
simple text and break line

end markdown text
""".trimIndent()

val markdownClearString = """
before header text
Header1 first line margin middle line without margin last line with margin
Header2 Header2
Header3 Header3 Header3
Header4 Header4 Header4 Header4
Header5 Header5 Header5 Header5 Header5
Header6 Header6 Header6 Header6 Header6 Header6
after header text and break line

Emphasis, aka italics, with asterisks or underscores.

Strong emphasis, aka bold, with asterisks or underscores.

Strikethrough uses two tildes. Scratch this.

Combined emphasis with asterisks and underscores.
or emphasis with underscores and asterisks.
or underscores for italic and asterisks for inner bold.
or asterisks for italic and underscores for inner bold.
or strikethrough two tildes for strike

And combine with asterisks and underscores two tildes for strike and underscores for inner strike bold and asterisks for inner strike bold.
and combined emphasis together two tildes for strike and underscores for inner strike italic bold and asterisks for inner strike italic bold.

Unordered list can use double asterisks or double underscores for emphasis aka bold
Use minuses for list item and underscores and asterisks for emphasis aka italic
Or use plus for list item and double tildes for strike

1. First ordered list item
2. Second item
3. Third item.

Blockquotes are very handy in email to emulate reply text.
This line is part of the same quote.

Use ` for wrap inline code split `code with line break
not` work only inline

simple single line 

Use ``` for wrap block code
```code block.code block.code block```
also it work for multiline code block 
```multiline code block
multiline code block
multiline code block
multiline code block```
Use three underscore character _ in new line for horizontal divider
 
or three asterisks
 
or three minus
 

simple text and break line

For inline link use [for title] and (for link) 
example link: I'm an inline-style link
simple text and break line

end markdown text
""".trimIndent()

val markdownOptionallyClearString = """
before header text
Header1 first line margin middle line without margin last line with margin
Header2 Header2
Header3 Header3 Header3
Header4 Header4 Header4 Header4
Header5 Header5 Header5 Header5 Header5
Header6 Header6 Header6 Header6 Header6 Header6
after header text and break line

Emphasis, aka italics, with asterisks or underscores.

Strong emphasis, aka bold, with asterisks or underscores.

Strikethrough uses two tildes. Scratch this.

Combined emphasis with asterisks and underscores.
or emphasis with underscores and asterisks.
or underscores for italic and asterisks for inner bold.
or asterisks for italic and underscores for inner bold.
or strikethrough two tildes for strike

And combine with asterisks and underscores two tildes for strike and underscores for inner strike bold and asterisks for inner strike bold.
and combined emphasis together two tildes for strike and underscores for inner strike italic bold and asterisks for inner strike italic bold.

Unordered list can use double asterisks or double underscores for emphasis aka bold
Use minuses for list item and underscores and asterisks for emphasis aka italic
Or use plus for list item and double tildes for strike

First ordered list item
Second item
Third item.

Blockquotes are very handy in email to emulate reply text.
This line is part of the same quote.

Use ` for wrap inline code split `code with line break
not` work only inline

simple single line 

Use ``` for wrap block code
code block.code block.code block
also it work for multiline code block 
multiline code block
multiline code block
multiline code block
multiline code block
Use three underscore character _ in new line for horizontal divider
 
or three asterisks
 
or three minus
 

simple text and break line

For inline link use [for title] and (for link) 
example link: I'm an inline-style link
simple text and break line

end markdown text
""".trimIndent()

val expectedMarkdown = mapOf(
    "header" to listOf(
        "Header1 first line margin middle line without margin last line with margin",
        "Header2 Header2",
        "Header3 Header3 Header3",
        "Header4 Header4 Header4 Header4",
        "Header5 Header5 Header5 Header5 Header5",
        "Header6 Header6 Header6 Header6 Header6 Header6"
    ),
    "bold" to listOf(
        "asterisks",
        "underscores",
        "asterisks and _underscores_",
        "underscores and *asterisks*",
        "asterisks for inner bold",
        "underscores for inner bold",
        "underscores for inner strike bold",
        "asterisks for inner strike bold",
        "underscores for inner *strike italic bold*",
        "asterisks for inner _strike italic bold_",
        "asterisks",
        "underscores",
        "bold",
        "the"
    ),
    "italic" to listOf(
        "asterisks",
        "underscores",
        "underscores",
        "asterisks",
        "underscores for italic and **asterisks for inner bold**",
        "asterisks for italic and __underscores for inner bold__",
        "strike italic bold",
        "strike italic bold",
        "underscores",
        "asterisks",
        "italic",
        "part"
    ),
    "strike" to listOf(
        "Scratch this.",
        "two tildes for strike",
        "two tildes for strike and __underscores for inner strike bold__ and **asterisks for inner strike bold**",
        "two tildes for strike and __underscores for inner *strike italic bold*__ and **asterisks for inner _strike italic bold_**",
        "double tildes",
        "email"
    ),
    "unorderedList" to listOf(
        "Unordered list can use double **asterisks** or double __underscores__ for emphasis aka **bold**",
        "Use minuses for list item and _underscores_ and *asterisks* for emphasis aka *italic*",
        "Or use plus for list item and ~~double tildes~~ for strike"
    ),
    "orderedList" to listOf(
        "First ordered list item",
        "Second item",
        "Third item."
    ),
    "inline" to listOf(
        "inline code",
        "only inline",
        "[for title]",
        "(for link)"
    ),
    "multiline" to listOf(
        "code block.code block.code block",
        "multiline code block\n",
        "multiline code block\n",
        "multiline code block\n",
        "multiline code block"
    ),
    "quote" to listOf(
        "Blockquotes are very handy in ~~email~~ to emulate reply text.",
        "This line is *part* of __the__ same quote."
    ),
    "linkTitles" to listOf(
        "I'm an inline-style link"
    ),
    "links" to listOf(
        "https://www.google.com"
    )
)

version = "1.3.2"
description =
        "Checks if the message starts with I or whatever the channel and avert the crisis if it does not start with that letter."

aliucord.changelog.set(
        """
            # 1.0.1
            * Bug fix: Hardcode the default value into the actual logic instead of when creating the Settings.
            # 1.1.0
            * Enhancement: Added support for "a", "b", and "h" channels
            * Code Improvements: More channels can now be added just by modifying one dictionary.
            # 1.1.1
            * Enhancement: Added support for "e" channel.
            # 1.1.2
            * Bug Fix.
            # 1.1.3
            * More bug fixes. ~~OH GOD WHY ARE THERE SO MANY BUGS~~
            # 1.1.4
            * Updated the prefix message for the "a" channel.
            # 1.2.0
            * Added support for "s" channel.
            # 1.2.1
            * I forgot to apply the fix from v1.1.2
            # 1.3.0
            * Added support for "r" channel.
            # 1.3.1
            * Testing a fix for messages that start with markdown syntax.
            # 1.3.2
            * The plugin should now watch out for valid markdown.
        """.trimIndent()
)

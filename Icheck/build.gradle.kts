version = "1.1.1"
description = "Checks if the message starts with I or whatever the channel and avert the crisis if it does not start with that letter."

aliucord.changelog.set(
        """
            ## 1.0.1
            * Bug fix: Hardcode the default value into the actual logic instead of when creating the Settings.
            ## 1.1.0
            * Enhancement: Added support for "a", "b", and "h" channels
            * Code Improvements: More channels can now be added just by modifying one dictionary.
            ## 1.1.1
            * Enhancement: Added support for "e" channel.
        """.trimIndent()
)

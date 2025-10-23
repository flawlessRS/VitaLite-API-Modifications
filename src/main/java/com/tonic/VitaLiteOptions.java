package com.tonic;

import com.tonic.util.AudioDeviceChecker;
import com.tonic.util.optionsparser.OptionsParser;
import com.tonic.util.optionsparser.annotations.CLIArgument;
import lombok.Getter;
import lombok.Setter;

@Getter
public class VitaLiteOptions extends OptionsParser
{
    @CLIArgument(
            name = "rsdump",
            description = "[Optional] Path to dump the gamepack to"
    )
    private String rsdump = null;

    @CLIArgument(
            name = "noPlugins",
            description = "[Optional] Disables loading of core plugins"
    )
    private boolean noPlugins = false;

    @CLIArgument(
            name = "incognito",
            description = "[Optional] Visually display as 'RuneLite' instead of 'VitaLite'"
    )
    private boolean incognito = false;

    @CLIArgument(
            name = "safeLaunch",
            description = ""
    )
    private boolean safeLaunch = false;

    @CLIArgument(
            name = "min",
            description = "Run with minimum memory on jvm (auto enables also -noPlugins and -noMusic)"
    )
    private boolean min = false;

    @CLIArgument(
            name = "noMusic",
            description = "Prevent the loading of music tracks"
    )
    private boolean noMusic = false;

    @CLIArgument(
            name = "proxy",
            description = "Set a proxy server to use (e.g., ip:port or ip:port:username:password)"
    )
    private String proxy = null;

    @CLIArgument(
            name = "launcherCom",
            description = ""
    )
    private String port;

    @CLIArgument(
            name = "disableMouseHook",
            description = "Disable RuneLites mousehook DLL from being loaded or called"
    )
    private boolean disableMouseHook = false;

    @CLIArgument(
            name = "login",
            description = "OSRS login credentials - Supports ALL account types: Legacy (username:password), Legacy+PIN (username:password:1234), Jagex (email:password:pin:otp)"
    )
    private String login = null;

    @CLIArgument(
            name = "user",
            description = "OSRS username/email for auto-login (alternative to --login, works with all account types)"
    )
    private String user = null;

    @CLIArgument(
            name = "pass",
            description = "OSRS password for auto-login (alternative to --login, works with all account types)"
    )
    private String pass = null;

    @CLIArgument(
            name = "quickstart",
            description = "Auto-enable FlawlessUtils plugin on startup"
    )
    private boolean quickstart = false;

    @CLIArgument(
            name = "plugin",
            description = "Auto-start specific plugin with last saved config (e.g., FlawlessMiner)"
    )
    private String plugin = null;

    @CLIArgument(
            name = "world",
            description = "Specific world to login to (e.g., 301, 302, etc.)"
    )
    private Integer world = null;

    @CLIArgument(
            name = "Xms",
            description = "JVM initial heap size (e.g., 512m, 1g, 2g)"
    )
    private String xms = null;

    @CLIArgument(
            name = "Xmx",
            description = "JVM maximum heap size (e.g., 1g, 2g, 4g)"
    )
    private String xmx = null;

    public void _checkAudio()
    {
        if(!AudioDeviceChecker.hasAudioDevice())
        {
            noMusic = true;
        }
    }
}

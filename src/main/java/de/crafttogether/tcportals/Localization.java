package de.crafttogether.tcportals;

import de.crafttogether.TCPortals;
import de.crafttogether.common.localization.LocalizationEnum;
import de.crafttogether.common.localization.LocalizationManager;

public class Localization extends LocalizationEnum {
    public static final Localization PREFIX = new Localization("prefix", "<gold>TC-Portals </gold><dark_gray>» </dark_gray>");

    public static final Localization ERROR_NOPERMISSION = new Localization("error.noPermission", "<hover:show_text:'<red>{permission}'><red>You do not have permission, ask an admin to do this for you.</red></hover>");
    public static final Localization ERROR_DATABASE = new Localization("error.database", "<prefix/><hover:show_text:'<white>{error}'><red>An error occurred. Please contact an administrator.</red></hover>");

    public static final Localization PORTAL_ENTER_NOEXIT = new Localization("portal.enter.noExit", "<prefix/><red>No </red><yellow>Portal-Exit</yellow><red> was found for channel </red><yellow>{name}</yellow><red>.</red>");
    public static final Localization PORTAL_ENTER_CONNECTIONREFUSED = new Localization("portal.enter.connectionRefused", "<prefix/><red>A connection to </red><yellow>{host}:{port}</yellow><red> could not be established.</red>");
    public static final Localization PORTAL_ENTER_NOREMOTECONNECTIONS = new Localization("portal.enter.noRemoteConnections", "<prefix/><red>A connection to </red><yellow>{host}:{port}</yellow><red> was refused, because remote connections are not allowed!</red>");
    public static final Localization PORTAL_ENTER_NOTAUTHENTICATED = new Localization("portal.enter.notAuthenticated", "<prefix/><red>Data was sent to </red><yellow>{host}:{port}</yellow><red> without authentication taking place.</red>");
    public static final Localization PORTAL_ENTER_INVALIDAUTH = new Localization("portal.enter.invalidAuth", "<prefix/><red>A connection to </red><yellow>{host}:{port}</yellow><red> was refused because an invalid 'SecretKey' was sent.</red>");

    public static final Localization PORTAL_EXIT_WORLDNOTFOUND = new Localization("portal.enter.worldNotFound", "<prefix/><red>No world named</red> <yellow>{world}</yellow><red> was found.</red>");
    public static final Localization PORTAL_EXIT_SIGNNOTFOUND = new Localization("portal.enter.signNotFound", "<prefix/><yellow>Portal-Exit</yellow><red> was found but not the sign for channel </red><yellow>{name}</yellow><newLine><prefix/><yellow>{world}, {x}, {y}, {z}</yellow><red>.</red>");
    public static final Localization PORTAL_EXIT_NOTRAIN = new Localization("portal.exit.noTrain", "<prefix/><red>Couldn't find the train (<yellow>{train}</yellow>) you were on.</red>");
    public static final Localization PORTAL_EXIT_NORAILS = new Localization("portal.exit.noRails", "<prefix/><red>Could not find a rail at </red><newLine><prefix/><yellow>{world} {x} {z} {y}</yellow><red>.</red>");
    public static final Localization PORTAL_EXIT_NOSPAWNLOCATION = new Localization("portal.exit.noSpawnLocation", "<prefix/><red>Could not find the right spot to spawn a train at </red><newLine><prefix/><yellow>{world} {x} {z} {y}</yellow><red>.</red>");
    public static final Localization PORTAL_EXIT_TRACKOCCUPIED = new Localization("portal.exit.trackOccupied", "<prefix/><red>Track is occupied by another train at </red><newLine><prefix/><yellow>{world} {x} {z} {y}</yellow><red>.</red>");

    public static final Localization PORTAL_CREATE_NONAME = new Localization("portal.create.noName", "<prefix/><red>Please write a name for this portal on the third line.</red>");
    public static final Localization PORTAL_CREATE_IN_NOTEXIST = new Localization("portal.create.in.notExist", "<prefix/><gold>Notice: </gold><yellow>No exit portal for channel </yellow><gold>{name}</gold> <yellow>has been created yet.</yellow>");
    public static final Localization PORTAL_CREATE_IN_SUCCESS = new Localization("portal.create.in.success", "<prefix/><green>You created a </green><yellow>Portal-Entrance</yellow><green> for channel (</green><yellow>{name}</yellow><green>).</green>");
    public static final Localization PORTAL_CREATE_OUT_EXIST = new Localization("portal.create.out.exist", "<prefix/><red>There is already a portal exit for channel </red><yellow>{name}</yellow><red>.</red>");
    public static final Localization PORTAL_CREATE_OUT_SUCCESS = new Localization("portal.create.out.success", "<prefix/><green>You created a </green><yellow>Portal-Exit</yellow><green> for channel (</green><yellow>{name}</yellow><green>).</green>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_EXISTS = new Localization("portal.create.bidirectional.exists", "<prefix/><red>There is already a complete portal pair for channel </red><yellow>{name}</yellow><red>.</red>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_SAMESERVER = new Localization("portal.create.bidirectional.sameServer", "<prefix/><red>There is already another bidirectional portal for channel </red><yellow>{name}</yellow><red> on this server.</red>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_SUCCESS = new Localization("portal.create.bidirectional.success", "<prefix/>><green>You created a </green><yellow>Bidirectional-Portal</yellow><green> for channel (</green><yellow>{name}</yellow><green>).</green>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_INFO_FIRST = new Localization("portal.create.bidirectional.info.first", "<prefix/><gold>Notice: </gold><yellow>A second portal for channel </yellow><gold>{name}</gold><yellow> has not yet been created.</red>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_INFO_SECOND = new Localization("portal.create.bidirectional.info.second", """
            <prefix/><gold>Notice: </gold><yellow>The other portal is in the world </yellow><gold>{world}</gold><yellow> on the server </yellow><gold>{server}</gold>
            <prefix/><yellow>Coordinates: </yellow><gold>{x}, {y}, {z}</gold>""");
    public static final Localization UPDATE_LASTBUILD = new Localization("update.lastBuild", "<prefix/><green>Your installed version is up to date</green>");
    public static final Localization UPDATE_RELEASE = new Localization("update.devBuild", """
            <hover:show_text:'<green>Click here to download this version'><click:open_url:'{url}'><prefix/><green>A new full version was released!</green>
            <prefix/><green>Version: </green><yellow>{version} #{build}</yellow>
            <prefix/><green>FileName: </green><yellow>{fileName}</yellow>
            <prefix/><green>FileSize: </green><yellow>{fileSize}</yellow>
            <prefix/><red>You are on version: </red><yellow>{currentVersion} #{currentBuild}</yellow></click></hover>""");
    public static final Localization UPDATE_DEVBUILD = new Localization("update.release", """
            <hover:show_text:'<green>Click here to download this version'><click:open_url:'{url}'><prefix/><green>A new snapshot build is available!</green>
            <prefix/><green>Version: </green><yellow>{version} #{build}</yellow>
            <prefix/><green>FileName: </green><yellow>{fileName}</yellow>
            <prefix/><green>FileSize: </green><yellow>{fileSize}</yellow>
            <prefix/><red>You are on version: </red><yellow>{currentVersion} #{currentBuild}</yellow></click></hover>""");
    public static final Localization UPDATE_ERROR = new Localization("update.error", "<prefix/><hover:show_text:'<white>{error}'><red>Unable to retrieve update informations</red></hover>");

    private Localization(String name, String defValue) {
        super(name, defValue);
    }

    @Override
    public LocalizationManager getManager() {
        return TCPortals.plugin.getLocalizationManager();
    }
}

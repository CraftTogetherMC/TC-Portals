package de.crafttogether.tcportals;

import de.crafttogether.TCPortals;
import de.crafttogether.common.localization.LocalizationEnum;
import de.crafttogether.common.localization.LocalizationManager;

public class Localization extends LocalizationEnum {
    public static final Localization PREFIX = new Localization("prefix", "<gold>TC-Portals </gold><dark_gray>» </dark_gray>");

    public static final Localization ERROR_NOPERMISSION = new Localization("error.noPermission", "<red>You lack the permission: </red><yellow>{permission}</yellow>");
    public static final Localization ERROR_DATABASE = new Localization("error.database", "<prefix/><red>Ein Fehler ist aufgetreten. Bitte kontaktiere einen Administrator.</red><newLine><red>{error}</red>");

    public static final Localization PORTAL_ENTER_NOEXIT = new Localization("portal.enter.noExit", "<prefix/><red>Es wurde kein Portal-Ausgang für den Kanal</red> <yellow>{name}</yellow> <red>gefunden.</red>");
    public static final Localization PORTAL_ENTER_CONNECTIONREFUSED = new Localization("portal.enter.connectionRefused", "<prefix/><red>A connection to </red><yellow>{host}:{port}</yellow><red> could not be established.</red>");
    public static final Localization PORTAL_ENTER_NOREMOTECONNECTIONS = new Localization("portal.enter.noRemoteConnections", "<prefix/><red>A connection to </red><yellow>{host}:{port}</yellow><red> was refused, because remote connections are not allowed!</red>");
    public static final Localization PORTAL_ENTER_NOTAUTHENTICATED = new Localization("portal.enter.notAuthenticated", "<prefix/><red>Data was sent to </red><yellow>{host}:{port}</yellow><red> without authentication taking place.</red>");
    public static final Localization PORTAL_ENTER_INVALIDAUTH = new Localization("portal.enter.invalidAuth", "<prefix/><red>A connection to </red><yellow>{host}:{port}</yellow><red> was refused because an invalid 'SecretKey' was sent.</red>");

    public static final Localization PORTAL_EXIT_WORLDNOTFOUND = new Localization("portal.enter.worldNotFound", "<prefix/><red>Es existiert keine Welt mit dem Namen</red> <yellow>{world}</yellow><red>.</red>");
    public static final Localization PORTAL_EXIT_SIGNNOTFOUND = new Localization("portal.enter.signNotFound", "<prefix/><red>Es wurde kein Portal-Schild für den Kanal</red> <yellow>{name}</yellow> <red>gefunden.</red><newLine><yellow>{world}, {x}, {y}, {z}</yellow>");
    public static final Localization PORTAL_EXIT_NOTRAIN = new Localization("portal.exit.noTrain", "<prefix/><red>Couldn't find the train (<yellow>{train}</yellow>) you were on</red>");
    public static final Localization PORTAL_EXIT_NORAILS = new Localization("portal.exit.noRails", "<prefix/><red>Could not find a rail at </red><yellow>{world} {x} {z} {y}</yellow>");
    public static final Localization PORTAL_EXIT_NOSPAWNLOCATION = new Localization("portal.exit.noSpawnLocation", "<prefix/><red>Could not find the right spot to spawn a train at </red><yellow>{world} {x} {z} {y}</yellow>");
    public static final Localization PORTAL_EXIT_TRACKOCCUPIED = new Localization("portal.exit.trackOccupied", "<prefix/><red>Track is occupied by another train at </red><yellow>{world} {x} {z} {y}</yellow>");

    public static final Localization PORTAL_CREATE_NONAME = new Localization("portal.create.noName", "<prefix/><red>Bitte schreibe einen Namen für das Portal in die dritte Zeile des Schildes.</red>");
    public static final Localization PORTAL_CREATE_IN_NOTEXIST = new Localization("portal.create.in.notExist", "<prefix/><gold>Hinweis:</gold> <red>Es wurde noch kein Ausgangs-Portal für den Kanal </red> <yellow>{name}</yellow> <red>erstellt.</red>");
    public static final Localization PORTAL_CREATE_IN_SUCCESS = new Localization("portal.create.in.success", "<prefix/><red>Portal-Eingang wurde erstellt (</red><yellow>{name}</yellow><red>)</red>");
    public static final Localization PORTAL_CREATE_OUT_EXIST = new Localization("portal.create.out.exist", "<prefix/><red>Es besteht bereits ein Portal-Ausgang für den Kanal</red> <yellow>{name}</yellow><red>.</red>");
    public static final Localization PORTAL_CREATE_OUT_SUCCESS = new Localization("portal.create.out.success", "<prefix/><red>Portal-Ausgang wurde erstellt (</red><yellow>{name}</yellow><red>)</red>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_EXISTS = new Localization("portal.create.bidirectional.exists", "<prefix/><red>Es besteht bereits ein Portal-Paar für den Kanal</red> <yellow>{name}</yellow><red>.</red>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_SAMESERVER = new Localization("portal.create.bidirectional.sameServer", "<prefix/><red>Es besteht bereits ein bidirektionales Portal für diesen Kanal auf diesem Server.</red>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_SUCCESS = new Localization("portal.create.bidirectional.success", "<prefix/><red>Portal wurde erstellt (</red><yellow>{name}</yellow><red>)</red>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_INFO_FIRST = new Localization("portal.create.bidirectional.info.first", "<prefix/><gold>Hinweis:</gold> <red>Es wurde noch kein zweites Portal für den Kanal </red> <yellow>{name}</yellow> <red>erstellt.</red>");
    public static final Localization PORTAL_CREATE_BIDIRECTIONAL_INFO_SECOND = new Localization("portal.create.bidirectional.info.second", "<prefix/><gold>Hinweis:</gold> <yellow>Das andere Portal befindet sich<newLine>in der Welt: </yellow><gold>{world}</gold> <yellow>auf dem Server: </yellow><gold>{server}</gold><newLine><yellow>Koordinaten:</yellow> <gold>{x}, {y}, {z}</gold>");

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

    private Localization(String name, String defValue) {
        super(name, defValue);
    }

    @Override
    public LocalizationManager getManager() {
        return TCPortals.plugin.getLocalizationManager();
    }
}
